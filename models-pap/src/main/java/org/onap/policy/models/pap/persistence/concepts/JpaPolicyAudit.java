/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2021 Nordix Foundation.
 *  Modifications Copyright (C) 2021 Bell Canada. All rights reserved.
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

package org.onap.policy.models.pap.persistence.concepts;

import java.time.Instant;
import java.util.Date;
import java.util.List;
import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Index;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
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
import org.onap.policy.models.base.validation.annotations.VerifyKey;
import org.onap.policy.models.pap.concepts.PolicyAudit;
import org.onap.policy.models.pap.concepts.PolicyAudit.AuditAction;
import org.onap.policy.models.tosca.authorative.concepts.ToscaConceptIdentifier;

/**
 * Entity to keep the records on policy actions for audit.
 *
 * @author Adheli Tavares (adheli.tavares@est.tech)
 *
 */
@Entity
@Table(name = "JpaPolicyAudit", indexes = {@Index(name = "JpaPolicyAuditIndex_timestamp", columnList = "timeStamp")})
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
@Data
@EqualsAndHashCode(callSuper = false)
public class JpaPolicyAudit extends PfConcept implements PfAuthorative<PolicyAudit> {
    private static final long serialVersionUID = -2935734300607322191L;

    @EmbeddedId
    @Column
    @NotNull
    @VerifyKey(versionNotNull = true)
    private PfGeneratedIdKey key;

    @Column
    private String pdpGroup;

    @Column
    private String pdpType;

    @Column
    @NotNull
    private AuditAction action;

    @Column
    @Temporal(TemporalType.TIMESTAMP)
    @NotNull
    private Date timeStamp;

    @Column
    private String user;

    /**
     * Default constructor.
     */
    public JpaPolicyAudit() {
        key = new PfGeneratedIdKey();
    }

    /**
     * Constructor from an authorative.
     *
     * @param audit authorative model
     */
    public JpaPolicyAudit(PolicyAudit audit) {
        fromAuthorative(audit);
    }

    /**
     * Constructor as a copy.
     *
     * @param copyConcept original entity to be copied
     */
    public JpaPolicyAudit(JpaPolicyAudit copyConcept) {
        this.key = new PfGeneratedIdKey(copyConcept.getKey());
        this.pdpGroup = copyConcept.getPdpGroup();
        this.pdpType = copyConcept.getPdpType();
        this.action = copyConcept.getAction();
        this.timeStamp = copyConcept.getTimeStamp();
        this.user = copyConcept.getUser();
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

        final JpaPolicyAudit other = (JpaPolicyAudit) o;

        // @formatter:off
        return new CompareToBuilder()
                        .append(key, other.key)
                        .append(pdpGroup, other.pdpGroup)
                        .append(pdpType, other.pdpType)
                        .append(action, other.action)
                        .append(timeStamp, other.timeStamp)
                        .append(user, other.user)
                        .toComparison();
        // @formatter:on
    }

    @Override
    public PolicyAudit toAuthorative() {
        var policyIdent = new ToscaConceptIdentifier(key.getName(), key.getVersion());

        // @formatter:off
        return PolicyAudit.builder()
                        .auditId(key.getGeneratedId())
                        .pdpGroup(pdpGroup)
                        .pdpType(pdpType)
                        .policy(policyIdent)
                        .action(action)
                        .timestamp(timeStamp == null ? null : timeStamp.toInstant())
                        .user(user)
                        .build();
        // @formatter:on
    }

    @Override
    public void fromAuthorative(PolicyAudit authorativeConcept) {
        if (authorativeConcept.getPolicy() != null) {
            final ToscaConceptIdentifier policy = authorativeConcept.getPolicy();
            key = new PfGeneratedIdKey(policy.getName(), policy.getVersion(), authorativeConcept.getAuditId());
        } else {
            key = new PfGeneratedIdKey();
        }

        pdpGroup = authorativeConcept.getPdpGroup();
        pdpType = authorativeConcept.getPdpType();
        action = authorativeConcept.getAction();
        timeStamp = authorativeConcept.getTimestamp() == null ? Date.from(Instant.now())
                : Date.from(authorativeConcept.getTimestamp());
        user = authorativeConcept.getUser();
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
        user = Assertions.validateStringParameter("user", user, PfReferenceKey.LOCAL_NAME_REGEXP);
    }
}
