package com.hms_networks.americas.sc.retry.test;

import com.hms_networks.americas.sc.retry.*;

import java.util.Random;

public class AutomaticRetryTestMain {
  private static boolean testCoreAbstractCode() {
    // Generate random parameters
    Random random = new Random();
    final int maxRetries = random.nextInt(50 - 25) + 25;
    final long delayBeforeRetryMillis = random.nextInt(5000 - 1000) + 1000;
    final int triesBeforeSuccess = random.nextInt(maxRetries - (maxRetries / 2)) + (maxRetries / 2);

    // Create test code implementation
    AutomaticRetryCode automaticRetryCode =
        new AutomaticRetryCode() {
          long previousEndTime = 0;
          int retry = 0;
          boolean finished = false;
          boolean stopping = false;

          protected int getMaxRetries() {
            return maxRetries;
          }

          protected long getDelayMillisBeforeRetry(int retry) {
            return delayBeforeRetryMillis;
          }

          protected void codeToRetry() {
            long startTime = System.currentTimeMillis();

            // Check if previously successful or critical stopped
            if (finished) {
              System.err.println(
                  "FAILURE["
                      + new Throwable().getStackTrace()[2].getMethodName()
                      + "]: Code was previously successful. Execution should have ended!");
            } else if (stopping) {
              System.err.println(
                  "FAILURE["
                      + new Throwable().getStackTrace()[2].getMethodName()
                      + "]: Code was previously encountered a critical error. Execution should have ended!");
            }

            // Check if delay was proper time
            if (previousEndTime > 0) {
              long diffLowest = getDelayMillisBeforeRetry(retry) - 1000;
              long diffHighest = getDelayMillisBeforeRetry(retry) + 1000;
              long diffActual = startTime - previousEndTime;

              if (diffActual < diffLowest || diffActual > diffHighest) {
                setState(AutomaticRetryState.ERROR_STOP);
                stopping = true;
                System.err.println(
                    "FAILURE["
                        + new Throwable().getStackTrace()[2].getMethodName()
                        + "]: Delay was outside expected range!");
              }
            }

            if (!stopping) {
              if (retry >= triesBeforeSuccess) {
                setState(AutomaticRetryState.FINISHED);
                finished = true;
                System.out.println(
                    "RUNNING["
                        + new Throwable().getStackTrace()[2].getMethodName()
                        + "]: Marked state as finished. Execution should end!");
              } else {
                setState(AutomaticRetryState.ERROR_RETRY);
                System.out.println(
                    "RUNNING["
                        + new Throwable().getStackTrace()[2].getMethodName()
                        + "]: Marked state as ERROR_RETRY. Code should re-attempt after delay: "
                        + getDelayMillisBeforeRetry(retry)
                        + "["
                        + (retry + 1)
                        + "/"
                        + triesBeforeSuccess
                        + "]");
              }
            }

            retry++;
            previousEndTime = System.currentTimeMillis();
          }
        };

    // Run implementation
    try {
      AutomaticRetryResult result = automaticRetryCode.run();
      if (result == AutomaticRetryResult.SUCCESS) {
        return true;
      }
    } catch (Exception ignored) {
    }

    // Return false if test not successful
    return false;
  }

  private static boolean testExponentialAbstractCode() {
    // Generate random parameters
    Random random = new Random();
    final int maxRetries = random.nextInt(50 - 25) + 25;
    final int triesBeforeSuccess = random.nextInt(maxRetries - (maxRetries / 2)) + (maxRetries / 2);
    final long maxRetryDelay =
        random.nextInt((maxRetries * 2500) - (maxRetries * 1000)) + (maxRetries * 1000);
    System.out.println(
        "RUNNING["
            + new Throwable().getStackTrace()[0].getMethodName()
            + "]: Using maximum retry delay of "
            + maxRetryDelay
            + " milliseconds.");

    // Create test code implementation
    AutomaticRetryCode automaticRetryCode =
        new AutomaticRetryCodeExponential() {
          long previousEndTime = 0;
          int retry = 0;
          boolean finished = false;
          boolean stopping = false;

          protected int getMaxRetries() {
            return maxRetries;
          }

          protected long getMaxDelayMillisBeforeRetry() {
            return maxRetryDelay;
          }

          protected void codeToRetry() {
            long startTime = System.currentTimeMillis();

            // Check if previously successful or critical stopped
            if (finished) {
              System.err.println(
                  "FAILURE["
                      + new Throwable().getStackTrace()[2].getMethodName()
                      + "]: Code was previously successful. Execution should have ended!");
            } else if (stopping) {
              System.err.println(
                  "FAILURE["
                      + new Throwable().getStackTrace()[2].getMethodName()
                      + "]: Code was previously encountered a critical error. Execution should have ended!");
            }

            // Check if delay was proper time
            if (previousEndTime > 0) {
              long diffLowest = getDelayMillisBeforeRetry(retry) - 1000;
              long diffHighest = getDelayMillisBeforeRetry(retry) + 1000;
              long diffActual = startTime - previousEndTime;

              if (diffActual < diffLowest || diffActual > diffHighest) {
                setState(AutomaticRetryState.ERROR_STOP);
                stopping = true;
                System.err.println(
                    "FAILURE["
                        + new Throwable().getStackTrace()[2].getMethodName()
                        + "]: Delay was outside expected range!");
              }
            }

            // Inspect delay millis within range
            if (getDelayMillisBeforeRetry(retry) > getMaxDelayMillisBeforeRetry()) {
              System.err.println(
                  "FAILURE["
                      + new Throwable().getStackTrace()[2].getMethodName()
                      + "]: Expected delay exceeded maximum delay specified by getMaxDelayMillisBeforeRetry()!");
            }

            if (!stopping) {
              if (retry >= triesBeforeSuccess) {
                setState(AutomaticRetryState.FINISHED);
                finished = true;
                System.out.println(
                    "RUNNING["
                        + new Throwable().getStackTrace()[2].getMethodName()
                        + "]: Marked state as finished. Execution should end!");
              } else {
                setState(AutomaticRetryState.ERROR_RETRY);
                System.out.println(
                    "RUNNING["
                        + new Throwable().getStackTrace()[2].getMethodName()
                        + "]: Marked state as ERROR_RETRY. Code should re-attempt after delay: "
                        + getDelayMillisBeforeRetry(retry)
                        + "["
                        + (retry + 1)
                        + "/"
                        + triesBeforeSuccess
                        + "]");
              }
            }

            retry++;
            previousEndTime = System.currentTimeMillis();
          }
        };

    // Run implementation
    try {
      AutomaticRetryResult result = automaticRetryCode.run();
      if (result == AutomaticRetryResult.SUCCESS) {
        return true;
      }
    } catch (Exception ignored) {
    }

    // Return false if test not successful
    return false;
  }

  private static boolean testLinearAbstractCode() {

    // Generate random parameters
    Random random = new Random();
    final int maxRetries = random.nextInt(50 - 25) + 25;
    final int triesBeforeSuccess = random.nextInt(maxRetries - (maxRetries / 2)) + (maxRetries / 2);
    final long linearSlopeMillis = random.nextInt(10000 - 2000) + 2000;
    final long maxRetryDelay =
        random.nextInt((maxRetries * 2500) - (maxRetries * 1000)) + (maxRetries * 1000);
    System.out.println(
        "RUNNING["
            + new Throwable().getStackTrace()[0].getMethodName()
            + "]: Using maximum retry delay of "
            + maxRetryDelay
            + " milliseconds.");

    // Create test code implementation
    AutomaticRetryCode automaticRetryCode =
        new AutomaticRetryCodeLinear() {

          long previousEndTime = 0;
          int retry = 0;
          boolean finished = false;
          boolean stopping = false;

          protected long getLinearSlopeMillis() {
            return linearSlopeMillis;
          }

          protected long getMaxDelayMillisBeforeRetry() {
            return maxRetryDelay;
          }

          protected int getMaxRetries() {
            return maxRetries;
          }

          protected void codeToRetry() {
            long startTime = System.currentTimeMillis();

            // Check if previously successful or critical stopped
            if (finished) {
              System.err.println(
                  "FAILURE["
                      + new Throwable().getStackTrace()[2].getMethodName()
                      + "]: Code was previously successful. Execution should have ended!");
            } else if (stopping) {
              System.err.println(
                  "FAILURE["
                      + new Throwable().getStackTrace()[2].getMethodName()
                      + "]: Code was previously encountered a critical error. Execution should have ended!");
            }

            // Check if delay was proper time
            if (previousEndTime > 0) {
              long diffLowest = getDelayMillisBeforeRetry(retry) - 1000;
              long diffHighest = getDelayMillisBeforeRetry(retry) + 1000;
              long diffActual = startTime - previousEndTime;

              if (diffActual < diffLowest || diffActual > diffHighest) {
                setState(AutomaticRetryState.ERROR_STOP);
                stopping = true;
                System.err.println(
                    "FAILURE["
                        + new Throwable().getStackTrace()[2].getMethodName()
                        + "]: Delay was outside expected range!");
              }
            }

            // Inspect delay millis within range
            if (getDelayMillisBeforeRetry(retry) > getMaxDelayMillisBeforeRetry()) {
              System.err.println(
                  "FAILURE["
                      + new Throwable().getStackTrace()[2].getMethodName()
                      + "]: Expected delay exceeded maximum delay specified by getMaxDelayMillisBeforeRetry()!");
            }

            if (!stopping) {
              if (retry >= triesBeforeSuccess) {
                setState(AutomaticRetryState.FINISHED);
                finished = true;
                System.out.println(
                    "RUNNING["
                        + new Throwable().getStackTrace()[2].getMethodName()
                        + "]: Marked state as finished. Execution should end!");
              } else {
                setState(AutomaticRetryState.ERROR_RETRY);
                System.out.println(
                    "RUNNING["
                        + new Throwable().getStackTrace()[2].getMethodName()
                        + "]: Marked state as ERROR_RETRY. Code should re-attempt after delay: "
                        + getDelayMillisBeforeRetry(retry)
                        + "["
                        + (retry + 1)
                        + "/"
                        + triesBeforeSuccess
                        + "]");
              }
            }

            retry++;
            previousEndTime = System.currentTimeMillis();
          }
        };

    // Run implementation
    try {
      AutomaticRetryResult result = automaticRetryCode.run();
      if (result == AutomaticRetryResult.SUCCESS) {
        return true;
      }
    } catch (Exception ignored) {
    }

    // Return false if test not successful
    return false;
  }

  public static void main(String[] args) {

    final boolean testCoreAbstractCode = testCoreAbstractCode();
    if (!testCoreAbstractCode) {
      System.err.println("FAILED: testCoreAbstractCode");
    } else {
      System.out.println("SUCCESS: testCoreAbstractCode");
    }

    final boolean testExponentialAbstractCode = testExponentialAbstractCode();
    if (!testExponentialAbstractCode) {
      System.err.println("FAILED: testExponentialAbstractCode");
    } else {
      System.out.println("SUCCESS: testExponentialAbstractCode");
    }

    final boolean testLinearAbstractCode = testLinearAbstractCode();
    if (!testLinearAbstractCode) {
      System.err.println("FAILED: testLinearAbstractCode");
    } else {
      System.out.println("SUCCESS: testLinearAbstractCode");
    }

    // If test failed, exit with non-zero status code
    if (!testCoreAbstractCode || !testExponentialAbstractCode || !testLinearAbstractCode) {
      System.exit(-1);
    }
  }
}
