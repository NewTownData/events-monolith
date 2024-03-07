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
package com.newtowndata.events.core.io;

import com.newtowndata.events.core.io.utils.StringInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

/**
 * Object storage abstraction.
 */
public interface ObjectStorage {

  public static final String PATH_SEPARATOR = "/";

  InputStream getObject(String storageName, String path);

  default String getObjectAsString(String storageName, String path) {
    try (InputStream isr = getObject(storageName, path)) {
      return new String(isr.readAllBytes(), StandardCharsets.UTF_8);
    } catch (IOException e) {
      throw new IllegalStateException("Failed to read string for " + storageName + ", path " + path,
          e);
    }
  }

  void putObject(String storageName, String path, InputStream inputStream);

  default void putObject(String storageName, String path, String data) {
    try (StringInputStream isr = new StringInputStream(data)) {
      putObject(storageName, path, isr);
    }
  }

  void deleteObject(String storageName, String path);

}
