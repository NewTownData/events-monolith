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
package com.newtowndata.events.core.router;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import com.newtowndata.events.core.ApplicationContext;
import com.newtowndata.events.core.ApplicationEvent;
import com.newtowndata.events.core.ApplicationException;
import com.newtowndata.events.core.ApplicationState;
import com.newtowndata.events.core.OutputEvent;
import com.newtowndata.events.core.StateContext;
import com.newtowndata.events.core.generic.ForkState;
import com.newtowndata.events.core.io.EventPublisher;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class EventRouterTest {

  private static final String STATE_TEST = "test";
  private static final String STATE_TARGET = "target";
  private static final String STATE_TARGET2 = "target2";

  private static final String ID_PATTERN = "[abcdef0-9-]+";

  private static final String TEST_ATTR = "test-attr";
  private static final String TEST_VALUE = "test-value";

  @Mock
  ApplicationContext applicationContext;

  @Mock
  ApplicationState state;

  @Mock
  EventPublisher eventPublisher;

  @Mock
  StateContext stateContext;

  @Test
  void testBuilder() {
    assertThrows(IllegalStateException.class,
        () -> EventRouter.of(applicationContext)
            .withState(new ForkState(STATE_TEST, List.of(STATE_TARGET)))
            .withState(new ForkState(STATE_TEST, List.of(STATE_TARGET))).build());
  }

  @Test
  void testProcessEventZeroTtl() {
    when(state.stateName()).thenReturn(STATE_TEST);
    
    EventRouter router = EventRouter.of(applicationContext).withState(state).build();

    router.processEvent(ApplicationEvent.ofStart(0, STATE_TEST, Map.of()));

    verify(state, never()).handleEvent(any(), any());
    verify(applicationContext, never()).eventPublisher();
  }

  @Test
  void testProcessEventStateError() {
    when(state.stateName()).thenReturn(STATE_TEST);
    when(state.handleEvent(any(), any())).thenThrow(new RuntimeException("test"));
    
    EventRouter router = EventRouter.of(applicationContext).withState(state).build();

    assertThrows(ApplicationException.class, () -> router.processEvent(ApplicationEvent.ofStart(1, STATE_TEST, Map.of())));
  }

  @Test
  void testProcessEventStateReturnsNull() {
    when(state.stateName()).thenReturn(STATE_TEST);
    when(state.handleEvent(any(), any())).thenReturn(null);
    
    EventRouter router = EventRouter.of(applicationContext).withState(state).build();

    router.processEvent(ApplicationEvent.ofStart(1, STATE_TEST, Map.of()));
    verify(applicationContext, never()).eventPublisher();
  }

  @Test
  void testProcessEvent() {
    when(state.stateName()).thenReturn(STATE_TEST);
    when(state.handleEvent(any(), any())).thenReturn(null);
    when(applicationContext.stateContext()).thenReturn(stateContext);
    
    EventRouter router = EventRouter.of(applicationContext).withState(state).build();

    ApplicationEvent event = ApplicationEvent.ofStart(1, STATE_TEST, Map.of());
    router.processEvent(event);
    
    verify(state, times(1)).handleEvent(stateContext, event);
  }

  @Test
  void testNoEventProducer() {
    when(state.stateName()).thenReturn(STATE_TEST);
    when(state.handleEvent(any(), any())).thenReturn(List.of(OutputEvent.of(STATE_TARGET, Map.of())));

    EventRouter router = EventRouter.of(applicationContext).withState(state).build();

    assertThrows(ApplicationException.class,
      () -> router.processEvent(ApplicationEvent.ofStart(1, STATE_TEST, Map.of())));
    
  }

  @Test
  void testEventProducerError() {
    when(state.stateName()).thenReturn(STATE_TEST);
    when(state.handleEvent(any(), any())).thenReturn(List.of(OutputEvent.of(STATE_TARGET, Map.of())));
    when(applicationContext.eventPublisher()).thenReturn(eventPublisher);
    doThrow(new RuntimeException("test")).when(eventPublisher).publishEvent(any());
    
    EventRouter router = EventRouter.of(applicationContext).withState(state).build();

    assertThrows(ApplicationException.class,
      () -> router.processEvent(ApplicationEvent.ofStart(1, STATE_TEST, Map.of())));
  }

  @Test
  void testEventProducer() {
    when(state.stateName()).thenReturn(STATE_TEST);
    when(state.handleEvent(any(), any())).thenReturn(
      List.of(OutputEvent.of(STATE_TARGET, Map.of(TEST_ATTR, TEST_VALUE)))
    );
    when(applicationContext.eventPublisher()).thenReturn(eventPublisher);
    
    EventRouter router = EventRouter.of(applicationContext).withState(state).build();

    ApplicationEvent event = ApplicationEvent.ofStart(1, STATE_TEST, Map.of());
    router.processEvent(event);

    ArgumentCaptor<ApplicationEvent> outputEventCaptor = ArgumentCaptor.forClass(ApplicationEvent.class);
    verify(eventPublisher, times(1)).publishEvent(outputEventCaptor.capture());

    ApplicationEvent outputEvent = outputEventCaptor.getValue();
    assertTrue(outputEvent.id().matches(ID_PATTERN));
    assertEquals(event.traceId(), outputEvent.traceId());
    assertEquals(0, outputEvent.ttl());
    assertEquals(STATE_TEST, outputEvent.sourceState());
    assertEquals(STATE_TARGET, outputEvent.targetState());
    assertEquals(Map.of(TEST_ATTR, TEST_VALUE), outputEvent.attributes());
  }

  @Test
  void testEventProducerForMultipleEvents() {
    when(state.stateName()).thenReturn(STATE_TEST);
    when(state.handleEvent(any(), any())).thenReturn(
      List.of(
        OutputEvent.of(STATE_TARGET, Map.of()),
        OutputEvent.of(STATE_TARGET2, Map.of())
      )
    );
    when(applicationContext.eventPublisher()).thenReturn(eventPublisher);
    
    EventRouter router = EventRouter.of(applicationContext).withState(state).build();

    ApplicationEvent event = ApplicationEvent.ofStart(1, STATE_TEST, Map.of());
    router.processEvent(event);

    ArgumentCaptor<ApplicationEvent> outputEventCaptor = ArgumentCaptor.forClass(ApplicationEvent.class);
    verify(eventPublisher, times(2)).publishEvent(outputEventCaptor.capture());

    List<ApplicationEvent> outputEvents = outputEventCaptor.getAllValues();
    assertEquals(
      Set.of(event.traceId()),
      outputEvents.stream().map(ApplicationEvent::traceId).collect(Collectors.toSet())
    );
    assertEquals(
      Set.of(STATE_TARGET, STATE_TARGET2),
      outputEvents.stream().map(ApplicationEvent::targetState).collect(Collectors.toSet())
    );
  }
}
