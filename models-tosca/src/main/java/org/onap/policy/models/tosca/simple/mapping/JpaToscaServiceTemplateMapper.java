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

package org.onap.policy.models.tosca.simple.mapping;

import org.onap.policy.models.tosca.simple.concepts.JpaToscaServiceTemplate;

/**
 * This interface is used to map legacy and proprietary policies into and out of TOSCA service templates.
 *
 * @author Liam Fallon (liam.fallon@est.tech)
 * @param <I> the type for the incoming policy definition
 * @param <O> the type for the outgoing policy definition
 */
public interface JpaToscaServiceTemplateMapper<I, O> {

    /**
     * Translate from the other format to a TOSCA service template.
     *
     * @param otherPolicyType the other policy type
     * @return the TOSCA service template
     */
    public JpaToscaServiceTemplate toToscaServiceTemplate(final I otherPolicyType);

    /**
     * Translate to the other format from a TOSCA service template.
     *
     * @param serviceTemplate the TOSCA service template
     * @return the policy in the other format
     */
    public O fromToscaServiceTemplate(final JpaToscaServiceTemplate serviceTemplate);
}
