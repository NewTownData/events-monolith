# CDK Clean Up

This document describes how CDK clean up works in this Java infrastructure project, which uses [AWS CDK](https://docs.aws.amazon.com/cdk/v2/guide/home.html).

## Limitations

* No support for ECR Docker images created by AWS CDK.
* No support for StackSets or Service Catalog assets.
* Assets must have consistent content, so their hash does not change with every build.

## Pre-requisites

[CDK Bootstrap](https://docs.aws.amazon.com/cdk/v2/guide/bootstrapping.html) creates a staging S3 bucket. This bucket has a lifecycle policy, which removes non-current object versions after 365 days. In order to make the clean up effective, we need to modify the expiration to 1 day.

First, export bootstrap template from the CDK:
```bash
cdk bootstrap --show-template > bootstrap-template.yaml 
```

Then you need to modify the following part of the template from 
```yaml
 LifecycleConfiguration:
        Rules:
          - Id: CleanupOldVersions
            Status: Enabled
            NoncurrentVersionExpiration:
              NoncurrentDays: 365
```

to

```yaml
 LifecycleConfiguration:
        Rules:
          - Id: CleanupOldVersions
            Status: Enabled
            NoncurrentVersionExpiration:
              NoncurrentDays: 1
```

The modified stack can be used by CDK bootstrap in the following way:
```bash
cdk bootstrap --template bootstrap-template.yaml
```

More details at https://docs.aws.amazon.com/cdk/v2/guide/bootstrapping.html#bootstrapping-customizing 

## CDK App Synthesizer Modification

If we deploy multiple CDK projects into an AWS account, assets might overlap and it is hard to determine, what project an S3 object belongs to.

We can use bucket prefix in a [synthesizer](https://docs.aws.amazon.com/cdk/v2/guide/bootstrapping.html#bootstrapping-custom-synth) to prefix all project S3 objects with a project-specific prefix.

Example is at [InfrastructureApp.java](https://github.com/NewTownData/events-monolith/blob/a712418b0af12d203a7cbbd83093f8fe37dc04af/infrastructure/src/main/java/com/newtowndata/infrastructure/InfrastructureApp.java#L45). Your project can use code like this:

```java
import software.amazon.awscdk.App;
import software.amazon.awscdk.AppProps;
import software.amazon.awscdk.DefaultStackSynthesizer;
import software.amazon.awscdk.DefaultStackSynthesizerProps;

AppProps.Builder appPropsBuilder = AppProps.builder();

DefaultStackSynthesizer synthesizer = new DefaultStackSynthesizer(
  DefaultStackSynthesizerProps.builder().
    bucketPrefix("my-project/").
    dockerTagPrefix("my-project-").
    build()
);

appPropsBuilder.defaultStackSynthesizer(synthesizer);

App app = new App(appPropsBuilder.build());
createStacks(app);
app.synth();
```

## Clean Up

### Cloud Assembly

The clean up code leverages CDK's cloud assembly output, which contains list of assets that should be deployed to the staging S3 bucket.

Example code: [InfrastructureApp#createApp](https://github.com/NewTownData/events-monolith/blob/a712418b0af12d203a7cbbd83093f8fe37dc04af/infrastructure/src/main/java/com/newtowndata/infrastructure/InfrastructureApp.java#L52)

### Extract Project Assets to Keep

The first phase is to extract the project assets (S3 objects) that should be kept. That is done by traversing cloud assembly structure and extracting S3 object buckets, keys, region, and assume role ARN.

Example code: [CleanUpAwsAssets#cleanUpAssetManifest](https://github.com/NewTownData/events-monolith/blob/a712418b0af12d203a7cbbd83093f8fe37dc04af/infrastructure/src/main/java/com/newtowndata/infrastructure/CleanUpAwsAssets.java#L120)

### Assume File Publisher Role

The second phase is to use CDK's file publisher role to call S3 API.

Example code: [CleanUpAwsAssets#createS3Client](https://github.com/NewTownData/events-monolith/blob/a712418b0af12d203a7cbbd83093f8fe37dc04af/infrastructure/src/main/java/com/newtowndata/infrastructure/CleanUpAwsAssets.java#L204)

### List All Objects Based on the Project Prefix

The third phase is to list all S3 objects in a project prefix.

Example code: [CleanUpAwsAssets#listObjectToDelete](https://github.com/NewTownData/events-monolith/blob/a712418b0af12d203a7cbbd83093f8fe37dc04af/infrastructure/src/main/java/com/newtowndata/infrastructure/CleanUpAwsAssets.java#L168)

### Delete Objects

The last phase is to delete S3 objects that should not be kept.

Example code: [CleanUpAwsAssets#deleteObjects](https://github.com/NewTownData/events-monolith/blob/a712418b0af12d203a7cbbd83093f8fe37dc04af/infrastructure/src/main/java/com/newtowndata/infrastructure/CleanUpAwsAssets.java#L145)


## Future Work

The approach above should be portable to TypeScript or Python. It is also possible to add ECR Docker image clean up code.
