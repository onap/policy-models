/*-
 * ============LICENSE_START=======================================================
 * ONAP
 * ================================================================================
 * Copyright (C) 2020-2021 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
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
 * ============LICENSE_END=========================================================
 */

package org.onap.policy.controlloop.actorserviceprovider.pipeline;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ListenerManagerTest {

    private static final String EXPECTED_EXCEPTION = "expected exception";

    @Mock
    private Runnable runnable1;

    @Mock
    private Runnable runnable2;

    @Mock
    private Runnable runnable3;

    private ListenerManager mgr;

    /**
     * Initializes fields, including {@link #mgr}.
     */
    @BeforeEach
    void setUp() {
        mgr = new ListenerManager();
    }

    @Test
     void testStop_testIsRunning() {
        mgr.add(runnable1);
        mgr.add(runnable2);
        mgr.add(runnable3);

        // arrange for one to throw an exception
        doThrow(new IllegalStateException(EXPECTED_EXCEPTION)).when(runnable2).run();

        // nothing should have been canceled yet
        verify(runnable1, never()).run();
        verify(runnable2, never()).run();
        verify(runnable3, never()).run();

        assertTrue(mgr.isRunning());

        // stop the controller
        mgr.stop();

        // all controllers should now be stopped
        assertFalse(mgr.isRunning());

        // everything should have been invoked
        verify(runnable1).run();
        verify(runnable2).run();
        verify(runnable3).run();

        // re-invoking stop should have no effect on the listeners
        mgr.stop();

        verify(runnable1).run();
        verify(runnable2).run();
        verify(runnable3).run();
    }

    @Test
     void testAdd() {
        // still running - this should not be invoked
        mgr.add(runnable1);
        verify(runnable1, never()).run();

        mgr.stop();

        verify(runnable1).run();

        // new additions should be invoked immediately
        mgr.add(runnable2);
        verify(runnable2).run();

        // should work with exceptions, too
        doThrow(new IllegalStateException(EXPECTED_EXCEPTION)).when(runnable3).run();
        mgr.add(runnable3);
    }

    @Test
     void testRemove() {
        mgr.add(runnable1);
        mgr.add(runnable2);

        verify(runnable1, never()).run();
        verify(runnable2, never()).run();

        // remove the second
        mgr.remove(runnable2);

        // should be able to remove it again
        mgr.remove(runnable2);

        mgr.stop();

        // first should have run, but not the second
        verify(runnable1).run();

        verify(runnable2, never()).run();
    }
}
