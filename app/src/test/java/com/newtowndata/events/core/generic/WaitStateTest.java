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
package com.newtowndata.events.core.generic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import com.newtowndata.events.core.ApplicationConstants;
import com.newtowndata.events.core.ApplicationEvent;
import com.newtowndata.events.core.OutputEvent;
import com.newtowndata.events.core.StateContext;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class WaitStateTest {

  private static final String STATE_WAIT = "wait";
  private static final String STATE_TARGET = "target";

  private static final String ID_PATTERN = "[abcdef0-9-]+";

  private static final String TEST_ATTR = "test-attr";
  private static final String TEST_VALUE = "test-value";

  @Mock
  StateContext stateContext;

  @Test
  void testNegativeWait() {
    assertThrows(IllegalArgumentException.class, () -> new WaitState(STATE_WAIT, STATE_TARGET, -1));
  }

  @Test
  void testHandleEvent() {
    WaitState state = new WaitState(STATE_WAIT, STATE_TARGET, 10);
    List<OutputEvent> result = state.handleEvent(stateContext,
        ApplicationEvent.ofStart(10, STATE_WAIT, Map.of(TEST_ATTR, TEST_VALUE)));
    assertNotNull(result);
    assertEquals(1, result.size());

    OutputEvent outputEvent = result.get(0);
    assertEquals(STATE_TARGET, outputEvent.targetState());
    assertTrue(outputEvent.id().matches(ID_PATTERN));
    assertEquals(2, outputEvent.attributes().size());
    assertEquals(TEST_VALUE, outputEvent.attributes().get(TEST_ATTR));
    assertEquals("10",
        outputEvent.attributes().get(ApplicationConstants.ATTRIBUTE_WAIT_TIME_IN_SEC));
  }
}
