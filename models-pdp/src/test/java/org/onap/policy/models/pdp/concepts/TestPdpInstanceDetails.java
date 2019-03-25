/*
 * ============LICENSE_START=======================================================
 * ONAP Policy Models
 * ================================================================================
 * Copyright (C) 2019 AT&T Intellectual Property. All rights reserved.
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

import org.junit.Test;
import org.onap.policy.models.pdp.enums.PdpHealthStatus;
import org.onap.policy.models.pdp.enums.PdpState;

/**
 * Test the copy constructor, as {@link TestModels} tests the other methods.
 */
public class TestPdpInstanceDetails {

    @Test
    public void testCopyConstructor() {
        assertThatThrownBy(() -> new PdpInstanceDetails(null)).isInstanceOf(NullPointerException.class);

        PdpInstanceDetails orig = new PdpInstanceDetails();

        // verify with null values
        assertEquals(orig.toString(), new PdpInstanceDetails(orig).toString());

        // verify with all values
        orig.setHealthy(PdpHealthStatus.TEST_IN_PROGRESS);
        orig.setInstanceId("my-instance");
        orig.setMessage("my-message");
        orig.setPdpState(PdpState.SAFE);

        assertEquals(orig.toString(), new PdpInstanceDetails(orig).toString());
    }
}
