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
import static org.mockito.MockitoAnnotations.openMocks;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.onap.policy.models.dao.impl.ProxyDao;

class DbPolicyModelsProviderImplTest {

    @Mock
    private ProxyDao mockPfDao;

    private DbPolicyModelsProviderImpl dbPolicyModelsProviderImplUnderTest;

    private AutoCloseable mockitoCloseable;

    @BeforeEach
    void setUp() {
        mockitoCloseable = openMocks(this);
        dbPolicyModelsProviderImplUnderTest = new DbPolicyModelsProviderImpl(mockPfDao);
    }

    @AfterEach
    void tearDown() throws Exception {
        mockitoCloseable.close();
    }

    @Test
    void testInit() throws Exception {
        assertThatCode(() -> dbPolicyModelsProviderImplUnderTest.init())
            .doesNotThrowAnyException();
    }

    @Test
    void testClose() throws Exception {
        assertThatCode(() -> dbPolicyModelsProviderImplUnderTest.close())
            .doesNotThrowAnyException();
    }
}
