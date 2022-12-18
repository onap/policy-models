/*-
 * ============LICENSE_START=======================================================
 * ONAP Policy Model
 * ================================================================================
 * Copyright (C) 2019-2021 Nordix Foundation.
 * Modifications Copyright (C) 2020-2021 AT&T Intellectual Property. All rights reserved.
 * Modifications Copyright (C) 2022 Bell Canada. All rights reserved.
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
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.Table;
import javax.persistence.TableGenerator;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import org.apache.commons.lang3.builder.CompareToBuilder;
import org.eclipse.persistence.annotations.Index;
import org.onap.policy.common.parameters.BeanValidationResult;
import org.onap.policy.common.parameters.ValidationStatus;
import org.onap.policy.common.parameters.annotations.Pattern;
import org.onap.policy.models.base.PfAuthorative;
import org.onap.policy.models.base.PfConcept;
import org.onap.policy.models.base.PfConceptKey;
import org.onap.policy.models.base.PfKey;
import org.onap.policy.models.base.PfUtils;
import org.onap.policy.models.base.Validated;
import org.onap.policy.models.pdp.concepts.PdpEngineWorkerStatistics;
import org.onap.policy.models.pdp.concepts.PdpStatistics;

/**
 * Class to represent a PDP statistics in the database.
 *
 */
@Entity
@Table(name = "PdpStatistics")
@Index(name = "IDXTSIDX1", columnNames = {
    "timeStamp", "name", "version"
})
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
@Data
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class JpaPdpStatistics extends PfConcept implements PfAuthorative<PdpStatistics>, Serializable {
    private static final long serialVersionUID = -7312974966820980659L;

    @Id
    @Column(name = "ID")
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "statisticsIdGen")
    @TableGenerator(
        name = "statisticsIdGen",
        table = "sequence",
        pkColumnName = "SEQ_NAME",
        valueColumnName = "SEQ_COUNT",
        pkColumnValue = "SEQ_GEN")
    private Long generatedId;

    @Column(name = "name", length = 120)
    @Pattern(regexp = PfKey.NAME_REGEXP)
    private String name;

    @Column(name = "version", length = 20)
    @Pattern(regexp = PfKey.VERSION_REGEXP)
    private String version;

    @Column(precision = 3)
    @Temporal(TemporalType.TIMESTAMP)
    private Date timeStamp;

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

    @Column
    private long policyUndeployCount;

    @Column
    private long policyUndeploySuccessCount;

    @Column
    private long policyUndeployFailCount;

    @ElementCollection
    private List<PdpEngineWorkerStatistics> engineStats;

    /**
     * The Default Constructor creates a {@link JpaPdpStatistics} object with a null key.
     */
    public JpaPdpStatistics() {
        this.setName(PfKey.NULL_KEY_NAME);
        this.setVersion(PfKey.NULL_KEY_VERSION);
    }

    /**
     * Copy constructor.
     *
     * @param copyConcept the concept to copy from
     */
    public JpaPdpStatistics(@NonNull final JpaPdpStatistics copyConcept) {
        super(copyConcept);
        this.name = copyConcept.name;
        this.version = copyConcept.version;
        this.generatedId = copyConcept.generatedId;
        this.timeStamp = copyConcept.timeStamp;
        this.pdpGroupName = copyConcept.pdpGroupName;
        this.pdpSubGroupName = copyConcept.pdpSubGroupName;
        this.policyDeployCount = copyConcept.policyDeployCount;
        this.policyDeploySuccessCount = copyConcept.policyDeploySuccessCount;
        this.policyDeployFailCount = copyConcept.policyDeployFailCount;
        this.policyUndeployCount = copyConcept.policyUndeployCount;
        this.policyUndeploySuccessCount = copyConcept.policyUndeploySuccessCount;
        this.policyUndeployFailCount = copyConcept.policyUndeployFailCount;
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
        return new CompareToBuilder()
            .append(this.name, other.name)
            .append(this.version, other.version)
            .append(this.generatedId, other.generatedId)
            .append(this.timeStamp, other.timeStamp)
            .append(this.pdpGroupName, other.pdpGroupName)
            .append(this.pdpSubGroupName, other.pdpSubGroupName)
            .append(this.policyDeployCount, other.policyDeployCount)
            .append(this.policyDeployFailCount, other.policyDeployFailCount)
            .append(this.policyDeploySuccessCount, other.policyDeploySuccessCount)
            .append(this.policyUndeployCount, other.policyUndeployCount)
            .append(this.policyUndeployFailCount, other.policyUndeployFailCount)
            .append(this.policyUndeploySuccessCount, other.policyUndeploySuccessCount)
            .append(this.policyExecutedCount, other.policyExecutedCount)
            .append(this.policyExecutedFailCount, other.policyExecutedFailCount)
            .append(this.policyExecutedSuccessCount, other.policyExecutedSuccessCount).toComparison();
    }

    @Override
    public PdpStatistics toAuthorative() {
        var pdpStatistics = new PdpStatistics();
        pdpStatistics.setPdpInstanceId(name);
        pdpStatistics.setGeneratedId(generatedId);
        pdpStatistics.setTimeStamp(timeStamp.toInstant());
        pdpStatistics.setPdpGroupName(pdpGroupName);
        pdpStatistics.setPdpSubGroupName(pdpSubGroupName);
        pdpStatistics.setPolicyDeployCount(policyDeployCount);
        pdpStatistics.setPolicyDeployFailCount(policyDeployFailCount);
        pdpStatistics.setPolicyDeploySuccessCount(policyDeploySuccessCount);
        pdpStatistics.setPolicyUndeployCount(policyUndeployCount);
        pdpStatistics.setPolicyUndeployFailCount(policyUndeployFailCount);
        pdpStatistics.setPolicyUndeploySuccessCount(policyUndeploySuccessCount);
        pdpStatistics.setPolicyExecutedCount(policyExecutedCount);
        pdpStatistics.setPolicyExecutedFailCount(policyExecutedFailCount);
        pdpStatistics.setPolicyExecutedSuccessCount(policyExecutedSuccessCount);
        pdpStatistics.setEngineStats(PfUtils.mapList(engineStats, PdpEngineWorkerStatistics::new, null));

        return pdpStatistics;
    }

    @Override
    public void fromAuthorative(@NonNull final PdpStatistics pdpStatistics) {
        if (pdpStatistics.getGeneratedId() != null) {
            this.setGeneratedId(pdpStatistics.getGeneratedId());
        }
        this.setName(pdpStatistics.getPdpInstanceId());
        this.setVersion(PfKey.NULL_KEY_VERSION);
        if (pdpStatistics.getTimeStamp() == null) {
            this.setTimeStamp(Date.from(Instant.EPOCH));
        } else {
            this.setTimeStamp(Date.from(pdpStatistics.getTimeStamp()));
        }
        this.setPdpGroupName(pdpStatistics.getPdpGroupName());
        this.setPdpSubGroupName(pdpStatistics.getPdpSubGroupName());
        this.setPolicyDeployCount(pdpStatistics.getPolicyDeployCount());
        this.setPolicyDeployFailCount(pdpStatistics.getPolicyDeployFailCount());
        this.setPolicyDeploySuccessCount(pdpStatistics.getPolicyDeploySuccessCount());
        this.setPolicyUndeployCount(pdpStatistics.getPolicyUndeployCount());
        this.setPolicyUndeployFailCount(pdpStatistics.getPolicyUndeployFailCount());
        this.setPolicyUndeploySuccessCount(pdpStatistics.getPolicyUndeploySuccessCount());
        this.setPolicyExecutedCount(pdpStatistics.getPolicyExecutedCount());
        this.setPolicyExecutedFailCount(pdpStatistics.getPolicyExecutedFailCount());
        this.setPolicyExecutedSuccessCount(pdpStatistics.getPolicyExecutedSuccessCount());
        this.setEngineStats(
            PfUtils.mapList(pdpStatistics.getEngineStats(), PdpEngineWorkerStatistics::new, null));
    }

    @Override
    public List<PfKey> getKeys() {
        final List<PfKey> keyList = new ArrayList<>();
        keyList.add(getKey());
        return keyList;
    }

    @Override
    public PfKey getKey() {
        return new PfConceptKey(name, version);
    }

    @Override
    public void clean() {
        pdpGroupName = pdpGroupName.trim();
        pdpSubGroupName = pdpSubGroupName.trim();
        if (engineStats != null) {
            for (PdpEngineWorkerStatistics engineStat : engineStats) {
                engineStat.clean();
            }
        }
    }

    @Override
    public BeanValidationResult validate(@NonNull String fieldName) {
        BeanValidationResult result = super.validate(fieldName);
        if (PfKey.NULL_KEY_NAME.equals(name)) {
            result.addResult("name", name, ValidationStatus.INVALID, Validated.IS_NULL);
        }
        return result;
    }
}
