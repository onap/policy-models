/*-
 * ============LICENSE_START=======================================================
 * ONAP Policy Models
 * ================================================================================
 * Copyright (C) 2019, 2021 AT&T Intellectual Property. All rights reserved.
 * Modifications Copyright (C) 2019 Nordix Foundation.
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

import org.junit.Test;
import org.onap.policy.models.pdp.enums.PdpState;

/**
 * Test the copy constructor, as {@link ModelsTest} tests the other methods.
 */
public class PdpStateChangeTest {

    @Test
    public void testCopyConstructor() {
        assertThatThrownBy(() -> new PdpStateChange(null)).isInstanceOf(NullPointerException.class);

        PdpStateChange orig = new PdpStateChange();

        // verify with null values
        assertEquals(removeVariableFields(orig.toString()), removeVariableFields(new PdpStateChange(orig).toString()));

        // verify with all values
        orig.setSource("my-source");
        orig.setName("my-name");
        orig.setPdpGroup("my-group");
        orig.setPdpSubgroup("my-subgroup");
        orig.setState(PdpState.SAFE);

        assertEquals(removeVariableFields(orig.toString()), removeVariableFields(new PdpStateChange(orig).toString()));
    }
}
