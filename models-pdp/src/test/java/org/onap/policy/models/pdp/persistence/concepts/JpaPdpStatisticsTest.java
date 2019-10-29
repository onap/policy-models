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
import org.onap.policy.models.pdp.concepts.PdpStatistics;
import org.onap.policy.models.pdp.testconcepts.DummyJpaPdpStatisticsChild;

/**
 * Test the {@link JpaPdpStatistics} class.
 *
 */
public class JpaPdpStatisticsTest {
    private static final String NULL_KEY_ERROR = "key is marked @NonNull but is null";
    private static final String PDP1 = "ThePDP";

    @Test
    public void testJpaPdpStatistics() {
        assertThatThrownBy(() -> {
            new JpaPdpStatistics((JpaPdpStatistics) null);
        }).hasMessage("copyConcept is marked @NonNull but is null");

        assertThatThrownBy(() -> {
            new JpaPdpStatistics((PfReferenceKey) null);
        }).hasMessage(NULL_KEY_ERROR);

        assertThatThrownBy(() -> {
            new JpaPdpStatistics((PdpStatistics) null);
        }).hasMessage("authorativeConcept is marked @NonNull but is null");

        assertNotNull(new JpaPdpStatistics((new PfReferenceKey())));
        PdpStatistics testPdpStatistics = new PdpStatistics();
        testPdpStatistics.setPdpInstanceId(PDP1);
        JpaPdpStatistics testJpaPdpStatistics = new JpaPdpStatistics();
        testJpaPdpStatistics.setKey(null);
        testJpaPdpStatistics.fromAuthorative(testPdpStatistics);
        assertEquals(PDP1, testJpaPdpStatistics.getKey().getLocalName());
        testJpaPdpStatistics.setKey(PfReferenceKey.getNullKey());
        testJpaPdpStatistics.fromAuthorative(testPdpStatistics);

        assertThatThrownBy(() -> {
            testJpaPdpStatistics.fromAuthorative(null);
        }).hasMessage("pdpStatistics is marked @NonNull but is null");

        assertThatThrownBy(() -> new JpaPdpStatistics((JpaPdpStatistics) null))
                .isInstanceOf(NullPointerException.class);

        assertEquals(PDP1, testJpaPdpStatistics.getKey().getLocalName());
        assertEquals(PDP1, new JpaPdpStatistics(testJpaPdpStatistics).getKey().getLocalName());
        assertEquals(PDP1, ((PfReferenceKey) new JpaPdpStatistics(testPdpStatistics).getKeys().get(0)).getLocalName());

        testJpaPdpStatistics.clean();
        assertEquals(PDP1, testJpaPdpStatistics.getKey().getLocalName());


        assertThatThrownBy(() -> {
            testJpaPdpStatistics.validate(null);
        }).hasMessage("resultIn is marked @NonNull but is null");

        assertFalse(testJpaPdpStatistics.validate(new PfValidationResult()).isOk());
        assertTrue(testJpaPdpStatistics.validate(new PfValidationResult()).toString()
                .contains("INVALID:parent of key is a null key"));

        testJpaPdpStatistics.getKey().setParentConceptKey(new PfConceptKey("Parent:1.0.0"));
        assertFalse(testJpaPdpStatistics.validate(new PfValidationResult()).isOk());
        assertFalse(testJpaPdpStatistics.validate(new PfValidationResult()).toString()
                .contains("INVALID:parent of key is a null key"));
        assertTrue(testJpaPdpStatistics.validate(new PfValidationResult()).toString()
                .contains("INVALID:local name of parent of key is null"));

        testJpaPdpStatistics.getKey().setParentLocalName("ParentLocal");
        assertTrue(testJpaPdpStatistics.validate(new PfValidationResult()).isOk());

        PfReferenceKey savedKey = testJpaPdpStatistics.getKey();
        testJpaPdpStatistics.setKey(PfReferenceKey.getNullKey());
        assertFalse(testJpaPdpStatistics.validate(new PfValidationResult()).isOk());
        testJpaPdpStatistics.setKey(savedKey);
        assertTrue(testJpaPdpStatistics.validate(new PfValidationResult()).isOk());

        JpaPdpStatistics otherJpaPdpStatistics = new JpaPdpStatistics(testJpaPdpStatistics);
        assertEquals(0, testJpaPdpStatistics.compareTo(otherJpaPdpStatistics));
        assertEquals(-1, testJpaPdpStatistics.compareTo(null));
        assertEquals(0, testJpaPdpStatistics.compareTo(testJpaPdpStatistics));
        assertFalse(testJpaPdpStatistics.compareTo(new DummyJpaPdpStatisticsChild()) == 0);

        testJpaPdpStatistics.getKey().setParentLocalName("ParentLocal1");
        assertEquals(1, testJpaPdpStatistics.compareTo(otherJpaPdpStatistics));
        testJpaPdpStatistics.getKey().setParentLocalName("ParentLocal");
        assertEquals(0, testJpaPdpStatistics.compareTo(otherJpaPdpStatistics));

        testJpaPdpStatistics.setPolicyDeployCount(-1L);
        assertEquals(-1, testJpaPdpStatistics.compareTo(otherJpaPdpStatistics));
        testJpaPdpStatistics.setPolicyDeployCount(0L);
        testJpaPdpStatistics.setPolicyDeployFailCount(-1L);
        assertEquals(-1, testJpaPdpStatistics.compareTo(otherJpaPdpStatistics));
        testJpaPdpStatistics.setPolicyDeployFailCount(0L);
        testJpaPdpStatistics.setPolicyDeploySuccessCount(-1L);
        assertEquals(-1, testJpaPdpStatistics.compareTo(otherJpaPdpStatistics));
        testJpaPdpStatistics.setPolicyDeploySuccessCount(0L);
        testJpaPdpStatistics.setPolicyExecutedCount(-1L);
        assertEquals(-1, testJpaPdpStatistics.compareTo(otherJpaPdpStatistics));
        testJpaPdpStatistics.setPolicyExecutedCount(0L);
        testJpaPdpStatistics.setPolicyExecutedFailCount(-1L);
        assertEquals(-1, testJpaPdpStatistics.compareTo(otherJpaPdpStatistics));

        PdpStatistics otherPdpStatistics = testJpaPdpStatistics.toAuthorative();
        assertEquals(-1, otherPdpStatistics.getPolicyExecutedFailCount());
    }
}
