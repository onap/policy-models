/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2019-2022 Nordix Foundation.
 *  Modifications Copyright (C) 2019, 2021 AT&T Intellectual Property. All rights reserved.
 *  Modifications Copyright (C) 2020 Bell Canada. All rights reserved.
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

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import javax.ws.rs.core.Response;
import lombok.NonNull;
import org.onap.policy.models.base.PfConceptKey;
import org.onap.policy.models.base.PfModelException;
import org.onap.policy.models.base.PfModelRuntimeException;
import org.onap.policy.models.pap.concepts.PolicyAudit;
import org.onap.policy.models.pap.persistence.provider.PolicyAuditProvider.AuditFilter;
import org.onap.policy.models.pdp.concepts.Pdp;
import org.onap.policy.models.pdp.concepts.PdpGroup;
import org.onap.policy.models.pdp.concepts.PdpGroupFilter;
import org.onap.policy.models.pdp.concepts.PdpPolicyStatus;
import org.onap.policy.models.pdp.concepts.PdpStatistics;
import org.onap.policy.models.pdp.concepts.PdpSubGroup;
import org.onap.policy.models.pdp.persistence.provider.PdpFilterParameters;
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
    public void close() throws PfModelException {
        // do nothing
    }

    @Override
    public void init() throws PfModelException {
        // do nothing
    }

    @Override
    public List<ToscaServiceTemplate> getFilteredServiceTemplateList(
            @NonNull ToscaEntityFilter<ToscaServiceTemplate> filter) throws PfModelException {
        return null;
    }

    @Override
    public ToscaServiceTemplate createServiceTemplate(@NonNull ToscaServiceTemplate serviceTemplate)
            throws PfModelException {
        return null;
    }

    @Override
    public ToscaServiceTemplate updateServiceTemplate(@NonNull ToscaServiceTemplate serviceTemplate)
            throws PfModelException {
        return null;
    }

    @Override
    public ToscaServiceTemplate deleteServiceTemplate(@NonNull String name, @NonNull String version)
            throws PfModelException {
        return null;
    }

    @Override
    public ToscaServiceTemplate getPolicyTypes(final String name, final String version) throws PfModelException {
        return null;
    }

    @Override
    public ToscaServiceTemplate createPolicyTypes(@NonNull ToscaServiceTemplate serviceTemplate)
            throws PfModelException {
        return null;
    }

    @Override
    public ToscaServiceTemplate updatePolicyTypes(@NonNull ToscaServiceTemplate serviceTemplate)
            throws PfModelException {
        return null;
    }

    @Override
    public ToscaServiceTemplate deletePolicyType(final String name, final String version) throws PfModelException {
        return null;
    }

    @Override
    public ToscaServiceTemplate getPolicies(final String name, final String version) throws PfModelException {
        return null;
    }

    @Override
    public ToscaServiceTemplate createPolicies(@NonNull ToscaServiceTemplate serviceTemplate) throws PfModelException {
        return null;
    }

    @Override
    public ToscaServiceTemplate updatePolicies(@NonNull ToscaServiceTemplate serviceTemplate) throws PfModelException {
        return null;
    }

    @Override
    public ToscaServiceTemplate deletePolicy(final String name, final String version) throws PfModelException {
        return null;
    }

    @Override
    public ToscaServiceTemplate createToscaNodeTemplates(@NonNull ToscaServiceTemplate serviceTemplate)
        throws PfModelException {
        return null;
    }

    @Override
    public ToscaServiceTemplate updateToscaNodeTemplates(@NonNull ToscaServiceTemplate serviceTemplate)
        throws PfModelRuntimeException, PfModelException {
        return null;
    }

    @Override
    public ToscaServiceTemplate deleteToscaNodeTemplate(@NonNull String name, @NonNull String version)
        throws PfModelException {
        return null;
    }

    @Override
    public List<Map<ToscaEntityKey, Map<String, Object>>> getNodeTemplateMetadataSets(@NonNull String name,
                                                                                      @NonNull String version)
        throws PfModelException {
        return null;
    }

    @Override
    public List<Map<PfConceptKey, ToscaNodeTemplate>> getToscaNodeTemplate(final String name,
                                                                           final String version)
        throws PfModelException {
        return null;
    }

    @Override
    public List<PdpGroup> getPdpGroups(String name) throws PfModelException {
        return Collections.emptyList();
    }

    @Override
    public List<PdpGroup> createPdpGroups(@NonNull List<PdpGroup> pdpGroups) throws PfModelException {
        return Collections.emptyList();
    }

    @Override
    public List<PdpGroup> updatePdpGroups(@NonNull List<PdpGroup> pdpGroups) throws PfModelException {
        return Collections.emptyList();
    }

    @Override
    public void updatePdp(@NonNull String pdpGroupName, @NonNull String pdpSubGroup, @NonNull Pdp pdp)
            throws PfModelException {
        // do nothing
    }

    @Override
    public PdpGroup deletePdpGroup(@NonNull String name) throws PfModelException {
        return null;
    }

    @Override
    public List<ToscaPolicyType> getPolicyTypeList(String name, String version) throws PfModelException {
        return Collections.emptyList();
    }

    @Override
    public ToscaServiceTemplate getFilteredPolicyTypes(@NonNull ToscaEntityFilter<ToscaPolicyType> filter)
            throws PfModelException {
        return null;
    }

    @Override
    public List<ToscaPolicyType> getFilteredPolicyTypeList(@NonNull ToscaEntityFilter<ToscaPolicyType> filter)
            throws PfModelException {
        return Collections.emptyList();
    }

    @Override
    public List<ToscaPolicy> getPolicyList(String name, String version) throws PfModelException {
        return Collections.emptyList();
    }

    @Override
    public ToscaServiceTemplate getFilteredPolicies(@NonNull ToscaTypedEntityFilter<ToscaPolicy> filter)
            throws PfModelException {
        return null;
    }

    @Override
    public List<ToscaPolicy> getFilteredPolicyList(@NonNull ToscaTypedEntityFilter<ToscaPolicy> filter)
            throws PfModelException {
        return Collections.emptyList();
    }

    @Override
    public List<PdpGroup> getFilteredPdpGroups(@NonNull PdpGroupFilter filter) throws PfModelException {
        return Collections.emptyList();
    }

    @Override
    public void updatePdpSubGroup(@NonNull String pdpGroupName, @NonNull PdpSubGroup pdpSubGroup)
            throws PfModelException {
        // do nothing
    }

    @Override
    public List<PdpStatistics> getFilteredPdpStatistics(PdpFilterParameters filterParams) throws PfModelException {
        // Not implemented
        return null;
    }

    @Override
    public List<PdpStatistics> createPdpStatistics(final List<PdpStatistics> pdpStatisticsList)
            throws PfModelException {
        // Not implemented
        return new ArrayList<>();
    }

    @Override
    public List<PdpStatistics> updatePdpStatistics(final List<PdpStatistics> pdpStatisticsList)
            throws PfModelException {
        // Not implemented
        return new ArrayList<>();
    }

    @Override
    public List<PdpStatistics> deletePdpStatistics(final String name, final Instant timestamp) {
        // Not implemented
        return null;
    }

    @Override
    public List<PdpPolicyStatus> getAllPolicyStatus() throws PfModelException {
        // Not implemented
        return null;
    }

    @Override
    public List<PdpPolicyStatus> getAllPolicyStatus(@NonNull ToscaConceptIdentifierOptVersion policy)
            throws PfModelException {
        // Not implemented
        return null;
    }

    @Override
    public List<PdpPolicyStatus> getGroupPolicyStatus(@NonNull String groupName) throws PfModelException {
        // Not implemented
        return null;
    }

    @Override
    public void cudPolicyStatus(Collection<PdpPolicyStatus> createObjs, Collection<PdpPolicyStatus> updateObjs,
            Collection<PdpPolicyStatus> deleteObjs) {
        // Not implemented
    }

    @Override
    public List<ToscaServiceTemplate> getServiceTemplateList(String name, String version) throws PfModelException {
        // Not implemented
        return null;
    }

    @Override
    public void createAuditRecords(List<PolicyAudit> auditRecords) {
        // Not implemented
    }

    @Override
    public List<PolicyAudit> getAuditRecords(AuditFilter auditFilter) {
        // Not implemented
        return null;
    }
}
