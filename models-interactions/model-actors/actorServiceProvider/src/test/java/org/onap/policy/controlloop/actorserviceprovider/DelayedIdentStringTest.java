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

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class DelayedIdentStringTest {

    private int countToStringCalls;
    private Object object;
    private DelayedIdentString delay;

    /**
     * Initializes fields, including {@link #delay}.
     */
    @BeforeEach
    public void setUp() {
        countToStringCalls = 0;

        object = new Object() {
            @Override
            public String toString() {
                ++countToStringCalls;
                return super.toString();
            }
        };

        delay = new DelayedIdentString(object);
    }

    @Test
    public void testToString() {
        String delayed = delay.toString();
        assertEquals(1, countToStringCalls);

        String real = object.toString();
        assertNotEquals(real, delayed);

        assertThat(delayed).startsWith("@");
        assertTrue(delayed.length() > 1);

        // test case where the object is null
        assertEquals(DelayedIdentString.NULL_STRING, new DelayedIdentString(null).toString());

        // test case where the object returns null from toString()
        object = new Object() {
            @Override
            public String toString() {
                return null;
            }
        };
        assertEquals(DelayedIdentString.NULL_STRING, new DelayedIdentString(object).toString());

        // test case where the object's toString() does not include "@"
        object = new Object() {
            @Override
            public String toString() {
                return "some text";
            }
        };
        assertEquals(object.toString(), new DelayedIdentString(object).toString());
    }

    @Test
    public void testDelayedIdentString() {
        // should not have called the object's toString() method yet
        assertEquals(0, countToStringCalls);
    }
}
