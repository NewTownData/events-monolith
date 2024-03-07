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
package com.newtowndata.events.local.io;

import com.newtowndata.events.core.io.StateTable;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 * Local in-memory implementation of {@StateTable}.
 */
public class LocalStateTable implements StateTable {

  private final Map<String, Set<String>> table;

  public LocalStateTable() {
    this.table = new HashMap<>();
  }

  @Override
  public Set<String> getStates(String key) {
    Set<String> result = this.table.get(key);
    if (result == null) {
      return Set.of();
    }
    return result;
  }

  @Override
  public void putState(String key, String state) {
    this.table.computeIfAbsent(key, k -> new LinkedHashSet<>()).add(state);
  }

  @Override
  public void deleteState(String key) {
    this.table.remove(key);
  }

}
