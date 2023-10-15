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
package com.newtowndata.events.local;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import com.newtowndata.events.core.ApplicationConstants;
import com.newtowndata.events.core.ApplicationEvent;
import com.newtowndata.events.core.StateContext;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class LocalApplicationContextTest {

  @Mock
  StateContext stateContext;

  @Test
  void testEventProducerNegativeWaitTime() {
    ApplicationEvent start = ApplicationEvent.ofStart(10, "test",
        Map.of(ApplicationConstants.ATTRIBUTE_WAIT_TIME_IN_SEC, "-1"));
    assertThrows(IllegalStateException.class,
        () -> new LocalApplicationContext(stateContext, start));
  }

  @Test
  void testEventProducer() {
    ApplicationEvent start = ApplicationEvent.ofStart(10, "test",
        Map.of(ApplicationConstants.ATTRIBUTE_WAIT_TIME_IN_SEC, "1"));
    LocalApplicationContext context = new LocalApplicationContext(stateContext, start);
    ApplicationEvent event = context.consumeEvent().get();
    assertEquals(Map.of(), event.attributes());
  }

}
