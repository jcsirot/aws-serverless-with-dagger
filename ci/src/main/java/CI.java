import io.dagger.client.BuildArg;
import io.dagger.client.CacheVolume;
import io.dagger.client.Client;
import io.dagger.client.Container;
import io.dagger.client.Container.BuildArguments;
import io.dagger.client.Dagger;
import io.dagger.client.Directory;
import io.dagger.client.Host.DirectoryArguments;
import io.dagger.client.Secret;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.cloudformation.CloudFormationClient;
import software.amazon.awssdk.services.cloudformation.model.DescribeStacksRequest;
import software.amazon.awssdk.services.cloudformation.model.Output;
import software.amazon.awssdk.services.cloudformation.model.Stack;
import software.amazon.awssdk.services.ecr.EcrClient;
import software.amazon.awssdk.services.ecr.model.CreateRepositoryRequest;
import software.amazon.awssdk.services.ecr.model.CreateRepositoryResponse;
import software.amazon.awssdk.services.ecr.model.DescribeRegistryRequest;
import software.amazon.awssdk.services.ecr.model.DescribeRepositoriesRequest;
import software.amazon.awssdk.services.ecr.model.DescribeRepositoriesResponse;
import software.amazon.awssdk.services.ecr.model.ImageTagMutability;
import software.amazon.awssdk.services.ecr.model.Repository;
import software.amazon.awssdk.services.ecr.model.RepositoryNotFoundException;

public class CI {

  private static final String STACK_NAME = "aws-serverless-with-dagger";
  private static final String DEFAULT_REGION = "eu-west-2";
  private static final Region REGION = Region.of(
      System.getenv().getOrDefault("AWS_DEFAULT_REGION", DEFAULT_REGION));
  private static final List<Handler> HANDLERS = List.of(
      new Handler("translate", "translate.App", "translate"));
  private static final Logger LOG = LoggerFactory.getLogger(CI.class);

  private static String imageRegistry() {
    try (EcrClient ecrClient = EcrClient.builder().region(REGION).build()) {
      return String.format("%s.dkr.ecr.%s.amazonaws.com",
          ecrClient.describeRegistry(DescribeRegistryRequest.builder().build()).registryId(),
          REGION.id());
    }
  }

  private static final String IMAGE_REGISTRY = imageRegistry();

  public static void main(String... args) throws Exception {
    verifyEnvVars();
    try (Client client = Dagger.connect()) {
      runCI(client);
    } catch (Exception e) {
      LOG.error(e.getMessage(), e);
      System.exit(-1);
    }
  }

  /**
   * Ensures that <code>AWS_ACCESS_KEY_ID</code> and <code>AWS_SECRET_ACCESS_KEY</code> are set.
   */
  private static void verifyEnvVars() {
    List.of("AWS_ACCESS_KEY_ID", "AWS_SECRET_ACCESS_KEY").forEach(v -> {
      String val = System.getenv(v);
      if (val == null || val.isEmpty()) {
        LOG.error("Environment variable {} is not set", v);
        System.exit(-1);
      }
    });
  }

  private static void createDockerRepository(Handler handler) {
    try (EcrClient ecrClient = EcrClient.builder().region(REGION).build()) {
      try {
        DescribeRepositoriesResponse describeRepositoriesResponse = ecrClient.describeRepositories(
            DescribeRepositoriesRequest.builder().repositoryNames(handler.name).build());
        Repository repository = describeRepositoriesResponse.repositories().get(0);
        LOG.info(String.format("Repository %s already exists... Skipping creation",
            repository.repositoryUri()));
      } catch (RepositoryNotFoundException rnfe) {
        CreateRepositoryResponse createRepositoryResponse = ecrClient.createRepository(
            CreateRepositoryRequest.builder()
                .repositoryName(handler.name)
                .imageTagMutability(ImageTagMutability.MUTABLE).build());
        LOG.info(String.format("Created repository: %s",
            createRepositoryResponse.repository().repositoryUri()));
      }
    }
  }

  /**
   * Returns the container registry credentials required to store the handler images
   *
   * @param client the Dagger client
   * @return the registry credentials
   * @throws Exception on error
   */
  private static Credentials getRegistryCredentials(Client client) throws Exception {
    try (EcrClient ecrClient = EcrClient.builder().region(REGION).build()) {
      return ecrClient.getAuthorizationToken().authorizationData().stream().findFirst()
          .map(data -> {
            String[] decoded = new String(
                Base64.getDecoder().decode(data.authorizationToken())).split(":");
            return new Credentials(decoded[0], client.setSecret("password", decoded[1]));
          }).orElseThrow(() -> new RuntimeException("Could not retrieve registry credentials"));
    }
  }

  /**
   * Retrieves the stack service URL by reading the "Outputs" section of the Cloudformation stack
   *
   * @return the Application URL
   * @throws Exception on error
   */
  private static String getAppURL() throws Exception {
    try (CloudFormationClient client = CloudFormationClient.builder().region(REGION)
        .build()) {
      Stack stack = client.describeStacks(
              DescribeStacksRequest.builder().stackName(STACK_NAME).build()).stacks()
          .stream().findFirst()
          .orElseThrow(() -> new RuntimeException("Could not retrieve stack"));
      return stack.outputs().stream().filter(output -> "TranslateApi".equals(output.outputKey()))
          .map(
              Output::outputValue).findFirst()
          .orElseThrow(() -> new RuntimeException("Could not retrieve Translate API"));
    }
  }

  private static void runCI(Client client) throws Exception {
    CacheVolume mavenCache = client.cacheVolume("maven-cache");

    // get reference to source code directory
    Directory source = client.host()
        .directory(".", new DirectoryArguments().withExclude(List.of("ci")));

    // use maven:3.9 container
    // mount cache and source code volumes
    // set working directory
    Container build = client.container()
        .from("maven:3.9-eclipse-temurin-17")
        .withMountedCache("/root/.m2", mavenCache)
        .withMountedDirectory("/app", source)
        .withWorkdir("/app/translate").withExec(
            List.of("mvn", "compile", "test", "dependency:copy-dependencies",
                "-DincludeScope=runtime"));

    // Get credentials to deploy images to the registry
    Credentials credentials = getRegistryCredentials(client);

    // Deploy the container image for each handler
    //String imageTag = Long.toString(System.currentTimeMillis() / 1000);
    Map<String, String> images = new HashMap<>();
    for (Handler handler : HANDLERS) {
      createDockerRepository(handler);
      // Build and publish the lambda image to registry
      BuildArg handlerArg = new BuildArg();
      handlerArg.setName("HANDLER_CLASS");
      handlerArg.setValue(handler.klass);
      String address = client.container()
          .build(build.directory("/app"), new BuildArguments().withBuildArgs(List.of(handlerArg)))
          .withRegistryAuth(IMAGE_REGISTRY, credentials.username, credentials.password).
          publish(String.format("%s/%s", IMAGE_REGISTRY, handler.imageName));
      images.put(handler.name, address);

      // print image address
      LOG.info(String.format("Image %s published at: %s", handler.klass, address));
    }

    // deploy the stack
    Secret awsSecret = client.setSecret("awsSecret", System.getenv("AWS_SECRET_ACCESS_KEY"));
    List<String> cmd = new ArrayList<>();
    cmd.addAll(List.of("sam", "deploy",
                "--template", "template.yaml",
                "--resolve-image-repos",
                "--no-confirm-changeset",
                "--no-fail-on-empty-changeset",
                "--use-json",
                "--region", REGION.id(),
                "--parameter-overrides"));
    for (Entry<String, String> image: images.entrySet()) {
      cmd.add(String.format("%sFunc=%s", image.getKey(), image.getValue()));
    }
    client.container().from("jcsirot/aws-sam-java17")
        .withEnvVariable("AWS_ACCESS_KEY_ID", System.getenv("AWS_ACCESS_KEY_ID"))
        .withSecretVariable("AWS_SECRET_ACCESS_KEY", awsSecret)
        .withDirectory("/app", build.directory("/app"))
        .withWorkdir("/app")
        .withExec(cmd)
        .sync();
    // retrieve the Application URL
    String url = getAppURL();
    LOG.info("Application deployed to: " + url);
  }

  private record Handler(String name, String klass, String imageName) {

  }

  private record Credentials(String username, Secret password) {

  }
}
