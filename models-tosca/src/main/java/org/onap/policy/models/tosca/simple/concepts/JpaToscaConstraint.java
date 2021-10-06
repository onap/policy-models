/*-
 * ============LICENSE_START=======================================================
 * ONAP Policy Model
 * ================================================================================
 * Copyright (C) 2019-2021 AT&T Intellectual Property. All rights reserved.
 * Modifications Copyright (C) 2019, 2021 Nordix Foundation.
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

import java.io.Serializable;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.onap.policy.models.base.PfAuthorative;
import org.onap.policy.models.tosca.authorative.concepts.ToscaConstraint;

/**
 * Immutable class to represent the Constraint of property in TOSCA definition.
 *
 * @author Chenfei Gao (cgao@research.att.com)
 * @author Liam Fallon (liam.fallon@est.tech)
 */
@NoArgsConstructor
@EqualsAndHashCode
public abstract class JpaToscaConstraint
        implements PfAuthorative<ToscaConstraint>, Serializable, Comparable<JpaToscaConstraint> {
    private static final long serialVersionUID = -2689472945262507455L;

    /**
     * Authorative constructor.
     *
     * @param authorativeConcept the authorative concept to copy from
     */
    protected JpaToscaConstraint(final ToscaConstraint authorativeConcept) {
        this.fromAuthorative(authorativeConcept);
    }

    /**
     * Create instances of constraints of various types.
     *
     * @param toscaConstraint the incoming constraint
     * @return the constraint
     */
    public static JpaToscaConstraint newInstance(final ToscaConstraint toscaConstraint) {
        if (toscaConstraint.getValidValues() != null) {
            return new JpaToscaConstraintValidValues(toscaConstraint);
        }

        if (toscaConstraint.getRangeValues() != null) {
            return new JpaToscaConstraintInRange(toscaConstraint);
        }

        return (new JpaToscaConstraintLogical(toscaConstraint));
    }
}
