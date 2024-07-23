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
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.MockitoAnnotations.openMocks;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.onap.policy.common.parameters.BeanValidationResult;
import org.onap.policy.models.base.PfConceptKey;

class JpaToscaCapabilityAssignmentsTest {

    @Mock
    private PfConceptKey mockKey;

    private JpaToscaCapabilityAssignments jpaToscaCapabilityAssignmentsUnderTest;

    @BeforeEach
    void setUp() {
        openMocks(this);
        jpaToscaCapabilityAssignmentsUnderTest = new JpaToscaCapabilityAssignments(mockKey, Map.ofEntries(
            Map.entry(new PfConceptKey("name", "1.0.0"),
                new JpaToscaCapabilityAssignment(new PfConceptKey("name", "1.0.0")))));
    }

    @Test
    void testConstructors() {
        JpaToscaCapabilityAssignments assignment = new JpaToscaCapabilityAssignments();
        assertNotNull(assignment);

        PfConceptKey key = new PfConceptKey();
        assignment = new JpaToscaCapabilityAssignments(key);
        assertNotNull(assignment);

        assignment = new JpaToscaCapabilityAssignments(key, new HashMap<>());
        assertNotNull(assignment);

        JpaToscaCapabilityAssignments assignmentCopy = new JpaToscaCapabilityAssignments(assignment);
        assertNotNull(assignmentCopy);

        assertThatThrownBy(() -> new JpaToscaCapabilityAssignments(List.of(new HashMap<>())))
            .hasMessageContaining("An incoming list of concepts must have at least one entry");
    }

    @Test
    void testValidate() {
        BeanValidationResult result = jpaToscaCapabilityAssignmentsUnderTest.validate("fieldName");
        assertThat(result.getResult()).contains("item has status INVALID");

        assertThatThrownBy(() -> jpaToscaCapabilityAssignmentsUnderTest.validate(null))
            .hasMessageContaining("fieldName is marked non-null but is null");
    }
}
