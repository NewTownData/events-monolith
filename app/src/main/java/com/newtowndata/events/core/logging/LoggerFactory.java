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
package com.newtowndata.events.core.logging;

import java.util.Objects;
import java.util.function.Consumer;

/**
 * Factory for {@link Logger}.
 */
public class LoggerFactory {

  private LoggerFactory() {}

  public static Logger create(String requestId, Consumer<String> logConsumer) {
    Objects.requireNonNull(requestId, "requestId");
    Objects.requireNonNull(logConsumer, "logConsumer");
    return new Logger(requestId, logConsumer);
  }

  public static Logger create(Class<?> clazz) {
    Objects.requireNonNull(clazz, "clazz");
    return new Logger(clazz.getSimpleName(), System.out::print);
  }

}
