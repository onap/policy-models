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
import org.onap.policy.models.tosca.simple.concepts.ToscaConstraintLogical;
import org.onap.policy.models.tosca.simple.concepts.testconcepts.DummyToscaConstraint;

/**
 * DAO test for ToscaConstraintLogical.
 *
 * @author Liam Fallon (liam.fallon@est.tech)
 */
public class ToscaConstraintLogicalTest {

    @Test
    public void testConstraintLogicalPojo() {
        assertNotNull(new ToscaConstraintLogical());
        assertNotNull(new ToscaConstraintLogical(new PfReferenceKey()));

        try {
            new ToscaConstraintLogical((PfReferenceKey) null);
            fail("test should throw an exception");
        } catch (Exception exc) {
            assertEquals("key is marked @NonNull but is null", exc.getMessage());
        }

        try {
            new ToscaConstraintLogical((ToscaConstraintLogical) null);
            fail("test should throw an exception");
        } catch (Exception exc) {
            assertEquals("copyConcept is marked @NonNull but is null", exc.getMessage());
        }

        PfConceptKey tclParentKey = new PfConceptKey("tParentKey", "0.0.1");
        PfReferenceKey tclKey = new PfReferenceKey(tclParentKey, "trigger0");
        ToscaConstraintLogical tcl = new ToscaConstraintLogical(tclKey, ToscaConstraintLogical.Operation.EQ);

        try {
            new ToscaConstraintLogical(tcl);
            fail("test should throw an exception");
        } catch (Exception exc) {
            assertEquals("cannot copy an immutable constraint", exc.getMessage());
        }

        ToscaConstraintLogical tclClone1 = new ToscaConstraintLogical();
        try {
            tcl.copyTo(tclClone1);
            fail("test should throw an exception");
        } catch (Exception exc) {
            assertEquals("cannot copy an immutable constraint", exc.getMessage());
        }
        tclClone1 = new ToscaConstraintLogical(tclKey, ToscaConstraintLogical.Operation.EQ);

        assertEquals(tcl, tclClone1);
        assertEquals(0, tcl.compareTo(tclClone1));

        assertEquals(-1, tcl.compareTo(null));
        assertEquals(0, tcl.compareTo(tcl));
        assertFalse(tcl.compareTo(tcl.getKey()) == 0);

        ToscaConstraintLogical differentTcl =
                new ToscaConstraintLogical(new PfReferenceKey(), ToscaConstraintLogical.Operation.EQ);
        assertFalse(tcl.compareTo(differentTcl) == 0);

        ToscaConstraintLogical otherTc = new ToscaConstraintLogical(tclKey, ToscaConstraintLogical.Operation.EQ);
        assertEquals(0, tcl.compareTo(otherTc));

        try {
            tcl.copyTo(null);
            fail("test should throw an exception");
        } catch (Exception exc) {
            assertEquals("target is marked @NonNull but is null", exc.getMessage());
        }

        assertEquals(1, tcl.getKeys().size());
        assertEquals(1, new ToscaConstraintLogical().getKeys().size());

        ToscaConstraintLogical tclClone0 = new ToscaConstraintLogical();
        new ToscaConstraintLogical().clean();
        tcl.clean();
        assertEquals(tclClone0, tcl);

        assertFalse(new ToscaConstraintLogical().validate(new PfValidationResult()).isValid());
        assertTrue(tcl.validate(new PfValidationResult()).isValid());

        try {
            tcl.validate(null);
            fail("test should throw an exception");
        } catch (Exception exc) {
            assertEquals("resultIn is marked @NonNull but is null", exc.getMessage());
        }

        DummyToscaConstraint dtc = new DummyToscaConstraint();
        try {
            new DummyToscaConstraint(dtc);
            fail("test should throw an exception");
        } catch (Exception exc) {
            assertEquals("cannot copy an immutable constraint", exc.getMessage());
        }

        try {
            new DummyToscaConstraint((PfReferenceKey)null);
            fail("test should throw an exception");
        } catch (Exception exc) {
            assertEquals("key is marked @NonNull but is null", exc.getMessage());
        }

        try {
            new DummyToscaConstraint((DummyToscaConstraint)null);
            fail("test should throw an exception");
        } catch (Exception exc) {
            assertEquals("copyConcept is marked @NonNull but is null", exc.getMessage());
        }

        DummyToscaConstraint dtcClone = new DummyToscaConstraint();

        assertEquals(dtc, dtcClone);
        assertEquals(dtc, dtc);
        assertEquals(0, dtc.compareTo(dtcClone));
        assertEquals(0, dtc.compareTo(dtc));
        assertEquals(-1, dtc.compareTo(null));
        assertEquals(0, dtc.compareTo(dtcClone));
        assertFalse(dtc.compareTo(dtcClone.getKey()) == 0);

        try {
            dtc.copyTo(null);
            fail("test should throw an exception");
        } catch (Exception exc) {
            assertEquals("target is marked @NonNull but is null", exc.getMessage());
        }

        try {
            dtc.copyTo(dtcClone);
            fail("target should throw an exception");
        } catch (Exception exc) {
            assertEquals("cannot copy an immutable constraint", exc.getMessage());
        }
    }
}
