/*-
 * ============LICENSE_START=======================================================
 * ONAP Policy Models
 * ================================================================================
 * Copyright (C) 2019 AT&T Intellectual Property. All rights reserved.
 * Modifications Copyright (C) 2020-2021 Nordix Foundation.
 * Modifications Copyright (C) 2023 Bell Canada. All rights reserved.
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
import static org.junit.Assert.assertEquals;
import static org.onap.policy.models.pdp.concepts.PdpMessageUtils.removeVariableFields;

import java.util.Arrays;
import java.util.Collections;
import org.junit.Test;
import org.onap.policy.models.pdp.enums.PdpHealthStatus;
import org.onap.policy.models.pdp.enums.PdpState;
import org.onap.policy.models.tosca.authorative.concepts.ToscaConceptIdentifier;

public class PdpStatusTest {

    @Test
    public void testCopyConstructor() {
        assertThatThrownBy(() -> new PdpStatus(null)).isInstanceOf(NullPointerException.class);

        final PdpStatus orig = new PdpStatus();

        // verify with null values
        orig.setPolicies(Collections.emptyList());
        assertEquals(removeVariableFields(orig.toString()), removeVariableFields(new PdpStatus(orig).toString()));

        // verify with all values
        orig.setDeploymentInstanceInfo("my-deployment");
        orig.setDescription("my-description");
        orig.setHealthy(PdpHealthStatus.NOT_HEALTHY);
        orig.setName("my-name");
        orig.setPdpGroup("my-group");
        orig.setPdpSubgroup("my-subgroup");
        orig.setPdpType("my-type");
        orig.setPolicies(Arrays.asList(new ToscaConceptIdentifier("policy-A", "1.0.0")));
        orig.setProperties("my-properties");

        final PdpResponseDetails resp = new PdpResponseDetails();
        resp.setResponseMessage("my-response");

        orig.setResponse(resp);
        orig.setState(PdpState.SAFE);

        assertEquals(removeVariableFields(orig.toString()), removeVariableFields(new PdpStatus(orig).toString()));
    }
}
