/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2019 Nordix Foundation.
 *  Modifications Copyright (C) 2019 AT&T Intellectual Property. All rights reserved.
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

package org.onap.policy.models.base;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;
import org.junit.Test;

import org.onap.policy.models.base.testconcepts.DummyPfObject;
import org.onap.policy.models.base.testconcepts.DummyPfObjectFilter;

/**
 * Test the {@link PfObjectFilter} interface.
 *
 * @author Liam Fallon (liam.fallon@est.tech)
 */
public class PfObjectFilterTest {

    private static final String NAME1 = "name1";
    private static final String NAME0 = "name0";
    private static final String HELLO = "Hello";
    private static final String DESCRIPTION1 = "Desc 1";
    private static final String VERSION100 = "1.0.0";
    private static final String VERSION002 = "0.0.2";

    @Test
    public void testPfObjectInterface() {
        DummyPfObject do0 = new DummyPfObject();
        do0.setName(NAME0);
        do0.setVersion(VERSION100);
        do0.setDescription("desc0 ");

        DummyPfObject do1 = new DummyPfObject();
        do1.setName(NAME0);
        do1.setVersion("0.0.1");
        do1.setDescription(DESCRIPTION1);

        DummyPfObject do2 = new DummyPfObject();
        do2.setName(NAME0);
        do2.setVersion(VERSION002);
        do2.setDescription(DESCRIPTION1);

        DummyPfObject do3 = new DummyPfObject();
        do3.setName(NAME1);
        do3.setVersion("0.0.1");
        do3.setDescription("desc0 ");

        DummyPfObject do4 = new DummyPfObject();
        do4.setName(NAME1);
        do4.setVersion("0.1.2");
        do4.setDescription(DESCRIPTION1);

        DummyPfObject do5 = new DummyPfObject();
        do5.setName("aaaaa");
        do5.setVersion(VERSION002);
        do5.setDescription(DESCRIPTION1);

        List<DummyPfObject> doList = new ArrayList<>();
        doList.add(do0);
        doList.add(do1);
        doList.add(do2);
        doList.add(do3);
        doList.add(do4);
        doList.add(do5);

        DummyPfObjectFilter dof = new DummyPfObjectFilter();
        assertFalse(dof.filterString(HELLO, "Goodbye"));
        assertTrue(dof.filterString(HELLO, HELLO));

        assertEquals(false, dof.filterString(HELLO, "Goodbye"));
        assertEquals(true, dof.filterString(HELLO, HELLO));
        assertEquals(true, dof.filterString(HELLO, null));

        List<DummyPfObject> latestVersionList = dof.latestVersionFilter(doList);
        assertEquals(3, latestVersionList.size());
        assertEquals("aaaaa", latestVersionList.get(0).getName());
        assertEquals(VERSION002, latestVersionList.get(0).getVersion());
        assertEquals(NAME0, latestVersionList.get(1).getName());
        assertEquals(VERSION100, latestVersionList.get(1).getVersion());
        assertEquals(NAME1, latestVersionList.get(2).getName());
        assertEquals("0.1.2", latestVersionList.get(2).getVersion());

        latestVersionList.remove(2);
        latestVersionList.remove(1);
        List<DummyPfObject> newestVersionList = dof.latestVersionFilter(latestVersionList);
        assertEquals(latestVersionList, newestVersionList);

        MyFilter filter = new MyFilter();

        assertEquals(true, filter.filterString(null, HELLO));

        DummyPfObject doNullVersion = new DummyPfObject();
        do5.setName("bbbbb");

        assertEquals(false, filter(filter::filterStringPred, DummyPfObject::getVersion, doNullVersion, VERSION100));
        assertEquals(false, filter(filter::filterStringPred, DummyPfObject::getVersion, do0, "1"));
        assertEquals(false, filter(filter::filterStringPred, DummyPfObject::getVersion, do0, "2.0.0"));
        assertEquals(true, filter(filter::filterStringPred, DummyPfObject::getVersion, doNullVersion, null));
        assertEquals(true, filter(filter::filterStringPred, DummyPfObject::getVersion, do0, null));
        assertEquals(true, filter(filter::filterStringPred, DummyPfObject::getVersion, do0, VERSION100));

        assertEquals(false, filter(filter::filterPrefixPred, DummyPfObject::getVersion, doNullVersion, "1."));
        assertEquals(false, filter(filter::filterPrefixPred, DummyPfObject::getVersion, do0, "1.1"));
        assertEquals(false, filter(filter::filterPrefixPred, DummyPfObject::getVersion, do0, "1.1"));
        assertEquals(false, filter(filter::filterPrefixPred, DummyPfObject::getVersion, do0, "2"));
        assertEquals(true, filter(filter::filterPrefixPred, DummyPfObject::getVersion, doNullVersion, null));
        assertEquals(true, filter(filter::filterPrefixPred, DummyPfObject::getVersion, do0, null));
        assertEquals(true, filter(filter::filterPrefixPred, DummyPfObject::getVersion, do0, "1."));
        assertEquals(true, filter(filter::filterPrefixPred, DummyPfObject::getVersion, do0, "1.0."));
        assertEquals(true, filter(filter::filterPrefixPred, DummyPfObject::getVersion, do0, VERSION100));

        assertEquals(false, filter(filter::filterRegexpPred, DummyPfObject::getVersion, doNullVersion, "1[.].*"));
        assertEquals(false, filter(filter::filterRegexpPred, DummyPfObject::getVersion, do0, "2[.].*"));
        assertEquals(true, filter(filter::filterRegexpPred, DummyPfObject::getVersion, doNullVersion, null));
        assertEquals(true, filter(filter::filterRegexpPred, DummyPfObject::getVersion, do0, null));
        assertEquals(true, filter(filter::filterRegexpPred, DummyPfObject::getVersion, do0, "1[.].*"));
        assertEquals(true, filter(filter::filterRegexpPred, DummyPfObject::getVersion, do0, "1[.]0[.].*"));
        assertEquals(true, filter(filter::filterRegexpPred, DummyPfObject::getVersion, do0, "1[.]0[.]0"));
        assertEquals(true, filter(filter::filterRegexpPred, DummyPfObject::getVersion, do0, "1...."));
    }

    private boolean filter(BiFunction<String, Function<DummyPfObject, String>, Predicate<DummyPfObject>> predMaker,
                    Function<DummyPfObject, String> extractor, DummyPfObject dpo, String text) {
        Predicate<DummyPfObject> pred = predMaker.apply(text, extractor);
        return pred.test(dpo);
    }

    private static class MyFilter implements PfObjectFilter<DummyPfObject> {
        @Override
        public List<DummyPfObject> filter(List<DummyPfObject> originalList) {
            return null;
        }
    }
}
