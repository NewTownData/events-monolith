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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class LoggerTest {

  private static final String REQUEST_ID = "test";

  private static final String TEST_MESSAGE = "test message";

  private static final Exception EXAMPLE_EXCEPTION =
      new Exception("Example message", produceException());

  private List<String> logLines;
  private Logger logger;

  @BeforeEach
  void beforeEach() {
    this.logLines = new ArrayList<>();
    this.logger = LoggerFactory.create(REQUEST_ID, logLines::add);
  }

  private static Exception produceException() {
    return produceExceptionAnotherLevel();
  }

  private static Exception produceExceptionAnotherLevel() {
    return new IllegalArgumentException("Other exception");
  }

  @Test
  void testError() {
    logger.error(TEST_MESSAGE);

    assertStandardMessage("ERROR");
  }

  @Test
  void testErrorWithExceptions() {
    logger.error(TEST_MESSAGE, EXAMPLE_EXCEPTION);

    assertExceptionMessage("ERROR");
  }

  @Test
  void testErrorWithSpecialExceptionsNegativeLineNumber() {
    logger.error(TEST_MESSAGE, new Exception() {
      @Override
      public StackTraceElement[] getStackTrace() {
        return new StackTraceElement[] {new StackTraceElement("TestClass", "test", null, -1)};
      }
    });

    assertEquals(1, logLines.size());
    String logLine = logLines.get(0);
    assertTrue(logLine.matches("^[0-9]+\t" + REQUEST_ID + "\tERROR\t" + TEST_MESSAGE
        + Pattern.quote("\t(null)") + "\t\n$"), logLine);
  }

  @Test
  void testErrorWithSpecialExceptionsNoFile() {
    logger.error(TEST_MESSAGE, new Exception() {
      @Override
      public StackTraceElement[] getStackTrace() {
        return new StackTraceElement[] {new StackTraceElement("TestClass", "test", null, 10)};
      }
    });

    assertEquals(1, logLines.size());
    String logLine = logLines.get(0);
    assertTrue(logLine.matches("^[0-9]+\t" + REQUEST_ID + "\tERROR\t" + TEST_MESSAGE
        + Pattern.quote("\t(null)") + "\t\n$"), logLine);
  }

  @Test
  void testErrorWithNoStack() {
    logger.error(TEST_MESSAGE, new Exception() {
      @Override
      public StackTraceElement[] getStackTrace() {
        return new StackTraceElement[0];
      }
    });

    assertEquals(1, logLines.size());
    String logLine = logLines.get(0);
    assertTrue(logLine.matches("^[0-9]+\t" + REQUEST_ID + "\tERROR\t" + TEST_MESSAGE
        + Pattern.quote("\t(null)") + "\t\n$"), logLine);
  }

  @Test
  void testInfo() {
    logger.info(TEST_MESSAGE);

    assertStandardMessage("INFO");
  }

  @Test
  void testInfoWithExceptions() {
    logger.info(TEST_MESSAGE, EXAMPLE_EXCEPTION);

    assertExceptionMessage("INFO");
  }

  @Test
  void testWarn() {
    logger.warn(TEST_MESSAGE);

    assertStandardMessage("WARN");
  }

  @Test
  void testWarnWithExceptions() {
    logger.warn(TEST_MESSAGE, EXAMPLE_EXCEPTION);

    assertExceptionMessage("WARN");
  }

  private void assertStandardMessage(String logLevel) {
    assertEquals(1, logLines.size());
    String logLine = logLines.get(0);
    assertTrue(
        logLine.matches("^[0-9]+\t" + REQUEST_ID + "\t" + logLevel + "\t" + TEST_MESSAGE + "\n$"),
        logLine);
  }

  private void assertExceptionMessage(String logLevel) {
    assertEquals(1, logLines.size());
    String logLine = logLines.get(0);
    assertTrue(logLine.matches("^[0-9]+\t" + REQUEST_ID + "\t" + logLevel + "\t" + TEST_MESSAGE
        + Pattern.quote("\tException(Example message)\tLoggerTest.java:") + "[0-9]+\t"
        + Pattern.quote("IllegalArgumentException(Other exception)\t" + "LoggerTest.java:")
        + "[0-9]+" + Pattern.quote("/LoggerTest.java:") + "[0-9]+"
        + Pattern.quote("/LoggerTest.java:") + "[0-9]+\n$"), logLine);
  }


}
