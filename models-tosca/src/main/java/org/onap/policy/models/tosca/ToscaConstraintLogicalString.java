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
import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.Table;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;

import org.onap.policy.common.utils.validation.ParameterValidationUtils;
import org.onap.policy.models.base.PfConcept;
import org.onap.policy.models.base.PfModelRuntimeException;
import org.onap.policy.models.base.PfReferenceKey;
import org.onap.policy.models.base.PfValidationMessage;
import org.onap.policy.models.base.PfValidationResult;
import org.onap.policy.models.base.PfValidationResult.ValidationResult;

/**
 * This class represents a logical TOSCA constraint: =,>,>=,<,<= that compares the owner of an
 * instance of the class to the given string.
 */
@Entity
@Table(name = "ToscaConstraintLogicalString")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@Data
@EqualsAndHashCode(callSuper = false)
public class ToscaConstraintLogicalString extends ToscaConstraintLogical {
    private static final long serialVersionUID = 8167550632122339195L;

    @Column
    @NonNull
    @Getter
    private final String compareToString;

    /**
     * The Default Constructor creates a {@link ToscaConstraintLogicalString} object with a null key.
     */
    public ToscaConstraintLogicalString() {
        this(new PfReferenceKey());
    }

    /**
     * The Key Constructor creates a {@link ToscaConstraintLogicalString} object with the given concept
     * key.
     *
     * @param key the key of the constraint
     */
    public ToscaConstraintLogicalString(@NonNull final PfReferenceKey key) {
        this(key, Operation.EQ, "");
    }

    /**
     * The Key Constructor creates a {@link ToscaConstraintLogicalString} object with the given concept
     * key, operation, and compare string.
     *
     * @param key the key of the constraint
     * @param operation the logical operation of the constraint
     * @param compareToString the key of the object to which the object that owns this constraint will
     *        be compared
     */
    public ToscaConstraintLogicalString(@NonNull final PfReferenceKey key, @NonNull final Operation operation,
            @NonNull final String compareToString) {
        super(key, operation);
        this.compareToString = compareToString.trim();
    }

    @Override
    public PfValidationResult validate(final PfValidationResult resultIn) {
        PfValidationResult result = super.validate(resultIn);

        if (!ParameterValidationUtils.validateStringParameter(compareToString)) {
            result.addValidationMessage(new PfValidationMessage(getKey(), this.getClass(), ValidationResult.INVALID,
                    "comparison string is null or blank"));
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

        final ToscaConstraintLogicalString other = (ToscaConstraintLogicalString) otherConcept;

        int result = super.compareTo(other);
        if (result != 0) {
            return result;
        }

        return compareToString.compareTo(other.compareToString);
    }

    @Override
    public PfConcept copyTo(@NonNull final PfConcept target) {
        throw new PfModelRuntimeException("cannot copy an immutable constraint");
    }
}
