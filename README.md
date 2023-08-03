# sample-aws-serverless

This project contains a [Dagger](https://dagger.io/) AWS Serverless Demo project! In this
repository, you'll find everything you need to showcase the integration of Dagger with an AWS
Serverless project.

This project contains a simple Translation Application providing an API using AWS Comprehend to
detect the text language and AWS Translate service. The application is deployed with AWS SAM.

## Prerequisites

Before you begin with the demo, ensure you have the following prerequisites:

- An AWS account with appropriate permissions to create and manage Cloudformation stacks, Lambda
  functions, API Gateway, and other necessary resources.
- Java 17 or higher installed on your local machine.
- Maven 3.9 or higher installed on your local machine.
- Basic knowledge of Cloudformation and serverless concepts.

## Deploy the demo application

Follow these steps to run the demo:

1. Clone this repository to your local machine

2. Set your AWS credentials environment variables

```shell
export AWS_ACCESS_KEY_ID=...
export AWS_SECRET_ACCESS_KEY=...
export AWS_DEFAULT_REGION=...
```

Setting the AWS region is optional, default is `eu-west-2`.

> [!IMPORTANT]  
> If you deploy the stack in another AWS region, make sure that AWS Comprehend is available in that
> region.
> To check the availability of services in AWS regions, you can refer
> to [this page](https://aws.amazon.com/about-aws/global-infrastructure/regional-product-services/).

3. Execute the dagger pipeline

Run this command to deploy the stack
```shell
mvn -f ci/pom.xml -Pdeploy exec:java
```

This command deletes an existing stack
```shell
mvn -f ci/pom.xml -Pdelete exec:java
```


## Description of the Demo stack

The project is composed of:

### the Cloudformation stack

- template.yaml - A template that defines the application's AWS stack resources.
- samconfig.toml - Some stack deployement configuration parameters
- Dockerfile - A parameterized Dockerfile used to build the lambda function images

### Translate/ directory

A maven project containing the serverless application code

- translate/src/main - Code for the application's Lambda function.
- translate/src/test - Unit tests for the application code.

### ci/ directory

A maven project containing the application build and deploy pipelines written
with [Dagger](https://dagger.io) Java SDK

The `ci/src/main/java/CI.java` file contains the code running this pipeline.

![AWS Serverless build & deploy pipeline](AWS%20Serverless%20Dagger%20CI.png)

