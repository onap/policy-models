/*-
 * ============LICENSE_START=======================================================
 * ONAP Policy Model
 * ================================================================================
 * Copyright (C) 2019 Nordix Foundation.
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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.persistence.Column;
import javax.persistence.Embeddable;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;
import org.onap.policy.common.utils.validation.Assertions;
import org.onap.policy.models.base.PfValidationResult.ValidationResult;

@Embeddable
@Getter
@ToString
@EqualsAndHashCode(callSuper = false)
public class PfTimestampConceptKey extends PfKey {
    private static final long serialVersionUID = -8410208962541783805L;

    private static final String LOCAL_KEY_ID_REGEXP = "^[A-Za-z0-9\\-_\\.]+:(\\d+.){2}\\d+:(\\d+)$";
    private static final String NAME_TOKEN = "name";
    private static final String VERSION_TOKEN = "version";
    private static final String TIMESTAMP_TOKEN = "timeStamp";

    @Column(name = NAME_TOKEN, length = 120)
    private String name;

    @Column(name = VERSION_TOKEN, length = 20)
    private String version;

    @Column(name = TIMESTAMP_TOKEN)
    private Date timeStamp;

    /**
     * The default constructor creates a null concept key.
     */
    public PfTimestampConceptKey() {
        this(NULL_KEY_NAME, NULL_KEY_VERSION, new Date(0));
    }

    /**
     * Copy constructor.
     *
     * @param copyConcept the concept to copy from
     */
    public PfTimestampConceptKey(@NonNull final PfTimestampConceptKey copyConcept) {
        super(copyConcept);
        this.timeStamp = new Date(copyConcept.getTimeStamp().getTime());
    }

    /**
     * Constructor to create a key with the specified name and version.
     *
     * @param name the key name
     * @param version the key version
     * @param timeStamp the timestamp of key
     */
    public PfTimestampConceptKey(@NonNull final String name, @NonNull final String version,
            @NonNull final Date timeStamp) {
        super();
        this.name = Assertions.validateStringParameter(NAME_TOKEN, name, NAME_REGEXP);
        this.version = Assertions.validateStringParameter(VERSION_TOKEN, version, VERSION_REGEXP);
        this.timeStamp = new Date(timeStamp.getTime());
    }

    /**
     * Constructor to create a key using the key and version from the specified key ID.
     *
     * @param id the key ID in a format that respects the LOCAL_KEY_ID_REGEXP
     */
    public PfTimestampConceptKey(@NonNull final String id) {
        // Check the incoming ID is valid
        Assertions.validateStringParameter("id", id, LOCAL_KEY_ID_REGEXP);

        // Split on colon, if the id passes the regular expression test above
        // it'll have just one colon separating the name and version
        // No need for range checks or size checks on the array
        final String[] nameVersionArray = id.split(":");

        // Return the new key
        name = Assertions.validateStringParameter(NAME_TOKEN, nameVersionArray[0], NAME_REGEXP);
        version = Assertions.validateStringParameter(VERSION_TOKEN, nameVersionArray[1], VERSION_REGEXP);
        try {
            timeStamp = new Date(Long.parseLong(nameVersionArray[2]));
        } catch (NumberFormatException e) {
            timeStamp = new Date(0);
        }
    }

    @Override
    public String getId() {
        return name + ':' + version + ':' + timeStamp.getTime();
    }

    @Override
    public Compatibility getCompatibility(@NonNull PfKey otherKey) {
        if (!(otherKey instanceof PfTimestampConceptKey)) {
            return Compatibility.DIFFERENT;
        }
        final PfTimestampConceptKey otherConceptKey = (PfTimestampConceptKey) otherKey;

        if (this.equals(otherConceptKey)) {
            return Compatibility.IDENTICAL;
        }
        if (!this.getName().equals(otherConceptKey.getName())
                || !this.timeStamp.equals(otherConceptKey.getTimeStamp())) {
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
    public boolean isCompatible(@NonNull PfKey otherKey) {
        if (!(otherKey instanceof PfTimestampConceptKey)) {
            return false;
        }
        final PfTimestampConceptKey otherConceptKey = (PfTimestampConceptKey) otherKey;

        final Compatibility compatibility = this.getCompatibility(otherConceptKey);

        return !(compatibility == Compatibility.DIFFERENT || compatibility == Compatibility.MAJOR);
    }

    @Override
    public boolean isNewerThan(@NonNull PfKey otherKey) {
        Assertions.instanceOf(otherKey, PfTimestampConceptKey.class);

        final PfTimestampConceptKey otherConceptKey = (PfTimestampConceptKey) otherKey;

        if (this.equals(otherConceptKey)) {
            return false;
        }

        if (!this.getName().equals(otherConceptKey.getName())) {
            return this.getName().compareTo(otherConceptKey.getName()) > 0;
        }

        final String[] thisVersionArray = getVersion().split("\\.");
        final String[] otherVersionArray = otherConceptKey.getVersion().split("\\.");

        if (!thisVersionArray[0].equals(otherVersionArray[0])) {
            return Integer.valueOf(thisVersionArray[0]) > Integer.valueOf(otherVersionArray[0]);
        }

        if (thisVersionArray.length >= 2 && otherVersionArray.length >= 2
                && !thisVersionArray[1].equals(otherVersionArray[1])) {
            return Integer.valueOf(thisVersionArray[1]) > Integer.valueOf(otherVersionArray[1]);
        }

        if (thisVersionArray.length >= 3 && otherVersionArray.length >= 3
                && !thisVersionArray[2].equals(otherVersionArray[2])) {
            return Integer.valueOf(thisVersionArray[2]) > Integer.valueOf(otherVersionArray[2]);
        }

        if (!timeStamp.equals(otherConceptKey.timeStamp)) {
            return timeStamp.after(otherConceptKey.timeStamp);
        }
        return false;
    }

    /**
     * Get a null concept key.
     *
     * @return a null concept key
     */
    public static final PfTimestampConceptKey getNullKey() {
        return new PfTimestampConceptKey(PfKey.NULL_KEY_NAME, PfKey.NULL_KEY_VERSION, new Date(0));
    }

    public void setName(@NonNull final String name) {
        this.name = Assertions.validateStringParameter(NAME_TOKEN, name, NAME_REGEXP);
    }

    public void setVersion(@NonNull final String version) {
        this.version = Assertions.validateStringParameter(VERSION_TOKEN, version, VERSION_REGEXP);
    }

    public void setTimeStamp(@NonNull final Date timeStamp) {
        this.timeStamp = new Date(timeStamp.getTime());
    }

    @Override
    public boolean isNullKey() {
        return this.equals(PfTimestampConceptKey.getNullKey());
    }

    @Override
    public int getMajorVersion() {
        final String[] versionArray = getVersion().split("\\.");
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
    public PfKey getKey() {
        return this;
    }

    @Override
    public List<PfKey> getKeys() {
        final List<PfKey> keyList = new ArrayList<>();
        keyList.add(getKey());
        return keyList;
    }

    @Override
    public PfValidationResult validate(@NonNull PfValidationResult result) {
        final String nameValidationErrorMessage =
                Assertions.getStringParameterValidationMessage(NAME_TOKEN, name, NAME_REGEXP);
        if (nameValidationErrorMessage != null) {
            result.addValidationMessage(new PfValidationMessage(this, this.getClass(), ValidationResult.INVALID,
                    "name invalid-" + nameValidationErrorMessage));
        }

        final String versionValidationErrorMessage =
                Assertions.getStringParameterValidationMessage(VERSION_TOKEN, version, VERSION_REGEXP);
        if (versionValidationErrorMessage != null) {
            result.addValidationMessage(new PfValidationMessage(this, this.getClass(), ValidationResult.INVALID,
                    "version invalid-" + versionValidationErrorMessage));
        }

        try {
            Assertions.argumentNotNull(timeStamp, "timeStamp must not be null");
        } catch (IllegalArgumentException e) {
            result.addValidationMessage(new PfValidationMessage(this, this.getClass(), ValidationResult.INVALID,
                    "timeStamp invalid-" + e.getMessage()));
        }

        return result;
    }

    @Override
    public void clean() {
        name = Assertions.validateStringParameter(NAME_TOKEN, name, NAME_REGEXP);
        version = Assertions.validateStringParameter(VERSION_TOKEN, version, VERSION_REGEXP);
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

        final PfTimestampConceptKey other = (PfTimestampConceptKey) otherObj;

        if (!name.equals(other.name)) {
            return name.compareTo(other.name);
        }
        if (!version.equals(other.version)) {
            return version.compareTo(other.version);
        }

        return timeStamp.compareTo(other.timeStamp);
    }

}
