/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2024 Nordix Foundation.
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.openMocks;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.onap.policy.models.base.PfConceptKey;
import org.onap.policy.models.tosca.authorative.concepts.ToscaRequirement;

class JpaToscaRequirementTest {

    @Mock
    private PfConceptKey mockKey;

    private final List<Integer> occurrences = new ArrayList<>();

    private JpaToscaRequirement jpaToscaRequirementUnderTest;

    private AutoCloseable mockitoCloseable;

    @BeforeEach
    void setUp() {
        mockitoCloseable = openMocks(this);
        jpaToscaRequirementUnderTest = new JpaToscaRequirement(mockKey);
        when(mockKey.getName()).thenReturn("testName");
        when(mockKey.getVersion()).thenReturn("1.0.0");
        occurrences.add(1);
        jpaToscaRequirementUnderTest.setOccurrences(occurrences);
    }

    @AfterEach
    void tearDown() throws Exception {
        mockitoCloseable.close();
    }

    @Test
    void testConstructor() {
        JpaToscaRequirement requirementCopy = new JpaToscaRequirement(jpaToscaRequirementUnderTest);
        assertNotNull(requirementCopy);

        ToscaRequirement req = new ToscaRequirement();
        req.setNode("nodeTest");
        req.setCapability("capabilityTest");
        List<Object> testOccurrences = new ArrayList<>();
        testOccurrences.add(1);
        req.setOccurrences(testOccurrences);
        req.setName("nameTest");
        req.setType("testType");
        req.setTypeVersion("1.0.0");
        requirementCopy = new JpaToscaRequirement(req);
        assertNotNull(requirementCopy);
    }

    @Test
    void testCapabilityGetterAndSetter() {
        final String capability = "capability";
        jpaToscaRequirementUnderTest.setCapability(capability);
        assertThat(jpaToscaRequirementUnderTest.getCapability()).isEqualTo(capability);

        final String node = "node";
        jpaToscaRequirementUnderTest.setNode(node);
        assertThat(jpaToscaRequirementUnderTest.getNode()).isEqualTo(node);

        final String relationship = "relationship";
        jpaToscaRequirementUnderTest.setRelationship(relationship);
        assertThat(jpaToscaRequirementUnderTest.getRelationship()).isEqualTo(relationship);

        assertThat(jpaToscaRequirementUnderTest.getOccurrences()).isEqualTo(occurrences);

        assertThat(jpaToscaRequirementUnderTest.toString())
            .hasToString("JpaToscaRequirement(capability=capability, node=node, relationship=relationship, "
                + "occurrences=[1])");
    }

    @Test
    void testToAuthorative() {
        ToscaRequirement toAuthoritiveReq = jpaToscaRequirementUnderTest.toAuthorative();
        assertNotNull(toAuthoritiveReq);
    }

    @Test
    void testDeserialize() {
        assertNotNull(jpaToscaRequirementUnderTest.deserializePropertyValue("occurences"));
    }

    @Test
    void testClean() {
        jpaToscaRequirementUnderTest.setCapability("capabilityTestValue");
        jpaToscaRequirementUnderTest.setNode("nodeTestValue");
        jpaToscaRequirementUnderTest.setRelationship("relationshipTestValue");
        assertThatCode(() -> jpaToscaRequirementUnderTest.clean()).doesNotThrowAnyException();
    }
}
