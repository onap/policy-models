/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2021 Nordix Foundation.
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

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;
import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.commons.lang3.builder.CompareToBuilder;
import org.onap.policy.common.utils.validation.Assertions;
import org.onap.policy.models.base.PfAuthorative;
import org.onap.policy.models.base.PfConcept;
import org.onap.policy.models.base.PfGeneratedIdKey;
import org.onap.policy.models.base.PfKey;
import org.onap.policy.models.base.PfReferenceKey;
import org.onap.policy.models.pdp.concepts.PdpPolicyDeploymentAudit;
import org.onap.policy.models.pdp.concepts.PdpPolicyDeploymentAudit.AuditAction;
import org.onap.policy.models.tosca.authorative.concepts.ToscaConceptIdentifier;

/**
 * Entity to keep the records on policy deployments for audit.
 *
 * @author Adheli Tavares (adheli.tavares@est.tech)
 *
 */
@Entity
@Table(
        name = "PdpPolicyTracker",
        uniqueConstraints = {@UniqueConstraint(columnNames = {"name", "version", "startDate", "endDate"})})
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
@Data
@EqualsAndHashCode(callSuper = false)
public class JpaPdpPolicyDeploymentAudit extends PfConcept implements PfAuthorative<PdpPolicyDeploymentAudit> {
    private static final long serialVersionUID = -2935734300607322191L;

    @EmbeddedId
    @Column
    @NotNull
    private PfGeneratedIdKey key;

    @Column
    @NotNull
    private String pdpGroup;

    @Column
    @NotNull
    private String pdpType;

    @Column
    @NotNull
    private AuditAction action;

    @Column
    @Temporal(TemporalType.TIMESTAMP)
    private Date timeStamp;

    @Column
    private String changedByUser;

    /**
     * Default constructor.
     */
    public JpaPdpPolicyDeploymentAudit() {
        key = new PfGeneratedIdKey();
    }

    /**
     * Constructor from an authorative.
     *
     * @param tracker authorative model
     */
    public JpaPdpPolicyDeploymentAudit(PdpPolicyDeploymentAudit tracker) {
        fromAuthorative(tracker);
    }

    /**
     * Constructor as a copy.
     *
     * @param copyConcept original entity to be copied
     */
    public JpaPdpPolicyDeploymentAudit(JpaPdpPolicyDeploymentAudit copyConcept) {
        this.key = new PfGeneratedIdKey(copyConcept.getKey());
        this.pdpGroup = copyConcept.getPdpGroup();
        this.pdpType = copyConcept.getPdpType();
        this.action = copyConcept.getAction();
        this.timeStamp = copyConcept.getTimeStamp();
        this.changedByUser = copyConcept.getChangedByUser();
    }

    @Override
    public int compareTo(PfConcept o) {
        if (o == null) {
            return -1;
        }
        if (this == o) {
            return 0;
        }
        if (getClass() != o.getClass()) {
            return getClass().getName().compareTo(o.getClass().getName());
        }

        final JpaPdpPolicyDeploymentAudit other = (JpaPdpPolicyDeploymentAudit) o;

        // @formatter:off
        return new CompareToBuilder()
                        .append(key, other.key)
                        .append(pdpGroup, other.pdpGroup)
                        .append(pdpType, other.pdpType)
                        .append(action, other.action)
                        .append(timeStamp, other.timeStamp)
                        .append(changedByUser, other.changedByUser)
                        .toComparison();
        // @formatter:on
    }

    @Override
    public PdpPolicyDeploymentAudit toAuthorative() {
        ToscaConceptIdentifier policyIdent = new ToscaConceptIdentifier(key.getName(), key.getVersion());

        // @formatter:off
        return PdpPolicyDeploymentAudit.builder()
                        .trackerId(key.getGeneratedId())
                        .pdpGroup(pdpGroup)
                        .pdpType(pdpType)
                        .policy(policyIdent)
                        .action(action)
                        .timestamp(timeStamp.toInstant())
                        .changedByUser(changedByUser)
                        .build();
        // @formatter:on
    }

    @Override
    public void fromAuthorative(PdpPolicyDeploymentAudit authorativeConcept) {
        final ToscaConceptIdentifier policyIdent = authorativeConcept.getPolicy();

        key = new PfGeneratedIdKey(policyIdent.getName(), policyIdent.getVersion(), authorativeConcept.getTrackerId());

        pdpGroup = authorativeConcept.getPdpGroup();
        pdpType = authorativeConcept.getPdpType();
        action = authorativeConcept.getAction();
        timeStamp = authorativeConcept.getTimestamp() == null ? Date.from(Instant.now().truncatedTo(ChronoUnit.SECONDS))
                : Date.from(authorativeConcept.getTimestamp());
        changedByUser =
                authorativeConcept.getChangedByUser() == null ? null : authorativeConcept.getChangedByUser().trim();
        authorativeConcept.setTimeStamp(timeStamp.toInstant());
    }

    @Override
    public List<PfKey> getKeys() {
        return getKey().getKeys();
    }

    @Override
    public void clean() {
        key.clean();

        pdpGroup = Assertions.validateStringParameter("pdpGroup", pdpGroup, PfReferenceKey.LOCAL_NAME_REGEXP);
        pdpType = Assertions.validateStringParameter("pdpType", pdpType, PfReferenceKey.LOCAL_NAME_REGEXP);
    }
}
