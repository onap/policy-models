/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2019 Nordix Foundation.
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

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.onap.policy.models.base.PfKey.Compatibility;
import org.onap.policy.models.base.testconcepts.DummyPfConceptKeySub;

public class PfKeyUseTest {

    private static final String OTHER_KEY_IS_NULL = "otherKey is marked @NonNull but is null";

    @Test
    public void testKeyUse() {
        assertNotNull(new PfKeyUse());
        assertNotNull(new PfKeyUse(new PfConceptKey()));
        assertNotNull(new PfKeyUse(new PfReferenceKey()));

        assertThatThrownBy(() -> new PfKeyUse((PfKeyUse) null))
                        .hasMessage("copyConcept is marked @NonNull but is null");

        PfConceptKey key = new PfConceptKey("Key", "0.0.1");
        PfKeyUse keyUse = new PfKeyUse();
        keyUse.setKey(key);
        assertEquals(key, keyUse.getKey());
        assertEquals("Key:0.0.1", keyUse.getId());
        assertEquals(key, keyUse.getKeys().get(0));
        assertFalse(keyUse.isNullKey());

        assertEquals(Compatibility.IDENTICAL, keyUse.getCompatibility(key));

        assertThatThrownBy(() -> key.getCompatibility(null)).hasMessage(OTHER_KEY_IS_NULL);

        assertTrue(keyUse.isCompatible(key));

        keyUse.clean();
        assertNotNull(keyUse);

        PfValidationResult result = new PfValidationResult();
        result = keyUse.validate(result);
        assertNotNull(result);

        assertNotEquals(0, keyUse.hashCode());

        PfKeyUse clonedKeyUse = new PfKeyUse(keyUse);
        assertEquals("PfKeyUse(usedKey=PfConceptKey(name=Key, version=0.0.1))", clonedKeyUse.toString());

        assertFalse(keyUse.hashCode() == 0);

        assertTrue(keyUse.equals(keyUse));
        assertTrue(keyUse.equals(clonedKeyUse));
        assertFalse(keyUse.equals("Hello"));
        assertTrue(keyUse.equals(new PfKeyUse(key)));

        assertEquals(0, keyUse.compareTo(keyUse));
        assertEquals(0, keyUse.compareTo(clonedKeyUse));
        assertNotEquals(0, keyUse.compareTo(new PfConceptKey()));
        assertEquals(0, keyUse.compareTo(new PfKeyUse(key)));

        PfKeyUse keyUseNull = new PfKeyUse(PfConceptKey.getNullKey());
        PfValidationResult resultNull = new PfValidationResult();
        assertEquals(false, keyUseNull.validate(resultNull).isValid());

        assertThatThrownBy(() -> keyUse.setKey(null)).hasMessage("key is marked @NonNull but is null");

        assertThatThrownBy(() -> keyUse.getCompatibility(null)).hasMessage(OTHER_KEY_IS_NULL);

        assertThatThrownBy(() -> keyUse.isCompatible(null)).hasMessage(OTHER_KEY_IS_NULL);

        assertThatThrownBy(() -> keyUse.validate(null)).hasMessage("result is marked @NonNull but is null");

        PfKeyUse testKeyUse = new PfKeyUse(new DummyPfConceptKeySub(new PfConceptKey()));
        assertEquals(testKeyUse, new PfKeyUse(testKeyUse));

        assertThatThrownBy(() -> new PfKeyUse((PfKeyUse) null)).isInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> keyUse.isNewerThan(null)).hasMessage(OTHER_KEY_IS_NULL);

        assertEquals(false, testKeyUse.isNewerThan(keyUse));
        assertEquals(false, testKeyUse.isNewerThan(testKeyUse));

        assertEquals(0, testKeyUse.getMajorVersion());
        assertEquals(0, testKeyUse.getMinorVersion());
        assertEquals(0, testKeyUse.getPatchVersion());
    }
}
