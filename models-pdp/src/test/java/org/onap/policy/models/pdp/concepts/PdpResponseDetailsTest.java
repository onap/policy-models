/*
 * ============LICENSE_START=======================================================
 * ONAP Policy Models
 * ================================================================================
 * Copyright (C) 2019 AT&T Intellectual Property. All rights reserved.
 * Modifications Copyright (C) 2024 Nordix Foundation
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
import static org.onap.policy.models.pdp.concepts.PdpMessageUtils.removeVariableFields;

import org.junit.jupiter.api.Test;
import org.onap.policy.models.pdp.enums.PdpResponseStatus;

class PdpResponseDetailsTest {

    @Test
    void testCopyConstructor() {
        assertThatThrownBy(() -> new PdpResponseDetails(null)).isInstanceOf(NullPointerException.class);

        PdpResponseDetails orig = new PdpResponseDetails();

        // verify with null values
        assertEquals(removeVariableFields(orig.toString()),
                        removeVariableFields(new PdpResponseDetails(orig).toString()));

        // verify with all values
        orig.setResponseMessage("my-message");
        orig.setResponseStatus(PdpResponseStatus.FAIL);
        orig.setResponseTo("original-request-id");

        assertEquals(removeVariableFields(orig.toString()),
                        removeVariableFields(new PdpResponseDetails(orig).toString()));
    }
}
