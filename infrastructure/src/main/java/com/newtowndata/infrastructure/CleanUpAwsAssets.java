/*
 * Copyright 2023-2024 Voyta Krizek, https://github.com/NewTownData
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package com.newtowndata.infrastructure;

import com.newtowndata.infrastructure.cleanup.CleanUpContext;
import com.newtowndata.infrastructure.cleanup.CleanUpDestination;
import com.newtowndata.infrastructure.cleanup.ObjectAsset;
import com.newtowndata.infrastructure.utils.StackUtils;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import software.amazon.awscdk.cloudassembly.schema.FileAsset;
import software.amazon.awscdk.cxapi.AssetManifestArtifact;
import software.amazon.awscdk.cxapi.CloudArtifact;
import software.amazon.awscdk.cxapi.CloudAssembly;
import software.amazon.awssdk.awscore.exception.AwsServiceException;
import software.amazon.awssdk.core.exception.SdkClientException;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.Delete;
import software.amazon.awssdk.services.s3.model.DeleteObjectsRequest;
import software.amazon.awssdk.services.s3.model.DeleteObjectsResponse;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.ObjectIdentifier;
import software.amazon.awssdk.services.sts.StsClient;
import software.amazon.awssdk.services.sts.auth.StsAssumeRoleCredentialsProvider;
import software.amazon.awssdk.services.sts.model.AssumeRoleRequest;
import software.amazon.awssdk.services.sts.model.GetCallerIdentityResponse;

public class CleanUpAwsAssets {

  private static final String AWS_PARTITION = "${AWS::Partition}";
  private static final String AWS_PARTITION_AWS = "aws";

  private static final String AWS_ACCOUNT = "${AWS::AccountId}";
  private static final String AWS_REGION = "${AWS::Region}";

  private static final int DELETE_OBJECTS_BATCH_SIZE = 1000;

  public static void main(final String[] args) throws IOException {
    Path outputPath = Path.of(System.getProperty("user.dir"), "target", "cdk-clean-up");
    if (!Files.exists(outputPath)) {
      Files.createDirectories(outputPath);
    }

    CloudAssembly cloudAssembly = InfrastructureApp.createApp(outputPath);
    cleanUpAssets(cloudAssembly.getArtifacts());
  }

  static void cleanUpAssets(List<CloudArtifact> artifacts) {
    CleanUpContext context = new CleanUpContext();

    artifacts.forEach(artifact -> {
      if (artifact instanceof AssetManifestArtifact assetManifest) {
        System.out.println("Processing " + assetManifest.getId());
        cleanUpAssetManifest(context, assetManifest);
      }
    });

    System.out.println("Buckets to check:");
    context.getDestinations().forEach(destination -> {
      System.out.println(destination.bucketName() + " via " + destination.assumeRoleArn());
    });
    System.out.println("Assets to keep:");
    context.getRetainedAssets().forEach(path -> {
      System.out.println("s3://" + path.bucketName() + "/" + path.objectKey());
    });

    cleanUpBuckets(context);
  }

  static String fixResource(String resourceName, String account, String region) {
    return resourceName.
        replace(AWS_PARTITION, AWS_PARTITION_AWS).
        replace(AWS_ACCOUNT, account).
        replace(AWS_REGION, region);
  }

  static void cleanUpAssetManifest(CleanUpContext context, AssetManifestArtifact assetManifest) {
    if (!assetManifest.getContents().getDockerImages().isEmpty()) {
      throw new IllegalStateException("Docker images are unsupported");
    }

    StsClient sts = StsClient.create();
    GetCallerIdentityResponse callerIdentity = sts.getCallerIdentity();
    String account = callerIdentity.account();
    System.out.println("Account detected: " + account);

    String region = sts.serviceClientConfiguration().region().id();
    System.out.println("Region detected: " + region);

    Map<String, FileAsset> files = assetManifest.getContents().getFiles();
    files.values().forEach(file -> {
      boolean isStack = file.getSource().getPath().endsWith(".template.json");

      file.getDestinations().values().forEach(destination -> {
        String bucketName = fixResource(destination.getBucketName(), account, region);

        context.addDestination(new CleanUpDestination(
            bucketName,
            destination.getRegion() == null ? region : destination.getRegion(),
            fixResource(destination.getAssumeRoleArn(), account, region)
        ));

        if (!isStack) {
          context.addObjectAsset(new ObjectAsset(
              bucketName,
              destination.getObjectKey()
          ));
        }
      });
    });
  }

  static void cleanUpBuckets(CleanUpContext context) {
    for (CleanUpDestination destination : context.getDestinations()) {
      System.out.println("Cleaning up bucket " + destination.bucketName());
      cleanUpBucket(context, destination);
    }

    System.out.println("All buckets were cleaned.");
  }

  static void cleanUpBucket(CleanUpContext context, CleanUpDestination destination) {
    S3Client s3 = createS3Client(destination);
    List<ObjectAsset> objectsToDelete = listObjectToDelete(s3, context, destination);
    deleteObjects(s3, destination, objectsToDelete);
    System.out.println("Bucket " + destination.bucketName() + " was cleaned");
  }

  static void deleteObjects(S3Client s3, CleanUpDestination destination, List<ObjectAsset> objectsToDelete) throws SdkClientException, IllegalStateException, AwsServiceException {
    for (int i = 0; i < objectsToDelete.size(); i += DELETE_OBJECTS_BATCH_SIZE) {
      List<ObjectAsset> batch = objectsToDelete.subList(i,
          Math.min(objectsToDelete.size(), i + DELETE_OBJECTS_BATCH_SIZE));

      List<ObjectIdentifier> objectIdentifiers = batch.stream().map(asset -> ObjectIdentifier.
          builder().key(asset.objectKey()).build()).toList();

      System.out.println("Deleting " + batch.size() + " objects ...");
      DeleteObjectsResponse result = s3.deleteObjects(DeleteObjectsRequest.builder().
          bucket(destination.bucketName()).
          delete(Delete.builder().objects(objectIdentifiers).build()).
          build());
      if (result.hasErrors()) {
        System.out.println("Failed to delete objects:");
        result.errors().forEach(error -> {
          System.out.println("Object key " + error.key() + ": " + error.message());
        });
        throw new IllegalStateException("Failed to delete objects");
      }
    }
  }

  static List<ObjectAsset> listObjectToDelete(S3Client s3, CleanUpContext context, CleanUpDestination destination) {
    List<ObjectAsset> objectsToDelete = new ArrayList<>();
    List<ObjectAsset> objectsToKeep = new ArrayList<>();

    System.out.println("Listing contents of bucket " + destination.bucketName() + " ...");
    s3.listObjectsV2Paginator(
        ListObjectsV2Request.builder().
            bucket(destination.bucketName()).
            prefix(StackUtils.getAssetBucketPrefix(StackUtils.getApplicationName())).
            build()
    ).stream().flatMap(response -> response.contents().stream()).forEach(s3Object -> {
      ObjectAsset asset = new ObjectAsset(destination.bucketName(), s3Object.key());
      if (context.hasAsset(asset)) {
        objectsToKeep.add(asset);
      } else {
        objectsToDelete.add(asset);
      }
    });

    System.out.println("Objects to keep: " + objectsToKeep.size());
    System.out.println("Objects to delete: " + objectsToDelete.size());

    return objectsToDelete;
  }

  static S3Client createS3Client(CleanUpDestination destination) {
    System.out.println("Using role " + destination.assumeRoleArn());
    Region region = Region.of(destination.region());
    StsClient sts = StsClient.builder().region(region).build();
    return S3Client.builder().
        region(region).
        credentialsProvider(
            StsAssumeRoleCredentialsProvider.builder().
                stsClient(sts).
                refreshRequest(
                    AssumeRoleRequest.builder().
                        roleArn(destination.assumeRoleArn()).
                        roleSessionName("cdk-clean-up").
                        build()
                ).
                build()
        ).
        build();
  }

}
