/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2019 Nordix Foundation.
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

package org.onap.policy.models.tosca;

import javax.persistence.Column;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;

import org.onap.policy.models.base.PfConcept;
import org.onap.policy.models.base.PfModelRuntimeException;
import org.onap.policy.models.base.PfReferenceKey;

/**
 * This class represents a logical TOSCA constraint: =,>,>=,<,<=.
 */
@EqualsAndHashCode(callSuper = false)
@ToString
public class ToscaConstraintLogical extends ToscaConstraint {
    private static final long serialVersionUID = 2562306457768745444L;

    public enum Operation {
        EQ,
        GT,
        GE,
        LT,
        LE
    }

    @Column
    @NonNull
    @Getter
    private final Operation operation;

    /**
     * The Default Constructor creates a {@link ToscaConstraintLogical} object with a null key.
     */
    public ToscaConstraintLogical() {
        this(new PfReferenceKey());
    }

    /**
     * The Key Constructor creates a {@link ToscaConstraintLogical} object with the given concept key.
     *
     * @param key the key of the constraint
     */
    public ToscaConstraintLogical(@NonNull final PfReferenceKey key) {
        this(key, Operation.EQ);
    }

    /**
     * The Key Constructor creates a {@link ToscaConstraintLogical} object with the given concept key and operation.
     *
     * @param key the key of the constraint
     * @param operation the logical operation of the constraint
     *
     */
    public ToscaConstraintLogical(@NonNull final PfReferenceKey key, @NonNull final Operation operation) {
        super(key);
        this.operation = operation;
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

        final ToscaConstraintLogical other = (ToscaConstraintLogical) otherConcept;

        int result = super.compareTo(other);
        if (result != 0) {
            return result;
        }

        return operation.compareTo(other.operation);
    }

    @Override
    public PfConcept copyTo(@NonNull final PfConcept target) {
        throw new PfModelRuntimeException("cannot copy an immutable constraint");
    }
}
