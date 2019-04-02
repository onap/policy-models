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

package org.onap.policy.models.pdp.persistence.provider;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import lombok.NonNull;

import org.apache.commons.lang3.tuple.Pair;
import org.onap.policy.models.base.PfModelException;
import org.onap.policy.models.dao.PfDao;
import org.onap.policy.models.pdp.concepts.PdpGroup;
import org.onap.policy.models.pdp.concepts.PdpGroups;
import org.onap.policy.models.pdp.concepts.PdpStatistics;
import org.onap.policy.models.pdp.concepts.PdpSubGroup;
import org.onap.policy.models.tosca.authorative.concepts.ToscaPolicy;

/**
 * This class provides the provision of information on PAP concepts in the database to callers.
 *
 * @author Liam Fallon (liam.fallon@est.tech)
 */
public class PdpProvider {
    /**
     * Get PDP groups.
     *
     * @param dao the DAO to use to access the database
     * @param name the name of the policy to get, null to get all PDP groups
     * @param version the version of the policy to get, null to get all versions of a PDP group
     * @return the PDP groups found
     * @throws PfModelException on errors getting PDP groups
     */
    public PdpGroups getPdpGroups(@NonNull final PfDao dao, final String name, final String version)
            throws PfModelException {
        return new PdpGroups();
    }

    /**
     * Get latest PDP Groups.
     *
     * @param dao the DAO to use to access the database
     * @param name the name of the PDP group to get, null to get all PDP groups
     * @return the PDP groups found
     * @throws PfModelException on errors getting policies
     */
    public PdpGroups getLatestPdpGroups(@NonNull final PfDao dao, final String name) throws PfModelException {
        return new PdpGroups();
    }

    /**
     * Get a filtered list of PDP groups.
     *
     * @param dao the DAO to use to access the database
     * @param pdpType The PDP type filter for the returned PDP groups
     * @param supportedPolicyTypes a list of policy type name/version pairs that the PDP groups must support.
     * @return the PDP groups found
     */
    public PdpGroups getFilteredPdpGroups(@NonNull final PfDao dao, @NonNull final String pdpType,
            @NonNull final List<Pair<String, String>> supportedPolicyTypes) {
        return new PdpGroups();
    }

    /**
     * Creates PDP groups.
     *
     * @param dao the DAO to use to access the database
     * @param pdpGroups a specification of the PDP groups to create
     * @return the PDP groups created
     * @throws PfModelException on errors creating PDP groups
     */
    public PdpGroups createPdpGroups(@NonNull final PfDao dao, @NonNull final PdpGroups pdpGroups)
            throws PfModelException {
        return new PdpGroups();
    }

    /**
     * Updates PDP groups.
     *
     * @param dao the DAO to use to access the database
     * @param pdpGroups a specification of the PDP groups to update
     * @return the PDP groups updated
     * @throws PfModelException on errors updating PDP groups
     */
    public PdpGroups updatePdpGroups(@NonNull final PfDao dao, @NonNull final PdpGroups pdpGroups)
            throws PfModelException {
        return new PdpGroups();
    }


    /**
     * Update a PDP subgroup.
     *
     * @param dao the DAO to use to access the database
     * @param pdpGroupName the name of the PDP group of the PDP subgroup
     * @param pdpGroupVersion the version of the PDP group of the PDP subgroup
     * @param pdpSubGroup the PDP subgroup to be updated
     * @throws PfModelException on errors updating PDP subgroups
     */
    public void updatePdpSubGroup(@NonNull final PfDao dao, @NonNull final String pdpGroupName,
            @NonNull final String pdpGroupVersion, @NonNull final PdpSubGroup pdpSubGroup) throws PfModelException {
        // Not implemented yet
    }

    /**
     * Delete a PDP group.
     *
     * @param dao the DAO to use to access the database
     * @param name the name of the policy to get, null to get all PDP groups
     * @param version the version of the policy to get, null to get all versions of a PDP group
     * @return the PDP group deleted
     * @throws PfModelException on errors deleting PDP groups
     */
    public PdpGroup deletePdpGroup(@NonNull final PfDao dao, @NonNull final String name, @NonNull final String version)
            throws PfModelException {
        return new PdpGroup();

    }

    /**
     * Get PDP statistics.
     *
     * @param dao the DAO to use to access the database
     * @param name the name of the PDP group to get statistics for, null to get all PDP groups
     * @param version the version of the PDP group to get statistics for, null to get all versions of a PDP group
     * @return the statistics found
     * @throws PfModelException on errors getting statistics
     */
    public List<PdpStatistics> getPdpStatistics(@NonNull final PfDao dao, final String name, final String version)
            throws PfModelException {
        return new ArrayList<>();
    }

    /**
     * Update PDP statistics for a PDP.
     *
     * @param dao the DAO to use to access the database
     * @param pdpGroupName the name of the PDP group containing the PDP that the statistics are for
     * @param pdpGroupVersion the version of the PDP group containing the PDP that the statistics are for
     * @param pdpType the PDP type of the subgroup containing the PDP that the statistics are for
     * @param pdpInstanceId the instance ID of the PDP to update statistics for
     * @throws PfModelException on errors updating statistics
     */
    public void updatePdpStatistics(@NonNull final PfDao dao, @NonNull final String pdpGroupName,
            @NonNull final String pdpGroupVersion, @NonNull final String pdpType, @NonNull final String pdpInstanceId,
            @NonNull final PdpStatistics pdppStatistics)  throws PfModelException {
        // Not implemented yet
    }

    /**
     * Get deployed policies.
     *
     * @param dao the DAO to use to access the database
     * @param name the name of the policy to get, null to get all policies
     * @return the policies deployed as a map of policy lists keyed by PDP group
     * @throws PfModelException on errors getting policies
     */
    public Map<PdpGroup, List<ToscaPolicy>> getDeployedPolicyList(@NonNull final PfDao dao, final String name)
            throws PfModelException {
        return new LinkedHashMap<>();
    }
}
