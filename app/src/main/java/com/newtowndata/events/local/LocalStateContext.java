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

import com.newtowndata.events.core.StateContext;
import com.newtowndata.events.core.io.ObjectStorage;
import com.newtowndata.events.core.io.StateTable;
import com.newtowndata.events.local.io.LocalObjectStorage;
import com.newtowndata.events.local.io.LocalStateTable;
import java.nio.file.Path;

/**
 * Local implementation of {@link StateContext}.
 */
public class LocalStateContext implements StateContext {

  private final LocalObjectStorage objectStorage;
  private final LocalStateTable stateTable;

  public LocalStateContext(Path objectStorageRoot) {
    this.objectStorage = new LocalObjectStorage(objectStorageRoot);
    this.stateTable = new LocalStateTable();
  }

  @Override
  public ObjectStorage objectStorage() {
    return objectStorage;
  }

  @Override
  public StateTable stateTable() {
    return stateTable;
  }

}
