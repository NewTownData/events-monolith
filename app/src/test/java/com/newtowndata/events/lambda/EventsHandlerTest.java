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
package com.newtowndata.events.lambda;

import static org.mockito.Mockito.when;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.events.SQSEvent;
import com.newtowndata.events.env.Environment;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.regions.Region;

@ExtendWith(MockitoExtension.class)
class EventsHandlerTest {

  private static final String AWS_REGION = "aws.region";

  @Mock
  Context context;

  @Mock
  Environment environment;

  private EventsHandler handler;

  @BeforeEach
  void beforeEach() {
    System.setProperty(AWS_REGION, Region.EU_WEST_1.id());

    when(environment.getEnvironmentVariable(AwsConstants.BUCKET_NAME)).thenReturn("example-bucket");
    when(environment.getEnvironmentVariable(AwsConstants.QUEUE_URL))
        .thenReturn("http://localhost/example-queue");
    when(environment.getEnvironmentVariable(AwsConstants.TABLE_NAME)).thenReturn("example-table");

    this.handler = new EventsHandler(environment);
  }

  @AfterEach
  void afterEach() {
    System.clearProperty(AWS_REGION);
  }

  @Test
  void testHandleRequest() {
    handler.handleRequest(new SQSEvent(), context);
  }
}
