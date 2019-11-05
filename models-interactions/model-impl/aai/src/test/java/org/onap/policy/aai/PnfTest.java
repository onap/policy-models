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

public class PnfTest {
    private static final String PNF_NAME_TEST = "pnf-name-test";
    private static final Logger logger = LoggerFactory.getLogger(PnfTest.class);

    @Test
    public void test() {
        Pnf pnf = new Pnf();
        pnf.setPnfName(PNF_NAME_TEST);
        pnf.setPnfType(PnfType.ENODEB);
        assertNotNull(pnf);

        Pnf pnfClone = new Pnf(pnf);
        assertNotNull(pnfClone);

        assertEquals(PNF_NAME_TEST, pnfClone.getPnfName());
        assertEquals(PnfType.ENODEB, pnfClone.getPnfType());

        assertNotNull(pnfClone.toString());
        assertNotEquals(0, pnfClone.hashCode());
        assertNotEquals(0, new Pnf().hashCode());

        Pnf pnfOther = new Pnf();
        pnfOther.setPnfName(PNF_NAME_TEST);

        assertEquals(pnf, pnf);
        assertNotNull(pnf);
        assertNotEquals("hello", pnf);
        assertEquals(pnf, pnfClone);
        assertNotEquals(pnf, new Pnf());
        assertNotEquals(new Pnf(), pnf);
        assertNotEquals(new Pnf(), pnfOther);
        assertNotEquals(pnfOther, pnf);

        logger.info(Serialization.gsonPretty.toJson(pnf));
    }
}
