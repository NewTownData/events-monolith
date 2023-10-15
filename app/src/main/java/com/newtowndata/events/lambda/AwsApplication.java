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
package com.newtowndata.events.lambda;

import com.amazonaws.services.lambda.runtime.events.SQSEvent;
import com.amazonaws.services.lambda.runtime.events.SQSEvent.SQSMessage;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.newtowndata.events.Application;
import com.newtowndata.events.core.ApplicationContext;
import com.newtowndata.events.core.ApplicationEvent;
import com.newtowndata.events.core.logging.Logger;
import com.newtowndata.events.core.logging.LoggerFactory;

/**
 * AWS-specific {@link Application} instance.
 */
public class AwsApplication {

  private static final Logger LOG = LoggerFactory.create(AwsApplication.class);

  private final Application application;
  private final Gson gson;

  public AwsApplication(ApplicationContext context, String bucketName) {
    this.application = new Application(context, bucketName);
    this.gson = new GsonBuilder().create();
  }

  public void run(SQSEvent event) {
    if (event.getRecords() != null) {
      for (SQSMessage message : event.getRecords()) {
        run(message);
      }
    }
  }

  private void run(SQSMessage message) {
    LOG.info("Processing SQS message: " + message.getMessageId());
    ApplicationEvent event = gson.fromJson(message.getBody(), ApplicationEvent.class);
    application.getEventRouter().processEvent(event);
    LOG.info("SQS message processed: " + message.getMessageId());
  }

}
