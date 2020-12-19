/*-
 * ============LICENSE_START=======================================================
 * ONAP Policy Model
 * ================================================================================
 * Copyright (C) 2019 Nordix Foundation.
 * Modifications Copyright (C) 2020 AT&T Intellectual Property. All rights reserved.
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

package org.onap.policy.models.pdp.persistence.concepts;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import org.apache.commons.lang3.builder.CompareToBuilder;
import org.onap.policy.common.parameters.annotations.NotNull;
import org.onap.policy.models.base.PfAuthorative;
import org.onap.policy.models.base.PfConcept;
import org.onap.policy.models.base.PfKey;
import org.onap.policy.models.base.PfTimestampKey;
import org.onap.policy.models.base.PfUtils;
import org.onap.policy.models.base.validation.annotations.VerifyKey;
import org.onap.policy.models.pdp.concepts.PdpEngineWorkerStatistics;
import org.onap.policy.models.pdp.concepts.PdpStatistics;


/**
 * Class to represent a PDP statistics in the database.
 *
 */
@Entity
@Table(name = "PdpStatistics")
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
@Data
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class JpaPdpStatistics extends PfConcept implements PfAuthorative<PdpStatistics>, Serializable {
    private static final long serialVersionUID = -7312974966820980659L;
    private static final String NULL_NAME = "NULL";

    @EmbeddedId
    @VerifyKey
    @NotNull
    private PfTimestampKey key;

    @Column(length = 120)
    private String pdpGroupName;

    @Column(length = 120)
    private String pdpSubGroupName;

    @Column
    private long policyDeployCount;

    @Column
    private long policyDeploySuccessCount;

    @Column
    private long policyDeployFailCount;

    @Column
    private long policyExecutedCount;

    @Column
    private long policyExecutedSuccessCount;

    @Column
    private long policyExecutedFailCount;

    @ElementCollection
    private List<PdpEngineWorkerStatistics> engineStats;

    /**
     * The Default Constructor creates a {@link JpaPdpStatistics} object with a null key.
     */
    public JpaPdpStatistics() {
        this(new PfTimestampKey());
    }

    /**
     * The Key Constructor creates a {@link JpaPdpStatistics} object with the given concept key.
     *
     * @param key the key
     */
    public JpaPdpStatistics(@NonNull final PfTimestampKey key) {
        this(key, NULL_NAME, NULL_NAME, 0L, 0L, 0L, 0L, 0L, 0L, null);
    }


    /**
     * Copy constructor.
     *
     * @param copyConcept the concept to copy from
     */
    public JpaPdpStatistics(@NonNull final JpaPdpStatistics copyConcept) {
        super(copyConcept);
        this.key = new PfTimestampKey(copyConcept.key);
        this.pdpGroupName = copyConcept.pdpGroupName;
        this.pdpSubGroupName = copyConcept.pdpSubGroupName;
        this.policyDeployCount = copyConcept.policyDeployCount;
        this.policyDeploySuccessCount = copyConcept.policyDeploySuccessCount;
        this.policyDeployFailCount = copyConcept.policyDeployFailCount;
        this.policyExecutedCount = copyConcept.policyExecutedCount;
        this.policyExecutedSuccessCount = copyConcept.policyExecutedSuccessCount;
        this.policyExecutedFailCount = copyConcept.policyExecutedFailCount;
        this.engineStats = PfUtils.mapList(copyConcept.engineStats, PdpEngineWorkerStatistics::new, null);
    }

    /**
     * Authorative constructor.
     *
     * @param authorativeConcept the authorative concept to copy from
     */
    public JpaPdpStatistics(@NonNull final PdpStatistics authorativeConcept) {
        this.fromAuthorative(authorativeConcept);
    }

    @Override
    public int compareTo(PfConcept otherConcept) {
        if (otherConcept == null) {
            return -1;
        }
        if (this == otherConcept) {
            return 0;
        }
        if (getClass() != otherConcept.getClass()) {
            return getClass().getName().compareTo(otherConcept.getClass().getName());
        }

        final JpaPdpStatistics other = (JpaPdpStatistics) otherConcept;
        return new CompareToBuilder().append(this.key, other.key).append(this.pdpGroupName, other.pdpGroupName)
                .append(this.pdpSubGroupName, other.pdpSubGroupName)
                .append(this.policyDeployCount, other.policyDeployCount)
                .append(this.policyDeployFailCount, other.policyDeployFailCount)
                .append(this.policyDeploySuccessCount, other.policyDeploySuccessCount)
                .append(this.policyExecutedCount, other.policyExecutedCount)
                .append(this.policyExecutedFailCount, other.policyExecutedFailCount)
                .append(this.policyExecutedSuccessCount, other.policyExecutedSuccessCount).toComparison();
    }

    @Override
    public PdpStatistics toAuthorative() {
        PdpStatistics pdpStatistics = new PdpStatistics();
        pdpStatistics.setPdpInstanceId(key.getName());
        pdpStatistics.setTimeStamp(new Date(key.getTimeStamp().getTime()));
        pdpStatistics.setPdpGroupName(pdpGroupName);
        pdpStatistics.setPdpSubGroupName(pdpSubGroupName);
        pdpStatistics.setPolicyDeployCount(policyDeployCount);
        pdpStatistics.setPolicyDeployFailCount(policyDeployFailCount);
        pdpStatistics.setPolicyDeploySuccessCount(policyDeploySuccessCount);
        pdpStatistics.setPolicyExecutedCount(policyExecutedCount);
        pdpStatistics.setPolicyExecutedFailCount(policyExecutedFailCount);
        pdpStatistics.setPolicyExecutedSuccessCount(policyExecutedSuccessCount);
        pdpStatistics.setEngineStats(PfUtils.mapList(engineStats, PdpEngineWorkerStatistics::new, null));

        return pdpStatistics;
    }

    @Override
    public void fromAuthorative(@NonNull final PdpStatistics pdpStatistics) {
        if (this.key == null || this.getKey().isNullKey()) {
            this.setKey(new PfTimestampKey(pdpStatistics.getPdpInstanceId(), PfKey.NULL_KEY_VERSION,
                    new Date(pdpStatistics.getTimeStamp() == null ? 0 : pdpStatistics.getTimeStamp().getTime())));
        }
        this.setPdpGroupName(pdpStatistics.getPdpGroupName());
        this.setPdpSubGroupName(pdpStatistics.getPdpSubGroupName());
        this.setPolicyDeployCount(pdpStatistics.getPolicyDeployCount());
        this.setPolicyDeployFailCount(pdpStatistics.getPolicyDeployFailCount());
        this.setPolicyDeploySuccessCount(pdpStatistics.getPolicyDeploySuccessCount());
        this.setPolicyExecutedCount(pdpStatistics.getPolicyExecutedCount());
        this.setPolicyExecutedFailCount(pdpStatistics.getPolicyExecutedFailCount());
        this.setPolicyExecutedSuccessCount(pdpStatistics.getPolicyExecutedSuccessCount());
        this.setEngineStats(
                PfUtils.mapList(pdpStatistics.getEngineStats(), PdpEngineWorkerStatistics::new, null));
    }

    @Override
    public List<PfKey> getKeys() {
        return getKey().getKeys();
    }

    @Override
    public void clean() {
        key.clean();
        pdpGroupName = pdpGroupName.trim();
        pdpSubGroupName = pdpSubGroupName.trim();
        if (engineStats != null) {
            for (PdpEngineWorkerStatistics engineStat : engineStats) {
                engineStat.clean();
            }
        }
    }
}
