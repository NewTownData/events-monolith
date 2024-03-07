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
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.newtowndata.events.core.ApplicationConstants;
import com.newtowndata.events.core.ApplicationEvent;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;
import software.amazon.awssdk.services.sqs.model.SendMessageResponse;

@ExtendWith(MockitoExtension.class)
class AwsEventPublisherTest {

  private static final String TEST_QUEUE_URL = "https://example.com/test-queue";
  private static final Gson GSON = new GsonBuilder().create();

  @Mock
  SqsClient client;

  private AwsEventPublisher eventPublisher;

  @BeforeEach
  void beforeEach() {
    this.eventPublisher = new AwsEventPublisher(TEST_QUEUE_URL, client);
  }

  @Test
  void testPublishEvent() {
    when(client.sendMessage(any(SendMessageRequest.class))).thenReturn(SendMessageResponse.builder().messageId("test-id").build());

    ApplicationEvent event = new ApplicationEvent(
      "51c1da07-0188-41e8-9999-ce960b3c1356",
      "51c1da07-0188-41e8-9999-ce960b3c1356",
      0, "start","test",
      Map.of());
    eventPublisher.publishEvent(event);

    ArgumentCaptor<SendMessageRequest> argumentCaptor =
        ArgumentCaptor.forClass(SendMessageRequest.class);
    verify(client, times(1)).sendMessage(argumentCaptor.capture());

    SendMessageRequest request = argumentCaptor.getValue();

    assertEquals(null, request.delaySeconds());
    assertEquals(TEST_QUEUE_URL, request.queueUrl());
    assertEquals(GSON.toJson(event), request.messageBody());
  }

  @Test
  void testPublishEventWithDelay() {
    when(client.sendMessage(any(SendMessageRequest.class))).thenReturn(SendMessageResponse.builder().messageId("test-id").build());

    ApplicationEvent event = new ApplicationEvent(
      "51c1da07-0188-41e8-9999-ce960b3c1356",
      "51c1da07-0188-41e8-9999-ce960b3c1356",
      10, "start","test",
      Map.of(ApplicationConstants.ATTRIBUTE_WAIT_TIME_IN_SEC, "2"));
    eventPublisher.publishEvent(event);

    ArgumentCaptor<SendMessageRequest> argumentCaptor =
        ArgumentCaptor.forClass(SendMessageRequest.class);
    verify(client, times(1)).sendMessage(argumentCaptor.capture());

    SendMessageRequest request = argumentCaptor.getValue();

    assertEquals(2, request.delaySeconds());
    assertEquals(TEST_QUEUE_URL, request.queueUrl());
    assertEquals(GSON.toJson(new ApplicationEvent(
      event.id(), event.traceId(), 10,
      event.sourceState(), event.targetState(), Map.of())
    ), request.messageBody());
  }

  @Test
  void testPublishEventWithNegativeDelay() {
    ApplicationEvent event = new ApplicationEvent("51c1da07-0188-41e8-9999-ce960b3c1356",
        "51c1da07-0188-41e8-9999-ce960b3c1356", 10, "start", "test",
        Map.of(ApplicationConstants.ATTRIBUTE_WAIT_TIME_IN_SEC, "-2"));
    assertThrows(IllegalStateException.class, () -> eventPublisher.publishEvent(event));
  }

  @Test
  void testPublishEventWithNegativeDelayDirect() {
    ApplicationEvent event = new ApplicationEvent("51c1da07-0188-41e8-9999-ce960b3c1356",
        "51c1da07-0188-41e8-9999-ce960b3c1356", 10, "start", "test", Map.of());
    assertThrows(IllegalStateException.class, () -> eventPublisher.publishEvent(event, -1));
  }
}
