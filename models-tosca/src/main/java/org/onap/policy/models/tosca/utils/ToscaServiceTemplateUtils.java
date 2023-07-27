/*-
 * ============LICENSE_START=======================================================
 * Copyright (C) 2020, 2022-2023 Nordix Foundation.
 * Modifications Copyright (C) 2020-2021 AT&T Intellectual Property. All rights reserved.
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

package org.onap.policy.models.tosca.utils;

import jakarta.ws.rs.core.Response;
import java.util.Map.Entry;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.onap.policy.common.parameters.BeanValidationResult;
import org.onap.policy.models.base.PfConceptContainer;
import org.onap.policy.models.base.PfConceptKey;
import org.onap.policy.models.base.PfModelRuntimeException;
import org.onap.policy.models.base.Validated;
import org.onap.policy.models.tosca.authorative.concepts.ToscaEntity;
import org.onap.policy.models.tosca.simple.concepts.JpaToscaEntityType;
import org.onap.policy.models.tosca.simple.concepts.JpaToscaServiceTemplate;
import org.onap.policy.models.tosca.simple.concepts.JpaToscaTopologyTemplate;

/**
 * This utility class provides methods to manage service templates.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ToscaServiceTemplateUtils {

    /**
     * Add a service template fragment to a service template. All entities in the service template fragment must either
     * a) not exist on the original service template or b) be identical to entities on the original service template.
     *
     * @param originalTemplate the original service template
     * @param fragmentTemplate the fragment being added to the original service template
     * @return JpaToscaServiceTemplate
     */
    public static JpaToscaServiceTemplate addFragment(@NonNull final JpaToscaServiceTemplate originalTemplate,
            @NonNull final JpaToscaServiceTemplate fragmentTemplate) {

        var result = new BeanValidationResult("incoming fragment", fragmentTemplate);

        if (originalTemplate.compareToWithoutEntities(fragmentTemplate) != 0) {
            Validated.addResult(result, "service template",
                            originalTemplate.getKey(),
                            "does not equal existing service template");
        }

        var compositeTemplate = new JpaToscaServiceTemplate(originalTemplate);

        compositeTemplate.setDataTypes(
                addFragmentEntitites(compositeTemplate.getDataTypes(), fragmentTemplate.getDataTypes(), result));
        compositeTemplate.setPolicyTypes(
                addFragmentEntitites(compositeTemplate.getPolicyTypes(), fragmentTemplate.getPolicyTypes(), result));
        compositeTemplate.setNodeTypes(
                addFragmentEntitites(compositeTemplate.getNodeTypes(), fragmentTemplate.getNodeTypes(), result));

        if (originalTemplate.getTopologyTemplate() != null && fragmentTemplate.getTopologyTemplate() != null) {
            if (originalTemplate.getTopologyTemplate()
                    .compareToWithoutEntities(fragmentTemplate.getTopologyTemplate()) == 0) {
                compositeTemplate.getTopologyTemplate()
                        .setPolicies(addFragmentEntitites(compositeTemplate.getTopologyTemplate().getPolicies(),
                                fragmentTemplate.getTopologyTemplate().getPolicies(), result));
                compositeTemplate.getTopologyTemplate()
                    .setNodeTemplates(addFragmentEntitites(compositeTemplate.getTopologyTemplate().getNodeTemplates(),
                        fragmentTemplate.getTopologyTemplate().getNodeTemplates(), result));
            } else {
                Validated.addResult(result, "topology template",
                                originalTemplate.getTopologyTemplate().getKey(),
                                "does not equal existing topology template");
            }
        } else if (fragmentTemplate.getTopologyTemplate() != null) {
            compositeTemplate.setTopologyTemplate(new JpaToscaTopologyTemplate(fragmentTemplate.getTopologyTemplate()));
        }

        if (result.isValid()) {
            result.addResult(compositeTemplate.validate("composite template"));
        }

        if (!result.isValid()) {
            String message = result.getResult();
            throw new PfModelRuntimeException(Response.Status.NOT_ACCEPTABLE, message);
        }

        return compositeTemplate;
    }

    /**
     * Check entities from a fragment container can be added to an original container.
     *
     * @param <E> The type of TOSCA entity
     * @param <J> The type of the JPA TOSCA entity
     * @param <S> The type of container
     *
     * @param compositeContainer the original container
     * @param fragmentContainer the fragment being added to the original container
     * @return the composite container with the result
     */
    private static <E extends ToscaEntity, J extends JpaToscaEntityType<E>, S extends PfConceptContainer<J, E>>
            S addFragmentEntitites(final S compositeContainer, final S fragmentContainer,
                    final BeanValidationResult result) {

        if (compositeContainer == null) {
            return fragmentContainer;
        }

        if (fragmentContainer == null) {
            return compositeContainer;
        }

        var result2 = new BeanValidationResult("incoming fragment", fragmentContainer);
        var originalContainerMap = compositeContainer.getConceptMap();
        var fragmentContainerMap = fragmentContainer.getConceptMap();

        for (Entry<PfConceptKey, J> fragmentEntry : fragmentContainerMap.entrySet()) {
            J containerEntity = originalContainerMap.get(fragmentEntry.getKey());
            if (containerEntity != null && containerEntity.compareTo(fragmentEntry.getValue()) != 0) {
                Validated.addResult(result, "entity", fragmentEntry.getKey(), "does not equal existing entity");
            }
        }

        if (!result2.isClean()) {
            result.addResult(result2);
        }

        originalContainerMap.putAll(fragmentContainerMap);

        return compositeContainer;
    }
}
