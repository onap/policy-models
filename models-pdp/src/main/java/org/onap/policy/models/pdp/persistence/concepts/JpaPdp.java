/*-
 * ============LICENSE_START=======================================================
 * ONAP Policy Model
 * ================================================================================
 * Copyright (C) 2019 AT&T Intellectual Property. All rights reserved.
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
import java.util.List;

import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.Table;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;

import org.apache.commons.lang3.ObjectUtils;
import org.onap.policy.common.utils.validation.Assertions;
import org.onap.policy.models.base.PfAuthorative;
import org.onap.policy.models.base.PfConcept;
import org.onap.policy.models.base.PfKey;
import org.onap.policy.models.base.PfReferenceKey;
import org.onap.policy.models.base.PfValidationMessage;
import org.onap.policy.models.base.PfValidationResult;
import org.onap.policy.models.base.PfValidationResult.ValidationResult;
import org.onap.policy.models.pdp.concepts.Pdp;
import org.onap.policy.models.pdp.enums.PdpHealthStatus;
import org.onap.policy.models.pdp.enums.PdpState;

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
        this.key = key;
        this.pdpState = pdpState;
        this.healthy = healthy;
    }

    /**
     * Copy constructor.
     *
     * @param copyConcept the concept to copy from
     */
    public JpaPdp(final JpaPdp copyConcept) {
        super(copyConcept);
    }

    /**
     * Authorative constructor.
     *
     * @param authorativeConcept the authorative concept to copy from
     */
    public JpaPdp(final Pdp authorativeConcept) {
        this.fromAuthorative(authorativeConcept);
    }

    @Override
    public Pdp toAuthorative() {
        Pdp pdp = new Pdp();

        pdp.setInstanceId(key.getLocalName());
        pdp.setPdpState(pdpState);
        pdp.setHealthy(healthy);
        pdp.setMessage(message);

        return pdp;
    }

    @Override
    public void fromAuthorative(final Pdp pdp) {
        if (this.getKey().isNullKey()) {
            this.setKey(new PfReferenceKey());
            getKey().setLocalName(pdp.getInstanceId());
        }

        this.setPdpState(pdp.getPdpState());
        this.setHealthy(pdp.getHealthy());
        this.setMessage(pdp.getMessage());
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
    public PfValidationResult validate(final PfValidationResult resultIn) {
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

        if (message != null && message.trim().length() == 0) {
            result.addValidationMessage(new PfValidationMessage(key, this.getClass(), ValidationResult.INVALID,
                    "message may not be blank"));
        }

        return result;
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
            return this.hashCode() - otherConcept.hashCode();
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

        return ObjectUtils.compare(message, other.message);
    }

    @Override
    public PfConcept copyTo(@NonNull final PfConcept target) {
        Assertions.instanceOf(target, JpaPdp.class);

        final JpaPdp copy = ((JpaPdp) target);
        copy.setKey(new PfReferenceKey(key));
        copy.setPdpState(pdpState);
        copy.setHealthy(healthy);
        copy.setMessage(message);

        return copy;
    }
}
