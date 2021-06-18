/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2019-2021 Nordix Foundation.
 *  Modifications Copyright (C) 2019-2021 AT&T Intellectual Property. All rights reserved.
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Date;
import org.junit.Test;
import org.onap.policy.models.base.PfConceptKey;
import org.onap.policy.models.base.PfReferenceKey;
import org.onap.policy.models.base.Validated;
import org.onap.policy.models.pdp.concepts.Pdp;
import org.onap.policy.models.pdp.enums.PdpHealthStatus;
import org.onap.policy.models.pdp.enums.PdpState;
import org.onap.policy.models.pdp.testconcepts.DummyJpaPdpChild;

/**
 * Test the {@link JpaPdp} class.
 *
 * @author Liam Fallon (liam.fallon@est.tech)
 */
public class JpaPdpTest {

    private static final String NULL_KEY_ERROR = "key is marked .*ull but is null";
    private static final String PDP1 = "ThePDP";
    private static final Date CURRENT_DATE = new Date();

    @Test
    public void testJpaPdp() {
        assertThatThrownBy(() -> {
            new JpaPdp((JpaPdp) null);
        }).hasMessageMatching("copyConcept is marked .*ull but is null");

        assertThatThrownBy(() -> {
            new JpaPdp((PfReferenceKey) null);
        }).hasMessageMatching(NULL_KEY_ERROR);

        assertThatThrownBy(() -> {
            new JpaPdp(null, null, null);
        }).hasMessageMatching(NULL_KEY_ERROR);

        assertThatThrownBy(() -> {
            new JpaPdp(new PfReferenceKey(), null, null);
        }).hasMessageMatching("pdpState is marked .*ull but is null");

        assertThatThrownBy(() -> {
            new JpaPdp(new PfReferenceKey(), PdpState.ACTIVE, null);
        }).hasMessageMatching("healthy is marked .*ull but is null");

        assertThatThrownBy(() -> {
            new JpaPdp(null, PdpState.ACTIVE, null);
        }).hasMessageMatching(NULL_KEY_ERROR);

        assertThatThrownBy(() -> {
            new JpaPdp(null, PdpState.ACTIVE, PdpHealthStatus.UNKNOWN);
        }).hasMessageMatching(NULL_KEY_ERROR);

        assertThatThrownBy(() -> {
            new JpaPdp((Pdp) null);
        }).hasMessageMatching("authorativeConcept is marked .*ull but is null");

        assertNotNull(new JpaPdp((new PfReferenceKey())));
    }

    @Test
    public void testJpaPdpInstace() {
        Pdp testPdp = new Pdp();
        testPdp.setInstanceId(PDP1);
        JpaPdp testJpaPdp = new JpaPdp();
        testJpaPdp.setKey(null);
        testJpaPdp.fromAuthorative(testPdp);
        assertEquals(PDP1, testJpaPdp.getKey().getLocalName());
        testJpaPdp.setKey(PfReferenceKey.getNullKey());
        testJpaPdp.fromAuthorative(testPdp);

        assertThatThrownBy(() -> {
            testJpaPdp.fromAuthorative(null);
        }).hasMessageMatching("pdp is marked .*ull but is null");

        assertThatThrownBy(() -> new JpaPdp((JpaPdp) null)).isInstanceOf(NullPointerException.class);

        assertEquals(PDP1, testJpaPdp.getKey().getLocalName());
        assertEquals(PDP1, new JpaPdp(testPdp).getKey().getLocalName());
        assertEquals(PDP1, ((PfReferenceKey) new JpaPdp(testPdp).getKeys().get(0)).getLocalName());

        testJpaPdp.clean();
        assertEquals(PDP1, testJpaPdp.getKey().getLocalName());

        testJpaPdp.setMessage("   A Message   ");
        testJpaPdp.clean();
        assertEquals("A Message", testJpaPdp.getMessage());
    }

    @Test
    public void testJpaPdpValidation() {
        Pdp testPdp = new Pdp();
        testPdp.setInstanceId(PDP1);
        JpaPdp testJpaPdp = new JpaPdp();
        testJpaPdp.setKey(PfReferenceKey.getNullKey());
        testJpaPdp.fromAuthorative(testPdp);

        assertThatThrownBy(() -> {
            testJpaPdp.validate(null);
        }).hasMessageMatching("fieldName is marked .*ull but is null");

        assertFalse(testJpaPdp.validate("").isValid());
        assertThat(testJpaPdp.validate("").getResult())
                .contains("parent").contains(Validated.IS_A_NULL_KEY);

        testJpaPdp.getKey().setParentConceptKey(new PfConceptKey("Parent:1.0.0"));
        assertFalse(testJpaPdp.validate("").isValid());
        assertThat(testJpaPdp.validate("").getResult())
            .doesNotContain("\"parent\"")
            .contains("local name").contains(Validated.IS_NULL);

        testJpaPdp.getKey().setParentLocalName("ParentLocal");
        assertFalse(testJpaPdp.validate("").isValid());
        assertThat(testJpaPdp.validate("").getResult())
            .doesNotContain("local name")
            .contains("pdpState").contains(Validated.IS_NULL);

        testJpaPdp.setPdpState(PdpState.ACTIVE);
        assertFalse(testJpaPdp.validate("").isValid());
        assertThat(testJpaPdp.validate("").getResult())
            .doesNotContain("pdpState")
            .contains("healthy").contains(Validated.IS_NULL);

        testJpaPdp.setHealthy(PdpHealthStatus.HEALTHY);
        assertTrue(testJpaPdp.validate("").isValid());
    }

    @Test
    public void testJpaPdpValidationSwapKey() {
        JpaPdp testJpaPdp = setUpJpaPdp();

        PfReferenceKey savedKey = testJpaPdp.getKey();
        testJpaPdp.setKey(PfReferenceKey.getNullKey());
        assertFalse(testJpaPdp.validate("").isValid());
        testJpaPdp.setKey(savedKey);
        assertTrue(testJpaPdp.validate("").isValid());

        testJpaPdp.setMessage(null);
        assertTrue(testJpaPdp.validate("").isValid());
        testJpaPdp.setMessage("");
        assertFalse(testJpaPdp.validate("").isValid());
        testJpaPdp.setMessage("Valid Message");
        assertTrue(testJpaPdp.validate("").isValid());
    }

    @Test
    public void testJpaPdpCompare_testToAuthorative() {
        JpaPdp testJpaPdp = setUpJpaPdp();

        JpaPdp otherJpaPdp = new JpaPdp(testJpaPdp);
        assertEquals(0, testJpaPdp.compareTo(otherJpaPdp));
        assertEquals(-1, testJpaPdp.compareTo(null));
        assertEquals(0, testJpaPdp.compareTo(testJpaPdp));
        assertNotEquals(0, testJpaPdp.compareTo(new DummyJpaPdpChild()));

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

        testJpaPdp.setLastUpdate(new Date(0));
        assertEquals(-1, testJpaPdp.compareTo(otherJpaPdp));
        testJpaPdp.setLastUpdate(CURRENT_DATE);
        assertEquals(0, testJpaPdp.compareTo(otherJpaPdp));

        assertEquals(testJpaPdp, new JpaPdp(testJpaPdp));

        otherJpaPdp.fromAuthorative(testJpaPdp.toAuthorative());
        assertEquals(0, testJpaPdp.compareTo(otherJpaPdp));
    }

    private JpaPdp setUpJpaPdp() {
        Pdp testPdp = new Pdp();
        testPdp.setInstanceId(PDP1);
        JpaPdp testJpaPdp = new JpaPdp();
        testJpaPdp.setKey(PfReferenceKey.getNullKey());
        testJpaPdp.fromAuthorative(testPdp);
        testJpaPdp.getKey().setParentConceptKey(new PfConceptKey("Parent:1.0.0"));
        testJpaPdp.getKey().setParentLocalName("ParentLocal");
        testJpaPdp.setPdpState(PdpState.ACTIVE);
        testJpaPdp.setHealthy(PdpHealthStatus.HEALTHY);
        testJpaPdp.setMessage("Valid Message");
        testJpaPdp.setLastUpdate(CURRENT_DATE);
        return testJpaPdp;
    }
}
