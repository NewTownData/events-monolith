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

import java.util.function.Consumer;

/**
 * Simple logger.
 */
public class Logger {

  private static final char SEPARATOR = '\t';

  private final String requestId;
  private final Consumer<String> loggerFunction;

  Logger(String requestId, Consumer<String> loggerFunction) {
    this.requestId = requestId;
    this.loggerFunction = loggerFunction;
  }

  public void error(Object message, Exception e) {
    log("ERROR", message, e);
  }

  public void error(Object message) {
    log("ERROR", message, null);
  }

  public void info(Object message) {
    log("INFO", message, null);
  }

  public void info(Object message, Exception e) {
    log("INFO", message, e);
  }

  public void warn(Object message) {
    log("WARN", message, null);
  }

  public void warn(Object message, Exception e) {
    log("WARN", message, e);
  }

  private void log(String severity, Object message, Throwable e) {
    StringBuilder sb = new StringBuilder();
    sb.append(System.currentTimeMillis());
    sb.append(SEPARATOR);
    sb.append(requestId);
    sb.append(SEPARATOR);
    sb.append(severity);
    sb.append(SEPARATOR);
    sb.append(message);

    while (e != null) {
      sb.append(SEPARATOR);
      sb.append(e.getClass().getSimpleName());
      sb.append('(');
      sb.append(e.getMessage());
      sb.append(')');
      sb.append(SEPARATOR);
      StackTraceElement[] stackTrace = e.getStackTrace();
      for (int i = 0; i < stackTrace.length; i++) {
        StackTraceElement element = stackTrace[i];
        String fileName = element.getFileName();
        int fileNumber = element.getLineNumber();

        if (fileNumber < 0 || fileName == null) {
          break;
        }

        if (i != 0) {
          sb.append('/');
        }
        sb.append(fileName);
        sb.append(':');
        sb.append(fileNumber);
      }
      e = e.getCause();
    }

    sb.append('\n');

    loggerFunction.accept(sb.toString());
  }

}
