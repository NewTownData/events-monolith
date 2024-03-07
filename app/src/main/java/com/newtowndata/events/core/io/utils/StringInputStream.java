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
package com.newtowndata.events.core.io.utils;

import com.newtowndata.events.core.io.ObjectStorage;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;

/**
 * Utility class for {@link ObjectStorage}.
 */
public class StringInputStream extends ByteArrayInputStream {

  public StringInputStream(String text) {
    super(text.getBytes(StandardCharsets.UTF_8));
  }

  @Override
  public void close() {}
}
