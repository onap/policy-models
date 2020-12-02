/*-
 * ============LICENSE_START=======================================================
 * ONAP Policy Model
 * ================================================================================
 * Copyright (C) 2019-2020 AT&T Intellectual Property. All rights reserved.
 * Modifications Copyright (C) 2019 Nordix Foundation.
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
import org.apache.commons.lang3.StringUtils;
import org.onap.policy.models.base.PfAuthorative;
import org.onap.policy.models.base.PfConcept;
import org.onap.policy.models.base.PfConceptKey;
import org.onap.policy.models.base.PfKey;
import org.onap.policy.models.base.PfReferenceKey;
import org.onap.policy.models.base.PfUtils;
import org.onap.policy.models.base.PfValidationMessage;
import org.onap.policy.models.base.PfValidationResult;
import org.onap.policy.models.base.PfValidationResult.ValidationResult;
import org.onap.policy.models.pdp.concepts.Pdp;
import org.onap.policy.models.pdp.enums.PdpHealthStatus;
import org.onap.policy.models.pdp.enums.PdpState;
import org.onap.policy.models.tosca.authorative.concepts.ToscaPolicyIdentifier;

/**
 * Class to represent a PDP in the database.
 *
 * @author Liam Fallon (liam.fallon@est.tech)
 */
@Entity
@Table(name = "Pdp")
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
@Data
@EqualsAndHashCode(callSuper = false)
public class JpaPdp extends PfConcept implements PfAuthorative<Pdp>, Serializable {
    private static final long serialVersionUID = -357224425637789775L;

    @EmbeddedId
    private PfReferenceKey key;

    @Column
    private PdpState pdpState;

    @Column
    private PdpHealthStatus healthy;

    @Column
    private String message;

    @ElementCollection
    private List<PfConceptKey> awaitingDeployment;

    @ElementCollection
    private List<PfConceptKey> awaitingUndeployment;

    @ElementCollection
    private List<PfConceptKey> failedDeployment;

    @ElementCollection
    private List<PfConceptKey> failedUndeployment;

    /**
     * The Default Constructor creates a {@link JpaPdp} object with a null key.
     */
    public JpaPdp() {
        this(new PfReferenceKey());
    }

    /**
     * The Key Constructor creates a {@link JpaPdp} object with the given concept key.
     *
     * @param key the key
     */
    public JpaPdp(@NonNull final PfReferenceKey key) {
        this(key, PdpState.PASSIVE, PdpHealthStatus.UNKNOWN);
    }

    /**
     * The Key Constructor creates a {@link JpaPdp} object with all mandatory fields.
     *
     * @param key the key
     * @param pdpState the state of the PDP
     * @param healthy the health state of the PDP
     */
    public JpaPdp(@NonNull final PfReferenceKey key, @NonNull final PdpState pdpState,
            @NonNull PdpHealthStatus healthy) {
        this(key, pdpState, healthy, new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>());
    }

    /**
     * The Key Constructor creates a {@link JpaPdp} object with all mandatory fields.
     *
     * @param key the key
     * @param pdpState the state of the PDP
     * @param healthy the health state of the PDP
     */
    public JpaPdp(@NonNull final PfReferenceKey key, @NonNull final PdpState pdpState, @NonNull PdpHealthStatus healthy,
                    @NonNull List<PfConceptKey> awaitingDeployment, @NonNull List<PfConceptKey> awaitingUndeployment,
                    @NonNull List<PfConceptKey> failedDeployment, @NonNull List<PfConceptKey> failedUndeployment) {
        this.key = key;
        this.pdpState = pdpState;
        this.healthy = healthy;
        this.awaitingDeployment = awaitingDeployment;
        this.awaitingUndeployment = awaitingUndeployment;
        this.failedDeployment = failedDeployment;
        this.failedUndeployment = failedUndeployment;
    }

    /**
     * Copy constructor.
     *
     * @param copyConcept the concept to copy from
     */
    public JpaPdp(@NonNull final JpaPdp copyConcept) {
        super(copyConcept);
        this.key = new PfReferenceKey(copyConcept.key);
        this.pdpState = copyConcept.pdpState;
        this.healthy = copyConcept.healthy;
        this.message = copyConcept.message;
        this.awaitingDeployment =
                        PfUtils.mapList(copyConcept.awaitingDeployment, PfConceptKey::new, new ArrayList<>(0));
        this.awaitingUndeployment =
                        PfUtils.mapList(copyConcept.awaitingUndeployment, PfConceptKey::new, new ArrayList<>(0));
        this.failedDeployment =
                        PfUtils.mapList(copyConcept.failedDeployment, PfConceptKey::new, new ArrayList<>(0));
        this.failedUndeployment =
                        PfUtils.mapList(copyConcept.failedUndeployment, PfConceptKey::new, new ArrayList<>(0));
    }

    /**
     * Authorative constructor.
     *
     * @param authorativeConcept the authorative concept to copy from
     */
    public JpaPdp(@NonNull final Pdp authorativeConcept) {
        this.fromAuthorative(authorativeConcept);
    }

    @Override
    public Pdp toAuthorative() {
        Pdp pdp = new Pdp();

        pdp.setInstanceId(key.getLocalName());
        pdp.setPdpState(pdpState);
        pdp.setHealthy(healthy);
        pdp.setMessage(message);

        pdp.setAwaitingDeployment(
                        PfUtils.mapList(awaitingDeployment, ToscaPolicyIdentifier::fromConcept, new ArrayList<>(0)));
        pdp.setAwaitingUndeployment(
                        PfUtils.mapList(awaitingUndeployment, ToscaPolicyIdentifier::fromConcept, new ArrayList<>(0)));

        pdp.setFailedDeployment(
                        PfUtils.mapList(failedDeployment, ToscaPolicyIdentifier::fromConcept, new ArrayList<>(0)));
        pdp.setFailedUndeployment(
                        PfUtils.mapList(failedUndeployment, ToscaPolicyIdentifier::fromConcept, new ArrayList<>(0)));

        return pdp;
    }

    @Override
    public void fromAuthorative(@NonNull final Pdp pdp) {
        if (this.key == null || this.getKey().isNullKey()) {
            this.setKey(new PfReferenceKey());
            getKey().setLocalName(pdp.getInstanceId());
        }

        this.setPdpState(pdp.getPdpState());
        this.setHealthy(pdp.getHealthy());
        this.setMessage(pdp.getMessage());

        this.setAwaitingDeployment(PfUtils.mapList(pdp.getAwaitingDeployment(), ToscaPolicyIdentifier::toConcept,
                        new ArrayList<>(0)));
        this.setAwaitingUndeployment(PfUtils.mapList(pdp.getAwaitingUndeployment(), ToscaPolicyIdentifier::toConcept,
                        new ArrayList<>(0)));

        this.setFailedDeployment(PfUtils.mapList(pdp.getFailedDeployment(), ToscaPolicyIdentifier::toConcept,
                        new ArrayList<>(0)));
        this.setFailedUndeployment(PfUtils.mapList(pdp.getFailedUndeployment(), ToscaPolicyIdentifier::toConcept,
                        new ArrayList<>(0)));
    }

    @Override
    public List<PfKey> getKeys() {
        return getKey().getKeys();
    }

    @Override
    public void clean() {
        key.clean();

        if (message != null) {
            message = message.trim();
        }
    }

    @Override
    public PfValidationResult validate(@NonNull final PfValidationResult resultIn) {
        PfValidationResult result = resultIn;

        if (key.isNullKey()) {
            result.addValidationMessage(
                    new PfValidationMessage(key, this.getClass(), ValidationResult.INVALID, "key is a null key"));
        }

        result = key.validate(result);

        if (key.getParentConceptKey().isNullKey()) {
            result.addValidationMessage(new PfValidationMessage(key, this.getClass(), ValidationResult.INVALID,
                    "parent of key is a null key"));
        }

        if (PfKey.NULL_KEY_NAME.equals(key.getParentLocalName())) {
            result.addValidationMessage(new PfValidationMessage(key, this.getClass(), ValidationResult.INVALID,
                    "local name of parent of key is null"));
        }

        if (pdpState == null) {
            result.addValidationMessage(new PfValidationMessage(key, this.getClass(), ValidationResult.INVALID,
                    "PDP state may not be null"));
        }

        if (healthy == null) {
            result.addValidationMessage(new PfValidationMessage(key, this.getClass(), ValidationResult.INVALID,
                    "PDP health status may not be null"));
        }

        if (message != null && StringUtils.isBlank(message)) {
            result.addValidationMessage(new PfValidationMessage(key, this.getClass(), ValidationResult.INVALID,
                    "message may not be blank"));
        }

        validatePolicies(result, awaitingDeployment, "awaiting deployment");
        validatePolicies(result, awaitingUndeployment, "awaiting undeployment");

        validatePolicies(result, failedDeployment, "failed deployment");
        validatePolicies(result, failedUndeployment, "failed undeployment");

        return result;
    }

    private void validatePolicies(PfValidationResult result, List<PfConceptKey> policies, String deployType) {
        if (policies == null) {
            result.addValidationMessage(new PfValidationMessage(key, this.getClass(), ValidationResult.INVALID,
                    "a PDP must have a list of policies " + deployType));
        } else {
            for (PfConceptKey policyKey : policies) {
                result = policyKey.validate(result);
            }
        }
    }

    @Override
    public int compareTo(final PfConcept otherConcept) {
        if (otherConcept == null) {
            return -1;
        }
        if (this == otherConcept) {
            return 0;
        }
        if (getClass() != otherConcept.getClass()) {
            return getClass().getName().compareTo(otherConcept.getClass().getName());
        }

        final JpaPdp other = (JpaPdp) otherConcept;
        if (!key.equals(other.key)) {
            return key.compareTo(other.key);
        }

        int result = ObjectUtils.compare(pdpState, other.pdpState);
        if (result != 0) {
            return result;
        }

        result = ObjectUtils.compare(healthy, other.healthy);
        if (result != 0) {
            return result;
        }

        result = ObjectUtils.compare(message, other.message);
        if (result != 0) {
            return result;
        }

        result = PfUtils.compareObjects(awaitingDeployment, other.awaitingDeployment);
        if (result != 0) {
            return result;
        }

        result = PfUtils.compareObjects(awaitingUndeployment, other.awaitingUndeployment);
        if (result != 0) {
            return result;
        }

        result = PfUtils.compareObjects(failedDeployment, other.failedDeployment);
        if (result != 0) {
            return result;
        }

        return PfUtils.compareObjects(failedUndeployment, other.failedUndeployment);
    }
}
