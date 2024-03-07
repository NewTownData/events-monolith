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
package com.newtowndata.events.lambda.io;

import com.newtowndata.events.core.io.ObjectStorage;
import com.newtowndata.events.core.logging.Logger;
import com.newtowndata.events.core.logging.LoggerFactory;
import com.newtowndata.events.lambda.io.utils.TemporaryFile;
import java.io.InputStream;
import java.util.Objects;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;

/**
 * AWS-specific S3 implementation of {@link ObjectStorage}.
 */
public class AwsObjectStorage implements ObjectStorage {

  private static final Logger LOG = LoggerFactory.create(AwsObjectStorage.class);

  private final S3Client client;

  AwsObjectStorage(S3Client client) {
    this.client = Objects.requireNonNull(client, "client");
  }

  public AwsObjectStorage() {
    this(S3Client.create());
  }

  @Override
  public InputStream getObject(String storageName, String path) {
    return client.getObject(GetObjectRequest.builder().bucket(storageName).key(path).build());
  }

  @Override
  public void putObject(String storageName, String path, InputStream inputStream) {
    // S3 needs to know object size before upload, so we will create a temp file first
    try (TemporaryFile tempFile = new TemporaryFile("s3-upload", ".bin")) {
      tempFile.overwrite(inputStream);
      PutObjectResponse response =
          client.putObject(PutObjectRequest.builder().bucket(storageName).key(path).build(),
              RequestBody.fromFile(tempFile.getTempFile()));
      LOG.info("S3 object s3://" + storageName + "/" + path + " created: " + response.eTag());
    }
  }

  @Override
  public void deleteObject(String storageName, String path) {
    client.deleteObject(DeleteObjectRequest.builder().bucket(storageName).key(path).build());
  }

}
