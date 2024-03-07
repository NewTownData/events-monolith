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

import static com.newtowndata.events.lambda.AwsConstants.BUCKET_NAME;
import static com.newtowndata.events.lambda.AwsConstants.QUEUE_URL;
import static com.newtowndata.events.lambda.AwsConstants.TABLE_NAME;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.SQSEvent;
import com.newtowndata.events.core.ApplicationContext;
import com.newtowndata.events.core.logging.Logger;
import com.newtowndata.events.core.logging.LoggerFactory;
import com.newtowndata.events.env.Environment;

/**
 * AWS Lambda event handler.
 */
public class EventsHandler implements RequestHandler<SQSEvent, String> {

  private static final Logger LOG = LoggerFactory.create(EventsHandler.class);

  public static final String RESULT_OK = "ok";

  private final ApplicationContext applicationContext;
  private final AwsApplication application;

  public EventsHandler() {
    this(new Environment());
  }

  public EventsHandler(Environment environment) {
    this.applicationContext =
        new AwsApplicationContext(environment.getEnvironmentVariable(QUEUE_URL),
            environment.getEnvironmentVariable(TABLE_NAME));
    this.application =
        new AwsApplication(applicationContext, environment.getEnvironmentVariable(BUCKET_NAME));
  }

  @Override
  public String handleRequest(SQSEvent input, Context context) {
    LOG.info("Request " + context.getAwsRequestId());
    application.run(input);
    return RESULT_OK;
  }
}
