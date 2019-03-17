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

package org.onap.policy.models.tosca.simple.concepts;

import java.util.List;

import javax.persistence.EmbeddedId;
import javax.ws.rs.core.Response;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;

import org.onap.policy.models.base.PfConcept;
import org.onap.policy.models.base.PfKey;
import org.onap.policy.models.base.PfModelRuntimeException;
import org.onap.policy.models.base.PfReferenceKey;
import org.onap.policy.models.base.PfValidationMessage;
import org.onap.policy.models.base.PfValidationResult;
import org.onap.policy.models.base.PfValidationResult.ValidationResult;

/**
 * Immutable class to represent the Constraint of property in TOSCA definition.
 *
 * @author Chenfei Gao (cgao@research.att.com)
 * @author Liam Fallon (liam.fallon@est.tech)
 */
@Data
@EqualsAndHashCode(callSuper = false)
public abstract class ToscaConstraint extends PfConcept {
    private static final long serialVersionUID = 6426438089914347734L;

    @EmbeddedId
    private final PfReferenceKey key;

    /**
     * The Default Constructor creates a {@link ToscaConstraint} object with a null key.
     */
    public ToscaConstraint() {
        this(new PfReferenceKey());
    }

    /**
     * The Key Constructor creates a {@link ToscaConstraint} object with the given concept key.
     *
     * @param key the key
     */
    public ToscaConstraint(@NonNull final PfReferenceKey key) {
        this.key = key;
    }

    /**
     * Copy constructor.
     *
     * @param copyConcept the concept to copy from
     */
    public ToscaConstraint(@NonNull final ToscaConstraint copyConcept) {
        throw new PfModelRuntimeException(Response.Status.INTERNAL_SERVER_ERROR, "cannot copy an immutable constraint");
    }

    @Override
    public List<PfKey> getKeys() {
        return getKey().getKeys();
    }

    @Override
    public void clean() {
        key.clean();
    }

    @Override
    public PfValidationResult validate(@NonNull final PfValidationResult resultIn) {
        PfValidationResult result = resultIn;

        if (key.isNullKey()) {
            result.addValidationMessage(
                    new PfValidationMessage(key, this.getClass(), ValidationResult.INVALID, "key is a null key"));
        }

        return key.validate(result);
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

        final ToscaConstraint other = (ToscaConstraint) otherConcept;

        return key.compareTo(other.key);
    }

    @Override
    public PfConcept copyTo(@NonNull final PfConcept target) {
        throw new PfModelRuntimeException(Response.Status.INTERNAL_SERVER_ERROR, "cannot copy an immutable constraint");
    }
}
