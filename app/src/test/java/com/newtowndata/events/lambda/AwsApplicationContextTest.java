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

import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import software.amazon.awssdk.regions.Region;

class AwsApplicationContextTest {

  private static final String AWS_REGION = "aws.region";

  private static final String QUEUE_URL = "https://example.com/example-queue";
  private static final String TABLE_NAME = "example-table";

  @BeforeEach
  void beforeEach() {
    System.setProperty(AWS_REGION, Region.EU_WEST_1.id());
  }

  @AfterEach
  void afterEach() {
    System.clearProperty(AWS_REGION);
  }

  @Test
  void testLifecycle() {
    AwsApplicationContext context = new AwsApplicationContext(QUEUE_URL, TABLE_NAME);
    assertNotNull(context.eventPublisher());
    assertNotNull(context.stateContext());
    assertNotNull(context.stateContext().objectStorage());
    assertNotNull(context.stateContext().stateTable());
  }

}
