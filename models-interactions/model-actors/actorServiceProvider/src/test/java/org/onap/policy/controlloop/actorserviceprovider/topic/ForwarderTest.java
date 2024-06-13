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

package org.onap.policy.controlloop.actorserviceprovider.topic;

import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.Arrays;
import java.util.Map;
import java.util.function.BiConsumer;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.onap.policy.common.utils.coder.StandardCoderObject;
import org.onap.policy.controlloop.actorserviceprovider.Util;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ExtendWith(MockitoExtension.class)
class ForwarderTest {
    private static final String TEXT = "some text";

    private static final String KEY1 = "requestId";
    private static final String KEY2 = "container";
    private static final String SUBKEY = "subRequestId";

    private static final String VALUEA_REQID = "hello";
    private static final String VALUEA_SUBREQID = "world";

    // request id is shared with value A
    private static final String VALUEB_REQID = "hello";
    private static final String VALUEB_SUBREQID = "another world";

    // unique values
    private static final String VALUEC_REQID = "bye";
    private static final String VALUEC_SUBREQID = "bye-bye";

    @Mock
    private BiConsumer<String, StandardCoderObject> listener1;

    @Mock
    private BiConsumer<String, StandardCoderObject> listener1b;

    @Mock
    private BiConsumer<String, StandardCoderObject> listener2;

    @Mock
    private BiConsumer<String, StandardCoderObject> listener3;

    private Forwarder forwarder;


    /**
     * Sets up.
     */
    @BeforeEach
    void setUp() {
        forwarder = new Forwarder(Arrays.asList(new SelectorKey(KEY1), new SelectorKey(KEY2, SUBKEY)));

        forwarder.register(Arrays.asList(VALUEA_REQID, VALUEA_SUBREQID), listener1);
        forwarder.register(Arrays.asList(VALUEA_REQID, VALUEA_SUBREQID), listener1b);
        forwarder.register(Arrays.asList(VALUEB_REQID, VALUEB_SUBREQID), listener2);
        forwarder.register(Arrays.asList(VALUEC_REQID, VALUEC_SUBREQID), listener3);
    }

    @Test
    void testRegister() {
        // key size mismatches
        assertThatIllegalArgumentException().isThrownBy(() -> forwarder.register(Arrays.asList(), listener1))
                        .withMessage("key/value mismatch");
        assertThatIllegalArgumentException()
                        .isThrownBy(() -> forwarder.register(Arrays.asList(VALUEA_REQID), listener1))
                        .withMessage("key/value mismatch");
    }

    @Test
    void testUnregister() {
        // remove listener1b
        forwarder.unregister(Arrays.asList(VALUEA_REQID, VALUEA_SUBREQID), listener1b);

        StandardCoderObject sco = makeMessage(Map.of(KEY1, VALUEA_REQID, KEY2, Map.of(SUBKEY, VALUEA_SUBREQID)));
        forwarder.onMessage(TEXT, sco);

        verify(listener1).accept(TEXT, sco);
        verify(listener1b, never()).accept(any(), any());

        // remove listener1
        forwarder.unregister(Arrays.asList(VALUEA_REQID, VALUEA_SUBREQID), listener1);
        forwarder.onMessage(TEXT, sco);

        // route a message to listener2
        sco = makeMessage(Map.of(KEY1, VALUEB_REQID, KEY2, Map.of(SUBKEY, VALUEB_SUBREQID)));
        forwarder.onMessage(TEXT, sco);
        verify(listener2).accept(TEXT, sco);

        // no more messages to listener1 or 1b
        verify(listener1).accept(any(), any());
        verify(listener1b, never()).accept(any(), any());
    }

    @Test
     void testOnMessage() {
        StandardCoderObject sco = makeMessage(Map.of(KEY1, VALUEA_REQID, KEY2, Map.of(SUBKEY, VALUEA_SUBREQID)));
        forwarder.onMessage(TEXT, sco);

        verify(listener1).accept(TEXT, sco);
        verify(listener1b).accept(TEXT, sco);

        // repeat - counts should increment
        forwarder.onMessage(TEXT, sco);

        verify(listener1, times(2)).accept(TEXT, sco);
        verify(listener1b, times(2)).accept(TEXT, sco);

        // should not have been invoked
        verify(listener2, never()).accept(any(), any());
        verify(listener3, never()).accept(any(), any());

        // try other listeners now
        sco = makeMessage(Map.of(KEY1, VALUEB_REQID, KEY2, Map.of(SUBKEY, VALUEB_SUBREQID)));
        forwarder.onMessage(TEXT, sco);
        verify(listener2).accept(TEXT, sco);

        sco = makeMessage(Map.of(KEY1, VALUEC_REQID, KEY2, Map.of(SUBKEY, VALUEC_SUBREQID)));
        forwarder.onMessage(TEXT, sco);
        verify(listener3).accept(TEXT, sco);

        // message has no listeners
        sco = makeMessage(Map.of(KEY1, "xyzzy", KEY2, Map.of(SUBKEY, VALUEB_SUBREQID)));
        forwarder.onMessage(TEXT, sco);

        // message doesn't have both keys
        sco = makeMessage(Map.of(KEY1, VALUEA_REQID));
        forwarder.onMessage(TEXT, sco);

        // counts should not have incremented
        verify(listener1, times(2)).accept(any(), any());
        verify(listener1b, times(2)).accept(any(), any());
        verify(listener2).accept(any(), any());
        verify(listener3).accept(any(), any());
    }

    /*
     * Tests onMessage() when listener1 throws an exception.
     */
    @Test
     void testOnMessageListenerException1() {
        doThrow(new IllegalStateException("expected exception")).when(listener1).accept(any(), any());

        StandardCoderObject sco = makeMessage(Map.of(KEY1, VALUEA_REQID, KEY2, Map.of(SUBKEY, VALUEA_SUBREQID)));
        forwarder.onMessage(TEXT, sco);

        verify(listener1b).accept(TEXT, sco);
    }

    /*
     * Tests onMessage() when listener1b throws an exception.
     */
    @Test
     void testOnMessageListenerException1b() {
        doThrow(new IllegalStateException("expected exception")).when(listener1b).accept(any(), any());

        StandardCoderObject sco = makeMessage(Map.of(KEY1, VALUEA_REQID, KEY2, Map.of(SUBKEY, VALUEA_SUBREQID)));
        forwarder.onMessage(TEXT, sco);

        verify(listener1).accept(TEXT, sco);
    }

    /**
     * Makes a message from a map.
     */
    private StandardCoderObject makeMessage(Map<String, Object> map) {
        return Util.translate("", map, StandardCoderObject.class);
    }
}
