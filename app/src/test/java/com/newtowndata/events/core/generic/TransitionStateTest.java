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
import static org.junit.jupiter.api.Assertions.assertTrue;
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
class TransitionStateTest {

  private static final String ID_PATTERN = "[abcdef0-9-]+";

  private static final String STATE_TEST = "test";
  private static final String STATE_TARGET1 = "target1";
  private static final String STATE_TARGET2 = "target2";

  private static final String TEST_ATTR = "test-attr";
  private static final String TEST_VALUE = "test-value";

  @Mock
  StateContext stateContext;

  @Test
  void testHandleEvent() {
    ForkState state = new ForkState(STATE_TEST, List.of(STATE_TARGET1, STATE_TARGET2));
    List<OutputEvent> result = state.handleEvent(stateContext,
        ApplicationEvent.ofStart(10, STATE_TEST, Map.of(TEST_ATTR, TEST_VALUE)));

    assertNotNull(result);
    assertEquals(2, result.size());

    OutputEvent outputEvent1 = result.get(0);
    assertEquals(STATE_TARGET1, outputEvent1.targetState());
    assertTrue(outputEvent1.id().matches(ID_PATTERN));
    assertEquals(1, outputEvent1.attributes().size());
    assertEquals(TEST_VALUE, outputEvent1.attributes().get(TEST_ATTR));

    OutputEvent outputEvent2 = result.get(1);
    assertEquals(STATE_TARGET2, outputEvent2.targetState());
    assertTrue(outputEvent2.id().matches(ID_PATTERN));
    assertEquals(1, outputEvent2.attributes().size());
    assertEquals(TEST_VALUE, outputEvent2.attributes().get(TEST_ATTR));
  }
}
