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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import com.newtowndata.events.core.ApplicationEvent;
import com.newtowndata.events.core.OutputEvent;
import com.newtowndata.events.core.StateContext;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ExecutionStateTest {

  private static final String ID_PATTERN = "[abcdef0-9-]+";
  
  private static final String STATE_TEST = "test";
  private static final String STATE_TARGET = "target";
  
  private static final String TEST_ATTR = "test-attr";
  private static final String TEST_VALUE = "test-value";

  @Mock
  StateContext stateContext;

  @Test
  void testHandleEvent() {
    AtomicBoolean executionMarker = new AtomicBoolean(false);

    ExecutionState state =
        new ExecutionState(STATE_TEST, STATE_TARGET, context -> executionMarker.set(true));
    assertFalse(executionMarker.get());
    List<OutputEvent> result = state.handleEvent(stateContext,
        ApplicationEvent.ofStart(10, STATE_TEST, Map.of(TEST_ATTR, TEST_VALUE)));
    assertTrue(executionMarker.get());

    assertNotNull(result);
    assertEquals(1, result.size());

    OutputEvent outputEvent = result.get(0);
    assertEquals(STATE_TARGET, outputEvent.targetState());
    assertTrue(outputEvent.id().matches(ID_PATTERN));
    assertEquals(1, outputEvent.attributes().size());
    assertEquals(TEST_VALUE, outputEvent.attributes().get(TEST_ATTR));
  }
}
