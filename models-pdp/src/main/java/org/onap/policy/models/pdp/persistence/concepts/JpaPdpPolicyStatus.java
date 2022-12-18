/*-
 * ============LICENSE_START=======================================================
 * ONAP
 * ================================================================================
 * Copyright (C) 2021 AT&T Intellectual Property. All rights reserved.
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
 * ============LICENSE_END=========================================================
 */

package org.onap.policy.models.pdp.persistence.concepts;

import java.util.List;
import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Index;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import org.apache.commons.lang3.builder.CompareToBuilder;
import org.onap.policy.common.parameters.BeanValidationResult;
import org.onap.policy.common.parameters.annotations.NotNull;
import org.onap.policy.common.parameters.annotations.Pattern;
import org.onap.policy.common.parameters.annotations.Valid;
import org.onap.policy.common.utils.validation.Assertions;
import org.onap.policy.models.base.PfAuthorative;
import org.onap.policy.models.base.PfConcept;
import org.onap.policy.models.base.PfConceptKey;
import org.onap.policy.models.base.PfKey;
import org.onap.policy.models.base.PfReferenceKey;
import org.onap.policy.models.base.Validated;
import org.onap.policy.models.base.validation.annotations.VerifyKey;
import org.onap.policy.models.pdp.concepts.PdpPolicyStatus;
import org.onap.policy.models.pdp.concepts.PdpPolicyStatus.State;
import org.onap.policy.models.tosca.authorative.concepts.ToscaConceptIdentifier;

/**
 * Class to represent PDP-Policy deployment status in the database.
 */
@Entity
@Table(name = "PdpPolicyStatus", indexes = {@Index(name = "PdpPolicyStatus_PdpGroup", columnList = "pdpGroup")})
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
@Data
@EqualsAndHashCode(callSuper = false)
public class JpaPdpPolicyStatus extends PfConcept implements PfAuthorative<PdpPolicyStatus> {
    private static final long serialVersionUID = -357224425637789775L;

    /**
     * Parent key & version identifies the policy, while localName identifies the pdpId.
     */
    @EmbeddedId
    @NotNull
    @Valid
    private PfReferenceKey key;

    @Column
    @NotNull
    @Pattern(regexp = PfReferenceKey.LOCAL_NAME_REGEXP)
    private String pdpGroup;

    @Column
    @NotNull
    @Pattern(regexp = PfReferenceKey.LOCAL_NAME_REGEXP)
    private String pdpType;

    @Column
    @NotNull
    @VerifyKey(versionNotNull = true)
    private PfConceptKey policyType;

    @Column(columnDefinition = "TINYINT DEFAULT 1")
    private boolean deploy;

    @Column
    @NotNull
    private State state;


    /**
     * Constructs an empty object.
     */
    public JpaPdpPolicyStatus() {
        key = new PfReferenceKey();
        pdpGroup = PfKey.NULL_KEY_NAME;
        pdpType = PfKey.NULL_KEY_NAME;
        policyType = new PfConceptKey();
        deploy = false;
        state = State.WAITING;
    }

    /**
     * Copy constructor.
     *
     * @param source object from which to copy
     */
    public JpaPdpPolicyStatus(JpaPdpPolicyStatus source) {
        key = new PfReferenceKey(source.getKey());
        pdpGroup = source.getPdpGroup();
        pdpType = source.getPdpType();
        policyType = new PfConceptKey(source.getPolicyType());
        deploy = source.isDeploy();
        state = source.getState();
    }

    /**
     * Authorative constructor.
     *
     * @param source authorative object from which to copy
     */
    public JpaPdpPolicyStatus(PdpPolicyStatus source) {
        fromAuthorative(source);
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

        final JpaPdpPolicyStatus other = (JpaPdpPolicyStatus) otherConcept;

        // @formatter:off
        return new CompareToBuilder()
                        .append(key, other.key)
                        .append(pdpGroup, other.pdpGroup)
                        .append(pdpType, other.pdpType)
                        .append(policyType, other.policyType)
                        .append(deploy, other.deploy)
                        .append(state, other.state)
                        .toComparison();
        // @formatter:on
    }

    @Override
    public PdpPolicyStatus toAuthorative() {
        PfConceptKey policyKey = key.getParentConceptKey();
        var policyIdent = new ToscaConceptIdentifier(policyKey.getName(), policyKey.getVersion());
        var policyTypeIdent = new ToscaConceptIdentifier(policyType.getName(), policyType.getVersion());

        // @formatter:off
        return PdpPolicyStatus.builder()
                        .pdpGroup(pdpGroup)
                        .pdpId(key.getLocalName())
                        .pdpType(pdpType)
                        .policyType(policyTypeIdent)
                        .policy(policyIdent)
                        .deploy(deploy)
                        .state(state)
                        .build();
        // @formatter:on
    }

    @Override
    public void fromAuthorative(PdpPolicyStatus source) {
        final ToscaConceptIdentifier policyIdent = source.getPolicy();
        final ToscaConceptIdentifier policyTypeIdent = source.getPolicyType();

        key = new PfReferenceKey(policyIdent.getName(), policyIdent.getVersion(), source.getPdpId());
        pdpGroup = source.getPdpGroup();
        pdpType = source.getPdpType();
        policyType = new PfConceptKey(policyTypeIdent.getName(), policyTypeIdent.getVersion());
        deploy = source.isDeploy();
        state = source.getState();
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
        policyType.clean();
    }

    @Override
    public BeanValidationResult validate(@NonNull String fieldName) {
        BeanValidationResult result = super.validate(fieldName);

        if (PfKey.NULL_KEY_NAME.equals(key.getParentKeyName())) {
            addResult(result, "policy name (parent key name of key)", key.getParentKeyName(), Validated.IS_NULL);
        }

        if (PfKey.NULL_KEY_VERSION.equals(key.getParentKeyVersion())) {
            addResult(result, "policy version (parent key version of key)", key.getParentKeyVersion(),
                            Validated.IS_NULL);
        }

        if (!PfKey.NULL_KEY_NAME.equals(key.getParentLocalName())) {
            addResult(result, "parent local name of key", key.getParentLocalName(), "must be " + PfKey.NULL_KEY_NAME);
        }

        if (PfKey.NULL_KEY_NAME.equals(key.getLocalName())) {
            addResult(result, "pdpId (local name of key)", key.getLocalName(), Validated.IS_NULL);
        }

        return result;
    }
}
