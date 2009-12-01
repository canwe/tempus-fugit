/*
 * Copyright (c) 2009, tempus-fugit committers
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at 
 *
 *      http://www.apache.org/licenses/LICENSE-2.0 
 *
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License.
 */

package com.google.code.tempusfugit.temporal;

import static com.google.code.tempusfugit.temporal.Duration.millis;
import static com.google.code.tempusfugit.temporal.Duration.seconds;
import static com.google.code.tempusfugit.temporal.WaitFor.waitOrTimeout;
import static com.google.code.tempusfugit.temporal.WaitFor.waitUntil;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.Sequence;
import org.jmock.integration.junit4.JMock;
import org.jmock.integration.junit4.JUnit4Mockery;
import org.junit.Test;
import org.junit.runner.RunWith;

import static java.lang.Thread.currentThread;
import java.util.concurrent.TimeoutException;

@RunWith(JMock.class)
public class WaitForTest {

    private final DeterministicDateFactory date = new DeterministicDateFactory();

    private final Mockery context = new JUnit4Mockery();

    private final Sequence sequence = context.sequence("sequence");
    private final Condition condition = context.mock(Condition.class);
    private static final Duration TIMEOUT = millis(10);

    @Test
    public void whenConditionPassesWaitContinues() throws TimeoutException {
        context.checking(new Expectations(){{
            one(condition).isSatisfied(); will(returnValue(true));
        }});
        waitOrTimeout(condition, TIMEOUT, StopWatch.start(date));
    }

    @Test
    public void whenConditionEventuallyPassesWaitContinues() throws TimeoutException {
        context.checking(new Expectations(){{
            one(condition).isSatisfied(); inSequence(sequence); will(returnValue(false));
            one(condition).isSatisfied(); inSequence(sequence); will(returnValue(true));
        }});
        waitOrTimeout(condition, TIMEOUT, StopWatch.start(date));
    }

    @Test(expected = TimeoutException.class)
    public void timesout() throws TimeoutException {
        waitOrTimeout(new ForceTimeout(), TIMEOUT, StopWatch.start(date));
    }

    @Test (expected = TimeoutException.class, timeout = 500)
    public void waitForCanBeInterrupted() throws TimeoutException {
        waitOrTimeout(IntrruptWaitFor(), seconds(10));
    }

    @Test (timeout = 500)
    public void shouldWaitForTimeoutCanBeInterrupted() throws TimeoutException {
        Thread thread = threadWaitsForever();
        thread.start();
        thread.interrupt();
        waitForInterrupt(thread);
    }

    private Thread threadWaitsForever() {
        return new Thread(new Runnable() {
            public void run() {
                waitUntil(new Timeout(seconds(1), StopWatch.start(date)));
            }
        }, "blocking-thread");
    }

    private void waitForInterrupt(final Thread thread) throws TimeoutException {
        waitOrTimeout(new Condition() {
            public boolean isSatisfied() {
                return !thread.isAlive();
            }
        }, seconds(1));
    }

    private Condition IntrruptWaitFor() {
        return new Condition() {
            public boolean isSatisfied() {
                currentThread().interrupt();
                return false;
            }
        };
    }

    private class ForceTimeout implements Condition {
        public boolean isSatisfied() {
            date.setTime(TIMEOUT.plus(millis(1)));
            return false;
        }
    }

}