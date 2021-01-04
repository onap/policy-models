/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2021 Nordix Foundation.
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

package org.onap.policy.models.pap.concepts;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.io.IOException;
import org.junit.Test;
import org.onap.policy.common.utils.coder.CoderException;
import org.onap.policy.common.utils.coder.StandardCoder;
import org.onap.policy.common.utils.resources.TextFileUtils;

/**
 * This only tests the methods that aren't already tested via TestModels.
 */
public class PapPolicyIdentifierTest {

    @Test
    public void testPapPolicyIdentifier() throws CoderException {
        assertNotNull(new PapPolicyIdentifier("Name", "Version"));
        assertNotNull(new PapPolicyIdentifier("Name", null));
        assertNotNull(new PapPolicyIdentifier(null, null));

        PapPolicyIdentifier ppi = new PapPolicyIdentifier("myname", "1.2.3");

        assertEquals("myname", ppi.getGenericIdentifier().getName());
        assertEquals("1.2.3", ppi.getGenericIdentifier().getVersion());

        PapPolicyIdentifier ppi2 = new PapPolicyIdentifier(null, null);
        assertNull(ppi2.getGenericIdentifier().getName());
        assertNull(ppi2.getGenericIdentifier().getVersion());
    }

    @Test
    public void testPapPolicyIdentifierSerialization() throws CoderException, IOException {
        String idString = TextFileUtils.getTextFileAsString("src/test/resources/json/PapPolicyIdentifier.json");

        StandardCoder coder = new StandardCoder();

        PapPolicyIdentifier ppi = coder.decode(idString, PapPolicyIdentifier.class);

        assertEquals("MyPolicy", ppi.getGenericIdentifier().getName());
        assertEquals("1.2.6", ppi.getGenericIdentifier().getVersion());

        String idStringBack = coder.encode(ppi);
        assertEquals(idString.replaceAll("\\s+", ""), idStringBack.replaceAll("\\s+", ""));
    }
}
