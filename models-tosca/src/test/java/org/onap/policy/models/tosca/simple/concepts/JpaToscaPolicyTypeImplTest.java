/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2021 Nordix Foundation.
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
import org.onap.policy.models.tosca.authorative.concepts.ToscaConceptIdentifier;
import org.onap.policy.models.tosca.authorative.concepts.ToscaPolicyTypeImpl;

/**
 * DAO test for ToscaPolicyTypeImpl.
 */
public class JpaToscaPolicyTypeImplTest {

    private static final String KEY_IS_NULL = "key is marked .*on.*ull but is null";
    private static final String VERSION_001 = "0.0.1";

    @Test
    public void testPolicyTypeImplPojo() {

        assertThatThrownBy(() -> {
            new JpaToscaPolicyTypeImpl((PfConceptKey) null);
        }).hasMessageMatching(KEY_IS_NULL);

        assertThatThrownBy(() -> new JpaToscaPolicyTypeImpl((JpaToscaPolicyTypeImpl) null))
            .isInstanceOf(NullPointerException.class);

    }

    @Test
    public void testJpaToscaPolicyTypeImpl() {
        PfConceptKey key1 = new PfConceptKey("policyTypeimplKey", VERSION_001);
        String pdpType = "dummy-pdp";
        JpaToscaPolicyTypeImpl jpaToscaPolicyTypeImpl = new JpaToscaPolicyTypeImpl(key1);

        Map<String, String> propertyMap = new HashMap<>();
        propertyMap.put("Property", "\"Property Value\"");
        jpaToscaPolicyTypeImpl.setPolicyModel(propertyMap);
        assertEquals(propertyMap, jpaToscaPolicyTypeImpl.getPolicyModel());

        PfConceptKey key = new PfConceptKey("target", VERSION_001);
        jpaToscaPolicyTypeImpl.setPolicyTypeImplKey(key);
        assertEquals(key, jpaToscaPolicyTypeImpl.getKey());

        JpaToscaPolicyTypeImpl policyTypeImplClone0 = new JpaToscaPolicyTypeImpl(jpaToscaPolicyTypeImpl);
        assertEquals(jpaToscaPolicyTypeImpl, policyTypeImplClone0);
        assertEquals(0, jpaToscaPolicyTypeImpl.compareTo(policyTypeImplClone0));

        JpaToscaPolicyTypeImpl jpaPolicyTypeImplClone1 = new JpaToscaPolicyTypeImpl(jpaToscaPolicyTypeImpl);
        assertEquals(jpaToscaPolicyTypeImpl, jpaPolicyTypeImplClone1);
        assertEquals(0, jpaToscaPolicyTypeImpl.compareTo(jpaPolicyTypeImplClone1));

        assertEquals(-1, jpaToscaPolicyTypeImpl.compareTo(null));
        assertNotEquals(0, jpaToscaPolicyTypeImpl.compareTo(jpaToscaPolicyTypeImpl.getKey()));

        PfConceptKey otherKey = new PfConceptKey("key2", VERSION_001);
        JpaToscaPolicyTypeImpl otherJpaPolicyTypeImpl = new JpaToscaPolicyTypeImpl(otherKey);

        assertNotEquals(0, jpaToscaPolicyTypeImpl.compareTo(otherJpaPolicyTypeImpl));
        otherJpaPolicyTypeImpl.setPolicyTypeImplKey(key1);
        assertNotEquals(0, jpaToscaPolicyTypeImpl.compareTo(otherJpaPolicyTypeImpl));
        otherJpaPolicyTypeImpl.setPolicyTypeImplKey(key1);
        assertNotEquals(0, jpaToscaPolicyTypeImpl.compareTo(otherJpaPolicyTypeImpl));
        otherJpaPolicyTypeImpl.setPolicyModel(propertyMap);
        assertNotEquals(0, jpaToscaPolicyTypeImpl.compareTo(otherJpaPolicyTypeImpl));

        assertEquals(1, jpaToscaPolicyTypeImpl.getKeys().size());
        assertEquals(1, new JpaToscaPolicyTypeImpl().getKeys().size());

        jpaToscaPolicyTypeImpl.clean();
        assertEquals(policyTypeImplClone0, jpaToscaPolicyTypeImpl);
    }


    @Test
    public void testJpaToscaPolicyImplAuthorative() {
        JpaToscaPolicyTypeImpl jpaToscaPolicyTypeImpl = setUpJpaToscaPolicyTypeImpl();
        PfConceptKey tpTypeKey = jpaToscaPolicyTypeImpl.getKey();
        assertNotNull(tpTypeKey);

        assertThatThrownBy(() -> {
            jpaToscaPolicyTypeImpl.validate(null);
        }).hasMessageMatching("fieldName is marked .*on.*ull but is null");

        assertNotNull(jpaToscaPolicyTypeImpl.toAuthorative());
        jpaToscaPolicyTypeImpl.getPolicyTypeImplKey().setVersion(PfKey.NULL_KEY_VERSION);
        assertNotNull(jpaToscaPolicyTypeImpl.toAuthorative());
        jpaToscaPolicyTypeImpl.setPolicyModel(new HashMap<>());
        assertNotNull(jpaToscaPolicyTypeImpl.toAuthorative());

        ToscaPolicyTypeImpl pol1 = new ToscaPolicyTypeImpl();
        pol1.setPolicyTypeImplRef(new ToscaConceptIdentifier("policyModel", "0.0.1"));
        pol1.setPolicyTypeRef(new ToscaConceptIdentifier("dummyPolicyType", "1.0.1"));
        jpaToscaPolicyTypeImpl.fromAuthorative(pol1);
        assertEquals("0.0.1", jpaToscaPolicyTypeImpl.getPolicyTypeImplKey().getVersion());
    }

    private JpaToscaPolicyTypeImpl setUpJpaToscaPolicyTypeImpl() {
        PfConceptKey key1 = new PfConceptKey("dummyPolicyImpl", VERSION_001);
        PfConceptKey policyTypeRef = new PfConceptKey("dummyPolicyType", "0.0.1");
        JpaToscaPolicyTypeImpl jpaPolicyTypeImpl = new JpaToscaPolicyTypeImpl(key1);

        Map<String, String> policyModel = new HashMap<>(Map.of("Property", "\"Property Value\""));
        jpaPolicyTypeImpl.setPolicyModel(policyModel);
        jpaPolicyTypeImpl.setPolicyTypeRef(policyTypeRef);
        jpaPolicyTypeImpl.setPdpType("dummy-pdp");

        return jpaPolicyTypeImpl;
    }
}
