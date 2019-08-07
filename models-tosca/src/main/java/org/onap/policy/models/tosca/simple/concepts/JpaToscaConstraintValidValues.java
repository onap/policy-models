/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2019 AT&T Intellectual Property. All rights reserved.
 *  Modifications Copyright (C) 2019 Nordix Foundation.
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

import java.util.ArrayList;
import java.util.List;
import javax.persistence.ElementCollection;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;

import org.onap.policy.models.base.PfUtils;
import org.onap.policy.models.tosca.authorative.concepts.ToscaConstraint;

/**
 * This class represents valid_values TOSCA constraint.
 *
 * @author Chenfei Gao (cgao@research.att.com)
 */
@EqualsAndHashCode(callSuper = false)
@ToString
public class JpaToscaConstraintValidValues extends JpaToscaConstraint {
    private static final long serialVersionUID = -5060193250508635456L;

    @ElementCollection
    @NonNull
    @Getter
    private List<String> validValues;

    /**
     * Constructor to set the valid values.
     *
     * @param validValues the valid values that are allowed
     */
    public JpaToscaConstraintValidValues(@NonNull final List<String> validValues) {
        this.validValues = validValues;
    }

    /**
     * Authorative constructor.
     *
     * @param authorativeConcept the authorative concept to copy from
     */
    public JpaToscaConstraintValidValues(final ToscaConstraint authorativeConcept) {
        super(authorativeConcept);
    }

    @Override
    public ToscaConstraint toAuthorative() {
        ToscaConstraint toscaConstraint = new ToscaConstraint();

        toscaConstraint.setValidValues(validValues);

        return toscaConstraint;
    }

    @Override
    public void fromAuthorative(final ToscaConstraint toscaConstraint) {
        if (toscaConstraint.getValidValues() != null) {
            validValues = new ArrayList<>();
            validValues.addAll(toscaConstraint.getValidValues());
        }
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
            return getClass().getName().compareTo(otherConstraint.getClass().getName());
        }

        final JpaToscaConstraintValidValues other = (JpaToscaConstraintValidValues) otherConstraint;

        return PfUtils.compareObjects(validValues, other.validValues);
    }
}
