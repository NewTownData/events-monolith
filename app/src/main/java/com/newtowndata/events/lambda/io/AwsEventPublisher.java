/*
 * Copyright 2023 Voyta Krizek, https://github.com/NewTownData
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

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.newtowndata.events.core.ApplicationEvent;
import com.newtowndata.events.core.io.EventPublisher;
import com.newtowndata.events.core.logging.Logger;
import com.newtowndata.events.core.logging.LoggerFactory;
import com.newtowndata.events.core.utils.EventUtils;
import java.util.Objects;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest.Builder;
import software.amazon.awssdk.services.sqs.model.SendMessageResponse;

/**
 * AWS-specific SQS implementation of {@link EventPublisher}.
 */
public class AwsEventPublisher implements EventPublisher {

  private static final Logger LOG = LoggerFactory.create(AwsEventPublisher.class);

  private final SqsClient client;
  private final String queueUrl;
  private final Gson gson;

  AwsEventPublisher(String queueUrl, SqsClient client) {
    this.queueUrl = Objects.requireNonNull(queueUrl, "queueUrl");
    this.client = Objects.requireNonNull(client, "client");
    this.gson = new GsonBuilder().create();
  }

  public AwsEventPublisher(String queueUrl) {
    this(queueUrl, SqsClient.create());
  }

  @Override
  public void publishEvent(ApplicationEvent event, int waitInSec) {
    if (waitInSec < 0) {
      throw new IllegalStateException("Negative wait time: " + waitInSec);
    }

    Builder builder = SendMessageRequest.builder();
    builder.messageBody(gson.toJson(event)).queueUrl(queueUrl);
    if (waitInSec > 0) {
      builder.delaySeconds(waitInSec);
      LOG.info(EventUtils.eventToString(event) + ": delaying for " + waitInSec + " sec");
    }

    SendMessageResponse result = client.sendMessage(builder.build());
    LOG.info(EventUtils.eventToString(event) + ": sent " + result.messageId());
  }

}
