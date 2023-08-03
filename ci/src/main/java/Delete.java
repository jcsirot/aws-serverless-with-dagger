import io.dagger.client.Client;
import io.dagger.client.Dagger;
import io.dagger.client.Directory;
import io.dagger.client.Host.DirectoryArguments;
import io.dagger.client.Secret;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.regions.Region;

public class Delete {

  private static final Logger LOG = LoggerFactory.getLogger(Delete.class);
  private static final String DEFAULT_REGION = "eu-west-2";
  private static final Region REGION = Region.of(
      System.getenv().getOrDefault("AWS_DEFAULT_REGION", DEFAULT_REGION));

  public static void main(String... args) throws Exception {
    verifyEnvVars();
    try (Client client = Dagger.connect()) {
      deleteStack(client);
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

  private static void deleteStack(Client client) throws Exception {

    // get reference to source code directory
    Directory source = client.host()
        .directory(".", new DirectoryArguments().withExclude(List.of("ci")));

    Secret awsSecret = client.setSecret("awsSecret", System.getenv("AWS_SECRET_ACCESS_KEY"));
    client.container().from("jcsirot/aws-sam-java17")
        .withEnvVariable("AWS_ACCESS_KEY_ID", System.getenv("AWS_ACCESS_KEY_ID"))
        .withSecretVariable("AWS_SECRET_ACCESS_KEY", awsSecret)
        .withMountedDirectory("/app", source)
        .withWorkdir("/app")
        .withExec(List.of("sam", "delete", "--region", REGION.id(), "--no-prompts"))
        .stdout();
  }
}
