/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2019 AT&T Intellectual Property. All rights reserved.
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

import com.google.gson.annotations.SerializedName;

import java.util.LinkedList;
import java.util.List;
import javax.persistence.ElementCollection;
import javax.ws.rs.core.Response;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;

import org.onap.policy.models.base.PfConcept;
import org.onap.policy.models.base.PfModelRuntimeException;
import org.onap.policy.models.base.PfReferenceKey;

/**
 * This class represents valid_values TOSCA constraint.
 *
 * @author Chenfei Gao (cgao@research.att.com)
 */
@EqualsAndHashCode(callSuper = false)
@Data
public class ToscaConstraintValidValues extends ToscaConstraint {
    private static final long serialVersionUID = 3152323457560746844L;

    @SerializedName("valid_values")
    @NonNull
    @ElementCollection
    private final List<String> validValues;

    /**
     * The Default Constructor creates a {@link ToscaConstraintValidValues} object with a null key.
     */
    public ToscaConstraintValidValues() {
        this(new PfReferenceKey());
    }

    /**
     * The Key Constructor creates a {@link ToscaConstraintValidValues} object with the given concept key.
     *
     * @param key the key of the constraint
     */
    public ToscaConstraintValidValues(final PfReferenceKey key) {
        super(key);
        validValues = new LinkedList<>();
    }

    /**
     * The Key Constructor creates a {@link ToscaConstraintLogical} object with the given concept key
     * and valid values list.
     *
     * @param key the key of the constraint
     * @param validValues the valid values list of the constraint
     *
     */
    public ToscaConstraintValidValues(final PfReferenceKey key, @NonNull final List<String> validValues) {
        super(key);
        this.validValues = validValues;
    }

    /**
     * Copy constructor.
     *
     * @param copyConcept the concept to copy from
     */
    public ToscaConstraintValidValues(@NonNull final ToscaConstraintValidValues copyConcept) {
        throw new PfModelRuntimeException(Response.Status.INTERNAL_SERVER_ERROR, "cannot copy an immutable constraint");
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

        final ToscaConstraintValidValues other = (ToscaConstraintValidValues) otherConcept;

        int result = super.compareTo(other);
        if (result != 0) {
            return result;
        }

        if (validValues.equals(other.validValues)) {
            return 0;
        }
        return -1;
    }

    @Override
    public PfConcept copyTo(@NonNull final PfConcept target) {
        throw new PfModelRuntimeException(Response.Status.INTERNAL_SERVER_ERROR, "cannot copy an immutable constraint");
    }
}
