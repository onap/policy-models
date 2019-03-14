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

package org.onap.policy.models.tosca.concepts;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Test;
import org.onap.policy.models.base.PfConceptKey;
import org.onap.policy.models.base.PfReferenceKey;
import org.onap.policy.models.base.PfValidationResult;

/**
 * DAO test for ToscaConstraintLogicalString.
 *
 * @author Liam Fallon (liam.fallon@est.tech)
 */
public class ToscaConstraintLogicalStringTest {

    @Test
    public void testConstraintLogicalStringPojo() {
        assertNotNull(new ToscaConstraintLogicalString());
        assertNotNull(new ToscaConstraintLogicalString(new PfReferenceKey()));
        assertNotNull(new ToscaConstraintLogicalString(new PfReferenceKey(), ToscaConstraintLogicalString.Operation.EQ,
                "Constraint"));

        try {
            new ToscaConstraintLogicalString((PfReferenceKey) null);
            fail("test should throw an exception");
        } catch (Exception exc) {
            assertEquals("key is marked @NonNull but is null", exc.getMessage());
        }

        try {
            new ToscaConstraintLogicalString((ToscaConstraintLogicalString) null);
            fail("test should throw an exception");
        } catch (Exception exc) {
            assertEquals("copyConcept is marked @NonNull but is null", exc.getMessage());
        }

        PfConceptKey tclParentKey = new PfConceptKey("tParentKey", "0.0.1");
        PfReferenceKey tclKey = new PfReferenceKey(tclParentKey, "trigger0");
        ToscaConstraintLogicalString tcl =
                new ToscaConstraintLogicalString(tclKey, ToscaConstraintLogicalString.Operation.EQ, "Constraint");

        try {
            new ToscaConstraintLogicalString(tcl);
            fail("test should throw an exception");
        } catch (Exception exc) {
            assertEquals("cannot copy an immutable constraint", exc.getMessage());
        }

        ToscaConstraintLogicalString tclClone1 = new ToscaConstraintLogicalString();
        try {
            tcl.copyTo(tclClone1);
            fail("test should throw an exception");
        } catch (Exception exc) {
            assertEquals("cannot copy an immutable constraint", exc.getMessage());
        }
        tclClone1 = new ToscaConstraintLogicalString(tclKey, ToscaConstraintLogicalString.Operation.EQ, "Constraint");

        assertEquals(tcl, tclClone1);
        assertEquals(0, tcl.compareTo(tclClone1));

        assertEquals(-1, tcl.compareTo(null));
        assertEquals(0, tcl.compareTo(tcl));
        assertFalse(tcl.compareTo(tcl.getKey()) == 0);

        ToscaConstraintLogicalString differentTcl = new ToscaConstraintLogicalString(new PfReferenceKey(),
                ToscaConstraintLogicalString.Operation.EQ, "Constraint");
        assertFalse(tcl.compareTo(differentTcl) == 0);

        ToscaConstraintLogicalString otherTc =
                new ToscaConstraintLogicalString(tclKey, ToscaConstraintLogicalString.Operation.EQ, "Constraint");
        assertEquals(0, tcl.compareTo(otherTc));

        try {
            tcl.copyTo(null);
            fail("test should throw an exception");
        } catch (Exception exc) {
            assertEquals("target is marked @NonNull but is null", exc.getMessage());
        }

        assertEquals(1, tcl.getKeys().size());
        assertEquals(1, new ToscaConstraintLogicalString().getKeys().size());

        new ToscaConstraintLogicalString().clean();
        tcl.clean();
        assertEquals(tclClone1, tcl);

        assertFalse(new ToscaConstraintLogicalString().validate(new PfValidationResult()).isValid());
        assertTrue(tcl.validate(new PfValidationResult()).isValid());

        try {
            tcl.validate(null);
            fail("test should throw an exception");
        } catch (Exception exc) {
            assertEquals("resultIn is marked @NonNull but is null", exc.getMessage());
        }

        try {
            new ToscaConstraintLogicalString(tclKey, ToscaConstraintLogicalString.Operation.EQ, null)
                    .validate(new PfValidationResult());
            fail("test should throw an exception");
        } catch (Exception exc) {
            assertEquals("compareToString is marked @NonNull but is null", exc.getMessage());
        }

        assertFalse(new ToscaConstraintLogicalString(tclKey, ToscaConstraintLogicalString.Operation.EQ, "")
                .validate(new PfValidationResult()).isValid());
    }
}
