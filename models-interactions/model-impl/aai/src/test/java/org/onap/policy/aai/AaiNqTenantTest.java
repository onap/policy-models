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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.onap.policy.aai.util.Serialization;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AaiNqTenantTest {
    private static final Logger logger = LoggerFactory.getLogger(AaiNqTenantTest.class);

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {}

    @AfterClass
    public static void tearDownAfterClass() throws Exception {}

    @Test
    public void test() {
        AaiNqTenant aaiNqTenant = new AaiNqTenant();
        aaiNqTenant.setTenantId("dhv-test-tenant");
        aaiNqTenant.setTenantName("dhv-test-tenant-name");
        aaiNqTenant.setResourceVersion("1485366334");
        assertNotNull(aaiNqTenant);
        assertEquals("dhv-test-tenant", aaiNqTenant.getTenantId());
        assertEquals("dhv-test-tenant-name", aaiNqTenant.getTenantName());
        assertEquals("1485366334", aaiNqTenant.getResourceVersion());

        logger.info(Serialization.gsonPretty.toJson(aaiNqTenant));
    }

}
