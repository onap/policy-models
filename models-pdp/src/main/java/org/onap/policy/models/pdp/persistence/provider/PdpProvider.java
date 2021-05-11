/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2019-2021 Nordix Foundation.
 *  Modifications Copyright (C) 2019-2021 AT&T Intellectual Property. All rights reserved.
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
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
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
import org.onap.policy.models.pdp.concepts.PdpPolicyStatus;
import org.onap.policy.models.pdp.concepts.PdpPolicyTracker;
import org.onap.policy.models.pdp.concepts.PdpStatistics;
import org.onap.policy.models.pdp.concepts.PdpSubGroup;
import org.onap.policy.models.pdp.persistence.concepts.JpaPdp;
import org.onap.policy.models.pdp.persistence.concepts.JpaPdpGroup;
import org.onap.policy.models.pdp.persistence.concepts.JpaPdpPolicyStatus;
import org.onap.policy.models.pdp.persistence.concepts.JpaPdpPolicyTracker;
import org.onap.policy.models.pdp.persistence.concepts.JpaPdpSubGroup;
import org.onap.policy.models.tosca.authorative.concepts.ToscaConceptIdentifierOptVersion;

/**
 * This class provides the provision of information on PAP concepts in the database to callers.
 *
 * @author Liam Fallon (liam.fallon@est.tech)
 */
public class PdpProvider {
    private static final Object statusLock = new Object();

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
            var jpaPdpGroup = new JpaPdpGroup();
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
            var jpaPdpGroup = dao.get(JpaPdpGroup.class, new PfConceptKey(pdpGroup.getName(), PfKey.NULL_KEY_VERSION));
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
            var jpaPdpGroup = new JpaPdpGroup();
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
            var jpaPdpGroup =
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

        final var subGroupKey =
                new PfReferenceKey(pdpGroupName, PfKey.NULL_KEY_VERSION, pdpSubGroup.getPdpType());
        final var jpaPdpSubgroup = new JpaPdpSubGroup(subGroupKey);
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

        final var pdpKey =
                new PfReferenceKey(pdpGroupName, PfKey.NULL_KEY_VERSION, pdpSubGroup, pdp.getInstanceId());
        final var jpaPdp = new JpaPdp(pdpKey);
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

        var pdpGroupKey = new PfConceptKey(name, PfKey.NULL_KEY_VERSION);

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
     * Gets all policy deployments.
     *
     * @param dao the DAO to use to access the database
     * @return the deployments found
     * @throws PfModelException on errors getting PDP groups
     */
    public List<PdpPolicyStatus> getAllPolicyStatus(@NonNull final PfDao dao)
                    throws PfModelException {

        return dao.getAll(JpaPdpPolicyStatus.class).stream().map(JpaPdpPolicyStatus::toAuthorative)
                        .collect(Collectors.toList());
    }

    /**
     * Gets all deployments for a policy.
     *
     * @param dao the DAO to use to access the database
     * @return the deployments found
     * @throws PfModelException on errors getting PDP groups
     */
    public List<PdpPolicyStatus> getAllPolicyStatus(@NonNull final PfDao dao,
                    @NonNull ToscaConceptIdentifierOptVersion policy) throws PfModelException {

        if (policy.getVersion() != null) {
            return dao.getAll(JpaPdpPolicyStatus.class, new PfConceptKey(policy.getName(), policy.getVersion()))
                            .stream().map(JpaPdpPolicyStatus::toAuthorative).collect(Collectors.toList());

        } else {
            return dao.getAllVersionsByParent(JpaPdpPolicyStatus.class, policy.getName()).stream()
                            .map(JpaPdpPolicyStatus::toAuthorative).collect(Collectors.toList());
        }
    }

    /**
     * Gets the policy deployments for a PDP group.
     *
     * @param dao the DAO to use to access the database
     * @param groupName the name of the PDP group of interest, null to get results for all
     *        PDP groups
     * @return the deployments found
     * @throws PfModelException on errors getting PDP groups
     */
    public List<PdpPolicyStatus> getGroupPolicyStatus(@NonNull final PfDao dao, @NonNull final String groupName)
                    throws PfModelException {

        Map<String, Object> filter = Map.of("pdpGroup", groupName);

        return dao.getFiltered(JpaPdpPolicyStatus.class, null, null, null, null, filter, null, 0).stream()
                        .map(JpaPdpPolicyStatus::toAuthorative).collect(Collectors.toList());
    }

    /**
     * Creates, updates, and deletes collections of policy status.
     *
     * @param dao the DAO to use to access the database
     * @param createObjs the objects to create
     * @param updateObjs the objects to update
     * @param deleteObjs the objects to delete
     */
    public void cudPolicyStatus(@NonNull final PfDao dao, Collection<PdpPolicyStatus> createObjs,
                    Collection<PdpPolicyStatus> updateObjs, Collection<PdpPolicyStatus> deleteObjs) {

        synchronized (statusLock) {
            dao.deleteCollection(fromAuthorativeStatus(deleteObjs, "deletePdpPolicyStatusList"));
            dao.createCollection(fromAuthorativeStatus(createObjs, "createPdpPolicyStatusList"));
            dao.createCollection(fromAuthorativeStatus(updateObjs, "updatePdpPolicyStatusList"));
        }
    }

    /**
     * Converts a collection of authorative policy status to a collection of JPA policy
     * status.  Validates the resulting list.
     *
     * @param objs authorative policy status to convert
     * @param fieldName name of the field containing the collection
     * @return a collection of JPA policy status
     */
    private Collection<JpaPdpPolicyStatus> fromAuthorativeStatus(Collection<PdpPolicyStatus> objs, String fieldName) {
        if (objs == null) {
            return Collections.emptyList();
        }

        List<JpaPdpPolicyStatus> jpas = objs.stream().map(JpaPdpPolicyStatus::new).collect(Collectors.toList());

        // validate the objects
        var result = new BeanValidationResult(fieldName, jpas);

        var count = 0;
        for (JpaPdpPolicyStatus jpa: jpas) {
            result.addResult(jpa.validate(String.valueOf(count++)));
        }

        if (!result.isValid()) {
            throw new PfModelRuntimeException(Response.Status.BAD_REQUEST, result.getResult());
        }

        return jpas;
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

    /**
     * Collect the deployment history of policies in a Pdp Group.
     * @param dao database access to be used.
     * @param groupName pdp group name to be audited.
     * @return list of deployments acted upon the policies on pdp group.
     */
    public List<PdpPolicyTracker> getPdpPolicyTrackers(@NonNull final PfDao dao,
            @NonNull final String groupName) {
        Map<String, Object> filter = Map.of("pdpGroup", groupName);

        return dao.getFiltered(JpaPdpPolicyTracker.class, null, null, null, null, filter, "timestamp ASC", 0).stream()
                        .map(JpaPdpPolicyTracker::toAuthorative).collect(Collectors.toList());
    }

    /**
     * Collect the deployment history of policies.
     * @param dao database access to be used.
     * @param name policy name
     * @param version policy version
     * @return list of deployments acted upon the policy with the name and version informed.
     */
    public List<PdpPolicyTracker> getPdpPolicyTrackers(@NonNull final PfDao dao,
            @NonNull final String name, @NonNull final String version) {

        return dao.getFiltered(JpaPdpPolicyTracker.class, name, version, null, null, null, null, 0).stream()
                .sorted().map(JpaPdpPolicyTracker::toAuthorative).collect(Collectors.toList());
    }

    /**
     * Creates trackers for PDP policies' status which (deploy/undeploy) have changed.
     *
     * @param dao the DAO to use to access the database
     * @param trackers a specification of the PDP groups to create
     * @throws PfModelException on errors creating trackers
     */
    public void trackPdpPolicyAction(@NonNull final PfDao dao, @NonNull final List<PdpPolicyTracker> trackers) {
        dao.createCollection(trackers.stream().map(JpaPdpPolicyTracker::new).collect(Collectors.toList()));
    }
}
