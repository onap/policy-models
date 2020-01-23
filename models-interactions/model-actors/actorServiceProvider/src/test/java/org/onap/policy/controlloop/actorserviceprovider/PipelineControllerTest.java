/*-
 * ============LICENSE_START=======================================================
 * ONAP
 * ================================================================================
 * Copyright (C) 2020 AT&T Intellectual Property. All rights reserved.
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

package org.onap.policy.controlloop.actorserviceprovider;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.Before;
import org.junit.Test;

public class PipelineControllerTest {

    private static final String EXPECTED_EXCEPTION = "expected exception";

    private PipelineController controller;

    @Before
    public void setUp() {
        controller = new PipelineController();
    }

    @Test
    public void testDoIfRunning() {
        AtomicInteger count = new AtomicInteger(0);
        controller.doIfRunning(() -> count.incrementAndGet());
        controller.doIfRunning(() -> count.incrementAndGet());

        // running, thus count should have been incremented each time
        int original = count.get();
        assertEquals(2, original);

        // stop the controller and try again
        controller.stop();
        controller.doIfRunning(() -> count.incrementAndGet());
        controller.doIfRunning(() -> count.incrementAndGet());

        // count should be unchanged
        assertEquals(original, count.get());
    }

    /**
     * Tests {@link PipelineController#doIfRunning(Runnable)} when the function, itself,
     * invokes {@link PipelineController#stop()}.
     */
    @Test
    public void testDoIfRunningStopWithinFunction() {
        AtomicInteger count = new AtomicInteger(0);
        controller.doIfRunning(() -> {
            count.incrementAndGet();
            controller.stop();
            count.incrementAndGet();
        });

        // count should have been incremented twice
        assertEquals(2, count.get());

        // this should have no impact as the controller is not running
        controller.doIfRunning(() -> count.incrementAndGet());
        assertEquals(2, count.get());
    }

    @Test
    public void testStop_testIsRunning_testPipelineControllerPipelineController() {
        AtomicInteger invoked1 = new AtomicInteger();
        controller.add(() -> invoked1.incrementAndGet());

        // arrange for one to throw an exception
        AtomicInteger invoked2 = new AtomicInteger();
        controller.add(() -> {
            invoked2.incrementAndGet();
            throw new IllegalStateException(EXPECTED_EXCEPTION);
        });

        PipelineController subController = new PipelineController(controller);
        AtomicInteger invoked3 = new AtomicInteger();
        AtomicInteger invoked4 = new AtomicInteger();
        subController.add(() -> invoked3.incrementAndGet());
        subController.add(() -> invoked4.incrementAndGet());

        PipelineController subController2 = new PipelineController(controller);
        AtomicInteger invoked5 = new AtomicInteger();
        subController2.add(() -> invoked5.incrementAndGet());

        // counts should all be zero still
        assertEquals(0, invoked1.get());
        assertEquals(0, invoked2.get());
        assertEquals(0, invoked3.get());
        assertEquals(0, invoked4.get());
        assertEquals(0, invoked5.get());

        assertTrue(controller.isRunning());
        assertTrue(subController.isRunning());
        assertTrue(subController2.isRunning());

        // stop the controller
        controller.stop();

        // all controllers should now be stopped
        assertFalse(controller.isRunning());
        assertFalse(subController.isRunning());
        assertFalse(subController2.isRunning());

        // counts should have been bumped
        assertEquals(1, invoked1.get());
        assertEquals(1, invoked2.get());
        assertEquals(1, invoked3.get());
        assertEquals(1, invoked4.get());
        assertEquals(1, invoked5.get());

        // re-invoking stop should have no effect on the counters
        controller.stop();
        subController.stop();
        subController2.stop();

        assertEquals(1, invoked1.get());
        assertEquals(1, invoked2.get());
        assertEquals(1, invoked3.get());
        assertEquals(1, invoked4.get());
        assertEquals(1, invoked5.get());
    }

    @Test
    public void testAdd() {
        // still running - this should not be invoked
        AtomicBoolean invoked = new AtomicBoolean();
        controller.add(() -> invoked.set(true));
        assertFalse(invoked.get());

        controller.stop();

        // new additions should be invoked immediately
        AtomicBoolean invoked2 = new AtomicBoolean();
        controller.add(() -> invoked2.set(true));
        assertTrue(invoked2.get());

        // should work with exceptions, too
        AtomicBoolean invoked3 = new AtomicBoolean();
        controller.add(() -> {
            invoked3.set(true);
            throw new IllegalStateException(EXPECTED_EXCEPTION);
        });
        assertTrue(invoked3.get());
    }

    @Test
    public void testRemove() {
        AtomicBoolean invoked = new AtomicBoolean();
        controller.add(() -> invoked.set(true));

        AtomicBoolean invoked2 = new AtomicBoolean();
        Runnable runnable2 = () -> invoked2.set(true);
        controller.add(runnable2);

        assertFalse(invoked.get());
        assertFalse(invoked2.get());

        // remove the second
        controller.remove(runnable2);

        controller.stop();

        // first should have run, but not the second
        assertTrue(invoked.get());

        assertFalse(invoked2.get());
    }
}
