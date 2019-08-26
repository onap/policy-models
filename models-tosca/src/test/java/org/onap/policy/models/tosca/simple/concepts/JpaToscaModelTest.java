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

package org.onap.policy.models.tosca.simple.concepts;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

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
public class JpaToscaModelTest {

    private static final String KEY_IS_NULL = "key is marked @NonNull but is null";
    private static final String VERSION_001 = "0.0.1";

    @Test
    public void testModelPojo() {
        assertNotNull(new JpaToscaModel());
        assertNotNull(new JpaToscaModel(new PfConceptKey()));
        assertNotNull(new JpaToscaModel(new PfConceptKey(), new JpaToscaServiceTemplates()));
        assertNotNull(new JpaToscaModel(new JpaToscaModel()));

        assertThatThrownBy(() -> new JpaToscaModel((PfConceptKey) null)).hasMessage(KEY_IS_NULL);

        assertThatThrownBy(() -> new JpaToscaModel(null, null)).hasMessage(KEY_IS_NULL);

        assertThatThrownBy(() -> new JpaToscaModel(null, new JpaToscaServiceTemplates())).hasMessage(KEY_IS_NULL);

        assertThatThrownBy(() -> new JpaToscaModel(new PfConceptKey(), null))
                        .hasMessage("serviceTemplates is marked @NonNull but is null");

        assertThatThrownBy(() -> new JpaToscaModel((JpaToscaModel) null))
                        .hasMessage("copyConcept is marked @NonNull but is null");

        PfConceptKey tstsKey = new PfConceptKey("tsts", VERSION_001);
        Map<PfConceptKey, JpaToscaServiceTemplate> tstMap = new TreeMap<>();
        JpaToscaServiceTemplates tsts = new JpaToscaServiceTemplates(tstsKey, tstMap);
        PfConceptKey tmKey = new PfConceptKey("tst", VERSION_001);
        JpaToscaModel tm = new JpaToscaModel(tmKey, tsts);

        JpaToscaModel tttClone0 = new JpaToscaModel(tm);
        assertEquals(tm, tttClone0);
        assertEquals(0, tm.compareTo(tttClone0));

        JpaToscaModel tttClone1 = new JpaToscaModel(tm);
        assertEquals(tm, tttClone1);
        assertEquals(0, tm.compareTo(tttClone1));

        assertEquals(-1, tm.compareTo(null));
        assertEquals(0, tm.compareTo(tm));
        assertFalse(tm.compareTo(tm.getKey()) == 0);

        PfConceptKey otherDtKey = new PfConceptKey("otherDt", VERSION_001);
        JpaToscaModel otherDt = new JpaToscaModel(otherDtKey);

        assertFalse(tm.compareTo(otherDt) == 0);
        otherDt.setKey(tmKey);
        assertFalse(tm.compareTo(otherDt) == 0);
        otherDt.setServiceTemplates(tsts);
        assertEquals(0, tm.compareTo(otherDt));

        assertThatThrownBy(() -> new JpaToscaModel((JpaToscaModel) null))
                        .isInstanceOf(NullPointerException.class);

        assertEquals(2, tm.getKeys().size());
        assertEquals(2, new JpaToscaModel().getKeys().size());

        new JpaToscaModel().clean();
        tm.clean();
        assertEquals(tttClone0, tm);

        assertFalse(new JpaToscaModel().validate(new PfValidationResult()).isValid());
        assertFalse(tm.validate(new PfValidationResult()).isValid());

        tm.register();
        assertTrue(PfModelService.existsModel(tm.getServiceTemplates().getId()));
        PfModelService.deregisterModel(tm.getServiceTemplates().getId());

        assertThatThrownBy(() -> tm.validate(null)).hasMessage("resultIn is marked @NonNull but is null");
    }
}
