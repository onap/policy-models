/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2019, 2023 Nordix Foundation.
 *  Modifications Copyright (C) 2019-2021 AT&T Intellectual Property. All rights reserved.
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

import java.io.Serial;
import java.io.Serializable;
import java.util.List;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;

/**
 * This class is the base class for all Policy Framework concept classes. It enforces implementation of abstract methods
 * and interfaces on all concepts that are subclasses of this class.
 */

@NoArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class PfConcept extends Validated implements Serializable, Comparable<PfConcept> {
    @Serial
    private static final long serialVersionUID = -7434939557282697490L;

    /**
     * Copy constructor.
     *
     * @param copyConcept the concept to copy from
     */
    protected PfConcept(@NonNull final PfConcept copyConcept) {
        // nothing else to do (other than @NonNull check)
    }

    /**
     * Gets the key of this concept.
     *
     * @return the concept key
     */
    public abstract PfKey getKey();

    /**
     * Gets a list of all keys for this concept and all concepts that are defined or referenced by this concept and its
     * sub-concepts.
     *
     * @return the keys used by this concept and its contained concepts
     */
    public abstract List<PfKey> getKeys();

    /**
     * Clean this concept, tidy up any superfluous information such as leading and trailing white space.
     */
    public abstract void clean();

    @Override
    public abstract boolean equals(Object otherObject);

    @Override
    public abstract String toString();

    @Override
    public abstract int hashCode();

    /**
     * Gets the ID string of this concept.
     *
     * @return the ID string of this concept
     */
    public String getId() {
        return getKey().getId();
    }

    /**
     * Gets the name of this concept.
     *
     * @return the name of this concept
     */
    public String getName() {
        return getKey().getName();
    }

    /**
     * Gets the version of this concept.
     *
     * @return the version of this concept
     */
    public String getVersion() {
        return getKey().getVersion();
    }

    /**
     * Checks if this key matches the given key ID.
     *
     * @param id the key ID to match against
     * @return true, if this key matches the ID
     */
    public final boolean matchesId(@NonNull final String id) {
        // Check the ID
        return getId().equals(id);
    }
}
