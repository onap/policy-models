/*-
 * ============LICENSE_START=======================================================
 * ONAP Policy Model
 * ================================================================================
 * Copyright (C) 2019-2021 AT&T Intellectual Property. All rights reserved.
 * Modifications Copyright (C) 2019, 2023 Nordix Foundation.
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

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import java.io.Serial;
import java.io.Serializable;
import java.util.Date;
import java.util.List;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import org.apache.commons.lang3.builder.CompareToBuilder;
import org.onap.policy.common.parameters.BeanValidationResult;
import org.onap.policy.common.parameters.annotations.NotBlank;
import org.onap.policy.common.parameters.annotations.NotNull;
import org.onap.policy.models.base.PfAuthorative;
import org.onap.policy.models.base.PfConcept;
import org.onap.policy.models.base.PfKey;
import org.onap.policy.models.base.PfReferenceKey;
import org.onap.policy.models.base.validation.annotations.VerifyKey;
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
    @Serial
    private static final long serialVersionUID = -357224425637789775L;

    @EmbeddedId
    @VerifyKey
    @NotNull
    private PfReferenceKey key;

    @Column
    @NotNull
    private PdpState pdpState;

    @Column
    @NotNull
    private PdpHealthStatus healthy;

    @Column
    @NotBlank
    private String message;

    @Column(precision = 0)
    @Temporal(TemporalType.TIMESTAMP)
    private Date lastUpdate;

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
        this.lastUpdate = new Date();
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
        this.lastUpdate = copyConcept.lastUpdate;
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
        var pdp = new Pdp();

        pdp.setInstanceId(key.getLocalName());
        pdp.setPdpState(pdpState);
        pdp.setHealthy(healthy);
        pdp.setMessage(message);
        pdp.setLastUpdate(lastUpdate.toInstant());

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

        if (pdp.getLastUpdate() == null) {
            this.setLastUpdate(new Date());
        } else {
            this.setLastUpdate(Date.from(pdp.getLastUpdate()));
        }
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
    public BeanValidationResult validate(@NonNull String fieldName) {
        BeanValidationResult result = super.validate(fieldName);

        validateKeyNotNull(result, "parent of key", key.getParentConceptKey());

        if (PfKey.NULL_KEY_NAME.equals(key.getParentLocalName())) {
            addResult(result, "local name of parent of key", key.getParentLocalName(), IS_NULL);
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
            return getClass().getName().compareTo(otherConcept.getClass().getName());
        }

        final JpaPdp other = (JpaPdp) otherConcept;

        // @formatter:off
        return new CompareToBuilder()
                        .append(this.key, other.key)
                        .append(this.pdpState, other.pdpState)
                        .append(this.healthy, other.healthy)
                        .append(this.message, other.message)
                        .append(this.lastUpdate, other.lastUpdate)
                        .toComparison();
        // @formatter:on
    }
}
