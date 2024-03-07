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
package com.newtowndata.events;

import com.newtowndata.events.core.ApplicationConstants;
import com.newtowndata.events.core.ApplicationContext;
import com.newtowndata.events.core.StateContext;
import com.newtowndata.events.core.generic.ExecutionState;
import com.newtowndata.events.core.generic.ForkState;
import com.newtowndata.events.core.generic.JoinState;
import com.newtowndata.events.core.generic.WaitState;
import com.newtowndata.events.core.router.EventRouter;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * Implementation of an application with defined states and events.
 */
public class Application {

  private static final String STORAGE_EXAMPLE = "example";

  private static final String PATH_HELLO = "hello.txt";
  private static final String PATH_JOHN = "john.txt";
  private static final String PATH_ALICE = "alice.txt";
  private static final String PATH_AMY = "amy.txt";
  private static final String PATH_HI_ALL = "hi_all.txt";

  private static final String STATE_HELLO_INPUT = "hello:input";
  private static final String STATE_HELLO_OUTPUT = "hello:output";
  private static final String STATE_JOHN = "john";
  private static final String STATE_JOHN_WAIT = "john:wait";
  private static final String STATE_ALICE = "alice";
  private static final String STATE_AMY = "amy";
  private static final String STATE_HI_ALL_JOIN = "hi_all:join";
  private static final String STATE_HI_ALL = "hi_all";

  private final EventRouter eventRouter;
  private final String storageName;

  public Application(ApplicationContext applicationContext) {
    this(applicationContext, STORAGE_EXAMPLE);
  }

  public Application(ApplicationContext applicationContext, String storageName) {
    this.storageName = Objects.requireNonNull(storageName, "storageName");
    this.eventRouter = EventRouter.of(applicationContext)
        .withState(new ExecutionState(STATE_HELLO_INPUT, STATE_HELLO_OUTPUT, this::createHello))
        .withState(new ForkState(STATE_HELLO_OUTPUT, List.of(STATE_JOHN, STATE_ALICE, STATE_AMY)))
        .withState(new ExecutionState(STATE_JOHN, STATE_JOHN_WAIT, this::createJohn))
        .withState(new WaitState(STATE_JOHN_WAIT, STATE_HI_ALL_JOIN, 1))
        .withState(new ExecutionState(STATE_ALICE, STATE_HI_ALL_JOIN, this::createAlice))
        .withState(new ExecutionState(STATE_AMY, STATE_HI_ALL_JOIN, this::createAmy))
        .withState(new JoinState(STATE_HI_ALL_JOIN, Set.of(STATE_JOHN_WAIT, STATE_ALICE, STATE_AMY),
            STATE_HI_ALL))
        .withState(
            new ExecutionState(STATE_HI_ALL, ApplicationConstants.STATE_END, this::createHiAll))
        .build();
  }

  public EventRouter getEventRouter() {
    return eventRouter;
  }

  private void createHello(StateContext stateContext) {
    stateContext.objectStorage().putObject(this.storageName, PATH_HELLO, "Hello");
  }

  private void createJohn(StateContext stateContext) {
    String hello = stateContext.objectStorage().getObjectAsString(this.storageName, PATH_HELLO);
    stateContext.objectStorage().putObject(this.storageName, PATH_JOHN, hello + " John");
  }

  private void createAlice(StateContext stateContext) {
    String hello = stateContext.objectStorage().getObjectAsString(this.storageName, PATH_HELLO);
    stateContext.objectStorage().putObject(this.storageName, PATH_ALICE, hello + " Alice");
  }

  private void createAmy(StateContext stateContext) {
    String hello = stateContext.objectStorage().getObjectAsString(this.storageName, PATH_HELLO);
    stateContext.objectStorage().putObject(this.storageName, PATH_AMY, hello + " Amy");
  }

  private void createHiAll(StateContext stateContext) {
    String john = stateContext.objectStorage().getObjectAsString(this.storageName, PATH_JOHN);
    String alice = stateContext.objectStorage().getObjectAsString(this.storageName, PATH_ALICE);
    String amy = stateContext.objectStorage().getObjectAsString(this.storageName, PATH_AMY);

    stateContext.objectStorage().putObject(this.storageName, PATH_HI_ALL,
        john + "\n" + alice + "\n" + amy);
  }

}
