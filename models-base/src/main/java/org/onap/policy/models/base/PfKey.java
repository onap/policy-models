/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2019, 2023 Nordix Foundation.
 *  Modifications Copyright (C) 2021 AT&T Intellectual Property. All rights reserved.
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
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;

/**
 * The key uniquely identifies every entity in the system. This class is an abstract class to give a common parent for
 * all key types in the system.
 */
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class PfKey extends PfConcept {
    @Serial
    private static final long serialVersionUID = 6281159885962014041L;

    /** Regular expression to specify the structure of key names. */
    public static final String NAME_REGEXP = "^[A-Za-z0-9\\-_\\.]+$";

    /** Regular expression to specify the structure of key versions. */
    public static final String VERSION_REGEXP
        = "^(0|[1-9]\\d*)\\.(0|[1-9]\\d*)\\.(0|[1-9]\\d*)(?:-((?:0|[1-9]\\d*|\\d*[a-zA-Z-][0-9a-zA-Z-]*)"
        + "(?:\\.(?:0|[1-9]\\d*|\\d*[a-zA-Z-][0-9a-zA-Z-]*))*))?(?:\\+([0-9a-zA-Z-]+(?:\\.[0-9a-zA-Z-]+)*))?$";

    /** Regular expression to specify the structure of key IDs. */
    public static final String KEY_ID_REGEXP = "^[A-Za-z0-9\\-_\\.]+:(\\d+.){2}\\d+$";

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
         * The name of the key matches but the Major version number of the keys is different (x in x.y.z do not match).
         */
        MAJOR,
        /**
         * The name of the key matches but the Minor version number of the keys is different (y in x.y.z do not match).
         */
        MINOR,
        /**
         * The name of the key matches but the Patch version number of the keys is different (z in x.y.z do not match).
         */
        PATCH,
        /** The keys match completely. */
        IDENTICAL
    }

    /**
     * Copy constructor.
     *
     * @param copyConcept the concept to copy from
     */
    protected PfKey(final PfKey copyConcept) {
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
    public abstract Compatibility getCompatibility(@NonNull PfKey otherKey);

    /**
     * Check if two keys are compatible, that is the keys are IDENTICAL or have only MINOR, PATCH differences.
     *
     * @param otherKey the key to check compatibility against
     * @return true, if the keys are compatible
     */
    public abstract boolean isCompatible(@NonNull PfKey otherKey);

    /**
     * Check if this key is a newer version than the other key.
     *
     * @param otherKey the key to check against
     * @return true, if this key is newer than the other key
     */
    public abstract boolean isNewerThan(@NonNull PfKey otherKey);

    /**
     * Check if a key equals its null key.
     *
     * @return true, if the key is a null key
     */
    public abstract boolean isNullKey();

    /**
     * Get the major version of a key.
     *
     * @return the major version of a key
     */
    public abstract int getMajorVersion();

    /**
     * Get the minor version of a key.
     *
     * @return the minor version of a key
     */
    public abstract int getMinorVersion();

    /**
     * Get the patch version of a key.
     *
     * @return the patch version of a key
     */
    public abstract int getPatchVersion();
}
