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

package org.onap.policy.models.base;

import java.io.Serializable;
import java.util.List;

import javax.xml.bind.annotation.XmlType;

import org.onap.policy.common.utils.validation.Assertions;

/**
 * This class is the base class for all model concept classes. It enforces implementation of
 * abstract methods and interfaces on all concepts that are sub-classes of this class.
 *
 * @author Liam Fallon (liam.fallon@ericsson.com)
 */

@XmlType(name = "PfConcept", namespace = "http://www.onap.org/policy/models")

public abstract class PfConcept implements Serializable, Comparable<PfConcept> {
    private static final long serialVersionUID = -7434939557282697490L;

    /**
     * Default constructor.
     */
    public PfConcept() {}

    /**
     * Copy constructor.
     *
     * @param copyConcept the concept to copy from
     */
    public PfConcept(final PfConcept copyConcept) {
        Assertions.argumentNotNull(copyConcept, "copy concept may not be null");
        copyConcept.copyTo(this);
    }

    /**
     * Gets the key of this concept.
     *
     * @return the concept key
     */
    public abstract PfKey getKey();

    /**
     * Gets a list of all keys for this concept and all concepts that are defined or referenced by
     * this concept and its sub-concepts.
     *
     * @return the keys used by this concept and it's contained concepts
     */
    public abstract List<PfKey> getKeys();

    /**
     * Validate that this concept is structurally correct.
     *
     * @param result the parameter in which the result of the validation will be returned
     * @return the validation result that was passed in in the @{link result} field with the result
     *         of this validation added
     */
    public abstract PfValidationResult validate(PfValidationResult result);

    /**
     * Clean this concept, tidy up any superfluous information such as leading and trailing white
     * space.
     */
    public abstract void clean();

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public abstract boolean equals(Object otherObject);

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Object#toString()
     */
    @Override
    public abstract String toString();

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Object#hashCode()
     */
    @Override
    public abstract int hashCode();

    /**
     * Copy this concept to another object. The target object must have the same class as the source
     * object.
     *
     * @param target the target object to which this object is copied
     * @return the copied object
     */
    public abstract PfConcept copyTo(PfConcept target);

    /**
     * Gets the ID string of this concept.
     *
     * @return the ID string of this concept
     */
    public String getId() {
        return getKey().getId();
    }

    /**
     * Checks if this key matches the given key ID.
     *
     * @param id the key ID to match against
     * @return true, if this key matches the ID
     */
    public final boolean matchesId(final String id) {
        Assertions.argumentNotNull(id, "id may not be null");

        // Check the ID
        return getId().equals(id);
    }
}
