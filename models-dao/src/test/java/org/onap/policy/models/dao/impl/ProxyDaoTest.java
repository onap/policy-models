/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2024 Nordix Foundation.
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

package org.onap.policy.models.dao.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.onap.policy.models.base.PfConcept;
import org.onap.policy.models.base.PfConceptKey;
import org.onap.policy.models.base.PfReferenceKey;
import org.onap.policy.models.base.PfReferenceTimestampKey;
import org.onap.policy.models.base.PfTimestampKey;
import org.onap.policy.models.dao.DaoParameters;
import org.onap.policy.models.dao.PfFilterParameters;

@ExtendWith(MockitoExtension.class)
class ProxyDaoTest {

    @Mock
    private EntityManager mockMg;

    @Mock
    private TypedQuery mockQuery;

    private ProxyDao proxyDaoUnderTest;

    @BeforeEach
    void setUp() {
        proxyDaoUnderTest = new ProxyDao(mockMg);
    }

    @Test
    void testInit() throws Exception {
        assertThatCode(() -> proxyDaoUnderTest.init(new DaoParameters())).doesNotThrowAnyException();
    }

    @Test
    void testClose() {
        assertThatCode((() -> proxyDaoUnderTest.close())).doesNotThrowAnyException();
    }

    @Test
    void testCreate_Null() {
        final PfConcept obj = null;

        proxyDaoUnderTest.create(obj);

        verify(mockMg, never()).merge(null);
        verify(mockMg, never()).flush();
    }

    @Test
    void testCreate() {
        final PfConceptKey obj = new PfConceptKey("name", "1.0.0");

        proxyDaoUnderTest.create(obj);

        verify(mockMg).merge(obj);
        verify(mockMg).flush();
    }

    @Test
    void testDelete1_Null() {
        final PfConcept obj = null;

        proxyDaoUnderTest.delete(obj);

        verify(mockMg, never()).remove(null);
    }

    @Test
    void testDelete1() {
        final PfConceptKey obj = new PfConceptKey("name", "1.0.0");

        when(mockMg.contains(obj)).thenReturn(false);
        when(mockMg.merge(obj)).thenReturn(obj);

        proxyDaoUnderTest.delete(obj);

        verify(mockMg).remove(obj);
    }

    @Test
    void testDelete2WithNullKey() {
        proxyDaoUnderTest.delete(PfConceptKey.class, (PfConceptKey) null);
        // Verify that no interactions with mg are made when key is null
        verify(mockMg, never()).createQuery(anyString(), any());
    }

    @Test
    void testDelete2WithValidKey() {
        PfConceptKey key = new PfConceptKey("name", "1.0.0");

        when(mockMg.createQuery(anyString(), eq(PfConceptKey.class))).thenReturn(mockQuery);
        when(mockQuery.setParameter(anyString(), any())).thenReturn(mockQuery);

        proxyDaoUnderTest.delete(PfConceptKey.class, key);

        ArgumentCaptor<String> queryCaptor = ArgumentCaptor.forClass(String.class);
        verify(mockMg).createQuery(queryCaptor.capture(), eq(PfConceptKey.class));
        verify(mockQuery).setParameter("name", key.getName());
        verify(mockQuery).setParameter("version", key.getVersion());
        verify(mockQuery).executeUpdate();

        assertEquals("DELETE FROM PfConceptKey c WHERE c.key.name = :name "
            + "AND c.key.version = :version", queryCaptor.getValue());
    }

    @Test
    void testDelete3WithNullKey() {
        proxyDaoUnderTest.delete(PfReferenceKey.class, (PfReferenceKey) null);
        // Verify that no interactions with mg are made when key is null
        verify(mockMg, never()).createQuery(anyString(), any());
    }

    @Test
    void testDelete3WithValidKey() {
        PfReferenceKey key = new PfReferenceKey("name", "1.0.0", "localName");

        when(mockMg.createQuery(anyString(), eq(PfReferenceKey.class))).thenReturn(mockQuery);
        when(mockQuery.setParameter(anyString(), any())).thenReturn(mockQuery);

        proxyDaoUnderTest.delete(PfReferenceKey.class, key);

        ArgumentCaptor<String> queryCaptor = ArgumentCaptor.forClass(String.class);
        verify(mockMg).createQuery(queryCaptor.capture(), eq(PfReferenceKey.class));
        verify(mockQuery).setParameter("parentname", key.getParentKeyName());
        verify(mockQuery).setParameter("parentversion", key.getParentKeyVersion());
        verify(mockQuery).setParameter("localname", key.getLocalName());
        verify(mockQuery).executeUpdate();

        assertEquals("DELETE FROM PfReferenceKey c WHERE c.key.parentKeyName = :parentname"
            + " AND c.key.parentKeyVersion = :parentversion"
            + " AND c.key.localName = :localname", queryCaptor.getValue());
    }

    @Test
    void testDelete4WithNullKey() {
        proxyDaoUnderTest.delete(PfTimestampKey.class, (PfTimestampKey) null);
        // Verify that no interactions with mg are made when key is null
        verify(mockMg, never()).createQuery(anyString(), any());
    }

    @Test
    void testDelete4WithValidKey() {
        final PfTimestampKey key = new PfTimestampKey("name", "1.0.0", LocalDateTime.of(2020,
            1, 1, 0, 0, 0, 0).toInstant(ZoneOffset.UTC));

        when(mockMg.createQuery(anyString(), eq(PfTimestampKey.class))).thenReturn(mockQuery);
        when(mockQuery.setParameter(anyString(), any())).thenReturn(mockQuery);

        proxyDaoUnderTest.delete(PfTimestampKey.class, key);

        ArgumentCaptor<String> queryCaptor = ArgumentCaptor.forClass(String.class);
        verify(mockMg).createQuery(queryCaptor.capture(), eq(PfTimestampKey.class));
        verify(mockQuery).setParameter("name", key.getName());
        verify(mockQuery).setParameter("version", key.getVersion());
        verify(mockQuery).setParameter("timeStamp", key.getTimeStamp());
        verify(mockQuery).executeUpdate();

        assertEquals("DELETE FROM PfTimestampKey c WHERE c.key.name = :name AND c.key.version = :version "
            + "AND c.key.timeStamp = :timeStamp", queryCaptor.getValue());
    }

    @Test
    void testCreateCollectionWithNull() {
        proxyDaoUnderTest.createCollection(null);

        verify(mockMg, never()).merge(null);
    }

    @Test
    void testCreateCollection() {
        List<PfConceptKey> list = List.of(new PfConceptKey("name", "1.0.0"),
            new PfConceptKey("name2", "1.0.1"));

        proxyDaoUnderTest.createCollection(list);

        for (final Object ck : list) {
            verify(mockMg).merge(ck);
        }
    }

    @Test
    void testCreateCollectionWithIsEmpty() {
        proxyDaoUnderTest.createCollection(List.of());

        verify(mockMg, never()).merge(null);
    }

    @Test
    void testDeleteCollection() {
        final Collection<PfConceptKey> list = List.of(
            new PfConceptKey("name", "1.0.0"),
                new PfConceptKey("name2", "1.0.1"));
        for (final Object o : list) {
            lenient().when(mockMg.contains(o)).thenReturn(true);
        }

        proxyDaoUnderTest.deleteCollection(list);

        for (final Object o : list) {
            verify(mockMg).remove(o);
        }
    }

    @Test
    void testDeleteCollection_EntityManagerContainsReturnsFalse() {
        final Collection<PfConceptKey> list = List.of(
            new PfConceptKey("name", "1.0.0"),
                new PfConceptKey("name2", "1.0.1"));
        for (final Object o : list) {
            lenient().when(mockMg.contains(o)).thenReturn(false);
            lenient().when(mockMg.merge(o)).thenReturn(o);
        }

        proxyDaoUnderTest.deleteCollection(list);

        for (final Object o : list) {
            verify(mockMg).remove(o);
        }
    }

    @Test
    void testDeleteByConceptKey() {
        final Collection<PfConceptKey> keys = List.of(new PfConceptKey("name", "1.0.0"));
        when(mockMg.createQuery(anyString(), eq(PfConceptKey.class))).thenReturn(mockQuery);
        when(mockQuery.setParameter(anyString(), any())).thenReturn(mockQuery);
        when(mockQuery.executeUpdate()).thenReturn(1);

        final int result = proxyDaoUnderTest.deleteByConceptKey(PfConceptKey.class, keys);

        assertThat(result).isEqualTo(1);
    }

    @Test
    void testDeleteByConceptKeyWithNull() {
        final int result = proxyDaoUnderTest.deleteByConceptKey(PfConceptKey.class, null);
        assertThat(result).isZero();
    }

    @Test
    void testDeleteByConceptKeyWithEmpty() {
        final int result = proxyDaoUnderTest.deleteByConceptKey(PfConceptKey.class, List.of());
        assertThat(result).isZero();
    }

    @Test
    void testDeleteByReferenceKey() {
        // Setup
        final Collection<PfReferenceKey> keys = List.of(
            new PfReferenceKey("parentkeyname", "1.0.0", "parentlocalname"));
        when(mockMg.createQuery(anyString(), eq(PfReferenceKey.class))).thenReturn(mockQuery);
        when(mockQuery.setParameter(anyString(), any())).thenReturn(mockQuery);
        when(mockQuery.executeUpdate()).thenReturn(1);

        final int result = proxyDaoUnderTest.deleteByReferenceKey(PfReferenceKey.class, keys);

        assertThat(result).isEqualTo(1);
    }

    @Test
    void testDeleteByReferenceKeyWithNull() {
        final int result = proxyDaoUnderTest.deleteByReferenceKey(PfReferenceKey.class, null);
        assertThat(result).isZero();
    }

    @Test
    void testDeleteByReferenceKeyWithIsEmpty() {
        final int result = proxyDaoUnderTest.deleteByReferenceKey(PfReferenceKey.class, List.of());
        assertThat(result).isZero();
    }

    @Test
    void testDeleteAll() {
        when(mockMg.createQuery(anyString(), eq(PfConceptKey.class))).thenReturn(mockQuery);
        proxyDaoUnderTest.deleteAll(PfConceptKey.class);
        verify(mockQuery).executeUpdate();
    }

    @Test
    void testGetFiltered1() {
        final List<PfConcept> expectedResult = List.of(new PfConceptKey());
        lenient().when(mockMg.createQuery(anyString(), eq(PfConcept.class))).thenReturn(mockQuery);
        for (final PfConcept ck : expectedResult) {
            lenient().when(mockMg.find(eq(
                PfConcept.class), any(Object.class))).thenReturn(ck);
        }

        final List<PfConcept> result = proxyDaoUnderTest.getFiltered(PfConcept.class, "name", "1.0.0");

        assertThat(result).isEqualTo(expectedResult);
    }

    @Test
    void testGenericGetNull() {
        assertEquals(null, proxyDaoUnderTest.get(null, new PfConceptKey(
            "name", "1.0.0")));
    }

    @Test
    void testGetAllNull() {
        assertEquals(Collections.emptyList(), proxyDaoUnderTest.getAll(null));
        assertEquals(Collections.emptyList(), proxyDaoUnderTest.getAll(null,
            new PfConceptKey("name", "1.0.0")));
        assertEquals(Collections.emptyList(), proxyDaoUnderTest.getAll(null,
            "name", 1));

    }

    @Test
    void testGetAllVersionsByParentNull() {
        assertEquals(Collections.emptyList(), proxyDaoUnderTest
            .getAllVersionsByParent(null, "name"));
        assertEquals(Collections.emptyList(), proxyDaoUnderTest
            .getAllVersionsByParent(PfConcept.class, null));
    }

    @Test
    void testGetAllVersionsNull() {
        assertEquals(Collections.emptyList(), proxyDaoUnderTest
            .getAllVersions(null, "conceptName"));
        assertEquals(Collections.emptyList(), proxyDaoUnderTest
            .getAllVersions(PfConcept.class, null));
    }

    @Test
    void testGetConceptNull() {
        assertNull(proxyDaoUnderTest.getConcept(null,
            new PfConceptKey("name", "1.0.0")));
        assertNull(proxyDaoUnderTest.getConcept(PfConcept.class, (PfConceptKey) null));
        assertNull(proxyDaoUnderTest.getConcept(null,
            new PfReferenceKey("name", "1.0.0", "localName")));
        assertNull(proxyDaoUnderTest.getConcept(PfConcept.class, (PfReferenceKey) null));

    }

    @Test
    void testGetFiltered2() {
        List<PfConceptKey> list = List.of(new PfConceptKey("name", "1.0.0"),
            new PfConceptKey("name2", "1.0.1"));

        proxyDaoUnderTest.createCollection(list);

        final PfFilterParameters filterParams = PfFilterParameters.builder()
            .name("name")
            .version("1.0.0")
            .build();
        final List<PfConcept> expectedResult = List.of(new PfConceptKey("name", "1.0.0"));

        when(mockMg.createQuery(anyString(), eq(PfConcept.class))).thenReturn(mockQuery);
        when(mockQuery.getResultList()).thenReturn(expectedResult);

        final List<PfConcept> result = proxyDaoUnderTest.getFiltered(PfConcept.class, filterParams);

        assertThat(result).isEqualTo(expectedResult);
    }

    @Test
    void testGet1() {
        final PfConceptKey key = new PfConceptKey("name", "1.0.0");
        final PfConcept expectedResult = new PfConceptKey(key);
        when(mockMg.find(eq(PfConcept.class), any(Object.class))).thenReturn(key);

        final PfConcept result = proxyDaoUnderTest.get(PfConcept.class, key);

        assertThat(result).isEqualTo(expectedResult);
    }

    @Test
    void testGet2() {
        final PfReferenceKey key =
            new PfReferenceKey("parentKeyName", "1.0.0", "parentLocalName", "localName");
        final PfConcept expectedResult = new PfReferenceKey(key);
        when(mockMg.find(eq(PfConcept.class), any(Object.class))).thenReturn(key);

        final PfConcept result = proxyDaoUnderTest.get(PfConcept.class, key);

        assertThat(result).isEqualTo(expectedResult);
    }

    @Test
    void testGet3() {
        final PfTimestampKey key =
            new PfTimestampKey("name", "1.0.0", LocalDateTime.of(2020, 1, 1, 0, 0, 0, 0).toInstant(ZoneOffset.UTC));
        final PfConcept expectedResult = new PfTimestampKey(key);
        when(mockMg.find(eq(PfConcept.class), any(Object.class))).thenReturn(key);

        final PfConcept result = proxyDaoUnderTest.get(PfConcept.class, key);

        assertThat(result).isEqualTo(expectedResult);
    }

    @Test
    void testGet4() {
        final PfReferenceTimestampKey key =
            new PfReferenceTimestampKey("parentKeyName", "1.0.0", "parentLocalName", "localName",
                LocalDateTime.of(2020, 1, 1, 0, 0, 0, 0).toInstant(ZoneOffset.UTC));
        final PfConcept expectedResult = new PfReferenceTimestampKey(key);
        when(mockMg.find(eq(PfConcept.class), any(Object.class))).thenReturn(key);

        final PfConcept result = proxyDaoUnderTest.get(PfConcept.class, key);

        assertThat(result).isEqualTo(expectedResult);
    }

    @Test
    void testGetAll1() {
        final List<PfConcept> expectedResult = List.of(new PfConceptKey("name", "1.0.0"));

        lenient().when(mockMg.createQuery(anyString(), eq(PfConcept.class))).thenReturn(mockQuery);
        lenient().when(mockQuery.getResultList()).thenReturn(expectedResult);
        final List<PfConcept> result = proxyDaoUnderTest.getAll(PfConcept.class);

        assertThat(result).isEqualTo(expectedResult);
    }

    @Test
    void testGetAll2() {
        final PfConceptKey parentKey = new PfConceptKey("name", "1.0.0");
        final List<PfConcept> expectedResult = List.of(parentKey);

        lenient().when(mockMg.createQuery(anyString(), eq(PfConcept.class))).thenReturn(mockQuery);
        lenient().when(mockQuery.setParameter(anyString(), any())).thenReturn(mockQuery);
        lenient().when(mockQuery.getResultList()).thenReturn(expectedResult);

        final List<PfConcept> result = proxyDaoUnderTest.getAll(PfConcept.class, parentKey);

        assertThat(result).isEqualTo(expectedResult);
    }

    @Test
    void testGetAll3() {
        final List<PfConcept> expectedResult = List.of(new PfConceptKey("name", "1.0.0"));
        lenient().when(mockMg.createQuery(anyString(), eq(PfConcept.class))).thenReturn(mockQuery);
        lenient().when(mockQuery.setMaxResults(1)).thenReturn(mockQuery);
        lenient().when(mockQuery.getResultList())
            .thenReturn(List.of(new PfConceptKey("name", "1.0.0")));

        final List<PfConcept> result = proxyDaoUnderTest.getAll(PfConcept.class, "name", 1);

        assertThat(result).isEqualTo(expectedResult);
    }

    @Test
    void testGetAllVersionsByParent() {
        final List<PfConcept> expectedResult = List.of(new PfConceptKey("name", "1.0.0"));
        lenient().when(mockMg.createQuery(anyString(), eq(PfConcept.class))).thenReturn(mockQuery);
        lenient().when(mockQuery.setParameter(anyString(), anyString())).thenReturn(mockQuery);
        lenient().when(mockQuery.getResultList())
            .thenReturn(List.of(new PfConceptKey("name", "1.0.0")));

        final List<PfConcept> result = proxyDaoUnderTest.getAllVersionsByParent(PfConcept.class, "name");

        assertThat(result).isEqualTo(expectedResult);
    }

    @Test
    void testGetAllVersions() {
        final List<PfConcept> expectedResult = List.of(new PfConceptKey("name", "1.0.0"));
        lenient().when(mockMg.createQuery(anyString(), eq(PfConcept.class))).thenReturn(mockQuery);
        lenient().when(mockQuery.setParameter(anyString(), anyString())).thenReturn(mockQuery);
        lenient().when(mockQuery.getResultList())
            .thenReturn(List.of(new PfConceptKey("name", "1.0.0")));

        final List<PfConcept> result = proxyDaoUnderTest.getAllVersions(PfConcept.class, "name");

        assertThat(result).isEqualTo(expectedResult);
    }

    @Test
    void testGetConcept1() {
        final PfConceptKey key = new PfConceptKey("name", "1.0.0");
        final PfConcept expectedResult = key;
        lenient().when(mockMg.createQuery(anyString(), eq(PfConcept.class))).thenReturn(mockQuery);
        lenient().when(mockQuery.setParameter(anyString(), anyString())).thenReturn(mockQuery);
        lenient().when(mockQuery.getResultList()).thenReturn(List.of(key));

        final PfConcept result = proxyDaoUnderTest.getConcept(PfConcept.class, key);

        assertThat(result).isEqualTo(expectedResult);
    }

    @Test
    void testGetConcept2() {
        final PfReferenceKey key = new PfReferenceKey();
        final PfConcept expectedResult = key;
        lenient().when(mockMg.createQuery(anyString(), eq(PfConcept.class))).thenReturn(mockQuery);
        lenient().when(mockQuery.setParameter(anyString(), anyString())).thenReturn(mockQuery);
        lenient().when(mockQuery.getResultList()).thenReturn(List.of(key));

        final PfConcept result = proxyDaoUnderTest.getConcept(PfConcept.class, key);

        assertThat(result).isEqualTo(expectedResult);
    }

    @Test
    void testUpdate() {
        final PfConcept obj = null;
        final PfConcept expectedResult = null;
        when(mockMg.merge(null)).thenReturn(null);

        final PfConcept result = proxyDaoUnderTest.update(obj);

        assertThat(result).isEqualTo(expectedResult);
        verify(mockMg).flush();
    }
}
