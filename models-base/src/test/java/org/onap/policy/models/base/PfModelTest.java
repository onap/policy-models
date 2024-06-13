/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2019-2021, 2024 Nordix Foundation.
 *  Modifications Copyright (C) 2019-2020 AT&T Intellectual Property. All rights reserved.
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

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.onap.policy.models.base.testconcepts.DummyPfModel;

/**
 * Test of the PfModel clas.
 *
 * @author Liam Fallon (liam.fallon@est.tech)
 */
class PfModelTest {

    private static final String VERSION001 = "0.0.1";

    @Test
    void testPfModel() {
        assertNotNull(new DummyPfModel());
        assertNotNull(new DummyPfModel(new PfConceptKey()));
        assertNotNull(new DummyPfModel(new DummyPfModel()));

        assertThatThrownBy(() -> new DummyPfModel((PfConceptKey) null))
            .hasMessageMatching("^key is marked .*on.*ull but is null$");

        assertThatThrownBy(() -> new DummyPfModel((DummyPfModel) null))
            .hasMessageMatching("^copyConcept is marked .*on.*ull but is null$");

        DummyPfModel dpm = new DummyPfModel(new PfConceptKey("modelKey", VERSION001));
        DummyPfModel dpmClone = new DummyPfModel(dpm);
        assertEquals(dpm, dpmClone);

        assertEquals(1, dpm.getKeys().size());

        dpmClone.clean();
        assertEquals(dpm, dpmClone);

        assertThatThrownBy(() -> new DummyPfModel((DummyPfModel) null)).isInstanceOf(NullPointerException.class);

        assertEquals(0, dpm.compareTo(dpmClone));
        assertEquals(-1, dpm.compareTo(null));
        assertEquals(0, dpm.compareTo(dpm));
        assertNotEquals(0, dpm.compareTo(dpm.getKey()));
    }

    @Test
    void testPfModelValidation() {
        PfConceptKey dpmKey = new PfConceptKey("modelKey", VERSION001);
        DummyPfModel dpm = new DummyPfModel(dpmKey);
        assertTrue(dpm.validate("").isValid());

        assertThatThrownBy(() -> dpm.validate(null)).hasMessageMatching("^fieldName is marked .*on.*ull but is null$");

        dpm.setKey(PfConceptKey.getNullKey());
        assertFalse(dpm.validate("").isValid());
        dpm.setKey(dpmKey);
        assertTrue(dpm.validate("").isValid());

        dpm.getKeyList().add(PfReferenceKey.getNullKey());
        dpm.getKeyList().add(new PfKeyUse(PfReferenceKey.getNullKey()));
        assertFalse(dpm.validate("").isValid());
        dpm.getKeyList().clear();
        assertTrue(dpm.validate("").isValid());

        PfConceptKey goodCKey = new PfConceptKey("goodCKey", VERSION001);
        PfReferenceKey goodRKey = new PfReferenceKey(goodCKey, "goodLocalName");

        dpm.getKeyList().add(goodCKey);
        dpm.getKeyList().add(goodRKey);
        assertTrue(dpm.validate("").isValid());
    }

    @Test
    void testPfReferenceValidation() {
        PfConceptKey dpmKey = new PfConceptKey("modelKey", VERSION001);
        DummyPfModel dpm = new DummyPfModel(dpmKey);

        PfConceptKey goodCKey = new PfConceptKey("goodCKey", VERSION001);
        PfReferenceKey goodRKey = new PfReferenceKey(goodCKey, "goodLocalName");

        dpm.getKeyList().add(goodCKey);
        dpm.getKeyList().add(goodRKey);
        assertTrue(dpm.validate("").isValid());

        PfConceptKey goodCKeyDup = new PfConceptKey(goodCKey);
        dpm.getKeyList().add(goodCKeyDup);
        assertFalse(dpm.validate("").isValid());
        dpm.getKeyList().remove(goodCKeyDup);
        assertTrue(dpm.validate("").isValid());

        PfReferenceKey goodRKeyDup = new PfReferenceKey(goodRKey);
        dpm.getKeyList().add(goodRKeyDup);
        assertFalse(dpm.validate("").isValid());
        dpm.getKeyList().remove(goodRKeyDup);
        assertTrue(dpm.validate("").isValid());

        PfKeyUse goodCKeyUse = new PfKeyUse(goodCKey);
        dpm.getKeyList().add(goodCKeyUse);
        assertTrue(dpm.validate("").isValid());

        PfKeyUse goodRKeyUse = new PfKeyUse(goodRKey);
        dpm.getKeyList().add(goodRKeyUse);
        assertTrue(dpm.validate("").isValid());

        PfConceptKey badCKey = new PfConceptKey("badCKey", VERSION001);
        PfKeyUse badCKeyUse = new PfKeyUse(badCKey);
        dpm.getKeyList().add(badCKeyUse);
        assertFalse(dpm.validate("").isValid());
        dpm.getKeyList().remove(badCKeyUse);
        assertTrue(dpm.validate("").isValid());

        PfKeyUse badRKeyUse = new PfKeyUse(new PfReferenceKey(badCKey, "badLocalName"));
        dpm.getKeyList().add(badRKeyUse);
        assertFalse(dpm.validate("").isValid());
        dpm.getKeyList().remove(badRKeyUse);
        assertTrue(dpm.validate("").isValid());
    }
}
