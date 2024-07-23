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

package org.onap.policy.models.dao;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PfFilterParametersTest {

    @Mock
    private Map<String, Object> mockFilterMap;

    private PfFilterParameters pfFilterParametersUnderTest;

    @BeforeEach
    void setUp() {
        pfFilterParametersUnderTest = new PfFilterParameters("name", "version",
            LocalDateTime.of(2020, 1, 1, 0, 0, 0, 0).toInstant(ZoneOffset.UTC),
            LocalDateTime.of(2020, 1, 1, 0, 0, 0, 0).toInstant(ZoneOffset.UTC), mockFilterMap, 0, "sortOrder");
    }

    @Test
    void testGetName() {
        assertThat(pfFilterParametersUnderTest.getName()).isEqualTo("name");
    }

    @Test
    void testGetVersion() {
        assertThat(pfFilterParametersUnderTest.getVersion()).isEqualTo("version");
    }

    @Test
    void testGetStartTime() {
        assertThat(pfFilterParametersUnderTest.getStartTime())
            .isEqualTo(LocalDateTime.of(2020, 1, 1, 0, 0, 0, 0).toInstant(ZoneOffset.UTC));
    }

    @Test
    void testGetEndTime() {
        assertThat(pfFilterParametersUnderTest.getEndTime())
            .isEqualTo(LocalDateTime.of(2020, 1, 1, 0, 0, 0, 0).toInstant(ZoneOffset.UTC));
    }

    @Test
    void testGetFilterMap() {
        assertThat(pfFilterParametersUnderTest.getFilterMap()).isEqualTo(mockFilterMap);
    }

    @Test
    void testGetRecordNum() {
        assertThat(pfFilterParametersUnderTest.getRecordNum()).isZero();
    }

    @Test
    void testGetSortOrder() {
        assertThat(pfFilterParametersUnderTest.getSortOrder()).isEqualTo("sortOrder");
    }

    @Test
    void testBuilder() {
        assertThatCode(PfFilterParameters::builder).doesNotThrowAnyException();
    }
}
