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

import javax.persistence.Column;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;

import org.apache.commons.lang3.ObjectUtils;
import org.onap.policy.models.tosca.authorative.concepts.ToscaConstraint;

/**
 * This class represents a logical TOSCA constraint: =,>,>=,<,<=.
 */
@EqualsAndHashCode(callSuper = false)
@ToString
public class JpaToscaConstraintLogical extends JpaToscaConstraint {
    private static final long serialVersionUID = -2730203215911880756L;

    @Column
    @NonNull
    @Getter
    private JpaToscaConstraintOperation operation;

    @Column
    @NonNull
    @Getter
    private String compareTo;

    /**
     * Constructor to set operation.
     *
     * @param operation the operation to set
     * @param compareTo the string to compare to
     */
    public JpaToscaConstraintLogical(@NonNull final JpaToscaConstraintOperation operation,
            @NonNull final String compareTo) {
        this.operation = operation;
        this.compareTo = compareTo;
    }

    /**
     * Authorative constructor.
     *
     * @param authorativeConcept the authorative concept to copy from
     */
    public JpaToscaConstraintLogical(final ToscaConstraint authorativeConcept) {
        super(authorativeConcept);
    }

    @Override
    public ToscaConstraint toAuthorative() {
        ToscaConstraint toscaConstraint = new ToscaConstraint();

        if (operation == null) {
            return null;
        }

        switch (operation) {
            case EQ:
                toscaConstraint.setEqual(compareTo);
                break;

            case GT:
                toscaConstraint.setGreaterThan(compareTo);
                break;

            case GE:
                toscaConstraint.setGreaterOrEqual(compareTo);
                break;

            case LT:
                toscaConstraint.setLessThan(compareTo);
                break;

            case LE:
                toscaConstraint.setLessOrEqual(compareTo);
                break;

            default:
                // Can't happen
        }

        return toscaConstraint;
    }

    @Override
    public void fromAuthorative(final ToscaConstraint toscaConstraint) {
        // @formatter:off
        if (toscaConstraint.getEqual() != null) {
            operation = JpaToscaConstraintOperation.EQ;
            compareTo = toscaConstraint.getEqual();
        }
        else if (toscaConstraint.getGreaterThan() != null) {
            operation = JpaToscaConstraintOperation.GT;
            compareTo = toscaConstraint.getGreaterThan();
        }
        else if (toscaConstraint.getGreaterOrEqual() != null) {
            operation = JpaToscaConstraintOperation.GE;
            compareTo = toscaConstraint.getGreaterOrEqual();
        }
        else if (toscaConstraint.getLessThan() != null) {
            operation = JpaToscaConstraintOperation.LT;
            compareTo = toscaConstraint.getLessThan();
        }
        else if (toscaConstraint.getLessOrEqual() != null) {
            operation = JpaToscaConstraintOperation.LE;
            compareTo = toscaConstraint.getLessOrEqual();
        }
        // @formatter:on
    }

    @Override
    public int compareTo(JpaToscaConstraint otherConstraint) {
        if (otherConstraint == null) {
            return -1;
        }
        if (this == otherConstraint) {
            return 0;
        }
        if (getClass() != otherConstraint.getClass()) {
            return this.hashCode() - otherConstraint.hashCode();
        }

        final JpaToscaConstraintLogical other = (JpaToscaConstraintLogical) otherConstraint;

        int result = ObjectUtils.compare(operation, other.operation);
        if (result != 0) {
            return result;
        }

        return ObjectUtils.compare(compareTo, other.compareTo);
    }
}
