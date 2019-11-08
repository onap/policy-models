/*-
 * ============LICENSE_START=======================================================
 * ONAP Policy Model
 * ================================================================================
 * Copyright (C) 2019 Nordix Foundation.
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
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import org.apache.commons.lang3.ObjectUtils;
import org.onap.policy.models.base.PfAuthorative;
import org.onap.policy.models.base.PfConcept;
import org.onap.policy.models.base.PfKey;
import org.onap.policy.models.base.PfTimestampKey;
import org.onap.policy.models.base.PfUtils;
import org.onap.policy.models.base.PfValidationMessage;
import org.onap.policy.models.base.PfValidationResult;
import org.onap.policy.models.base.PfValidationResult.ValidationResult;
import org.onap.policy.models.pdp.concepts.PdpEngineStatistics;
import org.onap.policy.models.pdp.concepts.PdpStatistics;


/**
 * Class to represent a PDP statistics in the database.
 *
 */
@Entity
@Table(name = "PdpStatistics")
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
@Data
@EqualsAndHashCode(callSuper = false)
public class JpaPdpStatistics extends PfConcept implements PfAuthorative<PdpStatistics>, Serializable {
    private static final long serialVersionUID = -7312974966820980659L;
    private static final String NULL_NAME = "NULL";
    @EmbeddedId
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
    private List<PdpEngineStatistics> engineStats;

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
     * The Key Constructor creates a {@link JpaPdpStatistics} object with all mandatory fields.
     *
     * @param key the key
     * @param pdpGroupName The pdp group name
     * @param pdpSubGroupName The pdp subgroup name
     * @param policyDeployCount The num of deployed policies
     * @param policyDeploySuccessCount The num of successful deployed policies
     * @param policyDeployFailCount The num of failed deployed policies
     * @param policyExecutedCount The num of executed policies
     * @param policyExecutedSuccessCount The num of successful executed policies
     * @param policyExecutedFailCount The num of failed executed policies
     * @param engineStats The pdp engine statistics
     */
    public JpaPdpStatistics(@NonNull final PfTimestampKey key, @NonNull final String pdpGroupName,
            @NonNull final String pdpSubGroupName, final long policyDeployCount, final long policyDeploySuccessCount,
            final long policyDeployFailCount, final long policyExecutedCount, final long policyExecutedSuccessCount,
            final long policyExecutedFailCount, final List<PdpEngineStatistics> engineStats) {
        this.key = key;
        this.pdpGroupName = pdpGroupName;
        this.pdpSubGroupName = pdpSubGroupName;
        this.policyDeployCount = policyDeployCount;
        this.policyDeploySuccessCount = policyDeploySuccessCount;
        this.policyDeployFailCount = policyDeployFailCount;
        this.policyExecutedCount = policyExecutedCount;
        this.policyExecutedSuccessCount = policyExecutedSuccessCount;
        this.policyExecutedFailCount = policyExecutedFailCount;
        this.engineStats = engineStats;
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
        this.engineStats = PfUtils.mapList(copyConcept.engineStats, PdpEngineStatistics::new, null);
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
        if (!key.equals(other.key)) {
            return key.compareTo(other.key);
        }

        if (!pdpGroupName.equals(other.pdpGroupName)) {
            return pdpGroupName.compareTo(other.pdpGroupName);
        }
        if (!pdpSubGroupName.equals(other.pdpSubGroupName)) {
            return pdpSubGroupName.compareTo(other.pdpSubGroupName);
        }

        int result = ObjectUtils.compare(policyDeployCount, other.policyDeployCount);
        if (result != 0) {
            return result;
        }

        result = ObjectUtils.compare(policyDeployFailCount, other.policyDeployFailCount);
        if (result != 0) {
            return result;
        }

        result = ObjectUtils.compare(policyDeploySuccessCount, other.policyDeploySuccessCount);
        if (result != 0) {
            return result;
        }

        result = ObjectUtils.compare(policyExecutedCount, other.policyExecutedCount);
        if (result != 0) {
            return result;
        }

        result = ObjectUtils.compare(policyExecutedFailCount, other.policyExecutedFailCount);
        if (result != 0) {
            return result;
        }

        return ObjectUtils.compare(policyExecutedSuccessCount, other.policyExecutedSuccessCount);

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
        pdpStatistics.setEngineStats(PfUtils.mapList(engineStats, PdpEngineStatistics::new, null));

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
                PfUtils.mapList(pdpStatistics.getEngineStats(), PdpEngineStatistics::new, null));
    }

    @Override
    public List<PfKey> getKeys() {
        return getKey().getKeys();
    }

    @Override
    public PfValidationResult validate(@NonNull PfValidationResult resultIn) {
        PfValidationResult result = resultIn;

        if (key.isNullKey()) {
            result.addValidationMessage(
                    new PfValidationMessage(key, this.getClass(), ValidationResult.INVALID, "key is a null key"));
        }

        result = key.validate(result);

        return result;
    }

    @Override
    public void clean() {
        key.clean();
        pdpGroupName = pdpGroupName.trim();
        pdpSubGroupName = pdpSubGroupName.trim();
    }

}
