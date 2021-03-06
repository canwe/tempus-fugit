/*
 * Copyright (c) 2009-2015, toby weston & tempus-fugit committers
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

package com.google.code.tempusfugit.concurrency;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

@RunWith(ConcurrentTestRunner.class)
@Ignore("Bug #10 (https://code.google.com/p/tempus-fugit/issues/detail?id=10)")
public class ConcurrentTestRunnerAfterClassTest {

    private static Thread TEST_THREAD;

    @Test
    public void runsMultipleTimes() {
        TEST_THREAD = Thread.currentThread();
    }

    @After
    public void assertAfterIsEvaluatedOnTestThread() {
        assertThat(Thread.currentThread(), is(TEST_THREAD));
    }

    @AfterClass
    public static void assertAfterClassIsEvaluatedOnTestThread() {
        assertThat(Thread.currentThread(), is(TEST_THREAD));
    }

}