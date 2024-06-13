/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2021-2024 Nordix Foundation.
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

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.io.IOException;
import org.junit.jupiter.api.Test;
import org.onap.policy.common.utils.coder.CoderException;
import org.onap.policy.common.utils.coder.StandardCoder;
import org.onap.policy.common.utils.resources.TextFileUtils;
import org.onap.policy.models.tosca.authorative.concepts.ToscaConceptIdentifier;
import org.onap.policy.models.tosca.authorative.concepts.ToscaConceptIdentifierOptVersion;

/**
 * This only tests the methods that aren't already tested via TestModels.
 */
class PapPolicyIdentifierTest {

    @Test
    void testPapPolicyIdentifier() throws CoderException {
        assertNotNull(new PapPolicyIdentifier("Name", "Version"));
        assertNotNull(new PapPolicyIdentifier("Name", null));
        assertNotNull(new PapPolicyIdentifier(null, null));

        assertNotNull(new PapPolicyIdentifier(new ToscaConceptIdentifier("Name", "Version")));
        assertNotNull(new PapPolicyIdentifier(new ToscaConceptIdentifierOptVersion("Name", "Version")));

        assertThatThrownBy(() -> new PapPolicyIdentifier((ToscaConceptIdentifier) null))
                .isInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> new PapPolicyIdentifier((ToscaConceptIdentifierOptVersion) null))
                .isInstanceOf(NullPointerException.class);

        PapPolicyIdentifier ppi = new PapPolicyIdentifier("myname", "1.2.3");

        assertEquals("myname", ppi.getGenericIdentifier().getName());
        assertEquals("1.2.3", ppi.getGenericIdentifier().getVersion());

        PapPolicyIdentifier ppi2 = new PapPolicyIdentifier(null, null);
        assertNull(ppi2.getGenericIdentifier().getName());
        assertNull(ppi2.getGenericIdentifier().getVersion());
    }

    @Test
    void testPapPolicyIdentifierSerialization() throws CoderException, IOException {
        String idString = TextFileUtils.getTextFileAsString("src/test/resources/json/PapPolicyIdentifier.json");

        StandardCoder coder = new StandardCoder();

        PapPolicyIdentifier ppi = coder.decode(idString, PapPolicyIdentifier.class);

        assertEquals("MyPolicy", ppi.getGenericIdentifier().getName());
        assertEquals("1.2.6", ppi.getGenericIdentifier().getVersion());

        String idStringBack = coder.encode(ppi);
        assertEquals(idString.replaceAll("\\s+", ""), idStringBack.replaceAll("\\s+", ""));
    }
}
