/*
 * ============LICENSE_START=======================================================
 * ONAP Policy Models
 * ================================================================================
 * Copyright (C) 2019 AT&T Intellectual Property. All rights reserved.
 * Modifications Copyright (C) 2024 Nordix Copyright
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

import org.junit.jupiter.api.Test;
import org.onap.policy.models.pdp.enums.PdpHealthStatus;
import org.onap.policy.models.pdp.enums.PdpState;

/**
 * Test the copy constructor, as {@link ModelsTest} tests the other methods.
 */
class PdpInstanceDetailsTest {

    @Test
    void testCopyConstructor() {
        assertThatThrownBy(() -> new Pdp(null)).isInstanceOf(NullPointerException.class);

        Pdp orig = new Pdp();

        // verify with null values
        assertEquals(orig.toString(), new Pdp(orig).toString());

        // verify with all values
        orig.setHealthy(PdpHealthStatus.TEST_IN_PROGRESS);
        orig.setInstanceId("my-instance");
        orig.setMessage("my-message");
        orig.setPdpState(PdpState.SAFE);

        assertEquals(orig.toString(), new Pdp(orig).toString());
    }
}
