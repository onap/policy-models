/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2019 Nordix Foundation.
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
 *
 * SPDX-License-Identifier: Apache-2.0
 * ============LICENSE_END=========================================================
 */

package org.onap.policy.models.tosca.simple.concepts;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Test;
import org.onap.policy.models.base.PfConceptKey;
import org.onap.policy.models.base.PfReferenceKey;
import org.onap.policy.models.base.PfValidationResult;
import org.onap.policy.models.tosca.simple.concepts.JpaToscaConstraintLogicalKey;

/**
 * DAO test for ToscaConstraintLogicalKey.
 *
 * @author Liam Fallon (liam.fallon@est.tech)
 */
public class JpaToscaConstraintLogicalKeyTest {

    @Test
    public void testConstraintLogicalKeyPojo() {
        assertNotNull(new JpaToscaConstraintLogicalKey());
        assertNotNull(new JpaToscaConstraintLogicalKey(new PfReferenceKey()));
        assertNotNull(new JpaToscaConstraintLogicalKey(new PfReferenceKey(), JpaToscaConstraintLogicalKey.Operation.EQ,
                new PfConceptKey()));

        try {
            new JpaToscaConstraintLogicalKey((PfReferenceKey) null);
            fail("test should throw an exception");
        } catch (Exception exc) {
            assertEquals("key is marked @NonNull but is null", exc.getMessage());
        }

        try {
            new JpaToscaConstraintLogicalKey((JpaToscaConstraintLogicalKey) null);
            fail("test should throw an exception");
        } catch (Exception exc) {
            assertEquals("copyConcept is marked @NonNull but is null", exc.getMessage());
        }

        PfConceptKey tclParentKey = new PfConceptKey("tParentKey", "0.0.1");
        PfReferenceKey tclKey = new PfReferenceKey(tclParentKey, "trigger0");
        PfConceptKey constraintKey = new PfConceptKey("tParentKey", "0.0.1");
        JpaToscaConstraintLogicalKey tcl =
                new JpaToscaConstraintLogicalKey(tclKey, JpaToscaConstraintLogicalKey.Operation.EQ, constraintKey);

        try {
            new JpaToscaConstraintLogicalKey(tcl);
            fail("test should throw an exception");
        } catch (Exception exc) {
            assertEquals("cannot copy an immutable constraint", exc.getMessage());
        }

        JpaToscaConstraintLogicalKey tclClone1 = new JpaToscaConstraintLogicalKey();
        try {
            tcl.copyTo(tclClone1);
            fail("test should throw an exception");
        } catch (Exception exc) {
            assertEquals("cannot copy an immutable constraint", exc.getMessage());
        }
        tclClone1 = new JpaToscaConstraintLogicalKey(tclKey, JpaToscaConstraintLogicalKey.Operation.EQ, constraintKey);

        assertEquals(tcl, tclClone1);
        assertEquals(0, tcl.compareTo(tclClone1));

        assertEquals(-1, tcl.compareTo(null));
        assertEquals(0, tcl.compareTo(tcl));
        assertFalse(tcl.compareTo(tcl.getKey()) == 0);

        JpaToscaConstraintLogicalKey differentTcl = new JpaToscaConstraintLogicalKey(new PfReferenceKey(),
                JpaToscaConstraintLogicalKey.Operation.EQ, constraintKey);
        assertFalse(tcl.compareTo(differentTcl) == 0);

        JpaToscaConstraintLogicalKey otherTc =
                new JpaToscaConstraintLogicalKey(tclKey, JpaToscaConstraintLogicalKey.Operation.EQ, constraintKey);
        assertEquals(0, tcl.compareTo(otherTc));

        try {
            tcl.copyTo(null);
            fail("test should throw an exception");
        } catch (Exception exc) {
            assertEquals("target is marked @NonNull but is null", exc.getMessage());
        }

        assertEquals(2, tcl.getKeys().size());
        assertEquals(2, new JpaToscaConstraintLogicalKey().getKeys().size());

        new JpaToscaConstraintLogicalKey().clean();
        tcl.clean();
        assertEquals(tclClone1, tcl);

        assertFalse(new JpaToscaConstraintLogicalKey().validate(new PfValidationResult()).isValid());
        assertTrue(tcl.validate(new PfValidationResult()).isValid());

        try {
            tcl.validate(null);
            fail("test should throw an exception");
        } catch (Exception exc) {
            assertEquals("resultIn is marked @NonNull but is null", exc.getMessage());
        }

        try {
            new JpaToscaConstraintLogicalKey(tclKey, JpaToscaConstraintLogicalKey.Operation.EQ, null)
                    .validate(new PfValidationResult());
            fail("test should throw an exception");
        } catch (Exception exc) {
            assertEquals("compareToKey is marked @NonNull but is null", exc.getMessage());
        }

        assertFalse(
                new JpaToscaConstraintLogicalKey(tclKey, JpaToscaConstraintLogicalKey.Operation.EQ, new PfConceptKey())
                        .validate(new PfValidationResult()).isValid());
    }
}
