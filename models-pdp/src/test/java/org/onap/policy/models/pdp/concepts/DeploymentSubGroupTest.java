/*-
 * ============LICENSE_START=======================================================
 * ONAP Policy Models
 * ================================================================================
 * Copyright (C) 2019 AT&T Intellectual Property. All rights reserved.
 * Modifications Copyright (C) 2021-2024 Nordix Foundation.
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

package org.onap.policy.models.pdp.concepts;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import org.junit.jupiter.api.Test;
import org.onap.policy.common.parameters.ValidationResult;
import org.onap.policy.common.utils.coder.Coder;
import org.onap.policy.common.utils.coder.CoderException;
import org.onap.policy.common.utils.coder.StandardCoder;
import org.onap.policy.models.pdp.concepts.DeploymentSubGroup.Action;
import org.onap.policy.models.tosca.authorative.concepts.ToscaConceptIdentifier;

/**
 * Test methods not tested by {@link ModelsTest}.
 */
class DeploymentSubGroupTest {
    private static final String VERSION_300 = "3.0.0";
    private static final Coder coder = new StandardCoder();

    @Test
    void testCopyConstructor() {
        assertThatThrownBy(() -> new DeploymentSubGroup(null)).isInstanceOf(NullPointerException.class);

        final DeploymentSubGroup orig = new DeploymentSubGroup();

        // verify with null values
        assertEquals("DeploymentSubGroup(pdpType=null, action=null, policies=[])",
                        new DeploymentSubGroup(orig).toString());

        orig.setPdpType("my-type");
        orig.setAction(Action.POST);

        final ToscaConceptIdentifier pol1 = new ToscaConceptIdentifier();
        pol1.setName("policy-A");
        pol1.setVersion("1.0.0");
        final ToscaConceptIdentifier pol2 = new ToscaConceptIdentifier();
        pol2.setName("policy-B");
        pol1.setVersion("2.0.0");
        orig.setPolicies(Arrays.asList(pol1, pol2));

        assertEquals(orig.toString(), new DeploymentSubGroup(orig).toString());
    }

    @Test
    void testValidatePapRest() throws Exception {
        DeploymentSubGroup subgrp = new DeploymentSubGroup();

        subgrp.setPdpType("pdp-type");
        subgrp.setAction(Action.PATCH);
        subgrp.setPolicies(Arrays.asList(makeIdent("policy-X", "4.0.0", ToscaConceptIdentifier.class)));

        // valid
        ValidationResult result = subgrp.validatePapRest();
        assertNotNull(result);
        assertTrue(result.isValid());
        assertNull(result.getResult());

        // null pdp type
        DeploymentSubGroup sub2 = new DeploymentSubGroup(subgrp);
        sub2.setPdpType(null);
        assertInvalid(sub2);

        // null action
        sub2 = new DeploymentSubGroup(subgrp);
        sub2.setAction(null);
        assertInvalid(sub2);

        // null policies
        sub2 = new DeploymentSubGroup(subgrp);
        sub2.setPolicies(null);
        assertInvalid(sub2);

        // null policy item
        sub2 = new DeploymentSubGroup(subgrp);
        sub2.getPolicies().set(0, null);
        assertInvalid(sub2);

        // invalid policy item
        sub2 = new DeploymentSubGroup(subgrp);
        sub2.getPolicies().set(0, makeIdent(null, VERSION_300, ToscaConceptIdentifier.class));
        assertInvalid(sub2);
    }

    private void assertInvalid(DeploymentSubGroup sub2) {
        ValidationResult result = sub2.validatePapRest();
        assertNotNull(result);
        assertFalse(result.isValid());
        assertNotNull(result.getResult());
    }

    /**
     * Makes an identifier. Uses JSON which does no error checking.
     *
     * @param name name to put into the identifier
     * @param version version to put into the identifier
     * @param clazz type of identifier to create
     * @return a new identifier
     * @throws CoderException if the JSON cannot be decoded
     */
    private <T> T makeIdent(String name, String version, Class<T> clazz) throws CoderException {
        StringBuilder bldr = new StringBuilder();
        bldr.append("{");

        if (name != null) {
            bldr.append("'name':'");
            bldr.append(name);
            bldr.append("'");
        }

        if (version != null) {
            if (name != null) {
                bldr.append(',');
            }

            bldr.append("'version':'");
            bldr.append(version);
            bldr.append("'");
        }

        bldr.append("}");

        String json = bldr.toString().replace('\'', '"');

        return coder.decode(json, clazz);
    }
}
