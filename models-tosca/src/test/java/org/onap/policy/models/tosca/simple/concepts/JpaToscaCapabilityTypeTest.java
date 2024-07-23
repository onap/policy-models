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
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.openMocks;

import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.onap.policy.models.base.PfConceptKey;
import org.onap.policy.models.tosca.authorative.concepts.ToscaCapabilityType;
import org.onap.policy.models.tosca.authorative.concepts.ToscaProperty;

class JpaToscaCapabilityTypeTest {

    @Mock
    private PfConceptKey mockKey;

    private JpaToscaCapabilityType jpaToscaCapabilityTypeUnderTest;

    private AutoCloseable mockitoCloseable;

    @BeforeEach
    void setUp() {
        mockitoCloseable = openMocks(this);
        jpaToscaCapabilityTypeUnderTest = new JpaToscaCapabilityType(mockKey);
    }

    @AfterEach
    void tearDown() throws Exception {
        mockitoCloseable.close();
    }

    @Test
    void testToAuthorative() {
        final ToscaCapabilityType expectedResult = new ToscaCapabilityType();
        expectedResult.setName("name");
        expectedResult.setVersion("version");
        expectedResult.setDerivedFrom("name");
        expectedResult.setMetadata(Map.ofEntries(Map.entry("value", "value")));
        expectedResult.setDescription("description");
        final ToscaProperty toscaProperty = new ToscaProperty();
        expectedResult.setProperties(Map.ofEntries(Map.entry("value", toscaProperty)));

        when(mockKey.getName()).thenReturn("name");
        when(mockKey.getVersion()).thenReturn("version");

        final ToscaCapabilityType result = jpaToscaCapabilityTypeUnderTest.toAuthorative();

        assertThat(result.toString()).contains(expectedResult.toString());
    }
}
