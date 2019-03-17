/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2019 Nordix Foundation.
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
 *
 * SPDX-License-Identifier: Apache-2.0
 * ============LICENSE_END=========================================================
 */

package org.onap.policy.models.tosca.simple.concepts;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Test;
import org.onap.policy.models.base.PfConceptKey;
import org.onap.policy.models.base.PfReferenceKey;
import org.onap.policy.models.base.PfValidationResult;
import org.onap.policy.models.tosca.simple.concepts.ToscaEventFilter;

/**
 * DAO test for ToscaEventFilter.
 *
 * @author Liam Fallon (liam.fallon@est.tech)
 */
public class ToscaEventFilterTest {

    @Test
    public void testEventFilterPojo() {
        assertNotNull(new ToscaEventFilter());
        assertNotNull(new ToscaEventFilter(new PfReferenceKey()));
        assertNotNull(new ToscaEventFilter(new PfReferenceKey(), new PfConceptKey()));
        assertNotNull(new ToscaEventFilter(new ToscaEventFilter()));

        try {
            new ToscaEventFilter((PfReferenceKey) null);
            fail("test should throw an exception");
        } catch (Exception exc) {
            assertEquals("key is marked @NonNull but is null", exc.getMessage());
        }

        try {
            new ToscaEventFilter(null, null);
            fail("test should throw an exception");
        } catch (Exception exc) {
            assertEquals("key is marked @NonNull but is null", exc.getMessage());
        }

        try {
            new ToscaEventFilter(null, new PfConceptKey());
            fail("test should throw an exception");
        } catch (Exception exc) {
            assertEquals("key is marked @NonNull but is null", exc.getMessage());
        }

        try {
            new ToscaEventFilter(new PfReferenceKey(), null);
            fail("test should throw an exception");
        } catch (Exception exc) {
            assertEquals("node is marked @NonNull but is null", exc.getMessage());
        }

        try {
            new ToscaEventFilter((ToscaEventFilter) null);
            fail("test should throw an exception");
        } catch (Exception exc) {
            assertEquals("copyConcept is marked @NonNull but is null", exc.getMessage());
        }

        PfConceptKey efParentKey = new PfConceptKey("tParentKey", "0.0.1");
        PfReferenceKey efKey = new PfReferenceKey(efParentKey, "trigger0");
        PfConceptKey nodeKey = new PfConceptKey("tParentKey", "0.0.1");
        ToscaEventFilter tef = new ToscaEventFilter(efKey, nodeKey);

        tef.setRequirement("A Requrement");
        assertEquals("A Requrement", tef.getRequirement());

        tef.setCapability("A Capability");
        assertEquals("A Capability", tef.getCapability());

        ToscaEventFilter tdtClone0 = new ToscaEventFilter(tef);
        assertEquals(tef, tdtClone0);
        assertEquals(0, tef.compareTo(tdtClone0));

        ToscaEventFilter tdtClone1 = new ToscaEventFilter();
        tef.copyTo(tdtClone1);
        assertEquals(tef, tdtClone1);
        assertEquals(0, tef.compareTo(tdtClone1));

        assertEquals(-1, tef.compareTo(null));
        assertEquals(0, tef.compareTo(tef));
        assertFalse(tef.compareTo(tef.getKey()) == 0);

        PfReferenceKey otherDtKey = new PfReferenceKey("otherDt", "0.0.1", "OtherEventFilter");
        ToscaEventFilter otherDt = new ToscaEventFilter(otherDtKey);

        assertFalse(tef.compareTo(otherDt) == 0);
        otherDt.setKey(efKey);
        assertFalse(tef.compareTo(otherDt) == 0);
        otherDt.setNode(nodeKey);
        assertFalse(tef.compareTo(otherDt) == 0);
        otherDt.setRequirement("A Requrement");
        assertFalse(tef.compareTo(otherDt) == 0);
        otherDt.setCapability("A Capability");
        assertEquals(0, tef.compareTo(otherDt));

        try {
            tef.copyTo(null);
            fail("test should throw an exception");
        } catch (Exception exc) {
            assertEquals("target is marked @NonNull but is null", exc.getMessage());
        }

        assertEquals(2, tef.getKeys().size());
        assertEquals(2, new ToscaEventFilter().getKeys().size());

        new ToscaEventFilter().clean();
        tef.clean();
        assertEquals(tdtClone0, tef);

        assertFalse(new ToscaEventFilter().validate(new PfValidationResult()).isValid());
        assertTrue(tef.validate(new PfValidationResult()).isValid());

        tef.setRequirement(null);
        assertTrue(tef.validate(new PfValidationResult()).isValid());
        tef.setRequirement("");
        assertFalse(tef.validate(new PfValidationResult()).isValid());
        tef.setRequirement("A Requrement");
        assertTrue(tef.validate(new PfValidationResult()).isValid());

        tef.setCapability(null);
        assertTrue(tef.validate(new PfValidationResult()).isValid());
        tef.setCapability("");
        assertFalse(tef.validate(new PfValidationResult()).isValid());
        tef.setCapability("A Capability");
        assertTrue(tef.validate(new PfValidationResult()).isValid());

        tef.setNode(null);
        assertFalse(tef.validate(new PfValidationResult()).isValid());
        tef.setNode(PfConceptKey.getNullKey());
        assertFalse(tef.validate(new PfValidationResult()).isValid());
        tef.setNode(nodeKey);
        assertTrue(tef.validate(new PfValidationResult()).isValid());

        try {
            tef.validate(null);
            fail("test should throw an exception");
        } catch (Exception exc) {
            assertEquals("resultIn is marked @NonNull but is null", exc.getMessage());
        }
    }
}
