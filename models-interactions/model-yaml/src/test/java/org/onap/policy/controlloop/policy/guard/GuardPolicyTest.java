/*-
 * ============LICENSE_START=======================================================
 * Copyright (C) 2018 Ericsson. All rights reserved.
 * Modifications Copyright (C) 2018-2019 AT&T Intellectual Property. All rights reserved.
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

package org.onap.policy.controlloop.policy.guard;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.LinkedList;
import java.util.List;

import org.junit.Test;

public class GuardPolicyTest {

    private static final String GUARD_DESCRIPTION = "guard description";
    private static final String GUARD_ID = "guard id";
    private static final String GUARD_NAME = "guard name";

    @Test
    public void testConstructor() {
        GuardPolicy guardPolicy = new GuardPolicy();

        assertNotNull(guardPolicy.getId());
        assertNull(guardPolicy.getName());
        assertNull(guardPolicy.getDescription());
        assertNull(guardPolicy.getMatch_parameters());
        assertNull(guardPolicy.getLimit_constraints());
    }

    @Test
    public void testConstructorString() {
        String id = GUARD_ID;
        GuardPolicy guardPolicy = new GuardPolicy(id);

        assertEquals(id, guardPolicy.getId());
        assertNull(guardPolicy.getName());
        assertNull(guardPolicy.getDescription());
        assertNull(guardPolicy.getMatch_parameters());
        assertNull(guardPolicy.getLimit_constraints());
    }

    @Test
    public void testConstructorStringStringStringMatchParameters() {
        String id = GUARD_ID;
        String name = GUARD_NAME;
        String description = GUARD_DESCRIPTION;
        MatchParameters matchParameters = new MatchParameters();
        List<Constraint> limitConstraints = new LinkedList<>();
        limitConstraints.add(new Constraint());
        GuardPolicy guardPolicy = new GuardPolicy(id, name, description, matchParameters);

        assertNotNull(guardPolicy.getId());
        assertEquals(name, guardPolicy.getName());
        assertEquals(description, guardPolicy.getDescription());
        assertEquals(matchParameters, guardPolicy.getMatch_parameters());
        assertNull(guardPolicy.getLimit_constraints());
    }

    @Test
    public void testConstructorStringMatchParametersList() {
        String name = GUARD_NAME;
        MatchParameters matchParameters = new MatchParameters();
        List<Constraint> limitConstraints = new LinkedList<>();
        limitConstraints.add(new Constraint());
        GuardPolicy guardPolicy = new GuardPolicy(name, matchParameters, limitConstraints);

        assertNotNull(guardPolicy.getId());
        assertEquals(name, guardPolicy.getName());
        assertNull(guardPolicy.getDescription());
        assertEquals(matchParameters, guardPolicy.getMatch_parameters());
        assertEquals(limitConstraints, guardPolicy.getLimit_constraints());
    }

    @Test
    public void testConstructorStringStringMatchParametersList() {
        String name = GUARD_NAME;
        String description = GUARD_DESCRIPTION;
        MatchParameters matchParameters = new MatchParameters();
        List<Constraint> limitConstraints = new LinkedList<>();
        limitConstraints.add(new Constraint());
        GuardPolicy guardPolicy = new GuardPolicy(name, description, matchParameters, limitConstraints);

        assertNotNull(guardPolicy.getId());
        assertEquals(name, guardPolicy.getName());
        assertEquals(description, guardPolicy.getDescription());
        assertEquals(matchParameters, guardPolicy.getMatch_parameters());
        assertEquals(limitConstraints, guardPolicy.getLimit_constraints());
    }

    @Test
    public void testConstructorStringStringStringMatchParametersList() {
        String id = GUARD_ID;
        String name = GUARD_NAME;
        String description = GUARD_DESCRIPTION;
        MatchParameters matchParameters = new MatchParameters();
        List<Constraint> limitConstraints = new LinkedList<>();
        limitConstraints.add(new Constraint());
        GuardPolicy guardPolicy = new GuardPolicy(id, name, description, matchParameters, limitConstraints);

        assertEquals(id, guardPolicy.getId());
        assertEquals(name, guardPolicy.getName());
        assertEquals(description, guardPolicy.getDescription());
        assertEquals(matchParameters, guardPolicy.getMatch_parameters());
        assertEquals(limitConstraints, guardPolicy.getLimit_constraints());
    }

    @Test
    public void testConstructorGuardPolicy() {
        String id = GUARD_ID;
        String name = GUARD_NAME;
        String description = GUARD_DESCRIPTION;
        MatchParameters matchParameters = new MatchParameters();
        List<Constraint> limitConstraints = new LinkedList<>();
        limitConstraints.add(new Constraint());
        GuardPolicy guardPolicy1 = new GuardPolicy(id, name, description, matchParameters, limitConstraints);

        GuardPolicy guardPolicy2 = new GuardPolicy(guardPolicy1);


        assertEquals(id, guardPolicy2.getId());
        assertEquals(name, guardPolicy2.getName());
        assertEquals(description, guardPolicy2.getDescription());
        assertEquals(matchParameters, guardPolicy2.getMatch_parameters());
        assertEquals(limitConstraints, guardPolicy2.getLimit_constraints());
    }

    @Test
    public void testSetAndGetId() {
        String id = GUARD_ID;
        GuardPolicy guardPolicy = new GuardPolicy();
        guardPolicy.setId(id);
        assertEquals(id, guardPolicy.getId());
    }

    @Test
    public void testSetAndGetName() {
        String name = GUARD_NAME;
        GuardPolicy guardPolicy = new GuardPolicy();
        guardPolicy.setName(name);
        assertEquals(name, guardPolicy.getName());
    }

    @Test
    public void testSetAndGetDescription() {
        String description = GUARD_DESCRIPTION;
        GuardPolicy guardPolicy = new GuardPolicy();
        guardPolicy.setDescription(description);
        assertEquals(description, guardPolicy.getDescription());
    }

    @Test
    public void testSetAndGetMatchParameters() {
        MatchParameters matchParameters = new MatchParameters();
        GuardPolicy guardPolicy = new GuardPolicy();
        guardPolicy.setMatch_parameters(matchParameters);
        assertEquals(matchParameters, guardPolicy.getMatch_parameters());
    }

    @Test
    public void testSetAndGetLimitConstraints() {
        LinkedList<Constraint> limitConstraints = new LinkedList<>();
        limitConstraints.add(new Constraint());
        GuardPolicy guardPolicy = new GuardPolicy();
        guardPolicy.setLimit_constraints(limitConstraints);
        assertEquals(limitConstraints, guardPolicy.getLimit_constraints());
    }

    @Test
    public void testIsValid() {
        GuardPolicy guardPolicy = new GuardPolicy();
        assertFalse(guardPolicy.isValid());

        guardPolicy.setName(GUARD_NAME);
        assertTrue(guardPolicy.isValid());

        guardPolicy.setId(null);
        assertFalse(guardPolicy.isValid());
    }

    @Test
    public void testToString() {
        String id = GUARD_ID;
        String name = GUARD_NAME;
        String description = GUARD_DESCRIPTION;
        MatchParameters matchParameters = new MatchParameters();
        List<Constraint> limitConstraints = new LinkedList<>();
        limitConstraints.add(new Constraint());
        GuardPolicy guardPolicy = new GuardPolicy(id, name, description, matchParameters, limitConstraints);

        assertEquals(guardPolicy.toString(), "Policy [id=guard id, name=guard name, description=guard description, "
                + "match_parameters=MatchParameters [controlLoopName=null, actor=null, recipe=null, targets=null], "
                + "limitConstraints=[Constraint [freq_limit_per_target=null, time_window=null, active_time_range=null,"
                + " blacklist=null]]]", guardPolicy.toString());
    }

    @Test
    public void testEquals() {
        final String id = GUARD_ID;
        final String name = GUARD_NAME;
        final String description = GUARD_DESCRIPTION;
        GuardPolicy guardPolicy1 = new GuardPolicy(id);
        GuardPolicy guardPolicy2 = new GuardPolicy();
        assertFalse(guardPolicy1.equals(guardPolicy2));

        guardPolicy2.setId(id);
        assertTrue(guardPolicy1.equals(guardPolicy2));
        assertEquals(guardPolicy1.hashCode(), guardPolicy2.hashCode());

        guardPolicy1.setName(name);
        assertFalse(guardPolicy1.equals(guardPolicy2));
        guardPolicy2.setName(name);
        assertTrue(guardPolicy1.equals(guardPolicy2));
        assertEquals(guardPolicy1.hashCode(), guardPolicy2.hashCode());

        guardPolicy1.setDescription(description);
        assertFalse(guardPolicy1.equals(guardPolicy2));
        guardPolicy2.setDescription(description);
        assertTrue(guardPolicy1.equals(guardPolicy2));
        assertEquals(guardPolicy1.hashCode(), guardPolicy2.hashCode());

        MatchParameters matchParameters = new MatchParameters();
        guardPolicy1.setMatch_parameters(matchParameters);
        assertFalse(guardPolicy1.equals(guardPolicy2));
        guardPolicy2.setMatch_parameters(matchParameters);
        assertTrue(guardPolicy1.equals(guardPolicy2));
        assertEquals(guardPolicy1.hashCode(), guardPolicy2.hashCode());

        LinkedList<Constraint> limitConstraints = new LinkedList<>();
        limitConstraints.add(new Constraint());
        guardPolicy1.setLimit_constraints(limitConstraints);
        assertFalse(guardPolicy1.equals(guardPolicy2));
        guardPolicy2.setLimit_constraints(limitConstraints);
        assertTrue(guardPolicy1.equals(guardPolicy2));
        assertEquals(guardPolicy1.hashCode(), guardPolicy2.hashCode());
    }

    @Test
    public void testEqualsSameObject() {
        GuardPolicy guardPolicy = new GuardPolicy();
        assertTrue(guardPolicy.equals(guardPolicy));
    }

    @Test
    public void testEqualsNull() {
        GuardPolicy guardPolicy = new GuardPolicy();
        assertFalse(guardPolicy.equals(null));
    }

    @Test
    public void testEqualsInstanceOfDiffClass() {
        GuardPolicy guardPolicy = new GuardPolicy();
        assertFalse(guardPolicy.equals(""));
    }
}
