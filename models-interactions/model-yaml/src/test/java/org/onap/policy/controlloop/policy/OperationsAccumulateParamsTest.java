/*-
 * ============LICENSE_START=======================================================
 * Copyright (C) 2018 Ericsson. All rights reserved.
 * Modifications Copyright (C) 2018 AT&T Intellectual Property. All rights reserved.
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

package org.onap.policy.controlloop.policy;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class OperationsAccumulateParamsTest {

    @Test
    public void testConstructor() {
        OperationsAccumulateParams operationsAccumulateParams = new OperationsAccumulateParams();
        assertNull(operationsAccumulateParams.getPeriod());
        assertNull(operationsAccumulateParams.getLimit());
    }

    @Test
    public void testConstructorOperationsAccumulateParams() {
        String period = "15m";
        Integer limit = 10;
        OperationsAccumulateParams operationsAccumulateParams1 = 
                        new OperationsAccumulateParams(period, limit);
        OperationsAccumulateParams operationsAccumulateParams2 = 
                        new OperationsAccumulateParams(operationsAccumulateParams1);
        assertEquals(period, operationsAccumulateParams1.getPeriod());
        assertEquals(limit, operationsAccumulateParams2.getLimit());
    }

    @Test
    public void testOperationsAccumulateParamsStringInteger() {
        String period = "15m";
        Integer limit = 10;
        OperationsAccumulateParams operationsAccumulateParams = new OperationsAccumulateParams(period, limit);
        assertEquals(period, operationsAccumulateParams.getPeriod());
        assertEquals(limit, operationsAccumulateParams.getLimit());
    }

    @Test
    public void testSetAndGetPeriod() {
        String period = "15m";
        OperationsAccumulateParams operationsAccumulateParams = new OperationsAccumulateParams();
        operationsAccumulateParams.setPeriod(period);
        assertEquals(period, operationsAccumulateParams.getPeriod());
    }

    @Test
    public void testSetLimit() {
        Integer limit = 10;
        OperationsAccumulateParams operationsAccumulateParams = new OperationsAccumulateParams();
        operationsAccumulateParams.setLimit(limit);
        assertEquals(limit, operationsAccumulateParams.getLimit());
    }

    @Test
    public void testToString() {
        String period = "15m";
        Integer limit = 10;
        OperationsAccumulateParams operationsAccumulateParams = new OperationsAccumulateParams(period, limit);
        assertEquals("OperationsAccumulateParams [period=15m, limit=10]", operationsAccumulateParams.toString());
    }

    @Test
    public void testEqualsAndHashCode() {
        final String period = "15m";
        final Integer limit = 10;
        OperationsAccumulateParams operationsAccumulateParams1 = new OperationsAccumulateParams();
        OperationsAccumulateParams operationsAccumulateParams2 = new OperationsAccumulateParams();

        assertTrue(operationsAccumulateParams1.equals(operationsAccumulateParams2));

        operationsAccumulateParams1.setPeriod(period);
        assertFalse(operationsAccumulateParams1.equals(operationsAccumulateParams2));
        operationsAccumulateParams2.setPeriod(period);
        assertTrue(operationsAccumulateParams1.equals(operationsAccumulateParams2));
        assertEquals(operationsAccumulateParams1.hashCode(), operationsAccumulateParams2.hashCode());

        operationsAccumulateParams1.setLimit(limit);;
        assertFalse(operationsAccumulateParams1.equals(operationsAccumulateParams2));
        operationsAccumulateParams2.setLimit(limit);
        assertTrue(operationsAccumulateParams1.equals(operationsAccumulateParams2));
        assertEquals(operationsAccumulateParams1.hashCode(), operationsAccumulateParams2.hashCode());
    }


    @Test
    public void testEqualsSameObject() {
        OperationsAccumulateParams operationsAccumulateParams = new OperationsAccumulateParams();
        assertTrue(operationsAccumulateParams.equals(operationsAccumulateParams));
    }

    @Test
    public void testEqualsNull() {
        OperationsAccumulateParams operationsAccumulateParams = new OperationsAccumulateParams();
        assertFalse(operationsAccumulateParams.equals(null));
    }

    @Test
    public void testEqualsInstanceOfDiffClass() {
        OperationsAccumulateParams operationsAccumulateParams = new OperationsAccumulateParams();
        assertFalse(operationsAccumulateParams.equals(""));
    }

}
