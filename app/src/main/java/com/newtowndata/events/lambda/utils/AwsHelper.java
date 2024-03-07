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
package com.newtowndata.events.lambda.utils;

import com.amazonaws.services.lambda.runtime.events.SQSEvent;
import com.amazonaws.services.lambda.runtime.events.SQSEvent.SQSMessage;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.newtowndata.events.core.ApplicationEvent;
import java.util.List;
import java.util.UUID;

/**
 * Utility method to create an {@link SQSEvent}.
 */
public final class AwsHelper {

  private AwsHelper() {}

  public static SQSEvent createEvent(ApplicationEvent applicationEvent) {
    Gson gson = new GsonBuilder().create();
    String message = gson.toJson(applicationEvent);

    SQSMessage sqsMessage = new SQSMessage();
    sqsMessage.setBody(message);
    sqsMessage.setMessageId(UUID.randomUUID().toString());

    SQSEvent event = new SQSEvent();
    event.setRecords(List.of(sqsMessage));

    return event;
  }
}
