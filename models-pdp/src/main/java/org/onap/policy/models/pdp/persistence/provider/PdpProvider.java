/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2019 Nordix Foundation.
 *  Modifications Copyright (C) 2019-2020 AT&T Intellectual Property. All rights reserved.
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
import java.util.List;
import javax.ws.rs.core.Response;
import lombok.NonNull;
import org.onap.policy.common.parameters.BeanValidationResult;
import org.onap.policy.models.base.PfConceptKey;
import org.onap.policy.models.base.PfKey;
import org.onap.policy.models.base.PfModelException;
import org.onap.policy.models.base.PfModelRuntimeException;
import org.onap.policy.models.base.PfReferenceKey;
import org.onap.policy.models.dao.PfDao;
import org.onap.policy.models.pdp.concepts.Pdp;
import org.onap.policy.models.pdp.concepts.PdpGroup;
import org.onap.policy.models.pdp.concepts.PdpGroupFilter;
import org.onap.policy.models.pdp.concepts.PdpStatistics;
import org.onap.policy.models.pdp.concepts.PdpSubGroup;
import org.onap.policy.models.pdp.persistence.concepts.JpaPdp;
import org.onap.policy.models.pdp.persistence.concepts.JpaPdpGroup;
import org.onap.policy.models.pdp.persistence.concepts.JpaPdpSubGroup;

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
     * @param name the name of the PDP group to get, null to get all PDP groups
     * @return the PDP groups found
     * @throws PfModelException on errors getting PDP groups
     */
    public List<PdpGroup> getPdpGroups(@NonNull final PfDao dao, final String name) throws PfModelException {

        return asPdpGroupList(dao.getFiltered(JpaPdpGroup.class, name, PfKey.NULL_KEY_VERSION));
    }

    /**
     * Get filtered PDP groups.
     *
     * @param dao the DAO to use to access the database
     * @param filter the filter for the PDP groups to get
     * @return the PDP groups found
     * @throws PfModelException on errors getting policies
     */
    public List<PdpGroup> getFilteredPdpGroups(@NonNull final PfDao dao, @NonNull final PdpGroupFilter filter) {

        return filter.filter(
                        asPdpGroupList(dao.getFiltered(JpaPdpGroup.class, filter.getName(), PfKey.NULL_KEY_VERSION)));
    }

    /**
     * Creates PDP groups.
     *
     * @param dao the DAO to use to access the database
     * @param pdpGroups a specification of the PDP groups to create
     * @return the PDP groups created
     * @throws PfModelException on errors creating PDP groups
     */
    public List<PdpGroup> createPdpGroups(@NonNull final PfDao dao, @NonNull final List<PdpGroup> pdpGroups)
            throws PfModelException {

        for (PdpGroup pdpGroup : pdpGroups) {
            JpaPdpGroup jpaPdpGroup = new JpaPdpGroup();
            jpaPdpGroup.fromAuthorative(pdpGroup);

            BeanValidationResult validationResult = jpaPdpGroup.validate("PDP group");
            if (!validationResult.isValid()) {
                throw new PfModelRuntimeException(Response.Status.BAD_REQUEST, validationResult.getResult());
            }

            dao.create(jpaPdpGroup);
        }

        // Return the created PDP groups
        List<PdpGroup> returnPdpGroups = new ArrayList<>();

        for (PdpGroup pdpGroup : pdpGroups) {
            JpaPdpGroup jpaPdpGroup =
                    dao.get(JpaPdpGroup.class, new PfConceptKey(pdpGroup.getName(), PfKey.NULL_KEY_VERSION));
            returnPdpGroups.add(jpaPdpGroup.toAuthorative());
        }

        return returnPdpGroups;
    }

    /**
     * Updates PDP groups.
     *
     * @param dao the DAO to use to access the database
     * @param pdpGroups a specification of the PDP groups to update
     * @return the PDP groups updated
     * @throws PfModelException on errors updating PDP groups
     */
    public List<PdpGroup> updatePdpGroups(@NonNull final PfDao dao, @NonNull final List<PdpGroup> pdpGroups)
            throws PfModelException {

        for (PdpGroup pdpGroup : pdpGroups) {
            JpaPdpGroup jpaPdpGroup = new JpaPdpGroup();
            jpaPdpGroup.fromAuthorative(pdpGroup);

            BeanValidationResult validationResult = jpaPdpGroup.validate("PDP group");
            if (!validationResult.isValid()) {
                throw new PfModelRuntimeException(Response.Status.BAD_REQUEST, validationResult.getResult());
            }

            dao.update(jpaPdpGroup);
        }

        // Return the created PDP groups
        List<PdpGroup> returnPdpGroups = new ArrayList<>();

        for (PdpGroup pdpGroup : pdpGroups) {
            JpaPdpGroup jpaPdpGroup =
                    dao.get(JpaPdpGroup.class, new PfConceptKey(pdpGroup.getName(), PfKey.NULL_KEY_VERSION));
            returnPdpGroups.add(jpaPdpGroup.toAuthorative());
        }

        return returnPdpGroups;
    }

    /**
     * Update a PDP subgroup.
     *
     * @param dao the DAO to use to access the database
     * @param pdpGroupName the name of the PDP group of the PDP subgroup
     * @param pdpSubGroup the PDP subgroup to be updated
     * @throws PfModelException on errors updating PDP subgroups
     */
    public void updatePdpSubGroup(@NonNull final PfDao dao, @NonNull final String pdpGroupName,
            @NonNull final PdpSubGroup pdpSubGroup) throws PfModelException {

        final PfReferenceKey subGroupKey =
                new PfReferenceKey(pdpGroupName, PfKey.NULL_KEY_VERSION, pdpSubGroup.getPdpType());
        final JpaPdpSubGroup jpaPdpSubgroup = new JpaPdpSubGroup(subGroupKey);
        jpaPdpSubgroup.fromAuthorative(pdpSubGroup);

        BeanValidationResult validationResult = jpaPdpSubgroup.validate("PDP sub group");
        if (!validationResult.isValid()) {
            throw new PfModelRuntimeException(Response.Status.BAD_REQUEST, validationResult.getResult());
        }

        dao.update(jpaPdpSubgroup);
    }

    /**
     * Update a PDP.
     *
     * @param dao the DAO to use to access the database
     * @param pdpGroupName the name of the PDP group of the PDP subgroup
     * @param pdpSubGroup the PDP subgroup to be updated
     * @param pdp the PDP to be updated
     * @throws PfModelException on errors updating PDP subgroups
     */
    public void updatePdp(@NonNull final PfDao dao, @NonNull final String pdpGroupName,
            @NonNull final String pdpSubGroup, @NonNull final Pdp pdp) {

        final PfReferenceKey pdpKey =
                new PfReferenceKey(pdpGroupName, PfKey.NULL_KEY_VERSION, pdpSubGroup, pdp.getInstanceId());
        final JpaPdp jpaPdp = new JpaPdp(pdpKey);
        jpaPdp.fromAuthorative(pdp);

        BeanValidationResult validationResult = jpaPdp.validate("PDP");
        if (!validationResult.isValid()) {
            throw new PfModelRuntimeException(Response.Status.BAD_REQUEST, validationResult.getResult());
        }

        dao.update(jpaPdp);
    }

    /**
     * Delete a PDP group.
     *
     * @param dao the DAO to use to access the database
     * @param name the name of the policy to get, null to get all PDP groups
     * @return the PDP group deleted
     * @throws PfModelException on errors deleting PDP groups
     */
    public PdpGroup deletePdpGroup(@NonNull final PfDao dao, @NonNull final String name) {

        PfConceptKey pdpGroupKey = new PfConceptKey(name, PfKey.NULL_KEY_VERSION);

        JpaPdpGroup jpaDeletePdpGroup = dao.get(JpaPdpGroup.class, pdpGroupKey);

        if (jpaDeletePdpGroup == null) {
            String errorMessage =
                    "delete of PDP group \"" + pdpGroupKey.getId() + "\" failed, PDP group does not exist";
            throw new PfModelRuntimeException(Response.Status.BAD_REQUEST, errorMessage);
        }

        dao.delete(jpaDeletePdpGroup);

        return jpaDeletePdpGroup.toAuthorative();
    }

    /**
     * Get PDP statistics.
     *
     * @param dao the DAO to use to access the database
     * @param name the name of the PDP group to get statistics for, null to get all PDP groups
     * @return the statistics found
     * @throws PfModelException on errors getting statistics
     */
    public List<PdpStatistics> getPdpStatistics(@NonNull final PfDao dao, final String name) throws PfModelException {
        return new ArrayList<>();
    }

    /**
     * Update PDP statistics for a PDP.
     *
     * @param dao the DAO to use to access the database
     * @param pdpGroupName the name of the PDP group containing the PDP that the statistics are for
     * @param pdpType the PDP type of the subgroup containing the PDP that the statistics are for
     * @param pdpInstanceId the instance ID of the PDP to update statistics for
     * @param pdpStatistics the statistics to update
     * @throws PfModelException on errors updating statistics
     */
    public void updatePdpStatistics(@NonNull final PfDao dao, @NonNull final String pdpGroupName,
            @NonNull final String pdpType, @NonNull final String pdpInstanceId,
            @NonNull final PdpStatistics pdpStatistics) throws PfModelException {
        // Not implemented yet
    }

    /**
     * Convert JPA PDP group list to an authorative PDP group list.
     *
     * @param foundPdpGroups the list to convert
     * @return the authorative list
     */
    private List<PdpGroup> asPdpGroupList(List<JpaPdpGroup> jpaPdpGroupList) {
        List<PdpGroup> pdpGroupList = new ArrayList<>(jpaPdpGroupList.size());

        for (JpaPdpGroup jpaPdpGroup : jpaPdpGroupList) {
            pdpGroupList.add(jpaPdpGroup.toAuthorative());
        }

        return pdpGroupList;
    }
}
