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

package org.onap.policy.models.pdp.persistence.concepts;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.onap.policy.models.base.PfConceptKey;
import org.onap.policy.models.base.PfReferenceKey;
import org.onap.policy.models.base.PfValidationResult;
import org.onap.policy.models.pdp.concepts.Pdp;
import org.onap.policy.models.pdp.enums.PdpHealthStatus;
import org.onap.policy.models.pdp.enums.PdpState;
import org.onap.policy.models.pdp.persistence.concepts.JpaPdp;
import org.onap.policy.models.pdp.testconcepts.DummyJpaPdpChild;

/**
 * Test the {@link JpaPdp} class.
 *
 * @author Liam Fallon (liam.fallon@est.tech)
 */
public class JpaPdpTest {

    @Test
    public void testJpaPdp() {
        assertThatThrownBy(() -> {
            new JpaPdp((JpaPdp) null);
        }).hasMessage("copyConcept is marked @NonNull but is null");

        assertThatThrownBy(() -> {
            new JpaPdp((PfReferenceKey) null);
        }).hasMessage("key is marked @NonNull but is null");

        assertThatThrownBy(() -> {
            new JpaPdp(null, null, null);
        }).hasMessage("key is marked @NonNull but is null");

        assertThatThrownBy(() -> {
            new JpaPdp(new PfReferenceKey(), null, null);
        }).hasMessage("pdpState is marked @NonNull but is null");

        assertThatThrownBy(() -> {
            new JpaPdp(new PfReferenceKey(), PdpState.ACTIVE, null);
        }).hasMessage("healthy is marked @NonNull but is null");

        assertThatThrownBy(() -> {
            new JpaPdp(null, PdpState.ACTIVE, null);
        }).hasMessage("key is marked @NonNull but is null");

        assertThatThrownBy(() -> {
            new JpaPdp(null, PdpState.ACTIVE, PdpHealthStatus.UNKNOWN);
        }).hasMessage("key is marked @NonNull but is null");

        assertThatThrownBy(() -> {
            new JpaPdp(null, null, PdpHealthStatus.UNKNOWN);
        }).hasMessage("key is marked @NonNull but is null");

        assertThatThrownBy(() -> {
            new JpaPdp((Pdp) null);
        }).hasMessage("authorativeConcept is marked @NonNull but is null");

        assertNotNull(new JpaPdp((new PfReferenceKey())));

        Pdp testPdp = new Pdp();
        testPdp.setInstanceId("ThePDP");
        JpaPdp testJpaPdp = new JpaPdp();
        testJpaPdp.setKey(null);
        testJpaPdp.fromAuthorative(testPdp);
        assertEquals("ThePDP", testJpaPdp.getKey().getLocalName());
        testJpaPdp.setKey(PfReferenceKey.getNullKey());
        testJpaPdp.fromAuthorative(testPdp);

        assertThatThrownBy(() -> {
            testJpaPdp.fromAuthorative(null);
        }).hasMessage("pdp is marked @NonNull but is null");

        assertThatThrownBy(() -> {
            testJpaPdp.copyTo(null);
        }).hasMessage("target is marked @NonNull but is null");

        assertEquals("ThePDP", testJpaPdp.getKey().getLocalName());
        assertEquals("ThePDP", new JpaPdp(testPdp).getKey().getLocalName());
        assertEquals("ThePDP", ((PfReferenceKey) new JpaPdp(testPdp).getKeys().get(0)).getLocalName());

        testJpaPdp.clean();
        assertEquals("ThePDP", testJpaPdp.getKey().getLocalName());

        testJpaPdp.setMessage("   A Message   ");
        testJpaPdp.clean();
        assertEquals("A Message", testJpaPdp.getMessage());

        assertThatThrownBy(() -> {
            testJpaPdp.validate(null);
        }).hasMessage("resultIn is marked @NonNull but is null");

        assertFalse(testJpaPdp.validate(new PfValidationResult()).isOk());
        assertTrue(testJpaPdp.validate(new PfValidationResult()).toString()
                .contains("INVALID:parent of key is a null key"));

        testJpaPdp.getKey().setParentConceptKey(new PfConceptKey("Parent:1.0.0"));
        assertFalse(testJpaPdp.validate(new PfValidationResult()).isOk());
        assertFalse(testJpaPdp.validate(new PfValidationResult()).toString()
                .contains("INVALID:parent of key is a null key"));
        assertTrue(testJpaPdp.validate(new PfValidationResult()).toString()
                .contains("INVALID:local name of parent of key is null"));

        testJpaPdp.getKey().setParentLocalName("ParentLocal");
        assertFalse(testJpaPdp.validate(new PfValidationResult()).isOk());
        assertFalse(testJpaPdp.validate(new PfValidationResult()).toString()
                .contains("INVALID:local name of parent of key is null"));
        assertTrue(testJpaPdp.validate(new PfValidationResult()).toString()
                .contains("INVALID:PDP state may not be null"));

        testJpaPdp.setPdpState(PdpState.ACTIVE);
        assertFalse(testJpaPdp.validate(new PfValidationResult()).isOk());
        assertFalse(testJpaPdp.validate(new PfValidationResult()).toString()
                .contains("INVALID:PDP state may not be null"));
        assertTrue(testJpaPdp.validate(new PfValidationResult()).toString()
                .contains("INVALID:PDP health status may not be null"));

        testJpaPdp.setHealthy(PdpHealthStatus.HEALTHY);
        assertTrue(testJpaPdp.validate(new PfValidationResult()).isOk());

        PfReferenceKey savedKey = testJpaPdp.getKey();
        testJpaPdp.setKey(PfReferenceKey.getNullKey());
        assertFalse(testJpaPdp.validate(new PfValidationResult()).isOk());
        testJpaPdp.setKey(savedKey);
        assertTrue(testJpaPdp.validate(new PfValidationResult()).isOk());

        testJpaPdp.setMessage(null);
        assertTrue(testJpaPdp.validate(new PfValidationResult()).isOk());
        testJpaPdp.setMessage("");
        assertFalse(testJpaPdp.validate(new PfValidationResult()).isOk());
        testJpaPdp.setMessage("Valid Message");
        assertTrue(testJpaPdp.validate(new PfValidationResult()).isOk());

        JpaPdp otherJpaPdp = new JpaPdp(testJpaPdp);
        assertEquals(0, testJpaPdp.compareTo(otherJpaPdp));
        assertEquals(-1, testJpaPdp.compareTo(null));
        assertEquals(0, testJpaPdp.compareTo(testJpaPdp));
        assertFalse(testJpaPdp.compareTo(new DummyJpaPdpChild()) == 0);

        testJpaPdp.getKey().setParentLocalName("ParentLocal1");
        assertEquals(1, testJpaPdp.compareTo(otherJpaPdp));
        testJpaPdp.getKey().setParentLocalName("ParentLocal");
        assertEquals(0, testJpaPdp.compareTo(otherJpaPdp));

        testJpaPdp.setPdpState(PdpState.PASSIVE);
        assertEquals(-3, testJpaPdp.compareTo(otherJpaPdp));
        testJpaPdp.setPdpState(PdpState.ACTIVE);
        assertEquals(0, testJpaPdp.compareTo(otherJpaPdp));

        testJpaPdp.setHealthy(PdpHealthStatus.NOT_HEALTHY);
        assertEquals(1, testJpaPdp.compareTo(otherJpaPdp));
        testJpaPdp.setHealthy(PdpHealthStatus.HEALTHY);
        assertEquals(0, testJpaPdp.compareTo(otherJpaPdp));

        testJpaPdp.setMessage("Invalid Message");
        assertEquals(-13, testJpaPdp.compareTo(otherJpaPdp));
        testJpaPdp.setMessage("Valid Message");
        assertEquals(0, testJpaPdp.compareTo(otherJpaPdp));
    }
}
