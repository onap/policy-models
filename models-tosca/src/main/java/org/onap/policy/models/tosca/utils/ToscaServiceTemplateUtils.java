/*-
 * ============LICENSE_START=======================================================
 * Copyright (C) 2020 Nordix Foundation.
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

import java.util.Map;
import java.util.Map.Entry;
import javax.ws.rs.core.Response;
import lombok.NonNull;
import org.onap.policy.models.base.PfConceptContainer;
import org.onap.policy.models.base.PfConceptKey;
import org.onap.policy.models.base.PfModelRuntimeException;
import org.onap.policy.models.base.PfValidationMessage;
import org.onap.policy.models.base.PfValidationResult;
import org.onap.policy.models.base.PfValidationResult.ValidationResult;
import org.onap.policy.models.tosca.authorative.concepts.ToscaEntity;
import org.onap.policy.models.tosca.simple.concepts.JpaToscaEntityType;
import org.onap.policy.models.tosca.simple.concepts.JpaToscaServiceTemplate;
import org.onap.policy.models.tosca.simple.concepts.JpaToscaTopologyTemplate;

/**
 * This utility class provides methods to manage service templates.
 */
public class ToscaServiceTemplateUtils {
    /**
     * Private constructor to prevent subclassing.
     */
    private ToscaServiceTemplateUtils() {
        // Private constructor to prevent subclassing
    }

    /**
     * Add a service template fragment to a service template. All entities in the service template fragment must either
     * a) not exist on the original service template or b) be identical to entities on the original service template.
     *
     * @param originalTemplate the original service template
     * @param fragmentTemplate the fragment being added to the original service template
     * @return
     */
    public static JpaToscaServiceTemplate addFragment(@NonNull final JpaToscaServiceTemplate originalTemplate,
            @NonNull final JpaToscaServiceTemplate fragmentTemplate) {
        PfValidationResult result = new PfValidationResult();

        if (originalTemplate.compareToWithoutEntities(fragmentTemplate) != 0) {
            result.addValidationMessage(new PfValidationMessage(originalTemplate.getKey(),
                    ToscaServiceTemplateUtils.class, ValidationResult.INVALID,
                    "service template in incoming fragment does not equal existing service template"));
        }

        JpaToscaServiceTemplate compositeTemplate = new JpaToscaServiceTemplate(originalTemplate);

        compositeTemplate.setDataTypes(
                addFragmentEntitites(compositeTemplate.getDataTypes(), fragmentTemplate.getDataTypes(), result));
        compositeTemplate.setPolicyTypes(
                addFragmentEntitites(compositeTemplate.getPolicyTypes(), fragmentTemplate.getPolicyTypes(), result));

        if (originalTemplate.getTopologyTemplate() != null && fragmentTemplate.getTopologyTemplate() != null) {
            if (originalTemplate.getTopologyTemplate()
                    .compareToWithoutEntities(fragmentTemplate.getTopologyTemplate()) == 0) {
                compositeTemplate.getTopologyTemplate()
                        .setPolicies(addFragmentEntitites(compositeTemplate.getTopologyTemplate().getPolicies(),
                                fragmentTemplate.getTopologyTemplate().getPolicies(), result));
            } else {
                result.addValidationMessage(new PfValidationMessage(originalTemplate.getTopologyTemplate().getKey(),
                        ToscaServiceTemplateUtils.class, ValidationResult.INVALID,
                        "topology template in incoming fragment does not equal existing topology template"));
            }
        } else if (fragmentTemplate.getTopologyTemplate() != null) {
            compositeTemplate.setTopologyTemplate(new JpaToscaTopologyTemplate(fragmentTemplate.getTopologyTemplate()));
        }

        if (result.isValid()) {
            result = compositeTemplate.validate(result);
        }

        if (!result.isValid()) {
            String message = result.toString();
            throw new PfModelRuntimeException(Response.Status.NOT_ACCEPTABLE, message);
        }

        return compositeTemplate;
    }

    /**
     * Check entities from a fragment container can be added to an original container.
     *
     * @param <S> The type of container
     *
     * @param compositeContainer the original container
     * @param fragmentContainer the fragment being added to the original container
     * @return the composite container with the result
     */
    @SuppressWarnings("unchecked")
    // @formatter:off
    private static
        <S extends PfConceptContainer<? extends JpaToscaEntityType<? extends ToscaEntity>, ? extends ToscaEntity>>
            S addFragmentEntitites(final S compositeContainer, final S fragmentContainer,
                    final PfValidationResult result) {

        if (compositeContainer == null) {
            return fragmentContainer;
        }

        if (fragmentContainer == null) {
            return compositeContainer;
        }

        for (Entry<PfConceptKey, ? extends JpaToscaEntityType<? extends ToscaEntity>> fragmentEntry : fragmentContainer
                .getConceptMap().entrySet()) {
            JpaToscaEntityType<? extends ToscaEntity> containerEntry =
                    compositeContainer.getConceptMap().get(fragmentEntry.getKey());
            if (containerEntry != null && !containerEntry.equals(fragmentEntry.getValue())) {
                result.addValidationMessage(new PfValidationMessage(fragmentEntry.getKey(),
                        ToscaServiceTemplateUtils.class,
                        ValidationResult.INVALID, "entity in incoming fragment does not equal existing entity"));
            }
        }

        // This use of a generic map is required to get around typing errors in directly adding the fragment map to the
        // original map
        @SuppressWarnings("rawtypes")
        Map originalContainerMap = compositeContainer.getConceptMap();
        originalContainerMap.putAll(fragmentContainer.getConceptMap());

        return compositeContainer;
    }
    // @formatter:on
}
