/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2019, 2024 Nordix Foundation.
 *  Modifications Copyright (C) 2019, 2021 AT&T Intellectual Property. All rights reserved.
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

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.Properties;
import org.junit.jupiter.api.Test;
import org.onap.policy.models.dao.converters.CDataConditioner;
import org.onap.policy.models.dao.converters.Uuid2String;

class DaoMiscTest {

    private static final String SOMEWHERE_OVER_THE_RAINBOW = "somewhere.over.the.rainbow";

    @Test
    void testUuid2StringMopUp() {
        final Uuid2String uuid2String = new Uuid2String();
        assertEquals("", uuid2String.convertToDatabaseColumn(null));
    }

    @Test
    void testCDataConditionerMopUp() {
        assertNull(CDataConditioner.clean(null));
    }

    @Test
    void testDaoFactory() {
        final DaoParameters daoParameters = new DaoParameters();

        daoParameters.setPluginClass(SOMEWHERE_OVER_THE_RAINBOW);
        assertThatThrownBy(() -> new PfDaoFactory().createPfDao(daoParameters)).hasMessage(
                        "Policy Framework DAO class not found for DAO plugin \"somewhere.over.the.rainbow\"");

        daoParameters.setPluginClass("java.lang.String");
        assertThatThrownBy(() -> new PfDaoFactory().createPfDao(daoParameters)).hasMessage(
                        "Specified DAO plugin class \"java.lang.String\" " + "does not implement the PfDao interface");
    }

    @Test
    void testDaoParameters() {
        final DaoParameters pars = new DaoParameters();
        pars.setJdbcProperties(new Properties());
        assertEquals(0, pars.getJdbcProperties().size());

        pars.setJdbcProperty("name", "Dorothy");
        assertEquals("Dorothy", pars.getJdbcProperty("name"));

        pars.setPersistenceUnit("Kansas");
        assertEquals("Kansas", pars.getPersistenceUnit());

        pars.setPluginClass(SOMEWHERE_OVER_THE_RAINBOW);
        assertEquals(SOMEWHERE_OVER_THE_RAINBOW, pars.getPluginClass());

        assertEquals("DaoParameters(pluginClass=somewhere.over.the.rainbow, "
                + "persistenceUnit=Kansas, jdbcProperties={name=Dorothy})", pars.toString());
    }
}
