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

package org.onap.policy.models.tosca.simple.concepts;

import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.Table;
import javax.ws.rs.core.Response;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;

import org.onap.policy.models.base.PfConcept;
import org.onap.policy.models.base.PfConceptKey;
import org.onap.policy.models.base.PfKey;
import org.onap.policy.models.base.PfModelRuntimeException;
import org.onap.policy.models.base.PfReferenceKey;
import org.onap.policy.models.base.PfValidationMessage;
import org.onap.policy.models.base.PfValidationResult;
import org.onap.policy.models.base.PfValidationResult.ValidationResult;

/**
 * This immutable class represents a logical TOSCA constraint: =,>,>=,<,<= that compares the owner
 * of an instance of the class to the referenced key.
 */
@Entity
@Table(name = "ToscaConstraintLogicalKey")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@EqualsAndHashCode(callSuper = false)
@ToString
public final class ToscaConstraintLogicalKey extends ToscaConstraintLogical {
    private static final long serialVersionUID = -2420828090326264341L;

    @Column
    @NonNull
    @Getter
    private final PfKey compareToKey;

    /**
     * The Default Constructor creates a {@link ToscaConstraintLogicalKey} object with a null key.
     */
    public ToscaConstraintLogicalKey() {
        this(new PfReferenceKey());
    }

    /**
     * The Key Constructor creates a {@link ToscaConstraintLogicalKey} object with the given concept
     * key.
     *
     * @param key the key of the constraint
     */
    public ToscaConstraintLogicalKey(final PfReferenceKey key) {
        this(key, Operation.EQ, PfConceptKey.getNullKey());
    }

    /**
     * The Key Constructor creates a {@link ToscaConstraintLogicalKey} object with the given concept
     * key, operation, and compare key.
     *
     * @param key the key of the constraint
     * @param operation the logical operation of the constraint
     * @param compareToKey the key of the object to which the object that owns this constraint will
     *        be compared
     */
    public ToscaConstraintLogicalKey(final PfReferenceKey key, @NonNull final Operation operation,
            @NonNull final PfKey compareToKey) {
        super(key, operation);
        this.compareToKey = compareToKey;
    }

    /**
     * Copy constructor.
     *
     * @param copyConcept the concept to copy from
     */
    public ToscaConstraintLogicalKey(@NonNull final ToscaConstraintLogical copyConcept) {
        throw new PfModelRuntimeException(Response.Status.INTERNAL_SERVER_ERROR, "cannot copy an immutable constraint");
    }

    @Override
    public List<PfKey> getKeys() {
        final List<PfKey> keyList = super.getKeys();
        keyList.addAll(compareToKey.getKeys());
        return keyList;
    }

    @Override
    public void clean() {
        super.clean();
        compareToKey.clean();
    }

    @Override
    public PfValidationResult validate(final PfValidationResult resultIn) {
        PfValidationResult result = super.validate(resultIn);

        if (compareToKey.isNullKey()) {
            result.addValidationMessage(new PfValidationMessage(getKey(), this.getClass(), ValidationResult.INVALID,
                    "comparison key is a null key"));
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

        final ToscaConstraintLogicalKey other = (ToscaConstraintLogicalKey) otherConcept;

        int result = super.compareTo(other);
        if (result != 0) {
            return result;
        }

        return compareToKey.compareTo(other.compareToKey);
    }

    @Override
    public PfConcept copyTo(@NonNull final PfConcept target) {
        throw new PfModelRuntimeException(Response.Status.INTERNAL_SERVER_ERROR, "cannot copy an immutable constraint");
    }
}
