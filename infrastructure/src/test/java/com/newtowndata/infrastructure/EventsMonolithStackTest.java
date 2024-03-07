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

import com.newtowndata.infrastructure.stacks.EventsMonolithStack;
import com.newtowndata.infrastructure.stacks.EventsMonolithStackProps;
import java.util.Map;
import org.junit.jupiter.api.Test;
import software.amazon.awscdk.App;
import software.amazon.awscdk.StackProps;
import software.amazon.awscdk.assertions.Template;

public class EventsMonolithStackTest {

  @Test
  public void testStack() {
    App app = new App();
    EventsMonolithStack stack = new EventsMonolithStack(app,
        new EventsMonolithStackProps("test", StackProps.builder().build())
    );

    Template template = Template.fromStack(stack);
    template.hasResourceProperties("AWS::SQS::Queue",
        Map.of("QueueName", "test-events-monolith-app-queue"));
    template.hasResourceProperties("AWS::SQS::Queue",
        Map.of("QueueName", "test-events-monolith-dlq"));
    template.hasResourceProperties("AWS::S3::Bucket",
        Map.of("BucketName", "test-events-monolith-store"));
    template.hasResourceProperties("AWS::DynamoDB::Table",
        Map.of("TableName", "test-events-monolith-state"));
    template.hasResourceProperties("AWS::Lambda::Function",
        Map.of("FunctionName", "test-events-monolith-app"));
    template.hasResourceProperties("AWS::Lambda::EventSourceMapping", Map.of("Enabled", true));

    template.hasResource("AWS::SQS::QueuePolicy", Map.of());
    template.hasResource("AWS::S3::BucketPolicy", Map.of());
    template.hasResource("Custom::S3AutoDeleteObjects", Map.of());
    template.hasResource("AWS::IAM::Role", Map.of());
    template.hasResource("AWS::IAM::Policy", Map.of());
  }
}
