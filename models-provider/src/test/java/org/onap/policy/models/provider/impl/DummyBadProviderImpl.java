/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2019-2023 Nordix Foundation.
 *  Modifications Copyright (C) 2019, 2021 AT&T Intellectual Property. All rights reserved.
 *  Modifications Copyright (C) 2020, 2022 Bell Canada. All rights reserved.
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

package org.onap.policy.models.provider.impl;

import jakarta.ws.rs.core.Response;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import lombok.NonNull;
import org.onap.policy.models.base.PfModelException;
import org.onap.policy.models.base.PfModelRuntimeException;
import org.onap.policy.models.pdp.concepts.Pdp;
import org.onap.policy.models.pdp.concepts.PdpGroup;
import org.onap.policy.models.pdp.concepts.PdpGroupFilter;
import org.onap.policy.models.pdp.concepts.PdpPolicyStatus;
import org.onap.policy.models.pdp.concepts.PdpSubGroup;
import org.onap.policy.models.provider.PolicyModelsProvider;
import org.onap.policy.models.tosca.authorative.concepts.ToscaConceptIdentifierOptVersion;
import org.onap.policy.models.tosca.authorative.concepts.ToscaEntityFilter;
import org.onap.policy.models.tosca.authorative.concepts.ToscaEntityKey;
import org.onap.policy.models.tosca.authorative.concepts.ToscaNodeTemplate;
import org.onap.policy.models.tosca.authorative.concepts.ToscaPolicy;
import org.onap.policy.models.tosca.authorative.concepts.ToscaPolicyType;
import org.onap.policy.models.tosca.authorative.concepts.ToscaServiceTemplate;
import org.onap.policy.models.tosca.authorative.concepts.ToscaTypedEntityFilter;

/**
 * Dummy implementation of {@link PolicyModelsProvider} with bad constructor.
 *
 * @author Liam Fallon (liam.fallon@est.tech)
 */
public class DummyBadProviderImpl implements PolicyModelsProvider {
    public DummyBadProviderImpl() {
        throw new PfModelRuntimeException(Response.Status.BAD_REQUEST, "Bad Request");
    }

    @Override
    public void close() {
        // do nothing
    }

    @Override
    public void init() {
        // do nothing
    }

    @Override
    public List<ToscaServiceTemplate> getFilteredServiceTemplateList(
            @NonNull ToscaEntityFilter<ToscaServiceTemplate> filter) {
        return null;
    }

    @Override
    public ToscaServiceTemplate createServiceTemplate(@NonNull ToscaServiceTemplate serviceTemplate) {
        return null;
    }

    @Override
    public ToscaServiceTemplate updateServiceTemplate(@NonNull ToscaServiceTemplate serviceTemplate) {
        return null;
    }

    @Override
    public ToscaServiceTemplate deleteServiceTemplate(@NonNull String name, @NonNull String version) {
        return null;
    }

    @Override
    public ToscaServiceTemplate getPolicyTypes(final String name, final String version) {
        return null;
    }

    @Override
    public ToscaServiceTemplate createPolicyTypes(@NonNull ToscaServiceTemplate serviceTemplate) {
        return null;
    }

    @Override
    public ToscaServiceTemplate updatePolicyTypes(@NonNull ToscaServiceTemplate serviceTemplate) {
        return null;
    }

    @Override
    public ToscaServiceTemplate deletePolicyType(final String name, final String version) {
        return null;
    }

    @Override
    public ToscaServiceTemplate getPolicies(final String name, final String version) {
        return null;
    }

    @Override
    public ToscaServiceTemplate createPolicies(@NonNull ToscaServiceTemplate serviceTemplate) {
        return null;
    }

    @Override
    public ToscaServiceTemplate updatePolicies(@NonNull ToscaServiceTemplate serviceTemplate) {
        return null;
    }

    @Override
    public ToscaServiceTemplate deletePolicy(final String name, final String version) {
        return null;
    }

    @Override
    public ToscaServiceTemplate createToscaNodeTemplates(@NonNull ToscaServiceTemplate serviceTemplate) {
        return null;
    }

    @Override
    public ToscaServiceTemplate updateToscaNodeTemplates(@NonNull ToscaServiceTemplate serviceTemplate)
        throws PfModelRuntimeException {
        return null;
    }

    @Override
    public ToscaServiceTemplate deleteToscaNodeTemplate(@NonNull String name, @NonNull String version) {
        return null;
    }

    @Override
    public List<Map<ToscaEntityKey, Map<String, Object>>> getNodeTemplateMetadataSets(@NonNull String name,
                                                                                      @NonNull String version) {
        return null;
    }

    @Override
    public List<ToscaNodeTemplate> getToscaNodeTemplate(final String name, final String version) {
        return null;
    }

    @Override
    public List<PdpGroup> getPdpGroups(String name) {
        return Collections.emptyList();
    }

    @Override
    public List<PdpGroup> createPdpGroups(@NonNull List<PdpGroup> pdpGroups) {
        return Collections.emptyList();
    }

    @Override
    public List<PdpGroup> updatePdpGroups(@NonNull List<PdpGroup> pdpGroups) {
        return Collections.emptyList();
    }

    @Override
    public void updatePdp(@NonNull String pdpGroupName, @NonNull String pdpSubGroup, @NonNull Pdp pdp) {
        // do nothing
    }

    @Override
    public PdpGroup deletePdpGroup(@NonNull String name) {
        return null;
    }

    @Override
    public List<ToscaPolicyType> getPolicyTypeList(String name, String version) {
        return Collections.emptyList();
    }

    @Override
    public ToscaServiceTemplate getFilteredPolicyTypes(@NonNull ToscaEntityFilter<ToscaPolicyType> filter) {
        return null;
    }

    @Override
    public List<ToscaPolicyType> getFilteredPolicyTypeList(@NonNull ToscaEntityFilter<ToscaPolicyType> filter) {
        return Collections.emptyList();
    }

    @Override
    public List<ToscaPolicy> getPolicyList(String name, String version) {
        return Collections.emptyList();
    }

    @Override
    public ToscaServiceTemplate getFilteredPolicies(@NonNull ToscaTypedEntityFilter<ToscaPolicy> filter) {
        return null;
    }

    @Override
    public List<ToscaPolicy> getFilteredPolicyList(@NonNull ToscaTypedEntityFilter<ToscaPolicy> filter) {
        return Collections.emptyList();
    }

    @Override
    public List<PdpGroup> getFilteredPdpGroups(@NonNull PdpGroupFilter filter) {
        return Collections.emptyList();
    }

    @Override
    public void updatePdpSubGroup(@NonNull String pdpGroupName, @NonNull PdpSubGroup pdpSubGroup) {
        // do nothing
    }

    @Override
    public List<PdpPolicyStatus> getAllPolicyStatus() {
        // Not implemented
        return null;
    }

    @Override
    public List<PdpPolicyStatus> getAllPolicyStatus(@NonNull ToscaConceptIdentifierOptVersion policy) {
        // Not implemented
        return null;
    }

    @Override
    public List<PdpPolicyStatus> getGroupPolicyStatus(@NonNull String groupName) {
        // Not implemented
        return null;
    }

    @Override
    public void cudPolicyStatus(Collection<PdpPolicyStatus> createObjs, Collection<PdpPolicyStatus> updateObjs,
            Collection<PdpPolicyStatus> deleteObjs) {
        // Not implemented
    }

    @Override
    public List<ToscaServiceTemplate> getServiceTemplateList(String name, String version) {
        // Not implemented
        return null;
    }
}
