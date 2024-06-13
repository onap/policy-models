/*-
 * ============LICENSE_START=======================================================
 * ONAP
 * ================================================================================
 * Copyright (C) 2020-2021 AT&T Intellectual Property. All rights reserved.
 * Modifications Copyright (C) 2024 Nordix Foundation
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
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.concurrent.Future;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class FutureManagerTest {

    private static final String EXPECTED_EXCEPTION = "expected exception";

    @Mock
    private Future<String> future1;

    @Mock
    private Future<String> future2;

    @Mock
    private Future<String> future3;

    private FutureManager mgr;

    /**
     * Initializes fields, including {@link #mgr}.
     */
    @BeforeEach
    void setUp() {
        mgr = new FutureManager();
    }

    @Test
     void testStop() {
        mgr.add(future1);
        mgr.add(future2);
        mgr.add(future3);

        // arrange for one to throw an exception
        when(future2.cancel(anyBoolean())).thenThrow(new IllegalStateException(EXPECTED_EXCEPTION));

        // nothing should have been canceled yet
        verify(future1, never()).cancel(anyBoolean());
        verify(future2, never()).cancel(anyBoolean());
        verify(future3, never()).cancel(anyBoolean());

        assertTrue(mgr.isRunning());

        // stop the controller

        // stop the controller
        mgr.stop();

        // all controllers should now be stopped
        assertFalse(mgr.isRunning());

        // everything should have been invoked
        verify(future1).cancel(anyBoolean());
        verify(future2).cancel(anyBoolean());
        verify(future3).cancel(anyBoolean());

        // re-invoking stop should have no effect on the listeners
        mgr.stop();

        verify(future1).cancel(anyBoolean());
        verify(future2).cancel(anyBoolean());
        verify(future3).cancel(anyBoolean());
    }

    @Test
     void testAdd() {
        // still running - this should not be invoked
        mgr.add(future1);
        verify(future1, never()).cancel(anyBoolean());

        // re-add should have no impact
        mgr.add(future1);
        verify(future1, never()).cancel(anyBoolean());

        mgr.stop();

        verify(future1).cancel(anyBoolean());

        // new additions should be invoked immediately
        mgr.add(future2);
        verify(future2).cancel(anyBoolean());

        // should work with exceptions, too
        when(future3.cancel(anyBoolean())).thenThrow(new IllegalStateException(EXPECTED_EXCEPTION));
        mgr.add(future3);
    }

    @Test
     void testRemove() {
        mgr.add(future1);
        mgr.add(future2);

        verify(future1, never()).cancel(anyBoolean());
        verify(future2, never()).cancel(anyBoolean());

        // remove the second
        mgr.remove(future2);

        // should be able to remove it again
        mgr.remove(future2);

        mgr.stop();

        // first should have run, but not the second
        verify(future1).cancel(anyBoolean());

        verify(future2, never()).cancel(anyBoolean());
    }
}
