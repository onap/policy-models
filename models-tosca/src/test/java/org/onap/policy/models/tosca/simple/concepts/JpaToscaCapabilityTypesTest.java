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

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.openMocks;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.onap.policy.models.base.PfConceptKey;
import org.onap.policy.models.tosca.authorative.concepts.ToscaCapabilityType;

class JpaToscaCapabilityTypesTest {

    @Mock
    private PfConceptKey mockKey;

    @Mock
    private Map<PfConceptKey, JpaToscaCapabilityType> mockConceptMap;

    private JpaToscaCapabilityTypes jpaToscaCapabilityTypesUnderTest;

    private AutoCloseable mockitoCloseable;

    @BeforeEach
    void setUp() {
        mockitoCloseable = openMocks(this);
        when(mockKey.getName()).thenReturn("testName");
        when(mockKey.getVersion()).thenReturn("1.0.0");
        jpaToscaCapabilityTypesUnderTest = new JpaToscaCapabilityTypes();
    }

    @AfterEach
    void tearDown() throws Exception {
        mockitoCloseable.close();
    }

    @Test
    void testConstructors() {
        jpaToscaCapabilityTypesUnderTest = new JpaToscaCapabilityTypes(mockKey);
        assertNotNull(jpaToscaCapabilityTypesUnderTest);

        jpaToscaCapabilityTypesUnderTest = new JpaToscaCapabilityTypes(mockKey, mockConceptMap);
        assertNotNull(jpaToscaCapabilityTypesUnderTest);

        JpaToscaCapabilityTypes jpaToscaCapabilityTypesCopy =
            new JpaToscaCapabilityTypes(jpaToscaCapabilityTypesUnderTest);
        assertNotNull(jpaToscaCapabilityTypesCopy);

        Map<String, ToscaCapabilityType> testMap = new HashMap<String, ToscaCapabilityType>();
        testMap.put("test1", new ToscaCapabilityType());
        List<Map<String, ToscaCapabilityType>> testList = List.of(testMap);
        JpaToscaCapabilityTypes jpaToscaCapabilityTypesList = new JpaToscaCapabilityTypes(testList);
        assertNotNull(jpaToscaCapabilityTypesList);
    }

    @Test
    void testValidate() {
        assertThatCode(() -> jpaToscaCapabilityTypesUnderTest.validate("name")).doesNotThrowAnyException();
    }
}
