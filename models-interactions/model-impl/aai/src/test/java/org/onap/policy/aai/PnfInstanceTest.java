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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.onap.policy.aai.util.Serialization;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PnfInstanceTest {
    private static final String PNF_NAME_TEST = "pnf-name-test";
    private static final Logger logger = LoggerFactory.getLogger(PnfInstanceTest.class);

    @Test
    public void test() {
        PnfInstance pnfInstance = new PnfInstance();
        pnfInstance.setPnfInstanceName("pnf-instance-name-test");
        pnfInstance.setPnfName(PNF_NAME_TEST);
        pnfInstance.setPnfType(PnfType.ENODEB);
        pnfInstance.setPnfSerial("pnf-serial-test");
        assertNotNull(pnfInstance);
        assertEquals("pnf-instance-name-test", pnfInstance.getPnfInstanceName());

        PnfInstance pnfInstanceNull = new PnfInstance(null);
        assertNotNull(pnfInstanceNull);

        PnfInstance pnfInstanceClone = new PnfInstance(pnfInstance);
        assertNotNull(pnfInstanceClone);

        assertEquals(PNF_NAME_TEST, pnfInstanceClone.getPnfName());
        assertEquals(PnfType.ENODEB, pnfInstanceClone.getPnfType());
        assertEquals("pnf-serial-test", pnfInstanceClone.getPnfSerial());

        assertNotNull(pnfInstanceClone.toString());
        assertNotEquals(0, pnfInstanceClone.hashCode());
        assertNotEquals(0, new Pnf().hashCode());

        PnfInstance pnfInstanceOther0 = new PnfInstance();
        pnfInstanceOther0.setPnfName(PNF_NAME_TEST);

        PnfInstance pnfInstanceOther1 = new PnfInstance(pnfInstance);
        pnfInstanceOther1.setPnfName("pnf-name-test-diff");

        PnfInstance pnfInstanceOther2 = new PnfInstance(pnfInstance);
        pnfInstanceOther2.setPnfInstanceName("pnf-instance-name-test-diff");

        PnfInstance pnfInstanceOther3 = new PnfInstance(pnfInstance);
        pnfInstanceOther3.setPnfName(null);

        PnfInstance pnfInstanceOther4 = new PnfInstance(pnfInstance);
        pnfInstanceOther4.setPnfSerial(null);

        PnfInstance pnfInstanceOther5 = new PnfInstance(pnfInstance);
        pnfInstanceOther5.setPnfSerial("pnf-serial-test-diff");

        assertEquals(pnfInstance, pnfInstance);
        assertNotEquals(null, pnfInstance);
        assertNotEquals("hello", pnfInstance);
        assertEquals(pnfInstance, pnfInstanceClone);
        assertNotEquals(pnfInstance, new Pnf());
        assertNotEquals(new Pnf(), pnfInstance);
        assertNotEquals(new Pnf(), pnfInstanceOther0);
        assertNotEquals(pnfInstanceOther0, pnfInstance);
        assertNotEquals(pnfInstanceOther1, pnfInstance);
        assertNotEquals(pnfInstanceOther2, pnfInstance);
        assertNotEquals(pnfInstanceOther3, pnfInstance);
        assertNotEquals(pnfInstanceOther4, pnfInstance);
        assertNotEquals(pnfInstanceOther5, pnfInstance);

        logger.info(Serialization.gsonPretty.toJson(pnfInstance));
    }

}
