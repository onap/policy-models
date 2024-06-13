/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2019-2020, 2024 Nordix Foundation.
 *  Modifications Copyright (C) 2019 AT&T Intellectual Property. All rights reserved.
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

package org.onap.policy.models.base;

import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.onap.policy.models.base.testconcepts.DummyPfConcept;

class PfConceptKeyTest {

    private static final String VERSION001 = "0.0.1";
    private static final String ID_IS_NULL = "id is marked .*on.*ull but is null$";

    @Test
    void testConceptKey() {
        PfConceptKey someKey0 = new PfConceptKey();
        assertEquals(PfConceptKey.getNullKey(), someKey0);
        assertTrue(someKey0.isNullKey());
        assertEquals("PfConceptKey(name=NULL, version=0.0.0)", someKey0.toString());

        PfConceptKey someKey1 = new PfConceptKey("my-name", VERSION001);
        PfConceptKey someKey2 = new PfConceptKey(someKey1);
        PfConceptKey someKey3 = new PfConceptKey(someKey1.getId());
        assertEquals(someKey1, someKey2);
        assertEquals(someKey1, someKey3);
        assertFalse(someKey1.isNullVersion());
        assertFalse(someKey1.isNullName());
        assertEquals("PfConceptKey(name=my-name, version=0.0.1)", someKey1.toString());

        assertEquals("my-name", someKey1.getName());
        assertEquals(VERSION001, someKey1.getVersion());

        assertEquals(someKey2, someKey1.getKey());
        assertEquals(1, someKey1.getKeys().size());

        PfConcept pfc = new DummyPfConcept();
        assertEquals(PfConceptKey.getNullKey().getId(), pfc.getId());

        assertTrue(PfConceptKey.getNullKey().matchesId(pfc.getId()));

        assertTrue(PfConceptKey.getNullKey().isNullKey());

        assertThatThrownBy(() -> PfConceptKey.getNullKey().matchesId(null)).hasMessageMatching(ID_IS_NULL);

        assertThatThrownBy(() -> someKey0.setName(null)).isInstanceOf(NullPointerException.class)
            .hasMessageMatching("^name is marked .*on.*ull but is null$");

        assertThatThrownBy(() -> someKey0.setVersion(null)).isInstanceOf(NullPointerException.class)
            .hasMessageMatching("^version is marked .*on.*ull but is null$");

        assertThatIllegalArgumentException().isThrownBy(() -> new PfConceptKey("my-name.*", VERSION001)).withMessage(
            "parameter 'name': value 'my-name.*', does not match regular expression '^[A-Za-z0-9\\-_\\.]+$'"
                .replace('\'', '"'));
    }
}
