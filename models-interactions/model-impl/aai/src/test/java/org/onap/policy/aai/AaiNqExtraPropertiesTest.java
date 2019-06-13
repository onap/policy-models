/*-
 * ============LICENSE_START=======================================================
 * aai
 * ================================================================================
 * Copyright (C) 2017-2019 AT&T Intellectual Property. All rights reserved.
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

package org.onap.policy.aai;

import static org.junit.Assert.assertNotNull;

import org.junit.Test;

public class AaiNqExtraPropertiesTest {

    @Test
    public void test() {
        AaiNqExtraProperties aaiNqExtraProperties = new AaiNqExtraProperties();
        aaiNqExtraProperties.getExtraProperty().add(new AaiNqExtraProperty("model.model-name", "service-instance"));
        aaiNqExtraProperties.getExtraProperty().add(new AaiNqExtraProperty("model.model-type", "widget"));
        aaiNqExtraProperties.getExtraProperty().add(new AaiNqExtraProperty("model.model-version", "1.0"));
        aaiNqExtraProperties.getExtraProperty()
                .add(new AaiNqExtraProperty("model.model-id", "82194af1-3c2c-485a-8f44-420e22a9eaa4"));
        aaiNqExtraProperties.getExtraProperty()
                .add(new AaiNqExtraProperty("model.model-name", "46b92144-923a-4d20-b85a-3cbd847668a9"));
        assertNotNull(aaiNqExtraProperties);
    }

}
