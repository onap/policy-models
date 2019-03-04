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
import java.util.UUID;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.onap.policy.models.base.PfConceptKey;
import org.onap.policy.models.base.PfModelException;
import org.onap.policy.models.base.PfReferenceKey;
import org.onap.policy.models.dao.DaoParameters;
import org.onap.policy.models.dao.PfDao;
import org.onap.policy.models.dao.PfDaoFactory;
import org.onap.policy.models.dao.impl.DefaultPfDao;

/**
 * JUnit test class.
 */
public class EntityTest {
    private Connection connection;
    private PfDao pfDao;

    @Before
    public void setup() throws Exception {
        connection = DriverManager.getConnection("jdbc:h2:mem:test");
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
        daoParameters.setPluginClass(DefaultPfDao.class.getCanonicalName());
        daoParameters.setPersistenceUnit("DaoTest");

        pfDao = new PfDaoFactory().createPfDao(daoParameters);
        pfDao.init(daoParameters);

        testAllOps();
        pfDao.close();
    }

    @Test
    public void testEntityTestBadVals() throws PfModelException {
        final DaoParameters daoParameters = new DaoParameters();
        daoParameters.setPluginClass(DefaultPfDao.class.getCanonicalName());
        daoParameters.setPersistenceUnit("DaoTest");

        pfDao = new PfDaoFactory().createPfDao(daoParameters);
        pfDao.init(daoParameters);

        final PfConceptKey nullKey = null;
        final PfReferenceKey nullRefKey = null;
        final List<PfConceptKey> nullKeyList = null;
        final List<PfConceptKey> emptyKeyList = new ArrayList<>();
        final List<PfReferenceKey> nullRKeyList = null;
        final List<PfReferenceKey> emptyRKeyList = new ArrayList<>();

        pfDao.create(nullKey);
        pfDao.createCollection(nullKeyList);
        pfDao.createCollection(emptyKeyList);

        pfDao.delete(nullKey);
        pfDao.deleteCollection(nullKeyList);
        pfDao.deleteCollection(emptyKeyList);
        pfDao.delete(PfConceptKey.class, nullKey);
        pfDao.delete(PfReferenceKey.class, nullRefKey);
        pfDao.deleteByConceptKey(PfConceptKey.class, nullKeyList);
        pfDao.deleteByConceptKey(PfConceptKey.class, emptyKeyList);
        pfDao.deleteByReferenceKey(PfReferenceKey.class, nullRKeyList);
        pfDao.deleteByReferenceKey(PfReferenceKey.class, emptyRKeyList);

        pfDao.get(null, nullKey);
        pfDao.get(null, nullRefKey);
        pfDao.getAll(null);
        pfDao.getAll(null, nullKey);
        pfDao.getConcept(null, nullKey);
        pfDao.getConcept(PfConceptKey.class, nullKey);
        pfDao.getConcept(null, nullRefKey);
        pfDao.getConcept(PfReferenceKey.class, nullRefKey);
        pfDao.size(null);

        pfDao.close();
    }

    private void testAllOps() {
        final PfConceptKey aKey0 = new PfConceptKey("A-KEY0", "0.0.1");
        final PfConceptKey aKey1 = new PfConceptKey("A-KEY1", "0.0.1");
        final PfConceptKey aKey2 = new PfConceptKey("A-KEY2", "0.0.1");
        final DummyConceptEntity keyInfo0 = new DummyConceptEntity(aKey0,
                UUID.fromString("00000000-0000-0000-0000-000000000000"), "key description 0");
        final DummyConceptEntity keyInfo1 = new DummyConceptEntity(aKey1,
                UUID.fromString("00000000-0000-0000-0000-000000000001"), "key description 1");
        final DummyConceptEntity keyInfo2 = new DummyConceptEntity(aKey2,
                UUID.fromString("00000000-0000-0000-0000-000000000002"), "key description 2");

        pfDao.create(keyInfo0);

        final DummyConceptEntity keyInfoBack0 = pfDao.get(DummyConceptEntity.class, aKey0);
        assertTrue(keyInfo0.equals(keyInfoBack0));

        final DummyConceptEntity keyInfoBackNull = pfDao.get(DummyConceptEntity.class, PfConceptKey.getNullKey());
        assertNull(keyInfoBackNull);

        final DummyConceptEntity keyInfoBack1 = pfDao.getConcept(DummyConceptEntity.class, aKey0);
        assertTrue(keyInfoBack0.equals(keyInfoBack1));

        final DummyConceptEntity keyInfoBack2 =
                pfDao.getConcept(DummyConceptEntity.class, new PfConceptKey("A-KEY3", "0.0.1"));
        assertNull(keyInfoBack2);

        final Set<DummyConceptEntity> keyInfoSetIn = new TreeSet<DummyConceptEntity>();
        keyInfoSetIn.add(keyInfo1);
        keyInfoSetIn.add(keyInfo2);

        pfDao.createCollection(keyInfoSetIn);

        Set<DummyConceptEntity> keyInfoSetOut = new TreeSet<DummyConceptEntity>(pfDao.getAll(DummyConceptEntity.class));

        keyInfoSetIn.add(keyInfo0);
        assertTrue(keyInfoSetIn.equals(keyInfoSetOut));

        pfDao.delete(keyInfo1);
        keyInfoSetIn.remove(keyInfo1);
        keyInfoSetOut = new TreeSet<DummyConceptEntity>(pfDao.getAll(DummyConceptEntity.class));
        assertTrue(keyInfoSetIn.equals(keyInfoSetOut));

        pfDao.deleteCollection(keyInfoSetIn);
        keyInfoSetOut = new TreeSet<DummyConceptEntity>(pfDao.getAll(DummyConceptEntity.class));
        assertEquals(0, keyInfoSetOut.size());

        keyInfoSetIn.add(keyInfo0);
        keyInfoSetIn.add(keyInfo1);
        keyInfoSetIn.add(keyInfo0);
        pfDao.createCollection(keyInfoSetIn);
        keyInfoSetOut = new TreeSet<DummyConceptEntity>(pfDao.getAll(DummyConceptEntity.class));
        assertTrue(keyInfoSetIn.equals(keyInfoSetOut));

        pfDao.delete(DummyConceptEntity.class, aKey0);
        keyInfoSetOut = new TreeSet<DummyConceptEntity>(pfDao.getAll(DummyConceptEntity.class));
        assertEquals(2, keyInfoSetOut.size());
        assertEquals(2, pfDao.size(DummyConceptEntity.class));

        final Set<PfConceptKey> keySetIn = new TreeSet<PfConceptKey>();
        keySetIn.add(aKey1);
        keySetIn.add(aKey2);

        final int deletedCount = pfDao.deleteByConceptKey(DummyConceptEntity.class, keySetIn);
        assertEquals(2, deletedCount);

        keyInfoSetOut = new TreeSet<DummyConceptEntity>(pfDao.getAll(DummyConceptEntity.class));
        assertEquals(0, keyInfoSetOut.size());

        keyInfoSetIn.add(keyInfo0);
        keyInfoSetIn.add(keyInfo1);
        keyInfoSetIn.add(keyInfo0);
        pfDao.createCollection(keyInfoSetIn);
        keyInfoSetOut = new TreeSet<DummyConceptEntity>(pfDao.getAll(DummyConceptEntity.class));
        assertTrue(keyInfoSetIn.equals(keyInfoSetOut));

        pfDao.deleteAll(DummyConceptEntity.class);
        assertEquals(0, pfDao.size(DummyConceptEntity.class));

        final PfConceptKey owner0Key = new PfConceptKey("Owner0", "0.0.1");
        final PfConceptKey owner1Key = new PfConceptKey("Owner1", "0.0.1");
        final PfConceptKey owner2Key = new PfConceptKey("Owner2", "0.0.1");
        final PfConceptKey owner3Key = new PfConceptKey("Owner3", "0.0.1");
        final PfConceptKey owner4Key = new PfConceptKey("Owner4", "0.0.1");
        final PfConceptKey owner5Key = new PfConceptKey("Owner5", "0.0.1");

        pfDao.create(new DummyReferenceEntity(new PfReferenceKey(owner0Key, "Entity0"), 100.0));
        pfDao.create(new DummyReferenceEntity(new PfReferenceKey(owner0Key, "Entity1"), 101.0));
        pfDao.create(new DummyReferenceEntity(new PfReferenceKey(owner0Key, "Entity2"), 102.0));
        pfDao.create(new DummyReferenceEntity(new PfReferenceKey(owner0Key, "Entity3"), 103.0));
        pfDao.create(new DummyReferenceEntity(new PfReferenceKey(owner0Key, "Entity4"), 104.0));
        pfDao.create(new DummyReferenceEntity(new PfReferenceKey(owner1Key, "Entity5"), 105.0));
        pfDao.create(new DummyReferenceEntity(new PfReferenceKey(owner1Key, "Entity6"), 106.0));
        pfDao.create(new DummyReferenceEntity(new PfReferenceKey(owner1Key, "Entity7"), 107.0));
        pfDao.create(new DummyReferenceEntity(new PfReferenceKey(owner2Key, "Entity8"), 108.0));
        pfDao.create(new DummyReferenceEntity(new PfReferenceKey(owner2Key, "Entity9"), 109.0));
        pfDao.create(new DummyReferenceEntity(new PfReferenceKey(owner3Key, "EntityA"), 110.0));
        pfDao.create(new DummyReferenceEntity(new PfReferenceKey(owner4Key, "EntityB"), 111.0));
        pfDao.create(new DummyReferenceEntity(new PfReferenceKey(owner5Key, "EntityC"), 112.0));
        pfDao.create(new DummyReferenceEntity(new PfReferenceKey(owner5Key, "EntityD"), 113.0));
        pfDao.create(new DummyReferenceEntity(new PfReferenceKey(owner5Key, "EntityE"), 114.0));
        pfDao.create(new DummyReferenceEntity(new PfReferenceKey(owner5Key, "EntityF"), 115.0));

        TreeSet<DummyReferenceEntity> testEntitySetOut =
                new TreeSet<DummyReferenceEntity>(pfDao.getAll(DummyReferenceEntity.class));
        assertEquals(16, testEntitySetOut.size());

        testEntitySetOut = new TreeSet<DummyReferenceEntity>(pfDao.getAll(DummyReferenceEntity.class, owner0Key));
        assertEquals(5, testEntitySetOut.size());

        testEntitySetOut = new TreeSet<DummyReferenceEntity>(pfDao.getAll(DummyReferenceEntity.class, owner1Key));
        assertEquals(3, testEntitySetOut.size());

        testEntitySetOut = new TreeSet<DummyReferenceEntity>(pfDao.getAll(DummyReferenceEntity.class, owner2Key));
        assertEquals(2, testEntitySetOut.size());

        testEntitySetOut = new TreeSet<DummyReferenceEntity>(pfDao.getAll(DummyReferenceEntity.class, owner3Key));
        assertEquals(1, testEntitySetOut.size());

        testEntitySetOut = new TreeSet<DummyReferenceEntity>(pfDao.getAll(DummyReferenceEntity.class, owner4Key));
        assertEquals(1, testEntitySetOut.size());

        testEntitySetOut = new TreeSet<DummyReferenceEntity>(pfDao.getAll(DummyReferenceEntity.class, owner5Key));
        assertEquals(4, testEntitySetOut.size());

        assertNotNull(pfDao.get(DummyReferenceEntity.class, new PfReferenceKey(owner0Key, "Entity0")));
        assertNotNull(pfDao.getConcept(DummyReferenceEntity.class, new PfReferenceKey(owner0Key, "Entity0")));
        assertNull(pfDao.get(DummyReferenceEntity.class, new PfReferenceKey(owner0Key, "Entity1000")));
        assertNull(pfDao.getConcept(DummyReferenceEntity.class, new PfReferenceKey(owner0Key, "Entity1000")));
        pfDao.delete(DummyReferenceEntity.class, new PfReferenceKey(owner0Key, "Entity0"));

        final Set<PfReferenceKey> rKeySetIn = new TreeSet<PfReferenceKey>();
        rKeySetIn.add(new PfReferenceKey(owner4Key, "EntityB"));
        rKeySetIn.add(new PfReferenceKey(owner5Key, "EntityD"));

        final int deletedRCount = pfDao.deleteByReferenceKey(DummyReferenceEntity.class, rKeySetIn);
        assertEquals(2, deletedRCount);

        pfDao.update(new DummyReferenceEntity(new PfReferenceKey(owner5Key, "EntityF"), 120.0));
    }
}
