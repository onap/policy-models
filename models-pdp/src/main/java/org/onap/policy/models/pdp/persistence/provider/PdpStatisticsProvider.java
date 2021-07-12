/*-
 * ============LICENSE_START=======================================================
 * ONAP Policy Model
 * ================================================================================
 * Copyright (C) 2019-2021 Nordix Foundation.
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

package org.onap.policy.models.pdp.persistence.provider;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import javax.ws.rs.core.Response;
import lombok.NonNull;
import org.onap.policy.common.parameters.BeanValidationResult;
import org.onap.policy.models.base.PfGeneratedIdKey;
import org.onap.policy.models.base.PfKey;
import org.onap.policy.models.base.PfModelException;
import org.onap.policy.models.base.PfModelRuntimeException;
import org.onap.policy.models.dao.PfDao;
import org.onap.policy.models.dao.PfFilterParameters;
import org.onap.policy.models.pdp.concepts.PdpStatistics;
import org.onap.policy.models.pdp.persistence.concepts.JpaPdpStatistics;


/**
 * This class provides the provision of information on PAP concepts in the database to callers.
 *
 * @author Ning Xi (ning.xi@est.tech)
 */
public class PdpStatisticsProvider {

    /**
     * Get PDP statistics.
     *
     * @param dao the DAO to use to access the database
     * @param name the name of the PDP statistics to get, null to get all PDPs
     * @return the PDP statistics found
     * @throws PfModelException on errors getting PDP statistics
     */
    public List<PdpStatistics> getPdpStatistics(@NonNull final PfDao dao, final String name, final Instant timeStamp)
            throws PfModelException {
        if (name != null && timeStamp != null) {
            return asPdpStatisticsList(dao.getByTimestamp(JpaPdpStatistics.class,
                    new PfGeneratedIdKey(name, PfKey.NULL_KEY_VERSION), timeStamp));
        } else {
            return asPdpStatisticsList(dao.getAll(JpaPdpStatistics.class));
        }
    }

    /**
     * Get PDP statistics.
     *
     * @param dao the DAO to use to access the database
     * @param name the name of the PDP statistics to get, null to get all PDPs
     * @return the PDP statistics found
     * @throws PfModelException on errors getting PDP statistics
     */
    public List<PdpStatistics> getPdpStatistics(@NonNull final PfDao dao, final String name)
            throws PfModelException {

        List<PdpStatistics> pdpStatistics = new ArrayList<>();
        if (name != null) {
            pdpStatistics
                    .add(dao.get(JpaPdpStatistics.class, new PfGeneratedIdKey(name, PfKey.NULL_KEY_VERSION))
                            .toAuthorative());
        } else {
            return asPdpStatisticsList(dao.getAll(JpaPdpStatistics.class));
        }
        return pdpStatistics;
    }

    /**
     * Get filtered PDP statistics.
     *
     * @param dao the DAO to use to access the database
     * @param filterParams filter parameters
     * @return the PDP statistics found
     * @throws PfModelException on errors getting policies
     */
    public List<PdpStatistics> getFilteredPdpStatistics(@NonNull final PfDao dao,
                    PdpFilterParameters filterParams) {
        return asPdpStatisticsList(dao.getFiltered(JpaPdpStatistics.class, filterParams));
    }

    /**
     * Creates PDP statistics.
     *
     * @param dao the DAO to use to access the database
     * @param pdpStatisticsList a specification of the PDP statistics to create
     * @return the PDP statistics created
     * @throws PfModelException on errors creating PDP statistics
     */
    public List<PdpStatistics> createPdpStatistics(@NonNull final PfDao dao,
            @NonNull final List<PdpStatistics> pdpStatisticsList) throws PfModelException {
        for (PdpStatistics pdpStatistics : pdpStatisticsList) {
            var jpaPdpStatistics = new JpaPdpStatistics();
            jpaPdpStatistics.fromAuthorative(pdpStatistics);
            BeanValidationResult validationResult = jpaPdpStatistics.validate("pdp statistics");
            if (!validationResult.isValid()) {
                throw new PfModelRuntimeException(Response.Status.BAD_REQUEST, validationResult.getResult());
            }

            dao.create(jpaPdpStatistics);
            pdpStatistics.setGeneratedId(jpaPdpStatistics.getKey().getGeneratedId());
        }

        // Return the created PDP statistics
        List<PdpStatistics> pdpStatistics = new ArrayList<>(pdpStatisticsList.size());

        for (PdpStatistics pdpStatisticsItem : pdpStatisticsList) {
            var jpaPdpStatistics =
                    dao.get(JpaPdpStatistics.class, new PfGeneratedIdKey(pdpStatisticsItem.getPdpInstanceId(),
                            PfKey.NULL_KEY_VERSION, pdpStatisticsItem.getGeneratedId()));
            pdpStatistics.add(jpaPdpStatistics.toAuthorative());
        }
        return pdpStatistics;
    }

    /**
     * Updates PDP statistics.
     *
     * @param dao the DAO to use to access the database
     * @param pdpStatisticsList a specification of the PDP statistics to update
     * @return the PDP statistics updated
     * @throws PfModelException on errors updating PDP statistics
     */
    public List<PdpStatistics> updatePdpStatistics(@NonNull final PfDao dao,
            @NonNull final List<PdpStatistics> pdpStatisticsList) throws PfModelException {

        for (PdpStatistics pdpStatistics : pdpStatisticsList) {
            var jpaPdpStatistics = new JpaPdpStatistics();
            jpaPdpStatistics.fromAuthorative(pdpStatistics);

            BeanValidationResult validationResult = jpaPdpStatistics.validate("pdp statistics");
            if (!validationResult.isValid()) {
                throw new PfModelRuntimeException(Response.Status.BAD_REQUEST, validationResult.getResult());
            }

            dao.update(jpaPdpStatistics);
        }

        // Return the created PDP statistics
        List<PdpStatistics> pdpStatistics = new ArrayList<>(pdpStatisticsList.size());

        for (PdpStatistics pdpStatisticsItem : pdpStatisticsList) {
            var jpaPdpStatistics =
                    dao.get(JpaPdpStatistics.class, new PfGeneratedIdKey(pdpStatisticsItem.getPdpInstanceId(),
                            PfKey.NULL_KEY_VERSION, pdpStatisticsItem.getGeneratedId()));
            pdpStatistics.add(jpaPdpStatistics.toAuthorative());
        }

        return pdpStatistics;
    }

    /**
     * Delete a PDP statistics.
     *
     * @param dao the DAO to use to access the database
     * @param name the name of the policy to get, null to get all PDP statistics
     * @param timestamp the timeStamp of statistics to delete, null to delete all statistics record of given PDP
     * @return the PDP statistics list deleted
     * @throws PfModelException on errors deleting PDP statistics
     */
    public List<PdpStatistics> deletePdpStatistics(@NonNull final PfDao dao, @NonNull final String name,
                    final Instant timestamp) {
        List<PdpStatistics> pdpStatisticsListToDel = asPdpStatisticsList(dao.getFiltered(JpaPdpStatistics.class,
                        PfFilterParameters.builder().name(name).startTime(timestamp).endTime(timestamp).build()));

        pdpStatisticsListToDel.stream().forEach(s -> dao.delete(JpaPdpStatistics.class,
                new PfGeneratedIdKey(s.getPdpInstanceId(), PfKey.NULL_KEY_VERSION, s.getGeneratedId())));

        return pdpStatisticsListToDel;
    }

    /**
     * Convert JPA PDP statistics list to an PDP statistics list.
     *
     * @param jpaPdpStatisticsList the list to convert
     * @return the PDP statistics list
     */
    private List<PdpStatistics> asPdpStatisticsList(List<JpaPdpStatistics> jpaPdpStatisticsList) {
        return jpaPdpStatisticsList.stream().map(JpaPdpStatistics::toAuthorative).collect(Collectors.toList());
    }
}
