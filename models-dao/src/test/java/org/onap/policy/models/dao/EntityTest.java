/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2016-2018 Ericsson. All rights reserved.
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

package org.onap.policy.models.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.onap.policy.models.base.PfConceptKey;
import org.onap.policy.models.base.PfModelException;
import org.onap.policy.models.dao.DaoParameters;
import org.onap.policy.models.dao.PfDao;
import org.onap.policy.models.dao.PfDaoFactory;

/**
 * JUnit test class.
 */
public class EntityTest {
    private Connection connection;
    private PfDao pfDao;

    @Before
    public void setup() throws Exception {
        Class.forName("org.apache.derby.jdbc.EmbeddedDriver").newInstance();
        connection = DriverManager.getConnection("jdbc:derby:memory:pf_test;create=true");
    }

    @After
    public void teardown() throws Exception {
        connection.close();
        new File("derby.log").delete();
    }

    @Test
    public void testEntityTestSanity() throws PfModelException {
        final DaoParameters daoParameters = new DaoParameters();

        pfDao = new PfDaoFactory().createPfDao(daoParameters);

        try {
            pfDao.init(null);
            fail("Test should throw an exception here");
        } catch (final Exception e) {
            assertEquals("Policy Framework persistence unit parameter not set", e.getMessage());
        }

        try {
            pfDao.init(daoParameters);
            fail("Test should throw an exception here");
        } catch (final Exception e) {
            assertEquals("Policy Framework persistence unit parameter not set", e.getMessage());
        }

        daoParameters.setPluginClass("somewhere.over.the.rainbow");
        daoParameters.setPersistenceUnit("Dorothy");
        try {
            pfDao.init(daoParameters);
            fail("Test should throw an exception here");
        } catch (final Exception e) {
            assertEquals("Creation of Policy Framework persistence unit \"Dorothy\" failed", e.getMessage());
        }
        try {
            pfDao.create(new PfConceptKey());
            fail("Test should throw an exception here");
        } catch (final Exception e) {
            assertEquals("Policy Framework DAO has not been initialized", e.getMessage());
        }
        pfDao.close();
    }

    @Test
    public void testEntityTestAllOpsJpa() throws PfModelException {
        final DaoParameters daoParameters = new DaoParameters();
        daoParameters.setPluginClass("org.onap.policy.models.dao.impl.DefaultPfDao");
        daoParameters.setPersistenceUnit("DaoTest");

        pfDao = new PfDaoFactory().createPfDao(daoParameters);
        pfDao.init(daoParameters);

        testAllOps();
        pfDao.close();
    }

    @Test
    public void testEntityTestBadVals() throws PfModelException {
        final DaoParameters daoParameters = new DaoParameters();
        daoParameters.setPluginClass("org.onap.policy.models.dao.impl.DefaultPfDao");
        daoParameters.setPersistenceUnit("DaoTest");

        pfDao = new PfDaoFactory().createPfDao(daoParameters);
        pfDao.init(daoParameters);

        final PfConceptKey nullKey = null;
        final List<PfConceptKey> nullKeyList = null;
        final List<PfConceptKey> emptyKeyList = new ArrayList<>();

        pfDao.create(nullKey);
        pfDao.createCollection(nullKeyList);
        pfDao.createCollection(emptyKeyList);

        pfDao.delete(nullKey);
        pfDao.deleteCollection(nullKeyList);
        pfDao.deleteCollection(emptyKeyList);
        pfDao.delete(PfConceptKey.class, nullKey);
        pfDao.deleteByConceptKey(PfConceptKey.class, nullKeyList);
        pfDao.deleteByConceptKey(PfConceptKey.class, emptyKeyList);

        pfDao.get(null, nullKey);
        pfDao.getAll(null);
        pfDao.getConcept(null, nullKey);
        pfDao.getConcept(PfConceptKey.class, nullKey);
        pfDao.size(null);

        pfDao.close();
    }

    private void testAllOps() {
        final PfConceptKey aKey0 = new PfConceptKey("A-KEY0", "0.0.1");
        final PfConceptKey aKey1 = new PfConceptKey("A-KEY1", "0.0.1");
        final PfConceptKey aKey2 = new PfConceptKey("A-KEY2", "0.0.1");
        final DummyEntity dummyEntity0 = new DummyEntity(aKey0, 123.45);
        final DummyEntity dummyEntity1 = new DummyEntity(aKey1, 3.14);
        final DummyEntity dummyEntity2 = new DummyEntity(aKey2, -654.321);

        pfDao.create(dummyEntity0);

        final DummyEntity dummyEntityBack0 = pfDao.get(DummyEntity.class, aKey0);
        assertTrue(dummyEntity0.equals(dummyEntityBack0));

        final DummyEntity dummyEntityBackNull = pfDao.get(DummyEntity.class, PfConceptKey.getNullKey());
        assertNull(dummyEntityBackNull);

        final DummyEntity dummyEntityBack1 = pfDao.getConcept(DummyEntity.class, aKey0);
        assertTrue(dummyEntityBack0.equals(dummyEntityBack1));

        final DummyEntity dummyEntityBack2 = pfDao.getConcept(DummyEntity.class, new PfConceptKey("A-KEY3", "0.0.1"));
        assertNull(dummyEntityBack2);

        final Set<DummyEntity> dummyEntitySetIn = new TreeSet<DummyEntity>();
        dummyEntitySetIn.add(dummyEntity1);
        dummyEntitySetIn.add(dummyEntity2);

        pfDao.createCollection(dummyEntitySetIn);

        Set<DummyEntity> dummyEntitySetOut = new TreeSet<DummyEntity>(pfDao.getAll(DummyEntity.class));

        dummyEntitySetIn.add(dummyEntity0);
        assertTrue(dummyEntitySetIn.equals(dummyEntitySetOut));

        pfDao.delete(dummyEntity1);
        dummyEntitySetIn.remove(dummyEntity1);
        dummyEntitySetOut = new TreeSet<DummyEntity>(pfDao.getAll(DummyEntity.class));
        assertTrue(dummyEntitySetIn.equals(dummyEntitySetOut));

        pfDao.deleteCollection(dummyEntitySetIn);
        dummyEntitySetOut = new TreeSet<DummyEntity>(pfDao.getAll(DummyEntity.class));
        assertEquals(0, dummyEntitySetOut.size());

        dummyEntitySetIn.add(dummyEntity0);
        dummyEntitySetIn.add(dummyEntity1);
        dummyEntitySetIn.add(dummyEntity0);
        pfDao.createCollection(dummyEntitySetIn);
        dummyEntitySetOut = new TreeSet<DummyEntity>(pfDao.getAll(DummyEntity.class));
        assertTrue(dummyEntitySetIn.equals(dummyEntitySetOut));

        pfDao.delete(DummyEntity.class, aKey0);
        dummyEntitySetOut = new TreeSet<DummyEntity>(pfDao.getAll(DummyEntity.class));
        assertEquals(2, dummyEntitySetOut.size());
        assertEquals(2, pfDao.size(DummyEntity.class));

        final Set<PfConceptKey> keySetIn = new TreeSet<PfConceptKey>();
        keySetIn.add(aKey1);
        keySetIn.add(aKey2);

        final int deletedCount = pfDao.deleteByConceptKey(DummyEntity.class, keySetIn);
        assertEquals(2, deletedCount);

        dummyEntitySetOut = new TreeSet<DummyEntity>(pfDao.getAll(DummyEntity.class));
        assertEquals(0, dummyEntitySetOut.size());

        dummyEntitySetIn.add(dummyEntity0);
        dummyEntitySetIn.add(dummyEntity1);
        dummyEntitySetIn.add(dummyEntity0);
        pfDao.createCollection(dummyEntitySetIn);
        dummyEntitySetOut = new TreeSet<DummyEntity>(pfDao.getAll(DummyEntity.class));
        assertTrue(dummyEntitySetIn.equals(dummyEntitySetOut));

        pfDao.deleteAll(DummyEntity.class);
        assertEquals(0, pfDao.size(DummyEntity.class));

        pfDao.create(new DummyEntity(new PfConceptKey("Owner0", "0.0.1"), 100.0));
        pfDao.create(new DummyEntity(new PfConceptKey("Owner0", "0.0.2"), 101.0));
        pfDao.create(new DummyEntity(new PfConceptKey("Owner0", "0.0.3"), 102.0));
        pfDao.create(new DummyEntity(new PfConceptKey("Owner0", "0.0.4"), 103.0));
        pfDao.create(new DummyEntity(new PfConceptKey("Owner0", "0.0.5"), 104.0));
        pfDao.create(new DummyEntity(new PfConceptKey("Owner1", "0.0.1"), 105.0));
        pfDao.create(new DummyEntity(new PfConceptKey("Owner1", "0.0.2"), 106.0));
        pfDao.create(new DummyEntity(new PfConceptKey("Owner1", "0.0.3"), 107.0));
        pfDao.create(new DummyEntity(new PfConceptKey("Owner2", "0.0.1"), 108.0));
        pfDao.create(new DummyEntity(new PfConceptKey("Owner2", "0.0.2"), 109.0));
        pfDao.create(new DummyEntity(new PfConceptKey("Owner3", "0.0.1"), 110.0));
        pfDao.create(new DummyEntity(new PfConceptKey("Owner4", "0.0.1"), 111.0));
        pfDao.create(new DummyEntity(new PfConceptKey("Owner5", "0.0.1"), 112.0));
        pfDao.create(new DummyEntity(new PfConceptKey("Owner5", "0.0.2"), 113.0));
        pfDao.create(new DummyEntity(new PfConceptKey("Owner5", "0.0.3"), 114.0));
        pfDao.create(new DummyEntity(new PfConceptKey("Owner5", "0.0.4"), 115.0));

        TreeSet<DummyEntity> testEntitySetOut = new TreeSet<DummyEntity>(pfDao.getAll(DummyEntity.class));
        assertEquals(16, testEntitySetOut.size());

        assertNotNull(pfDao.get(DummyEntity.class, new PfConceptKey("Owner0", "0.0.1")));
        assertNotNull(pfDao.getConcept(DummyEntity.class, new PfConceptKey("Owner0", "0.0.2")));
        assertNull(pfDao.get(DummyEntity.class, new PfConceptKey("Owner0", "0.0.99")));
        assertEquals(101.0, pfDao.getConcept(DummyEntity.class, new PfConceptKey("Owner0", "0.0.2")).getDoubleValue(),
                0.001);
        pfDao.delete(DummyEntity.class, new PfConceptKey("Owner0", "0.0.5"));

        final Set<PfConceptKey> rKeySetIn = new TreeSet<PfConceptKey>();
        rKeySetIn.add(new PfConceptKey("Owner0", "0.0.9"));
        rKeySetIn.add(new PfConceptKey("Owner1", "0.0.9"));

        final int deletedRCount = pfDao.deleteByConceptKey(DummyEntity.class, rKeySetIn);
        assertEquals(0, deletedRCount);

        pfDao.update(new DummyEntity(new PfConceptKey("Owner0", "0.0.6"), 120.0));
    }
}
