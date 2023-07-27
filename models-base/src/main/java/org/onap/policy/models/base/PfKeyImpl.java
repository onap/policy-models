/*
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2019-2020, 2023 Nordix Foundation.
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
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;
import org.onap.policy.common.utils.validation.Assertions;

/**
 * A key, upon which other key subclasses can be built, providing implementations of the methods.
 */
@Getter
@ToString
public abstract class PfKeyImpl extends PfKey {
    @Serial
    private static final long serialVersionUID = 8932717618579392561L;

    public static final String NAME_TOKEN = "name";
    public static final String VERSION_TOKEN = "version";

    /**
     * The default constructor creates a null concept key.
     */
    protected PfKeyImpl() {
        this(NULL_KEY_NAME, NULL_KEY_VERSION);
    }

    /**
     * Copy constructor.
     *
     * @param copyConcept the concept to copy from
     */
    protected PfKeyImpl(final PfKeyImpl copyConcept) {
        super(copyConcept);
        setName(copyConcept.getName());
        setVersion(copyConcept.getVersion());
    }

    /**
     * Constructor to create a key with the specified name and version.
     *
     * @param name the key name
     * @param version the key version
     */
    protected PfKeyImpl(@NonNull final String name, @NonNull final String version) {
        super();
        setName(name);
        setVersion(version);
    }

    /**
     * Constructor to create a key using the key and version from the specified key ID.
     *
     * @param id the key ID in a format that respects the KEY_ID_REGEXP
     */
    protected PfKeyImpl(@NonNull final String id) {
        // Check the incoming ID is valid
        Assertions.validateStringParameter("id", id, getKeyIdRegEx());

        // Split on colon, if the id passes the regular expression test above
        // it'll have just one colon separating the name and version
        // No need for range checks or size checks on the array
        final String[] nameVersionArray = id.split(":");

        // Return the new key
        setName(nameVersionArray[0]);
        setVersion(nameVersionArray[1]);
    }

    public abstract void setName(@NonNull final String name);

    public abstract void setVersion(@NonNull final String version);

    @Override
    public PfKeyImpl getKey() {
        return this;
    }

    @Override
    public List<PfKey> getKeys() {
        final List<PfKey> keyList = new ArrayList<>();
        keyList.add(getKey());
        return keyList;
    }

    @Override
    public String getId() {
        return getName() + ':' + getVersion();
    }

    @Override
    public boolean isNullKey() {
        return (PfKey.NULL_KEY_NAME.equals(getName()) && PfKey.NULL_KEY_VERSION.equals(getVersion()));
    }

    /**
     * Determines if the name is "null".
     *
     * @return {@code true} if the name is null, {@code false} otherwise
     */
    public boolean isNullName() {
        return PfKey.NULL_KEY_NAME.equals(getName());
    }

    /**
     * Determines if the version is "null".
     *
     * @return {@code true} if the version is null, {@code false} otherwise
     */
    public boolean isNullVersion() {
        return PfKey.NULL_KEY_VERSION.equals(getVersion());
    }

    @Override
    public PfKey.Compatibility getCompatibility(@NonNull final PfKey otherKey) {
        if (!(otherKey instanceof PfKeyImpl otherConceptKey)) {
            return Compatibility.DIFFERENT;
        }

        if (this.equals(otherConceptKey)) {
            return Compatibility.IDENTICAL;
        }
        if (!this.getName().equals(otherConceptKey.getName())) {
            return Compatibility.DIFFERENT;
        }

        final String[] thisVersionArray = getVersion().split("\\.");
        final String[] otherVersionArray = otherConceptKey.getVersion().split("\\.");

        // There must always be at least one element in each version
        if (!thisVersionArray[0].equals(otherVersionArray[0])) {
            return Compatibility.MAJOR;
        }

        if (thisVersionArray.length >= 2 && otherVersionArray.length >= 2
            && !thisVersionArray[1].equals(otherVersionArray[1])) {
            return Compatibility.MINOR;
        }

        return Compatibility.PATCH;
    }

    @Override
    public boolean isCompatible(@NonNull final PfKey otherKey) {
        if (!(otherKey instanceof PfKeyImpl otherConceptKey)) {
            return false;
        }

        final var compatibility = this.getCompatibility(otherConceptKey);

        return !(compatibility == Compatibility.DIFFERENT || compatibility == Compatibility.MAJOR);
    }

    @Override
    public boolean isNewerThan(@NonNull final PfKey otherKey) {
        Assertions.instanceOf(otherKey, PfKeyImpl.class);

        final PfKeyImpl otherConceptKey = (PfKeyImpl) otherKey;

        if (this.equals(otherConceptKey)) {
            return false;
        }

        if (!this.getName().equals(otherConceptKey.getName())) {
            return this.getName().compareTo(otherConceptKey.getName()) > 0;
        }

        final String[] thisVersionArray = getVersion().split("\\.");
        final String[] otherVersionArray = otherConceptKey.getVersion().split("\\.");

        // There must always be at least one element in each version
        if (!thisVersionArray[0].equals(otherVersionArray[0])) {
            return Integer.parseInt(thisVersionArray[0]) > Integer.parseInt(otherVersionArray[0]);
        }

        if (thisVersionArray.length >= 2 && otherVersionArray.length >= 2
            && !thisVersionArray[1].equals(otherVersionArray[1])) {
            return Integer.parseInt(thisVersionArray[1]) > Integer.parseInt(otherVersionArray[1]);
        }

        if (thisVersionArray.length >= 3 && otherVersionArray.length >= 3
            && !thisVersionArray[2].equals(otherVersionArray[2])) {
            return Integer.parseInt(thisVersionArray[2]) > Integer.parseInt(otherVersionArray[2]);
        }

        return false;
    }

    @Override
    public int getMajorVersion() {
        final String[] versionArray = getVersion().split("\\.");

        // There must always be at least one element in each version
        return Integer.parseInt(versionArray[0]);
    }

    @Override
    public int getMinorVersion() {
        final String[] versionArray = getVersion().split("\\.");

        if (versionArray.length >= 2) {
            return Integer.parseInt(versionArray[1]);
        } else {
            return 0;
        }
    }

    @Override
    public int getPatchVersion() {
        final String[] versionArray = getVersion().split("\\.");

        if (versionArray.length >= 3) {
            return Integer.parseInt(versionArray[2]);
        } else {
            return 0;
        }
    }

    @Override
    public void clean() {
        setName(getName());
        setVersion(getVersion());
    }

    @Override
    public int compareTo(@NonNull final PfConcept otherObj) {
        Assertions.argumentNotNull(otherObj, "comparison object may not be null");

        if (this == otherObj) {
            return 0;
        }
        if (getClass() != otherObj.getClass()) {
            return getClass().getName().compareTo(otherObj.getClass().getName());
        }

        final PfKeyImpl other = (PfKeyImpl) otherObj;

        if (!getName().equals(other.getName())) {
            return getName().compareTo(other.getName());
        }
        return getVersion().compareTo(other.getVersion());
    }

    /**
     * Gets the regular expression used to validate a name.
     *
     * @return the regular expression used to validate a name
     */
    protected String getNameRegEx() {
        return NAME_REGEXP;
    }

    /**
     * Gets the regular expression used to validate a version.
     *
     * @return the regular expression used to validate a version
     */
    protected String getVersionRegEx() {
        return VERSION_REGEXP;
    }

    /**
     * Gets the regular expression used to validate a key id.
     *
     * @return the regular expression used to validate a key id
     */
    protected String getKeyIdRegEx() {
        return KEY_ID_REGEXP;
    }
}
