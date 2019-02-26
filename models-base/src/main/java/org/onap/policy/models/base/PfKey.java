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

/**
 * The key uniquely identifies every entity in the system. This class is an abstract class to give a
 * common parent for all key types in the system.
 */
public abstract class PfKey extends PfConcept {
    private static final long serialVersionUID = 6281159885962014041L;

    /** Regular expression to specify the structure of key names. */
    public static final String NAME_REGEXP = "[A-Za-z0-9\\-_\\.]+";

    /** Regular expression to specify the structure of key versions. */
    public static final String VERSION_REGEXP = "[A-Za-z0-9.]+";

    /** Regular expression to specify the structure of key IDs. */
    public static final String KEY_ID_REGEXP = "[A-Za-z0-9\\-_\\.]+:[0-9].[0-9].[0-9]";

    /** Specifies the value for names in NULL keys. */
    public static final String NULL_KEY_NAME = "NULL";

    /** Specifies the value for versions in NULL keys. */
    public static final String NULL_KEY_VERSION = "0.0.0";

    /**
     * This enumeration is returned on key compatibility checks.
     */
    public enum Compatibility {
        /** The keys have different names. */
        DIFFERENT,
        /**
         * The name of the key matches but the Major version number of the keys is different (x in
         * x.y.z do not match).
         */
        MAJOR,
        /**
         * The name of the key matches but the Minor version number of the keys is different (y in
         * x.y.z do not match).
         */
        MINOR,
        /**
         * The name of the key matches but the Patch version number of the keys is different (z in
         * x.y.z do not match).
         */
        PATCH,
        /** The keys match completely. */
        IDENTICAL
    }

    /**
     * Default constructor.
     */
    public PfKey() {
        super();
    }

    /**
     * Copy constructor.
     *
     * @param copyConcept the concept to copy from
     */
    public PfKey(final PfKey copyConcept) {
        super(copyConcept);
    }

    @Override
    public abstract String getId();

    /**
     * Return the result of a compatibility check of two keys.
     *
     * @param otherKey the key to check compatibility against
     * @return the compatibility result of the check
     */
    public abstract Compatibility getCompatibility(PfKey otherKey);

    /**
     * Check if two keys are compatible, that is the keys are IDENTICAL or have only MINOR, PATCH
     * differences.
     *
     * @param otherKey the key to check compatibility against
     * @return true, if the keys are compatible
     */
    public abstract boolean isCompatible(PfKey otherKey);
}
