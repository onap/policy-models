/*
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2019 Nordix Foundation.
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.onap.policy.models.base.PfKey.Compatibility;

public class PfKeyUseTest {

    @Test
    public void test() {
        assertNotNull(new PfKeyUse());
        assertNotNull(new PfKeyUse(new PfConceptKey()));
        assertNotNull(new PfKeyUse(new PfReferenceKey()));

        PfConceptKey key = new PfConceptKey("Key", "0.0.1");
        PfKeyUse keyUse = new PfKeyUse();
        keyUse.setKey(key);
        assertEquals(key, keyUse.getKey());
        assertEquals("Key:0.0.1", keyUse.getId());
        assertEquals(key, keyUse.getKeys().get(0));

        assertEquals(Compatibility.IDENTICAL, keyUse.getCompatibility(key));
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
    }
}
