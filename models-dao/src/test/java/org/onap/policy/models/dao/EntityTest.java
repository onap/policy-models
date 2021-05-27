/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2019-2021 Nordix Foundation.
 *  Modifications Copyright (C) 2019-2021 AT&T Intellectual Property. All rights reserved.
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

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;
import org.eclipse.persistence.config.PersistenceUnitProperties;
import org.junit.After;
import org.junit.Test;
import org.onap.policy.models.base.PfConceptKey;
import org.onap.policy.models.base.PfGeneratedIdKey;
import org.onap.policy.models.base.PfModelException;
import org.onap.policy.models.base.PfReferenceKey;
import org.onap.policy.models.base.PfReferenceTimestampKey;
import org.onap.policy.models.base.PfTimestampKey;
import org.onap.policy.models.dao.impl.DefaultPfDao;

/**
 * JUnit test class.
 */
public class EntityTest {
    private static final String DESCRIPTION2 = "key description 2";
    private static final String DESCRIPTION1 = "key description 1";
    private static final String DESCRIPTION0 = "key description 0";
    private static final String ENTITY0 = "Entity0";
    private static final String UUID2 = "00000000-0000-0000-0000-000000000002";
    private static final String UUID1 = "00000000-0000-0000-0000-000000000001";
    private static final String UUID0 = "00000000-0000-0000-0000-000000000000";
    private static final String VERSION003 = "0.0.3";
    private static final String VERSION002 = "0.0.2";
    private static final String VERSION001 = "0.0.1";
    private static final Instant TIMESTAMP0 = Instant.ofEpochSecond(1613494293);
    private static final Instant TIMESTAMP1 = Instant.ofEpochSecond(1613494293).plusSeconds(55);
    private static final Instant TIMESTAMP2 = Instant.ofEpochSecond(1613494293).plusSeconds(90);
    private static final Long GENERATEDID0 = 10000L;
    private static final Long GENERATEDID1 = 10001L;
    private static final Long GENERATEDID2 = 10002L;

    private PfDao pfDao;

    /**
     * Closes the DAO.
     */
    @After
    public void tearDown() {
        if (pfDao != null) {
            pfDao.close();
            pfDao = null;
        }
    }

    @Test
    public void testEntityTestSanity() throws PfModelException {
        final DaoParameters daoParameters = new DaoParameters();

        Properties jdbcProperties = new Properties();
        // @formatter:off
        jdbcProperties.setProperty(PersistenceUnitProperties.JDBC_DRIVER,   "org.h2.Driver");
        jdbcProperties.setProperty(PersistenceUnitProperties.JDBC_URL,      "jdbc:h2:mem:EntityTest");
        jdbcProperties.setProperty(PersistenceUnitProperties.JDBC_USER,     "sa");
        jdbcProperties.setProperty(PersistenceUnitProperties.JDBC_PASSWORD, "");
        // @formatter:on

        daoParameters.setJdbcProperties(jdbcProperties);

        pfDao = new PfDaoFactory().createPfDao(daoParameters);

        assertThatThrownBy(() -> pfDao.init(null)).hasMessage("Policy Framework persistence unit parameter not set");

        assertThatThrownBy(() -> pfDao.init(daoParameters))
                        .hasMessage("Policy Framework persistence unit parameter not set");

        daoParameters.setPluginClass("somewhere.over.the.rainbow");
        daoParameters.setPersistenceUnit("Dorothy");

        assertThatThrownBy(() -> pfDao.init(daoParameters))
                        .hasMessage("Creation of Policy Framework persistence unit \"Dorothy\" failed");

        assertThatThrownBy(() -> pfDao.create(new PfConceptKey()))
                        .hasMessage("Policy Framework DAO has not been initialized");
    }

    @Test
    public void testEntityTestAllOpsJpa() throws PfModelException {

        final DaoParameters daoParameters = new DaoParameters();
        daoParameters.setPluginClass(DefaultPfDao.class.getName());
        daoParameters.setPersistenceUnit("DaoTest");

        Properties jdbcProperties = new Properties();
        jdbcProperties.setProperty("javax.persistence.jdbc.driver", "org.h2.Driver");
        jdbcProperties.setProperty("javax.persistence.jdbc.url", "jdbc:h2:mem:EntityTest");
        jdbcProperties.setProperty("javax.persistence.jdbc.user", "sa");
        jdbcProperties.setProperty("javax.persistence.jdbc.password", "");

        daoParameters.setJdbcProperties(jdbcProperties);

        pfDao = new PfDaoFactory().createPfDao(daoParameters);
        pfDao.init(daoParameters);

        testAllOps();

        testGeneratedId();

        testReferenceTimestamp();

        testVersionOps();

        testgetFilteredOps();

        testgetFilteredOps2();

        testgetFilteredOps3();
    }

    @Test
    public void testEntityTestBadVals() throws PfModelException {
        final DaoParameters daoParameters = new DaoParameters();
        daoParameters.setPluginClass(DefaultPfDao.class.getName());
        daoParameters.setPersistenceUnit("DaoTest");

        pfDao = new PfDaoFactory().createPfDao(daoParameters);
        pfDao.init(daoParameters);

        final PfConceptKey nullKey = null;
        final PfReferenceKey nullRefKey = null;
        final PfTimestampKey nullTimeKey = null;
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
        pfDao.delete(PfTimestampKey.class, nullTimeKey);
        pfDao.deleteByConceptKey(PfConceptKey.class, nullKeyList);
        pfDao.deleteByConceptKey(PfConceptKey.class, emptyKeyList);
        pfDao.deleteByReferenceKey(PfReferenceKey.class, nullRKeyList);
        pfDao.deleteByReferenceKey(PfReferenceKey.class, emptyRKeyList);

        pfDao.get(null, nullKey);
        pfDao.get(null, nullRefKey);
        pfDao.get(null, nullTimeKey);
        pfDao.getAll(null);
        pfDao.getAll(null, nullKey);
        pfDao.getAll(null, null, null);
        pfDao.getConcept(null, nullKey);
        pfDao.getConcept(PfConceptKey.class, nullKey);
        pfDao.getConcept(null, nullRefKey);
        pfDao.getConcept(PfReferenceKey.class, nullRefKey);

        assertThatCode(() -> pfDao.size(null)).doesNotThrowAnyException();
    }

    private void testAllOps() {
        final PfConceptKey aKey0 = new PfConceptKey("A-KEY0", VERSION001);
        final PfConceptKey aKey1 = new PfConceptKey("A-KEY1", VERSION001);
        final PfConceptKey aKey2 = new PfConceptKey("A-KEY2", VERSION001);
        final DummyConceptEntity keyInfo0 = new DummyConceptEntity(aKey0,
                UUID.fromString(UUID0), DESCRIPTION0);
        final DummyConceptEntity keyInfo1 = new DummyConceptEntity(aKey1,
                UUID.fromString(UUID1), DESCRIPTION1);
        final DummyConceptEntity keyInfo2 = new DummyConceptEntity(aKey2,
                UUID.fromString(UUID2), DESCRIPTION2);

        pfDao.create(keyInfo0);

        final DummyConceptEntity keyInfoBack0 = pfDao.get(DummyConceptEntity.class, aKey0);
        assertEquals(keyInfo0, keyInfoBack0);

        final DummyConceptEntity keyInfoBackNull = pfDao.get(DummyConceptEntity.class, PfConceptKey.getNullKey());
        assertNull(keyInfoBackNull);

        final DummyConceptEntity keyInfoBack1 = pfDao.getConcept(DummyConceptEntity.class, aKey0);
        assertEquals(keyInfoBack0, keyInfoBack1);

        final DummyConceptEntity keyInfoBack2 =
                pfDao.getConcept(DummyConceptEntity.class, new PfConceptKey("A-KEY3", VERSION001));
        assertNull(keyInfoBack2);

        final Set<DummyConceptEntity> keyInfoSetIn = new TreeSet<>();
        keyInfoSetIn.add(keyInfo1);
        keyInfoSetIn.add(keyInfo2);

        pfDao.createCollection(keyInfoSetIn);

        Set<DummyConceptEntity> keyInfoSetOut = new TreeSet<>(pfDao.getAll(DummyConceptEntity.class));

        keyInfoSetIn.add(keyInfo0);
        assertEquals(keyInfoSetIn, keyInfoSetOut);

        pfDao.delete(keyInfo1);
        keyInfoSetIn.remove(keyInfo1);
        keyInfoSetOut = new TreeSet<>(pfDao.getAll(DummyConceptEntity.class));
        assertEquals(keyInfoSetIn, keyInfoSetOut);

        pfDao.deleteCollection(keyInfoSetIn);
        keyInfoSetOut = new TreeSet<>(pfDao.getAll(DummyConceptEntity.class));
        assertEquals(0, keyInfoSetOut.size());

        keyInfoSetIn.add(keyInfo0);
        keyInfoSetIn.add(keyInfo1);
        keyInfoSetIn.add(keyInfo0);
        pfDao.createCollection(keyInfoSetIn);
        keyInfoSetOut = new TreeSet<>(pfDao.getAll(DummyConceptEntity.class));
        assertEquals(keyInfoSetIn, keyInfoSetOut);

        pfDao.delete(DummyConceptEntity.class, aKey0);
        keyInfoSetOut = new TreeSet<>(pfDao.getAll(DummyConceptEntity.class));
        assertEquals(2, keyInfoSetOut.size());
        assertEquals(2, pfDao.size(DummyConceptEntity.class));

        final Set<PfConceptKey> keySetIn = new TreeSet<>();
        keySetIn.add(aKey1);
        keySetIn.add(aKey2);

        final int deletedCount = pfDao.deleteByConceptKey(DummyConceptEntity.class, keySetIn);
        assertEquals(2, deletedCount);

        keyInfoSetOut = new TreeSet<>(pfDao.getAll(DummyConceptEntity.class));
        assertEquals(0, keyInfoSetOut.size());

        keyInfoSetIn.add(keyInfo0);
        keyInfoSetIn.add(keyInfo1);
        keyInfoSetIn.add(keyInfo0);
        pfDao.createCollection(keyInfoSetIn);
        keyInfoSetOut = new TreeSet<>(pfDao.getAll(DummyConceptEntity.class));
        assertEquals(keyInfoSetIn, keyInfoSetOut);

        pfDao.deleteAll(DummyConceptEntity.class);
        assertEquals(0, pfDao.size(DummyConceptEntity.class));

        final PfConceptKey owner0Key = new PfConceptKey("Owner0", VERSION001);
        final PfConceptKey owner1Key = new PfConceptKey("Owner1", VERSION001);
        final PfConceptKey owner2Key = new PfConceptKey("Owner2", VERSION001);
        final PfConceptKey owner3Key = new PfConceptKey("Owner3", VERSION001);
        final PfConceptKey owner4Key = new PfConceptKey("Owner4", VERSION001);
        final PfConceptKey owner5Key = new PfConceptKey("Owner5", VERSION001);

        pfDao.create(new DummyReferenceEntity(new PfReferenceKey(owner0Key, ENTITY0), 100.0));
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
                new TreeSet<>(pfDao.getAll(DummyReferenceEntity.class));
        assertEquals(16, testEntitySetOut.size());

        testEntitySetOut = new TreeSet<>(pfDao.getAll(DummyReferenceEntity.class, owner0Key));
        assertEquals(5, testEntitySetOut.size());

        testEntitySetOut = new TreeSet<>(pfDao.getAll(DummyReferenceEntity.class, owner1Key));
        assertEquals(3, testEntitySetOut.size());

        testEntitySetOut = new TreeSet<>(pfDao.getAll(DummyReferenceEntity.class, owner2Key));
        assertEquals(2, testEntitySetOut.size());

        testEntitySetOut = new TreeSet<>(pfDao.getAll(DummyReferenceEntity.class, owner3Key));
        assertEquals(1, testEntitySetOut.size());

        testEntitySetOut = new TreeSet<>(pfDao.getAll(DummyReferenceEntity.class, owner4Key));
        assertEquals(1, testEntitySetOut.size());

        testEntitySetOut = new TreeSet<>(pfDao.getAll(DummyReferenceEntity.class, owner5Key));
        assertEquals(4, testEntitySetOut.size());

        assertNotNull(pfDao.get(DummyReferenceEntity.class, new PfReferenceKey(owner0Key, ENTITY0)));
        assertNotNull(pfDao.getConcept(DummyReferenceEntity.class, new PfReferenceKey(owner0Key, ENTITY0)));
        assertNull(pfDao.get(DummyReferenceEntity.class, new PfReferenceKey(owner0Key, "Entity1000")));
        assertNull(pfDao.getConcept(DummyReferenceEntity.class, new PfReferenceKey(owner0Key, "Entity1000")));
        pfDao.delete(DummyReferenceEntity.class, new PfReferenceKey(owner0Key, ENTITY0));

        final Set<PfReferenceKey> rKeySetIn = new TreeSet<>();
        rKeySetIn.add(new PfReferenceKey(owner4Key, "EntityB"));
        rKeySetIn.add(new PfReferenceKey(owner5Key, "EntityD"));

        final int deletedRCount = pfDao.deleteByReferenceKey(DummyReferenceEntity.class, rKeySetIn);
        assertEquals(2, deletedRCount);

        pfDao.update(new DummyReferenceEntity(new PfReferenceKey(owner5Key, "EntityF"), 120.0));

        final PfTimestampKey atKey0 = new PfTimestampKey("AT-KEY0", VERSION001, TIMESTAMP0);
        final PfTimestampKey atKey1 = new PfTimestampKey("AT-KEY1", VERSION001, TIMESTAMP1);
        final PfTimestampKey atKey2 = new PfTimestampKey("AT-KEY2", VERSION001, TIMESTAMP2);
        final DummyTimestampEntity tkeyInfo0 = new DummyTimestampEntity(atKey0, 200.0);
        final DummyTimestampEntity tkeyInfo1 = new DummyTimestampEntity(atKey1, 200.1);
        final DummyTimestampEntity tkeyInfo2 = new DummyTimestampEntity(atKey2, 200.2);

        pfDao.create(tkeyInfo0);

        final DummyTimestampEntity tkeyInfoBack0 = pfDao.get(DummyTimestampEntity.class, atKey0);
        assertEquals(tkeyInfo0, tkeyInfoBack0);

        final DummyTimestampEntity tkeyInfoBackNull =
                pfDao.get(DummyTimestampEntity.class, PfTimestampKey.getNullKey());
        assertNull(tkeyInfoBackNull);



        final Set<DummyTimestampEntity> tkeyInfoSetIn = new TreeSet<>();
        tkeyInfoSetIn.add(tkeyInfo1);
        tkeyInfoSetIn.add(tkeyInfo2);

        pfDao.createCollection(tkeyInfoSetIn);

        Set<DummyTimestampEntity> tkeyInfoSetOut = new TreeSet<>(pfDao.getAll(DummyTimestampEntity.class));

        tkeyInfoSetIn.add(tkeyInfo0);
        assertEquals(tkeyInfoSetIn, tkeyInfoSetOut);

        pfDao.delete(tkeyInfo1);
        tkeyInfoSetIn.remove(tkeyInfo1);
        tkeyInfoSetOut = new TreeSet<>(pfDao.getAll(DummyTimestampEntity.class));
        assertEquals(tkeyInfoSetIn, tkeyInfoSetOut);

        pfDao.deleteCollection(tkeyInfoSetIn);
        tkeyInfoSetOut = new TreeSet<>(pfDao.getAll(DummyTimestampEntity.class));
        assertEquals(0, tkeyInfoSetOut.size());

        tkeyInfoSetIn.add(tkeyInfo2);
        pfDao.createCollection(tkeyInfoSetIn);
        tkeyInfoSetOut = new TreeSet<>(pfDao.getAll(DummyTimestampEntity.class));
        assertEquals(keyInfoSetIn, keyInfoSetOut);

        pfDao.delete(DummyTimestampEntity.class, atKey2);
        tkeyInfoSetOut = new TreeSet<>(pfDao.getAll(DummyTimestampEntity.class));
        assertEquals(3, keyInfoSetOut.size());
        assertEquals(1, pfDao.size(DummyTimestampEntity.class));

        pfDao.deleteAll(DummyTimestampEntity.class);
        assertEquals(0, pfDao.size(DummyTimestampEntity.class));
    }

    private void testGeneratedId() {
        final PfGeneratedIdKey agKey0 = new PfGeneratedIdKey("AT-KEY0", VERSION001, GENERATEDID0);
        final PfGeneratedIdKey agKey1 = new PfGeneratedIdKey("AT-KEY1", VERSION001, GENERATEDID1);
        final PfGeneratedIdKey agKey2 = new PfGeneratedIdKey("AT-KEY2", VERSION001, GENERATEDID2);
        final DummyGeneratedIdEntity gkeyInfo0 = new DummyGeneratedIdEntity(agKey0, Date.from(TIMESTAMP0));
        final DummyGeneratedIdEntity gkeyInfo1 = new DummyGeneratedIdEntity(agKey1, Date.from(TIMESTAMP1));
        final DummyGeneratedIdEntity gkeyInfo2 = new DummyGeneratedIdEntity(agKey2, Date.from(TIMESTAMP2));

        pfDao.create(gkeyInfo0);

        final DummyGeneratedIdEntity gkeyInfoBack0 = pfDao.get(DummyGeneratedIdEntity.class, agKey0);
        assertEquals(gkeyInfo0, gkeyInfoBack0);

        assertEquals(1, pfDao.getByTimestamp(DummyGeneratedIdEntity.class, agKey0, TIMESTAMP0).size());

        final DummyGeneratedIdEntity gkeyInfoBackNull =
                pfDao.get(DummyGeneratedIdEntity.class, PfGeneratedIdKey.getNullKey());
        assertNull(gkeyInfoBackNull);

        final Set<DummyGeneratedIdEntity> gkeyInfoSetIn = new TreeSet<>();
        gkeyInfoSetIn.add(gkeyInfo1);
        gkeyInfoSetIn.add(gkeyInfo2);

        pfDao.createCollection(gkeyInfoSetIn);

        Set<DummyGeneratedIdEntity> gkeyInfoSetOut = new TreeSet<>(pfDao.getAll(DummyGeneratedIdEntity.class));

        gkeyInfoSetIn.add(gkeyInfo0);
        assertEquals(gkeyInfoSetIn, gkeyInfoSetOut);

        pfDao.delete(gkeyInfo1);
        gkeyInfoSetIn.remove(gkeyInfo1);
        gkeyInfoSetOut = new TreeSet<>(pfDao.getAll(DummyGeneratedIdEntity.class));
        assertEquals(gkeyInfoSetIn, gkeyInfoSetOut);

        pfDao.deleteCollection(gkeyInfoSetIn);
        gkeyInfoSetOut = new TreeSet<>(pfDao.getAll(DummyGeneratedIdEntity.class));
        assertEquals(0, gkeyInfoSetOut.size());

        gkeyInfoSetIn.add(gkeyInfo2);
        pfDao.createCollection(gkeyInfoSetIn);
        gkeyInfoSetOut = new TreeSet<>(pfDao.getAll(DummyGeneratedIdEntity.class));
        assertEquals(gkeyInfoSetIn, gkeyInfoSetOut);

        pfDao.delete(DummyGeneratedIdEntity.class, agKey2);
        gkeyInfoSetOut = new TreeSet<>(pfDao.getAll(DummyGeneratedIdEntity.class));
        assertEquals(gkeyInfoSetOut.size(), pfDao.size(DummyGeneratedIdEntity.class));

        pfDao.deleteAll(DummyGeneratedIdEntity.class);
        assertEquals(0, pfDao.size(DummyGeneratedIdEntity.class));

        final PfGeneratedIdKey agKey3 = new PfGeneratedIdKey("AT-KEY0", VERSION001);
        final PfGeneratedIdKey agKey4 = new PfGeneratedIdKey("AT-KEY1", VERSION001);
        final PfGeneratedIdKey agKey5 = new PfGeneratedIdKey("AT-KEY2", VERSION001);
        final DummyGeneratedIdEntity gkeyInfo3 = new DummyGeneratedIdEntity(agKey3, Date.from(TIMESTAMP0));
        final DummyGeneratedIdEntity gkeyInfo4 = new DummyGeneratedIdEntity(agKey4, Date.from(TIMESTAMP1));
        final DummyGeneratedIdEntity gkeyInfo5 = new DummyGeneratedIdEntity(agKey5, Date.from(TIMESTAMP2));

        pfDao.create(gkeyInfo3);

        final DummyGeneratedIdEntity gkeyInfoBack3 = pfDao.get(DummyGeneratedIdEntity.class, agKey3);
        assertEquals(gkeyInfo3, gkeyInfoBack3);

        assertEquals(1, pfDao.getByTimestamp(DummyGeneratedIdEntity.class, agKey3, TIMESTAMP0).size());

        assertEquals(1, gkeyInfo3.getKeys().size());

        assertEquals(1, gkeyInfo4.compareTo(gkeyInfo3));

        assertNull(gkeyInfo4.validate(VERSION002).getResult());


        gkeyInfoSetIn.clear();
        gkeyInfoSetIn.add(gkeyInfo4);
        gkeyInfoSetIn.add(gkeyInfo5);

        pfDao.createCollection(gkeyInfoSetIn);

        gkeyInfoSetOut = new TreeSet<>(pfDao.getAll(DummyGeneratedIdEntity.class));

        gkeyInfoSetIn.add(gkeyInfo3);
        assertEquals(gkeyInfoSetIn, gkeyInfoSetOut);

        pfDao.delete(gkeyInfo4);
        gkeyInfoSetIn.remove(gkeyInfo4);
        gkeyInfoSetOut = new TreeSet<>(pfDao.getAll(DummyGeneratedIdEntity.class));
        assertEquals(gkeyInfoSetIn, gkeyInfoSetOut);

        pfDao.deleteCollection(gkeyInfoSetIn);
        gkeyInfoSetOut = new TreeSet<>(pfDao.getAll(DummyGeneratedIdEntity.class));
        assertEquals(0, gkeyInfoSetOut.size());

        gkeyInfoSetIn.add(gkeyInfo5);
        pfDao.createCollection(gkeyInfoSetIn);
        gkeyInfoSetOut = new TreeSet<>(pfDao.getAll(DummyGeneratedIdEntity.class));
        assertEquals(gkeyInfoSetIn, gkeyInfoSetOut);

        pfDao.delete(DummyGeneratedIdEntity.class, agKey5);
        gkeyInfoSetOut = new TreeSet<>(pfDao.getAll(DummyGeneratedIdEntity.class));
        assertEquals(gkeyInfoSetOut.size(), pfDao.size(DummyGeneratedIdEntity.class));

        pfDao.deleteAll(DummyGeneratedIdEntity.class);
        assertEquals(0, pfDao.size(DummyGeneratedIdEntity.class));
    }

    private void testReferenceTimestamp() {
        final PfConceptKey owner0Key = new PfConceptKey("Owner0", VERSION001);
        final PfConceptKey owner1Key = new PfConceptKey("Owner1", VERSION001);
        final PfConceptKey owner2Key = new PfConceptKey("Owner2", VERSION001);
        final PfReferenceTimestampKey arKey0 = new PfReferenceTimestampKey(owner0Key, "AT-KEY0", TIMESTAMP0);
        final PfReferenceTimestampKey arKey1 = new PfReferenceTimestampKey(owner1Key, "AT-KEY1", TIMESTAMP1);;
        final PfReferenceTimestampKey arKey2 = new PfReferenceTimestampKey(owner2Key, "AT-KEY2", TIMESTAMP2);
        final DummyReferenceTimestampEntity rkeyInfo0 = new DummyReferenceTimestampEntity(arKey0);
        final DummyReferenceTimestampEntity rkeyInfo1 = new DummyReferenceTimestampEntity(arKey1);
        final DummyReferenceTimestampEntity rkeyInfo2 = new DummyReferenceTimestampEntity(arKey2);

        pfDao.create(rkeyInfo0);

        final  DummyReferenceTimestampEntity rkeyInfoBack0 = pfDao.get(DummyReferenceTimestampEntity.class, arKey0);
        assertEquals(rkeyInfo0, rkeyInfoBack0);


        final DummyReferenceTimestampEntity rkeyInfoBackNull =
                pfDao.get(DummyReferenceTimestampEntity.class, PfReferenceTimestampKey.getNullKey());
        assertNull(rkeyInfoBackNull);

        final Set<DummyReferenceTimestampEntity> rkeyInfoSetIn = new TreeSet<>();
        rkeyInfoSetIn.add(rkeyInfo1);
        rkeyInfoSetIn.add(rkeyInfo2);

        pfDao.createCollection(rkeyInfoSetIn);

        Set<DummyReferenceTimestampEntity> rkeyInfoSetOut =
                new TreeSet<>(pfDao.getAll(DummyReferenceTimestampEntity.class));

        rkeyInfoSetIn.add(rkeyInfo0);
        assertEquals(rkeyInfoSetIn, rkeyInfoSetOut);

        pfDao.delete(rkeyInfo1);
        rkeyInfoSetIn.remove(rkeyInfo1);
        rkeyInfoSetOut = new TreeSet<>(pfDao.getAll(DummyReferenceTimestampEntity.class));
        assertEquals(rkeyInfoSetIn, rkeyInfoSetOut);

        pfDao.deleteCollection(rkeyInfoSetIn);
        rkeyInfoSetOut = new TreeSet<>(pfDao.getAll(DummyReferenceTimestampEntity.class));
        assertEquals(0, rkeyInfoSetOut.size());

        rkeyInfoSetIn.add(rkeyInfo2);
        pfDao.createCollection(rkeyInfoSetIn);
        rkeyInfoSetOut = new TreeSet<>(pfDao.getAll(DummyReferenceTimestampEntity.class));
        assertEquals(rkeyInfoSetIn, rkeyInfoSetOut);

        pfDao.deleteAll(DummyReferenceTimestampEntity.class);
        assertEquals(0, pfDao.size(DummyReferenceTimestampEntity.class));
    }

    private void testVersionOps() {
        final PfConceptKey aKey0 = new PfConceptKey("AAA0", VERSION001);
        final PfConceptKey aKey1 = new PfConceptKey("AAA0", VERSION002);
        final PfConceptKey aKey2 = new PfConceptKey("AAA0", VERSION003);
        final PfConceptKey bKey0 = new PfConceptKey("BBB0", VERSION001);
        final PfConceptKey bKey1 = new PfConceptKey("BBB0", VERSION002);
        final PfConceptKey bKey2 = new PfConceptKey("BBB0", VERSION003);
        final DummyConceptEntity keyInfo0 = new DummyConceptEntity(aKey0,
                UUID.fromString(UUID0), DESCRIPTION0);
        final DummyConceptEntity keyInfo1 = new DummyConceptEntity(aKey1,
                UUID.fromString(UUID1), DESCRIPTION1);
        final DummyConceptEntity keyInfo2 = new DummyConceptEntity(aKey2,
                UUID.fromString(UUID2), DESCRIPTION2);
        final DummyConceptEntity keyInfo3 = new DummyConceptEntity(bKey0,
                UUID.fromString(UUID0), DESCRIPTION0);
        final DummyConceptEntity keyInfo4 = new DummyConceptEntity(bKey1,
                UUID.fromString(UUID1), DESCRIPTION1);
        final DummyConceptEntity keyInfo5 = new DummyConceptEntity(bKey2,
                UUID.fromString(UUID2), DESCRIPTION2);

        pfDao.create(keyInfo0);
        pfDao.create(keyInfo1);
        pfDao.create(keyInfo2);
        pfDao.create(keyInfo3);
        pfDao.create(keyInfo4);
        pfDao.create(keyInfo5);

        assertEquals(3, pfDao.getAllVersions(DummyConceptEntity.class, "AAA0").size());
        assertEquals(0, pfDao.getAllVersions(null, "AAA0").size());
        assertEquals(0, pfDao.getAllVersions(DummyConceptEntity.class, null).size());
    }

    private void testgetFilteredOps() {
        final PfConceptKey aKey0 = new PfConceptKey("AAA0", VERSION001);
        final PfConceptKey aKey1 = new PfConceptKey("AAA0", VERSION002);
        final PfConceptKey aKey2 = new PfConceptKey("AAA0", VERSION003);
        final PfConceptKey bKey0 = new PfConceptKey("BBB0", VERSION001);
        final PfConceptKey bKey1 = new PfConceptKey("BBB0", VERSION002);
        final PfConceptKey bKey2 = new PfConceptKey("BBB0", VERSION003);
        final DummyConceptEntity keyInfo0 = new DummyConceptEntity(aKey0,
                UUID.fromString(UUID0), DESCRIPTION0);
        final DummyConceptEntity keyInfo1 = new DummyConceptEntity(aKey1,
                UUID.fromString(UUID1), DESCRIPTION1);
        final DummyConceptEntity keyInfo2 = new DummyConceptEntity(aKey2,
                UUID.fromString(UUID2), DESCRIPTION2);
        final DummyConceptEntity keyInfo3 = new DummyConceptEntity(bKey0,
                UUID.fromString(UUID0), DESCRIPTION0);
        final DummyConceptEntity keyInfo4 = new DummyConceptEntity(bKey1,
                UUID.fromString(UUID1), DESCRIPTION1);
        final DummyConceptEntity keyInfo5 = new DummyConceptEntity(bKey2,
                UUID.fromString(UUID2), DESCRIPTION2);

        pfDao.create(keyInfo0);
        pfDao.create(keyInfo1);
        pfDao.create(keyInfo2);
        pfDao.create(keyInfo3);
        pfDao.create(keyInfo4);
        pfDao.create(keyInfo5);

        assertEquals(6, pfDao.getFiltered(DummyConceptEntity.class, null, null).size());
        assertEquals(3, pfDao.getFiltered(DummyConceptEntity.class, "AAA0", null).size());
        assertEquals(3, pfDao.getFiltered(DummyConceptEntity.class, "BBB0", null).size());
        assertEquals(1, pfDao.getFiltered(DummyConceptEntity.class, "BBB0", VERSION003).size());
        assertEquals(6, pfDao.getFiltered(DummyConceptEntity.class, null, VERSION003).size());

        final PfTimestampKey atKey0 = new PfTimestampKey("AT-KEY0", VERSION001, TIMESTAMP0);
        final PfTimestampKey atKey1 = new PfTimestampKey("AT-KEY1", VERSION001, TIMESTAMP1);
        final PfTimestampKey atKey2 = new PfTimestampKey("AT-KEY2", VERSION001, TIMESTAMP2);
        final DummyTimestampEntity tkeyInfo0 = new DummyTimestampEntity(atKey0, 200.0);
        final DummyTimestampEntity tkeyInfo1 = new DummyTimestampEntity(atKey1, 200.1);
        final DummyTimestampEntity tkeyInfo2 = new DummyTimestampEntity(atKey2, 200.2);

        pfDao.create(tkeyInfo0);
        pfDao.create(tkeyInfo1);
        pfDao.create(tkeyInfo2);


        assertEquals(1, pfDao
                .getFiltered(DummyTimestampEntity.class, "AT-KEY0", VERSION001, null, null, null, "DESC", 0).size());
        assertEquals(1,
                pfDao.getFiltered(DummyTimestampEntity.class, "AT-KEY0", null, null, null, null, "DESC", 0).size());
        assertEquals(3, pfDao
                .getFiltered(DummyTimestampEntity.class, null, VERSION001, TIMESTAMP0, TIMESTAMP2, null, "DESC", 0)
                .size());
        assertEquals(1, pfDao
                .getFiltered(DummyTimestampEntity.class, "AT-KEY0", VERSION001, TIMESTAMP0, TIMESTAMP2, null, "DESC", 0)
                .size());
        assertEquals(3, pfDao
                .getFiltered(DummyTimestampEntity.class, null, VERSION001, null, TIMESTAMP2, null, "DESC", 0).size());
        assertEquals(3, pfDao
                .getFiltered(DummyTimestampEntity.class, null, VERSION001, TIMESTAMP0, null, null, "DESC", 0).size());
        assertEquals(2,
                pfDao.getFiltered(DummyTimestampEntity.class, null, VERSION001, TIMESTAMP0, TIMESTAMP2, null, "DESC", 2)
                        .size());

        Map<String, Object> filterMap = new HashMap<>();
        filterMap.put("doubleValue", 200.1);
        assertEquals(1,
                pfDao.getFiltered(DummyTimestampEntity.class, null, null, null, null, filterMap, "DESC", 0).size());
    }

    private void testgetFilteredOps2() {
        Map<String, Object> filterMap = new HashMap<>();
        filterMap.put("doubleValue", 200.1);

        final PfGeneratedIdKey agKey0 = new PfGeneratedIdKey("AT-KEY0", VERSION001);
        final PfGeneratedIdKey agKey1 = new PfGeneratedIdKey("AT-KEY1", VERSION001);
        final PfGeneratedIdKey agKey2 = new PfGeneratedIdKey("AT-KEY2", VERSION001);
        final DummyGeneratedIdEntity gkeyInfo0 = new DummyGeneratedIdEntity(agKey0, Date.from(TIMESTAMP0), 200.0);
        final DummyGeneratedIdEntity gkeyInfo1 = new DummyGeneratedIdEntity(agKey1, Date.from(TIMESTAMP1), 200.1);
        final DummyGeneratedIdEntity gkeyInfo2 = new DummyGeneratedIdEntity(agKey2, Date.from(TIMESTAMP2), 200.2);

        pfDao.create(gkeyInfo0);
        pfDao.create(gkeyInfo1);
        pfDao.create(gkeyInfo2);


        assertEquals(1, pfDao
                .getFiltered(DummyGeneratedIdEntity.class, "AT-KEY0", VERSION001, null, null, null, "DESC", 0).size());
        assertEquals(1,
                pfDao.getFiltered(DummyGeneratedIdEntity.class, "AT-KEY0", null, null, null, null, "DESC", 0).size());
        assertEquals(3, pfDao
                .getFiltered(DummyGeneratedIdEntity.class, null, VERSION001, TIMESTAMP0, TIMESTAMP2, null, "DESC", 0)
                .size());
        assertEquals(1, pfDao
                .getFiltered(DummyGeneratedIdEntity.class, "AT-KEY0", VERSION001,
                        TIMESTAMP0, TIMESTAMP2, null, "DESC", 0)
                .size());
        assertEquals(3, pfDao
                .getFiltered(DummyGeneratedIdEntity.class, null, VERSION001, null, TIMESTAMP2, null, "DESC", 0).size());
        assertEquals(3, pfDao
                .getFiltered(DummyGeneratedIdEntity.class, null, VERSION001,
                        TIMESTAMP0, null, null, "DESC", 0).size());
        assertEquals(2,
                pfDao.getFiltered(DummyGeneratedIdEntity.class, null, VERSION001,
                        TIMESTAMP0, TIMESTAMP2, null, "DESC", 2)
                        .size());

        assertEquals(1,
                pfDao.getFiltered(DummyGeneratedIdEntity.class, null, null, null, null, filterMap, "DESC", 0).size());
    }

    private void testgetFilteredOps3() {
        Map<String, Object> filterMap = new HashMap<>();
        filterMap.put("localName", "AT-KEY0");

        final PfConceptKey owner0Key = new PfConceptKey("Owner0", VERSION001);
        final PfConceptKey owner1Key = new PfConceptKey("Owner1", VERSION001);
        final PfConceptKey owner2Key = new PfConceptKey("Owner2", VERSION001);
        final PfReferenceTimestampKey arKey0 = new PfReferenceTimestampKey(owner0Key, "AT-KEY0", TIMESTAMP0);
        final PfReferenceTimestampKey arKey1 = new PfReferenceTimestampKey(owner1Key, "AT-KEY1", TIMESTAMP1);;
        final PfReferenceTimestampKey arKey2 = new PfReferenceTimestampKey(owner2Key, "AT-KEY2", TIMESTAMP2);
        final DummyReferenceTimestampEntity rkeyInfo0 = new DummyReferenceTimestampEntity(arKey0);
        final DummyReferenceTimestampEntity rkeyInfo1 = new DummyReferenceTimestampEntity(arKey1);
        final DummyReferenceTimestampEntity rkeyInfo2 = new DummyReferenceTimestampEntity(arKey2);

        pfDao.create(rkeyInfo0);
        pfDao.create(rkeyInfo1);
        pfDao.create(rkeyInfo2);


        assertEquals(1, pfDao
                .getFiltered(DummyReferenceTimestampEntity.class,
                    "Owner0", VERSION001, null, null, null, "DESC", 0).size());
        assertEquals(1,
                pfDao.getFiltered(DummyReferenceTimestampEntity.class,
                    "Owner0", null, null, null, null, "DESC", 0).size());
        assertEquals(3, pfDao
                .getFiltered(DummyReferenceTimestampEntity.class,
                    null, VERSION001, TIMESTAMP0, TIMESTAMP2, null, "DESC", 0)
                .size());
        assertEquals(1, pfDao
                .getFiltered(DummyReferenceTimestampEntity.class,
                    "Owner0", VERSION001, TIMESTAMP0, TIMESTAMP2, null, "DESC", 0)
                .size());
        assertEquals(3, pfDao
                .getFiltered(DummyReferenceTimestampEntity.class, null,
                    VERSION001, null, TIMESTAMP2, null, "DESC", 0).size());
        assertEquals(3, pfDao
                .getFiltered(DummyReferenceTimestampEntity.class, null,
                    VERSION001, TIMESTAMP0, null, null, "DESC", 0).size());
        assertEquals(2,
                pfDao.getFiltered(DummyReferenceTimestampEntity.class,
                    null, VERSION001, TIMESTAMP0, TIMESTAMP2, null, "DESC", 2)
                        .size());

        assertEquals(1,
                pfDao.getFiltered(DummyReferenceTimestampEntity.class,
                    null, null, null, null, filterMap, "DESC", 0).size());
    }
}
