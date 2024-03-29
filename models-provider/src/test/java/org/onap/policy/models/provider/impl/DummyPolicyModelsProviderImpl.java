/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2019-2023 Nordix Foundation.
 *  Copyright (C) 2019, 2021 AT&T Intellectual Property. All rights reserved.
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
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import lombok.NonNull;
import org.onap.policy.common.utils.coder.StandardCoder;
import org.onap.policy.common.utils.resources.ResourceUtils;
import org.onap.policy.models.base.PfModelException;
import org.onap.policy.models.base.PfModelRuntimeException;
import org.onap.policy.models.pdp.concepts.Pdp;
import org.onap.policy.models.pdp.concepts.PdpGroup;
import org.onap.policy.models.pdp.concepts.PdpGroupFilter;
import org.onap.policy.models.pdp.concepts.PdpPolicyStatus;
import org.onap.policy.models.pdp.concepts.PdpSubGroup;
import org.onap.policy.models.provider.PolicyModelsProvider;
import org.onap.policy.models.provider.PolicyModelsProviderParameters;
import org.onap.policy.models.tosca.authorative.concepts.ToscaConceptIdentifierOptVersion;
import org.onap.policy.models.tosca.authorative.concepts.ToscaEntityFilter;
import org.onap.policy.models.tosca.authorative.concepts.ToscaEntityKey;
import org.onap.policy.models.tosca.authorative.concepts.ToscaNodeTemplate;
import org.onap.policy.models.tosca.authorative.concepts.ToscaPolicy;
import org.onap.policy.models.tosca.authorative.concepts.ToscaPolicyType;
import org.onap.policy.models.tosca.authorative.concepts.ToscaServiceTemplate;
import org.onap.policy.models.tosca.authorative.concepts.ToscaTypedEntityFilter;

/**
 * This class provides a dummy implementation of the Policy Models Provider for the ONAP Policy Framework.
 *
 * @author Liam Fallon (liam.fallon@est.tech)
 * @author Chenfei Gao (cgao@research.att.com)
 */
public class DummyPolicyModelsProviderImpl implements PolicyModelsProvider {
    /**
     * Constructor that takes the parameters.
     *
     * @param parameters the parameters for the provider
     */
    public DummyPolicyModelsProviderImpl(final PolicyModelsProviderParameters parameters) {
        // Default constructor
    }

    @Override
    public void init() {
        // Not required on the dummy provider
    }

    @Override
    public void close() {
        // Not required on the dummy provider
    }


    @Override
    public List<ToscaServiceTemplate> getServiceTemplateList(String name, String version) {
        return new ArrayList<>();
    }

    @Override
    public List<ToscaServiceTemplate> getFilteredServiceTemplateList(
            @NonNull ToscaEntityFilter<ToscaServiceTemplate> filter) {
        return new ArrayList<>();
    }

    @Override
    public ToscaServiceTemplate createServiceTemplate(@NonNull ToscaServiceTemplate serviceTemplate)
            throws PfModelException {
        return serviceTemplate;
    }

    @Override
    public ToscaServiceTemplate updateServiceTemplate(@NonNull ToscaServiceTemplate serviceTemplate)
            throws PfModelException {
        return serviceTemplate;
    }

    @Override
    public ToscaServiceTemplate deleteServiceTemplate(@NonNull String name, @NonNull String version)
            throws PfModelException {
        return getDummyResponse("dummyimpl/DummyToscaServiceTemplateDeleteResponse.json");
    }

    @Override
    public ToscaServiceTemplate getPolicyTypes(final String name, final String version) {
        return getDummyResponse("dummyimpl/DummyToscaPolicyTypeGetResponse.json");
    }

    @Override
    public List<ToscaPolicyType> getPolicyTypeList(final String name, final String version) {
        return new ArrayList<>();
    }

    @Override
    public ToscaServiceTemplate getFilteredPolicyTypes(ToscaEntityFilter<ToscaPolicyType> filter) {
        return getDummyResponse("dummyimpl/DummyToscaPolicyTypeGetResponse.json");
    }

    @Override
    public List<ToscaPolicyType> getFilteredPolicyTypeList(ToscaEntityFilter<ToscaPolicyType> filter) {
        return new ArrayList<>();
    }

    @Override
    public ToscaServiceTemplate createPolicyTypes(final ToscaServiceTemplate serviceTemplate) {
        return serviceTemplate;
    }

    @Override
    public ToscaServiceTemplate updatePolicyTypes(final ToscaServiceTemplate serviceTemplate) {
        return serviceTemplate;
    }

    @Override
    public ToscaServiceTemplate deletePolicyType(final String name, final String version) {
        return getDummyResponse("dummyimpl/DummyToscaPolicyTypeDeleteResponse.json");
    }

    @Override
    public ToscaServiceTemplate getPolicies(final String name, final String version) {
        return getDummyResponse("dummyimpl/DummyToscaPolicyGetResponse.json");
    }

    @Override
    public List<ToscaPolicy> getPolicyList(final String name, final String version) {
        return new ArrayList<>();
    }

    @Override
    public ToscaServiceTemplate getFilteredPolicies(ToscaTypedEntityFilter<ToscaPolicy> filter) {
        return getDummyResponse("dummyimpl/DummyToscaPolicyGetResponse.json");
    }

    @Override
    public List<ToscaPolicy> getFilteredPolicyList(ToscaTypedEntityFilter<ToscaPolicy> filter) {
        return new ArrayList<>();
    }

    @Override
    public ToscaServiceTemplate createPolicies(final ToscaServiceTemplate serviceTemplate) {
        return serviceTemplate;
    }

    @Override
    public ToscaServiceTemplate updatePolicies(final ToscaServiceTemplate serviceTemplate) {
        return serviceTemplate;
    }

    @Override
    public ToscaServiceTemplate deletePolicy(final String name, final String version) {
        return getDummyResponse("dummyimpl/DummyToscaPolicyDeleteResponse.json");
    }

    @Override
    public ToscaServiceTemplate createToscaNodeTemplates(@NonNull ToscaServiceTemplate serviceTemplate) {
        return getDummyNodeTemplateResponse("dummyimpl/DummyToscaNodeTemplateResponse.json");
    }

    @Override
    public ToscaServiceTemplate updateToscaNodeTemplates(@NonNull ToscaServiceTemplate serviceTemplate)
        throws PfModelRuntimeException {
        return getDummyNodeTemplateResponse("dummyimpl/DummyToscaNodeTemplateResponse.json");
    }

    @Override
    public ToscaServiceTemplate deleteToscaNodeTemplate(@NonNull String name, @NonNull String version) {
        return getDummyNodeTemplateResponse("dummyimpl/DummyToscaNodeTemplateResponse.json");
    }

    @Override
    public List<Map<ToscaEntityKey, Map<String, Object>>> getNodeTemplateMetadataSets(@NonNull String name,
                                                                                      @NonNull String version) {
        return new ArrayList<>();
    }

    @Override
    public List<ToscaNodeTemplate> getToscaNodeTemplate(final String name, final String version) {
        return new ArrayList<>();
    }

    @Override
    public List<PdpGroup> getPdpGroups(final String name) {
        return new ArrayList<>();
    }

    @Override
    public List<PdpGroup> getFilteredPdpGroups(PdpGroupFilter filter) {
        return new ArrayList<>();
    }

    @Override
    public List<PdpGroup> createPdpGroups(final List<PdpGroup> pdpGroups) {
        return new ArrayList<>();
    }

    @Override
    public List<PdpGroup> updatePdpGroups(final List<PdpGroup> pdpGroups) {
        return new ArrayList<>();
    }

    @Override
    public void updatePdpSubGroup(final String pdpGroupName, final PdpSubGroup pdpSubGroup) {
        // Not implemented
    }

    @Override
    public void updatePdp(String pdpGroupName, String pdpSubGroup, Pdp pdp) {
        // Not implemented
    }

    @Override
    public PdpGroup deletePdpGroup(final String name) {
        return null;
    }

    @Override
    public List<PdpPolicyStatus> getAllPolicyStatus() {
        // Not implemented
        return new ArrayList<>();
    }

    @Override
    public List<PdpPolicyStatus> getAllPolicyStatus(@NonNull ToscaConceptIdentifierOptVersion policy) {
        // Not implemented
        return new ArrayList<>();
    }

    @Override
    public List<PdpPolicyStatus> getGroupPolicyStatus(String groupName) {
        // Not implemented
        return new ArrayList<>();
    }

    @Override
    public void cudPolicyStatus(Collection<PdpPolicyStatus> createObjs, Collection<PdpPolicyStatus> updateObjs,
            Collection<PdpPolicyStatus> deleteObjs) {
        // Not implemented
    }

    /**
     * Return a ToscaServicetemplate dummy response.
     *
     * @param fileName the file name containing the dummy response
     * @return the ToscaServiceTemplate with the dummy response
     */
    protected ToscaServiceTemplate getDummyResponse(final String fileName) {
        var standardCoder = new StandardCoder();
        ToscaServiceTemplate serviceTemplate;

        try {
            serviceTemplate =
                    standardCoder.decode(ResourceUtils.getResourceAsString(fileName), ToscaServiceTemplate.class);
            if (serviceTemplate == null) {
                throw new PfModelException(Response.Status.INTERNAL_SERVER_ERROR, "error reading specified file");
            }
        } catch (Exception exc) {
            throw new PfModelRuntimeException(Response.Status.INTERNAL_SERVER_ERROR, "error serializing object", exc);
        }

        return serviceTemplate;
    }

    /**
     * Return a tosca node template dummy response.
     *
     * @param fileName the file name containing the dummy response
     * @return the service template with the dummy response
     */
    protected ToscaServiceTemplate getDummyNodeTemplateResponse(final String fileName) {
        var standardCoder = new StandardCoder();
        ToscaServiceTemplate serviceTemplate;

        try {
            serviceTemplate =
                standardCoder.decode(ResourceUtils.getResourceAsString(fileName), ToscaServiceTemplate.class);
            if (serviceTemplate == null) {
                throw new PfModelException(Response.Status.INTERNAL_SERVER_ERROR, "error reading specified file");
            }
        } catch (Exception exc) {
            throw new PfModelRuntimeException(Response.Status.INTERNAL_SERVER_ERROR, "error serializing object", exc);
        }

        return serviceTemplate;
    }
}
