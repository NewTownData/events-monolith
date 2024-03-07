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
import java.util.Objects;
import java.util.function.Consumer;

/**
 * State that executes code.
 */
public class ExecutionState extends GenericState {

  private final String targetState;
  private final Consumer<StateContext> executionRunner;

  public ExecutionState(String stateName, String targetState,
      Consumer<StateContext> executionRunner) {
    super(stateName);
    this.targetState = Objects.requireNonNull(targetState, "targetState");
    this.executionRunner = Objects.requireNonNull(executionRunner, "executionRunner");
  }

  @Override
  protected void handleEvent(StateContext stateContext, ApplicationEvent input,
      Consumer<OutputEvent> outputConsumer) {
    executionRunner.accept(stateContext);
    outputConsumer.accept(OutputEvent.of(targetState, input.attributes()));
  }

}
