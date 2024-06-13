/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2019, 2024 Nordix Foundation.
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

package org.onap.policy.models.dao.converters;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.UUID;
import org.junit.jupiter.api.Test;

/**
 * Test the UUID conditioner class.
 *
 * @author Liam Fallon (liam.fallon@est.tech)
 */
class Uuid2StringConditionerTest {

    @Test
    void testUuidConditioner() throws Exception {
        UUID randomUuid = UUID.randomUUID();
        assertEquals(randomUuid.toString(), new Uuid2String().convertToDatabaseColumn(randomUuid));
        assertEquals(randomUuid, new Uuid2String().convertToEntityAttribute(randomUuid.toString()));
        assertEquals(randomUuid.toString(), new Uuid2String().marshal(randomUuid));
        assertEquals(randomUuid, new Uuid2String().unmarshal(randomUuid.toString()));
    }
}
