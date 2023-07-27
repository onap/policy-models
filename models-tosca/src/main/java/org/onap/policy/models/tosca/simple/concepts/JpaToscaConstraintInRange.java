/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2021, 2023 Nordix Foundation.
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

import jakarta.persistence.ElementCollection;
import java.io.Serial;
import java.util.ArrayList;
import java.util.List;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;
import org.onap.policy.models.base.PfUtils;
import org.onap.policy.models.tosca.authorative.concepts.ToscaConstraint;

/**
 * This class represents in_range TOSCA constraint.
 */
@EqualsAndHashCode(callSuper = false)
@ToString
public class JpaToscaConstraintInRange extends JpaToscaConstraint {
    @Serial
    private static final long serialVersionUID = -5060193250508635456L;

    @ElementCollection
    @Getter
    private List<String> rangeValues;

    /**
     * Constructor to set the range values.
     *
     * @param rangeValues the range values that are allowed
     */
    public JpaToscaConstraintInRange(@NonNull final List<String> rangeValues) {
        this.rangeValues = rangeValues;
    }

    /**
     * Authorative constructor.
     *
     * @param authorativeConcept the authorative concept to copy from
     */
    public JpaToscaConstraintInRange(final ToscaConstraint authorativeConcept) {
        /*
         * The following will call invoke fromAuthorative() which will populate the class fields.
         */
        super(authorativeConcept);
    }

    @Override
    public ToscaConstraint toAuthorative() {
        var toscaConstraint = new ToscaConstraint();

        toscaConstraint.setRangeValues(rangeValues);

        return toscaConstraint;
    }

    @Override
    public void fromAuthorative(final ToscaConstraint toscaConstraint) {
        rangeValues = new ArrayList<>();
        if (toscaConstraint.getRangeValues() != null) {
            rangeValues.addAll(toscaConstraint.getRangeValues());
        }
    }

    @Override
    public int compareTo(@NonNull JpaToscaConstraint otherConstraint) {
        if (this == otherConstraint) {
            return 0;
        }
        if (getClass() != otherConstraint.getClass()) {
            return getClass().getName().compareTo(otherConstraint.getClass().getName());
        }

        final JpaToscaConstraintInRange other = (JpaToscaConstraintInRange) otherConstraint;

        return PfUtils.compareObjects(rangeValues, other.rangeValues);
    }
}
