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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import com.newtowndata.events.core.ApplicationContext;
import com.newtowndata.events.core.ApplicationEvent;
import com.newtowndata.events.core.StateContext;
import com.newtowndata.events.core.io.EventPublisher;
import com.newtowndata.events.core.io.ObjectStorage;
import com.newtowndata.events.lambda.utils.AwsHelper;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AwsApplicationTest {

  @Mock
  ApplicationContext applicationContext;

  @Mock
  StateContext stateContext;

  @Mock
  ObjectStorage objectStorage;

  @Mock
  EventPublisher eventPublisher;

  private AwsApplication application;

  @BeforeEach
  void beforeEach() {
    this.application = new AwsApplication(applicationContext, "example-bucket");
  }

  @Test
  void testHandleRequest() {
    when(applicationContext.eventPublisher()).thenReturn(eventPublisher);
    when(applicationContext.stateContext()).thenReturn(stateContext);
    when(stateContext.objectStorage()).thenReturn(objectStorage);

    application.run(AwsHelper.createEvent(ApplicationEvent.ofStart(10, "hello:input", Map.of())));

    verify(eventPublisher).publishEvent(any());
  }

}
