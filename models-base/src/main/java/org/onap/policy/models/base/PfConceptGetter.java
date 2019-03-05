/*
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

import java.util.Set;

/**
 * This interface is used to allow get methods to be placed on concepts that have embedded maps.
 *
 * <p>It forces those concepts with maps to implement the get methods specified on this interface as convenience methods
 * to avoid concept users having to use a second level of referencing to access concepts in the maps.
 *
 * @param <C> the type of concept on which the interface is applied.
 */
public interface PfConceptGetter<C> {

    /**
     * Get the latest version for a concept with the given key.
     *
     * @param conceptKey The key of the concept
     * @return The concept
     */
    C get(PfConceptKey conceptKey);

    /**
     * Get the latest version for a concept with the given key name.
     *
     * @param conceptKeyName The name of the concept
     * @return The concept
     */
    C get(String conceptKeyName);

    /**
     * Get the latest version for a concept with the given key name and version.
     *
     * @param conceptKeyName The name of the concept
     * @param conceptKeyVersion The version of the concept
     * @return The concept
     */
    C get(String conceptKeyName, String conceptKeyVersion);

    /**
     * Get the all versions for a concept with the given key name.
     *
     * @param conceptKeyName The name of the concept
     * @return The concepts
     */
    Set<C> getAll(String conceptKeyName);

    /**
     * Get the all versions for a concept with the given key name and starting version.
     *
     * @param conceptKeyName The name of the concept
     * @param conceptKeyVersion The first version version of the concept to get
     * @return The concepts
     */
    Set<C> getAll(String conceptKeyName, String conceptKeyVersion);
}
