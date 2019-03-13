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

import java.util.Map;
import java.util.TreeMap;

import org.junit.Test;
import org.onap.policy.models.base.PfConceptKey;
import org.onap.policy.models.base.PfModelService;
import org.onap.policy.models.base.PfValidationResult;

/**
 * DAO test for ToscaDatatype.
 *
 * @author Liam Fallon (liam.fallon@est.tech)
 */
public class ToscaModelTest {

    @Test
    public void testModelPojo() {
        assertNotNull(new ToscaModel());
        assertNotNull(new ToscaModel(new PfConceptKey()));
        assertNotNull(new ToscaModel(new PfConceptKey(), new ToscaServiceTemplates()));
        assertNotNull(new ToscaModel(new ToscaModel()));

        try {
            new ToscaModel((PfConceptKey) null);
            fail("test should throw an exception");
        } catch (Exception exc) {
            assertEquals("key is marked @NonNull but is null", exc.getMessage());
        }

        try {
            new ToscaModel(null, null);
            fail("test should throw an exception");
        } catch (Exception exc) {
            assertEquals("key is marked @NonNull but is null", exc.getMessage());
        }

        try {
            new ToscaModel(null, new ToscaServiceTemplates());
            fail("test should throw an exception");
        } catch (Exception exc) {
            assertEquals("key is marked @NonNull but is null", exc.getMessage());
        }

        try {
            new ToscaModel(new PfConceptKey(), null);
            fail("test should throw an exception");
        } catch (Exception exc) {
            assertEquals("serviceTemplates is marked @NonNull but is null", exc.getMessage());
        }

        try {
            new ToscaModel((ToscaModel) null);
            fail("test should throw an exception");
        } catch (Exception exc) {
            assertEquals("copyConcept is marked @NonNull but is null", exc.getMessage());
        }

        PfConceptKey tstsKey = new PfConceptKey("tsts", "0.0.1");
        Map<PfConceptKey, ToscaServiceTemplate> tstMap = new TreeMap<>();
        ToscaServiceTemplates tsts = new ToscaServiceTemplates(tstsKey, tstMap);
        PfConceptKey tmKey = new PfConceptKey("tst", "0.0.1");
        ToscaModel tm = new ToscaModel(tmKey, tsts);

        ToscaModel tttClone0 = new ToscaModel(tm);
        assertEquals(tm, tttClone0);
        assertEquals(0, tm.compareTo(tttClone0));

        ToscaModel tttClone1 = new ToscaModel();
        tm.copyTo(tttClone1);
        assertEquals(tm, tttClone1);
        assertEquals(0, tm.compareTo(tttClone1));

        assertEquals(-1, tm.compareTo(null));
        assertEquals(0, tm.compareTo(tm));
        assertFalse(tm.compareTo(tm.getKey()) == 0);

        PfConceptKey otherDtKey = new PfConceptKey("otherDt", "0.0.1");
        ToscaModel otherDt = new ToscaModel(otherDtKey);

        assertFalse(tm.compareTo(otherDt) == 0);
        otherDt.setKey(tmKey);
        assertFalse(tm.compareTo(otherDt) == 0);
        otherDt.setServiceTemplates(tsts);
        assertEquals(0, tm.compareTo(otherDt));

        try {
            tm.copyTo(null);
            fail("test should throw an exception");
        } catch (Exception exc) {
            assertEquals("targetObject is marked @NonNull but is null", exc.getMessage());
        }

        assertEquals(2, tm.getKeys().size());
        assertEquals(2, new ToscaModel().getKeys().size());

        new ToscaModel().clean();
        tm.clean();
        assertEquals(tttClone0, tm);

        assertFalse(new ToscaModel().validate(new PfValidationResult()).isValid());
        assertFalse(tm.validate(new PfValidationResult()).isValid());

        tm.register();
        assertTrue(PfModelService.existsModel(tm.getServiceTemplates().getId()));
        PfModelService.deregisterModel(tm.getServiceTemplates().getId());

        try {
            tm.validate(null);
            fail("test should throw an exception");
        } catch (Exception exc) {
            assertEquals("resultIn is marked @NonNull but is null", exc.getMessage());
        }
    }
}
