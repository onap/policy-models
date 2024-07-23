/*-
 * ============LICENSE_START=======================================================
 * ONAP Policy Provider Models
 * ================================================================================
 * Copyright (C) 2024 Nordix Foundation
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

package org.onap.policy.models.provider.impl;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.openMocks;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.onap.policy.models.base.PfModelException;
import org.onap.policy.models.provider.PolicyModelsProviderParameters;

class AbstractModelsProviderTest {

    @Mock
    private PolicyModelsProviderParameters mockParameters;

    private AbstractModelsProvider abstractModelsProviderUnderTest;

    private AutoCloseable mockitoCloseable;

    @BeforeEach
    void setUp() {
        mockitoCloseable = openMocks(this);
        abstractModelsProviderUnderTest = new AbstractModelsProvider(mockParameters) {};
    }

    @AfterEach
    void tearDown() throws Exception {
        mockitoCloseable.close();
    }

    @Test
    void testInitError() throws Exception {
        when(mockParameters.getDatabaseUrl()).thenReturn("invalidParameter");
        when(mockParameters.getDatabaseDriver()).thenReturn("invalidParameter");
        when(mockParameters.getDatabaseUser()).thenReturn("invalidParameter");
        when(mockParameters.getDatabasePassword()).thenReturn("invalidParameter");
        when(mockParameters.getPersistenceUnit()).thenReturn("invalidParameter");

        assertThrows(PfModelException.class, () -> abstractModelsProviderUnderTest.init());
    }

    @Test
    void testClose() throws PfModelException {
        when(mockParameters.getDatabaseUrl()).thenReturn("result");
        when(mockParameters.getPersistenceUnit()).thenReturn("result");

        assertThatCode(abstractModelsProviderUnderTest::close).doesNotThrowAnyException();
    }
}
