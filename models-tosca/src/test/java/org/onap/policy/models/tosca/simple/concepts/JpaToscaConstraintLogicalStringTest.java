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
import org.onap.policy.models.tosca.simple.concepts.JpaToscaConstraintLogicalString;

/**
 * DAO test for ToscaConstraintLogicalString.
 *
 * @author Liam Fallon (liam.fallon@est.tech)
 */
public class JpaToscaConstraintLogicalStringTest {

    @Test
    public void testConstraintLogicalStringPojo() {
        assertNotNull(new JpaToscaConstraintLogicalString());
        assertNotNull(new JpaToscaConstraintLogicalString(new PfReferenceKey()));
        assertNotNull(new JpaToscaConstraintLogicalString(new PfReferenceKey(),
                JpaToscaConstraintLogicalString.Operation.EQ, "Constraint"));

        try {
            new JpaToscaConstraintLogicalString((PfReferenceKey) null);
            fail("test should throw an exception");
        } catch (Exception exc) {
            assertEquals("key is marked @NonNull but is null", exc.getMessage());
        }

        try {
            new JpaToscaConstraintLogicalString((JpaToscaConstraintLogicalString) null);
            fail("test should throw an exception");
        } catch (Exception exc) {
            assertEquals("copyConcept is marked @NonNull but is null", exc.getMessage());
        }

        PfConceptKey tclParentKey = new PfConceptKey("tParentKey", "0.0.1");
        PfReferenceKey tclKey = new PfReferenceKey(tclParentKey, "trigger0");
        JpaToscaConstraintLogicalString tcl =
                new JpaToscaConstraintLogicalString(tclKey, JpaToscaConstraintLogicalString.Operation.EQ, "Constraint");

        try {
            new JpaToscaConstraintLogicalString(tcl);
            fail("test should throw an exception");
        } catch (Exception exc) {
            assertEquals("cannot copy an immutable constraint", exc.getMessage());
        }

        JpaToscaConstraintLogicalString tclClone1 = new JpaToscaConstraintLogicalString();
        try {
            tcl.copyTo(tclClone1);
            fail("test should throw an exception");
        } catch (Exception exc) {
            assertEquals("cannot copy an immutable constraint", exc.getMessage());
        }
        tclClone1 =
                new JpaToscaConstraintLogicalString(tclKey, JpaToscaConstraintLogicalString.Operation.EQ, "Constraint");

        assertEquals(tcl, tclClone1);
        assertEquals(0, tcl.compareTo(tclClone1));

        assertEquals(-1, tcl.compareTo(null));
        assertEquals(0, tcl.compareTo(tcl));
        assertFalse(tcl.compareTo(tcl.getKey()) == 0);

        JpaToscaConstraintLogicalString differentTcl = new JpaToscaConstraintLogicalString(new PfReferenceKey(),
                JpaToscaConstraintLogicalString.Operation.EQ, "Constraint");
        assertFalse(tcl.compareTo(differentTcl) == 0);

        JpaToscaConstraintLogicalString otherTc =
                new JpaToscaConstraintLogicalString(tclKey, JpaToscaConstraintLogicalString.Operation.EQ, "Constraint");
        assertEquals(0, tcl.compareTo(otherTc));

        try {
            tcl.copyTo(null);
            fail("test should throw an exception");
        } catch (Exception exc) {
            assertEquals("target is marked @NonNull but is null", exc.getMessage());
        }

        assertEquals(1, tcl.getKeys().size());
        assertEquals(1, new JpaToscaConstraintLogicalString().getKeys().size());

        new JpaToscaConstraintLogicalString().clean();
        tcl.clean();
        assertEquals(tclClone1, tcl);

        assertFalse(new JpaToscaConstraintLogicalString().validate(new PfValidationResult()).isValid());
        assertTrue(tcl.validate(new PfValidationResult()).isValid());

        try {
            tcl.validate(null);
            fail("test should throw an exception");
        } catch (Exception exc) {
            assertEquals("resultIn is marked @NonNull but is null", exc.getMessage());
        }

        try {
            new JpaToscaConstraintLogicalString(tclKey, JpaToscaConstraintLogicalString.Operation.EQ, null)
                    .validate(new PfValidationResult());
            fail("test should throw an exception");
        } catch (Exception exc) {
            assertEquals("compareToString is marked @NonNull but is null", exc.getMessage());
        }

        assertFalse(new JpaToscaConstraintLogicalString(tclKey, JpaToscaConstraintLogicalString.Operation.EQ, "")
                .validate(new PfValidationResult()).isValid());
    }
}
