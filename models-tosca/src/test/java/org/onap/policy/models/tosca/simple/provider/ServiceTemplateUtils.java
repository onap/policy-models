/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2021 Nordix Foundation.
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

package org.onap.policy.models.tosca.simple.provider;

import org.onap.policy.models.base.PfConceptKey;
import org.onap.policy.models.base.PfModelException;
import org.onap.policy.models.dao.PfDao;
import org.onap.policy.models.tosca.simple.concepts.JpaToscaDataType;
import org.onap.policy.models.tosca.simple.concepts.JpaToscaDataTypes;
import org.onap.policy.models.tosca.simple.concepts.JpaToscaServiceTemplate;

public class ServiceTemplateUtils {
    static JpaToscaServiceTemplate prepareDbServiceTemplate(PfDao pfDao, String customName, String customVersion) throws
        PfModelException {
        final var provider = new SimpleToscaServiceTemplateProvider();
        // Prepare default service template
        final var serviceTemplateDefault = new JpaToscaServiceTemplate();
        final var dataType1Key = new PfConceptKey("DataType1", "0.0.3");
        final var dataType1 = new JpaToscaDataType();
        dataType1.setKey(dataType1Key);
        serviceTemplateDefault.setDataTypes(new JpaToscaDataTypes());
        serviceTemplateDefault.getDataTypes().getConceptMap().put(dataType1Key, dataType1);

        provider.write(pfDao, serviceTemplateDefault);

        // prepare custom service template
        final var mainKey = new PfConceptKey(customName, customVersion);
        final var serviceTemplate = new JpaToscaServiceTemplate(mainKey);

        return provider.write(pfDao, serviceTemplate);
    }
}
