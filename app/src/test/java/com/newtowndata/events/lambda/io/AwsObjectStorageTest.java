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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;

@ExtendWith(MockitoExtension.class)
class AwsObjectStorageTest {

  private static final String TEST_BUCKET = "example";
  private static final String TEST_OBJECT_KEY = "prefix/test.txt";

  @Mock
  S3Client client;

  private AwsObjectStorage objectStorage;

  @BeforeEach
  void beforeEach() {
    this.objectStorage = new AwsObjectStorage(client);
  }

  @Test
  void testGetObjectException() {
    when(client.getObject(any(GetObjectRequest.class))).thenReturn(new ResponseInputStream<GetObjectResponse>(GetObjectResponse.builder().build(), new InputStream() {

      @Override
      public int read() throws IOException {
        throw new IOException("Test");
      }
      
    }));

    assertThrows(IllegalStateException.class, 
      () -> objectStorage.getObjectAsString(TEST_BUCKET, TEST_OBJECT_KEY));
  }

  @Test
  void testGetObject() {
    String exampleContent = "hello world";
    when(client.getObject(any(GetObjectRequest.class)))
        .thenReturn(new ResponseInputStream<GetObjectResponse>(GetObjectResponse.builder().build(),
            new ByteArrayInputStream(exampleContent.getBytes(StandardCharsets.UTF_8))));

    String content = objectStorage.getObjectAsString(TEST_BUCKET, TEST_OBJECT_KEY);
    assertEquals(exampleContent, content);

    ArgumentCaptor<GetObjectRequest> argumentCaptor =
        ArgumentCaptor.forClass(GetObjectRequest.class);
    verify(client, times(1)).getObject(argumentCaptor.capture());

    GetObjectRequest request = argumentCaptor.getValue();

    assertEquals(TEST_BUCKET, request.bucket());
    assertEquals(TEST_OBJECT_KEY, request.key());
  }

  @Test
  void testPutObject() throws IOException {
    String exampleContent = "hello world";
    when(client.putObject(any(PutObjectRequest.class), any(RequestBody.class)))
        .thenAnswer(answer -> {
          PutObjectRequest request = answer.getArgument(0);
          assertEquals(TEST_BUCKET, request.bucket());
          assertEquals(TEST_OBJECT_KEY, request.key());

          RequestBody requestBody = answer.getArgument(1);
          assertEquals(exampleContent,
              new String(requestBody.contentStreamProvider().newStream().readAllBytes(),
                  StandardCharsets.UTF_8));

          return PutObjectResponse.builder().eTag("abc").build();
        });

    objectStorage.putObject(TEST_BUCKET, TEST_OBJECT_KEY, exampleContent);

    verify(client, times(1)).putObject(any(PutObjectRequest.class), any(RequestBody.class));
  }

  @Test
  void testDeleteObject() {
    objectStorage.deleteObject(TEST_BUCKET, TEST_OBJECT_KEY);

    ArgumentCaptor<DeleteObjectRequest> argumentCaptor =
        ArgumentCaptor.forClass(DeleteObjectRequest.class);
    verify(client, times(1)).deleteObject(argumentCaptor.capture());

    DeleteObjectRequest request = argumentCaptor.getValue();

    assertEquals(TEST_BUCKET, request.bucket());
    assertEquals(TEST_OBJECT_KEY, request.key());
  }
}
