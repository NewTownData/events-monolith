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

import com.newtowndata.events.core.ApplicationEvent;
import com.newtowndata.events.core.OutputEvent;
import com.newtowndata.events.core.StateContext;
import com.newtowndata.events.core.io.StateTable;
import com.newtowndata.events.core.logging.Logger;
import com.newtowndata.events.core.logging.LoggerFactory;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;

/**
 * State that waits for all required source states to finish.
 */
public class JoinState extends GenericState {

  private static final Logger LOG = LoggerFactory.create(JoinState.class);

  private static final String KEY_SEPARATOR = "|";

  private final Set<String> requiredSourceStates;
  private final String targetState;

  public JoinState(String stateName, Set<String> requiredSourceStates, String targetState) {
    super(stateName);
    this.requiredSourceStates =
        Set.copyOf(Objects.requireNonNull(requiredSourceStates, "requiredSourceStates"));
    this.targetState = Objects.requireNonNull(targetState, "targetState");
  }

  @Override
  protected void handleEvent(StateContext stateContext, ApplicationEvent input,
      Consumer<OutputEvent> outputConsumer) {
    StateTable table = stateContext.stateTable();

    String key = stateName() + KEY_SEPARATOR + input.traceId();

    Set<String> savedStates = table.getStates(key);
    Set<String> allStates = new HashSet<>(savedStates);
    allStates.add(input.sourceState());
    LOG.info("States for key " + key + ": " + allStates);

    if (requiredSourceStates.stream().allMatch(allStates::contains)) {
      OutputEvent event = OutputEvent.of(targetState, input.attributes());
      outputConsumer.accept(event);
      table.deleteState(key);
      LOG.info("All states matched for key " + key + ": " + event.id());
    } else {
      table.putState(key, input.sourceState());
      LOG.info("Not all states matched yet for key " + key);
    }
  }

}
