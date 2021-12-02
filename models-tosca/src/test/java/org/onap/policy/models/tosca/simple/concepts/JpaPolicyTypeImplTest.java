/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2022 Nordix Foundation.
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

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;

import java.util.HashMap;
import java.util.Map;
import org.junit.Test;
import org.onap.policy.models.base.PfConceptKey;
import org.onap.policy.models.base.PfKey;
import org.onap.policy.models.tosca.authorative.concepts.PolicyTypeImpl;
import org.onap.policy.models.tosca.authorative.concepts.ToscaConceptIdentifier;

/**
 * DAO test for PolicyTypeImpl.
 */
public class JpaPolicyTypeImplTest {

    private static final String KEY_IS_NULL = "key is marked .*on.*ull but is null";
    private static final String VERSION_001 = "0.0.1";

    @Test
    public void testPolicyTypeImplPojo() {

        assertThatThrownBy(() -> {
            new JpaPolicyTypeImpl((PfConceptKey) null);
        }).hasMessageMatching(KEY_IS_NULL);

        assertThatThrownBy(() -> new JpaPolicyTypeImpl((JpaPolicyTypeImpl) null))
            .isInstanceOf(NullPointerException.class);

    }

    @Test
    public void testJpaPolicyTypeImpl() {
        PfConceptKey key1 = new PfConceptKey("policyTypeimplKey", VERSION_001);
        String pdpType = "dummy-pdp";
        JpaPolicyTypeImpl jpaPolicyTypeImpl = new JpaPolicyTypeImpl(key1);

        Map<String, String> propertyMap = new HashMap<>();
        propertyMap.put("Property", "\"Property Value\"");
        jpaPolicyTypeImpl.setMetadata(propertyMap);
        assertEquals(propertyMap, jpaPolicyTypeImpl.getMetadata());

        PfConceptKey key = new PfConceptKey("target", VERSION_001);
        jpaPolicyTypeImpl.setPolicyTypeImplKey(key);
        assertEquals(key, jpaPolicyTypeImpl.getKey());

        JpaPolicyTypeImpl policyTypeImplClone0 = new JpaPolicyTypeImpl(jpaPolicyTypeImpl);
        assertEquals(jpaPolicyTypeImpl, policyTypeImplClone0);
        assertEquals(0, jpaPolicyTypeImpl.compareTo(policyTypeImplClone0));

        JpaPolicyTypeImpl jpaPolicyTypeImplClone1 = new JpaPolicyTypeImpl(jpaPolicyTypeImpl);
        assertEquals(jpaPolicyTypeImpl, jpaPolicyTypeImplClone1);
        assertEquals(0, jpaPolicyTypeImpl.compareTo(jpaPolicyTypeImplClone1));

        assertEquals(-1, jpaPolicyTypeImpl.compareTo(null));
        assertNotEquals(0, jpaPolicyTypeImpl.compareTo(jpaPolicyTypeImpl.getKey()));

        PfConceptKey otherKey = new PfConceptKey("key2", VERSION_001);
        JpaPolicyTypeImpl otherJpaPolicyTypeImpl = new JpaPolicyTypeImpl(otherKey);

        assertNotEquals(0, jpaPolicyTypeImpl.compareTo(otherJpaPolicyTypeImpl));
        otherJpaPolicyTypeImpl.setPolicyTypeImplKey(key1);
        assertNotEquals(0, jpaPolicyTypeImpl.compareTo(otherJpaPolicyTypeImpl));
        otherJpaPolicyTypeImpl.setPolicyTypeImplKey(key1);
        assertNotEquals(0, jpaPolicyTypeImpl.compareTo(otherJpaPolicyTypeImpl));
        otherJpaPolicyTypeImpl.setMetadata(propertyMap);
        assertNotEquals(0, jpaPolicyTypeImpl.compareTo(otherJpaPolicyTypeImpl));

        assertEquals(1, jpaPolicyTypeImpl.getKeys().size());
        assertEquals(1, new JpaPolicyTypeImpl().getKeys().size());

        jpaPolicyTypeImpl.clean();
        assertEquals(policyTypeImplClone0, jpaPolicyTypeImpl);
    }


    @Test
    public void testJpaPolicyImplAuthorative() {
        JpaPolicyTypeImpl jpaPolicyTypeImpl = setUpJpaPolicyTypeImpl();
        PfConceptKey tpTypeKey = jpaPolicyTypeImpl.getKey();
        assertNotNull(tpTypeKey);

        assertThatThrownBy(() -> {
            jpaPolicyTypeImpl.validate(null);
        }).hasMessageMatching("fieldName is marked .*on.*ull but is null");

        assertNotNull(jpaPolicyTypeImpl.toAuthorative());
        jpaPolicyTypeImpl.getPolicyTypeImplKey().setVersion(PfKey.NULL_KEY_VERSION);
        assertNotNull(jpaPolicyTypeImpl.toAuthorative());
        jpaPolicyTypeImpl.setMetadata(new HashMap<>());
        assertNotNull(jpaPolicyTypeImpl.toAuthorative());

        PolicyTypeImpl pol1 = new PolicyTypeImpl();
        pol1.setPolicyTypeImplRef(new ToscaConceptIdentifier("policyModel", "0.0.1"));
        pol1.setPolicyTypeRef(new ToscaConceptIdentifier("dummyPolicyType", "1.0.1"));
        jpaPolicyTypeImpl.fromAuthorative(pol1);
        assertEquals("0.0.1", jpaPolicyTypeImpl.getPolicyTypeImplKey().getVersion());
    }

    private JpaPolicyTypeImpl setUpJpaPolicyTypeImpl() {
        PfConceptKey key1 = new PfConceptKey("dummyPolicyImpl", VERSION_001);
        PfConceptKey policyTypeRef = new PfConceptKey("dummyPolicyType", "0.0.1");
        JpaPolicyTypeImpl jpaPolicyTypeImpl = new JpaPolicyTypeImpl(key1);

        Map<String, String> policyModel = new HashMap<>(Map.of("Property", "\"Property Value\""));
        jpaPolicyTypeImpl.setMetadata(policyModel);
        jpaPolicyTypeImpl.setPolicyTypeRef(policyTypeRef);
        jpaPolicyTypeImpl.setPdpType("dummy-pdp");

        return jpaPolicyTypeImpl;
    }
}
