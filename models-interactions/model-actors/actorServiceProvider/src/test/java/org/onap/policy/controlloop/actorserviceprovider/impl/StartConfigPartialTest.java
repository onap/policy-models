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

package org.onap.policy.controlloop.actorserviceprovider.impl;

import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.assertj.core.api.Assertions.assertThatIllegalStateException;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class StartConfigPartialTest {
    private static final IllegalArgumentException EXPECTED_EXCEPTION =
                    new IllegalArgumentException("expected exception");
    private static final String MY_NAME = "my-name";
    private static final String PARAMS = "config data";
    private static final String PARAMS2 = "config data #2";
    private static final String PARAMSX = "config data exception";

    private StartConfigPartial<String> config;

    /**
     * Creates a config whose doXxx() methods do nothing.
     */
    @BeforeEach
    void setUp() {
        config = new StartConfigPartial<>(MY_NAME) {
            @Override
            protected void doConfigure(String parameters) {
                // do nothing
            }

            @Override
            protected void doStart() {
                // do nothing
            }

            @Override
            protected void doStop() {
                // do nothing
            }

            @Override
            protected void doShutdown() {
                // do nothing
            }
        };

        config = spy(config);
    }

    @Test
    void testConfigImpl_testGetFullName() {
        assertEquals(MY_NAME, config.getFullName());
    }

    @Test
    void testIsAlive() {
        assertFalse(config.isAlive());
    }

    @Test
    void testIsConfigured_testConfigure() {
        // throw an exception during doConfigure(), but should remain unconfigured
        assertFalse(config.isConfigured());
        doThrow(EXPECTED_EXCEPTION).when(config).doConfigure(PARAMSX);
        assertThatIllegalArgumentException().isThrownBy(() -> config.configure(PARAMSX)).isEqualTo(EXPECTED_EXCEPTION);
        assertFalse(config.isConfigured());

        assertFalse(config.isConfigured());
        config.configure(PARAMS);
        verify(config).doConfigure(PARAMS);
        assertTrue(config.isConfigured());

        // should not be able to re-configure while running
        config.start();
        assertThatIllegalStateException().isThrownBy(() -> config.configure(PARAMS2)).withMessageContaining(MY_NAME);
        verify(config, never()).doConfigure(PARAMS2);

        // should be able to re-configure after stopping
        config.stop();
        config.configure(PARAMS2);
        verify(config).doConfigure(PARAMS2);
        assertTrue(config.isConfigured());

        // should remain configured after exception
        doThrow(EXPECTED_EXCEPTION).when(config).doConfigure(PARAMSX);
        assertThatIllegalArgumentException().isThrownBy(() -> config.configure(PARAMSX)).isEqualTo(EXPECTED_EXCEPTION);
        assertTrue(config.isConfigured());
    }

    @Test
    void testStart() {
        assertFalse(config.isAlive());

        // can't start if not configured yet
        assertThatIllegalStateException().isThrownBy(() -> config.start()).withMessageContaining(MY_NAME);
        assertFalse(config.isAlive());

        config.configure(PARAMS);

        config.start();
        verify(config).doStart();
        assertTrue(config.isAlive());
        assertTrue(config.isConfigured());

        // ok to restart when running, but shouldn't invoke doStart() again
        config.start();
        verify(config).doStart();
        assertTrue(config.isAlive());
        assertTrue(config.isConfigured());

        // should never have invoked these
        verify(config, never()).doStop();
        verify(config, never()).doShutdown();

        // throw exception when started again, but should remain stopped
        config.stop();
        doThrow(EXPECTED_EXCEPTION).when(config).doStart();
        assertThatIllegalArgumentException().isThrownBy(() -> config.start()).isEqualTo(EXPECTED_EXCEPTION);
        assertFalse(config.isAlive());
        assertTrue(config.isConfigured());
    }

    @Test
    void testStop() {
        config.configure(PARAMS);

        // ok to stop if not running, but shouldn't invoke doStop()
        config.stop();
        verify(config, never()).doStop();
        assertFalse(config.isAlive());
        assertTrue(config.isConfigured());

        config.start();

        // now stop should have an effect
        config.stop();
        verify(config).doStop();
        assertFalse(config.isAlive());
        assertTrue(config.isConfigured());

        // should have only invoked this once
        verify(config).doStart();

        // should never have invoked these
        verify(config, never()).doShutdown();

        // throw exception when stopped again, but should go ahead and stop
        config.start();
        doThrow(EXPECTED_EXCEPTION).when(config).doStop();
        assertThatIllegalArgumentException().isThrownBy(() -> config.stop()).isEqualTo(EXPECTED_EXCEPTION);
        assertFalse(config.isAlive());
        assertTrue(config.isConfigured());
    }

    @Test
    void testShutdown() {
        config.configure(PARAMS);

        // ok to shutdown if not running, but shouldn't invoke doShutdown()
        config.shutdown();
        verify(config, never()).doShutdown();
        assertFalse(config.isAlive());
        assertTrue(config.isConfigured());

        config.start();

        // now stop should have an effect
        config.shutdown();
        verify(config).doShutdown();
        assertFalse(config.isAlive());
        assertTrue(config.isConfigured());

        // should have only invoked this once
        verify(config).doStart();

        // should never have invoked these
        verify(config, never()).doStop();

        // throw exception when shut down again, but should go ahead and shut down
        config.start();
        doThrow(EXPECTED_EXCEPTION).when(config).doShutdown();
        assertThatIllegalArgumentException().isThrownBy(() -> config.shutdown()).isEqualTo(EXPECTED_EXCEPTION);
        assertFalse(config.isAlive());
        assertTrue(config.isConfigured());
    }
}
