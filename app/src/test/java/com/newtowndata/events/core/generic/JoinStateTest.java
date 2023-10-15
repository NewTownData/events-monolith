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
package com.newtowndata.events.core.generic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import com.newtowndata.events.core.ApplicationEvent;
import com.newtowndata.events.core.OutputEvent;
import com.newtowndata.events.core.StateContext;
import com.newtowndata.events.core.io.StateTable;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class JoinStateTest {

  private static final String STATE_TARGET = "target";
  private static final String STATE_SOURCE2 = "source2";
  private static final String STATE_SOURCE1 = "source1";
  private static final String STATE_TEST = "test";

  private static final String ID_PATTERN = "[abcdef0-9-]+";

  private static final String TEST_ATTR = "test-attr";
  private static final String TEST_VALUE = "test-value";

  @Mock
  StateContext stateContext;

  @Mock
  StateTable stateTable;

  @Test
  void handleEventNoSavedState() {
    final String eventId = UUID.randomUUID().toString();

    when(stateContext.stateTable()).thenReturn(stateTable);

    JoinState state = new JoinState(STATE_TEST, Set.of(STATE_SOURCE1, STATE_SOURCE2), STATE_TARGET);
    List<OutputEvent> result = state.handleEvent(stateContext, new ApplicationEvent(eventId,
        eventId, 10, STATE_SOURCE1, STATE_TEST, Map.of(TEST_ATTR, TEST_VALUE)));

    assertNotNull(result);
    assertEquals(0, result.size());

    verify(stateTable, times(1)).putState(STATE_TEST + "|" + eventId, STATE_SOURCE1);
  }

  @Test
  void handleEventRepeatedState() {
    final String eventId = UUID.randomUUID().toString();

    when(stateContext.stateTable()).thenReturn(stateTable);
    when(stateTable.getStates(STATE_TEST + "|" + eventId)).thenReturn(Set.of(STATE_SOURCE1));


    JoinState state = new JoinState(STATE_TEST, Set.of(STATE_SOURCE1, STATE_SOURCE2), STATE_TARGET);
    List<OutputEvent> result = state.handleEvent(stateContext, new ApplicationEvent(eventId,
        eventId, 10, STATE_SOURCE1, STATE_TEST, Map.of(TEST_ATTR, TEST_VALUE)));

    assertNotNull(result);
    assertEquals(0, result.size());

    verify(stateTable, times(1)).putState(STATE_TEST + "|" + eventId, STATE_SOURCE1);
  }

  @Test
  void handleEventAllStates() {
    final String eventId = UUID.randomUUID().toString();

    when(stateContext.stateTable()).thenReturn(stateTable);
    when(stateTable.getStates(STATE_TEST + "|" + eventId)).thenReturn(Set.of(STATE_SOURCE2));


    JoinState state = new JoinState(STATE_TEST, Set.of(STATE_SOURCE1, STATE_SOURCE2), STATE_TARGET);
    List<OutputEvent> result = state.handleEvent(stateContext, new ApplicationEvent(eventId,
        eventId, 10, STATE_SOURCE1, STATE_TEST, Map.of(TEST_ATTR, TEST_VALUE)));

    assertNotNull(result);
    assertEquals(1, result.size());

    OutputEvent outputEvent = result.get(0);
    assertEquals(STATE_TARGET, outputEvent.targetState());
    assertTrue(outputEvent.id().matches(ID_PATTERN));
    assertEquals(1, outputEvent.attributes().size());
    assertEquals(TEST_VALUE, outputEvent.attributes().get(TEST_ATTR));

    verify(stateTable, never()).putState(any(), any());
    verify(stateTable, times(1)).deleteState(STATE_TEST + "|" + eventId);
  }
}
