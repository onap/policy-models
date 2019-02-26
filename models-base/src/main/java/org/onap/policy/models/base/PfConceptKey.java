/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2016-2018 Ericsson. All rights reserved.
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
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.onap.policy.common.utils.validation.Assertions;
import org.onap.policy.models.base.PfValidationResult.ValidationResult;

/**
 * An artifact key uniquely identifies every first order entity in the system. Every first order
 * concept in the system must have an {@link PfConceptKey} to identify it. Concepts that are wholly
 * contained in another concept are identified using a {@link AxReferenceKey} key.
 *
 * <p>Key validation checks that the name and version fields match the NAME_REGEXP and VERSION_REGEXP
 * regular expressions respectively.
 */
@Embeddable
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "pfConceptKey", namespace = "http://www.onap.org/policy/models")

@XmlType(name = "PfConceptKey", namespace = "http://www.onap.org/policy/models", propOrder = {"name", "version"})

public class PfConceptKey extends PfKey {
    private static final long serialVersionUID = 8932717618579392561L;

    private static final String NAME_TOKEN = "name";
    private static final String VERSION_TOKEN = "version";

    @Column(name = NAME_TOKEN)
    @XmlElement(required = true)
    private String name;

    @Column(name = VERSION_TOKEN)
    @XmlElement(required = true)
    private String version;

    /**
     * The default constructor creates a null artifact key.
     */
    public PfConceptKey() {
        this(NULL_KEY_NAME, NULL_KEY_VERSION);
    }

    /**
     * Copy constructor.
     *
     * @param copyConcept the concept to copy from
     */
    public PfConceptKey(final PfConceptKey copyConcept) {
        super(copyConcept);
    }

    /**
     * Constructor to create a key with the specified name and version.
     *
     * @param name the key name
     * @param version the key version
     */
    public PfConceptKey(final String name, final String version) {
        super();
        this.name = Assertions.validateStringParameter(NAME_TOKEN, name, NAME_REGEXP);
        this.version = Assertions.validateStringParameter(VERSION_TOKEN, version, VERSION_REGEXP);
    }

    /**
     * Constructor to create a key using the key and version from the specified key ID.
     *
     * @param id the key ID in a format that respects the KEY_ID_REGEXP
     */
    public PfConceptKey(final String id) {
        Assertions.argumentNotNull(id, "id may not be null");

        // Check the incoming ID is valid
        Assertions.validateStringParameter("id", id, KEY_ID_REGEXP);

        // Split on colon, if the id passes the regular expression test above
        // it'll have just one colon separating the name and version
        // No need for range checks or size checks on the array
        final String[] nameVersionArray = id.split(":");

        // Return the new key
        name = Assertions.validateStringParameter(NAME_TOKEN, nameVersionArray[0], NAME_REGEXP);
        version = Assertions.validateStringParameter(VERSION_TOKEN, nameVersionArray[1], VERSION_REGEXP);
    }

    /**
     * Get a null artifact key.
     *
     * @return a null artifact key
     */
    public static final PfConceptKey getNullKey() {
        return new PfConceptKey(PfKey.NULL_KEY_NAME, PfKey.NULL_KEY_VERSION);
    }

    @Override
    public PfConceptKey getKey() {
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
        return name + ':' + version;
    }

    /**
     * Gets the key name.
     *
     * @return the key name
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the key name.
     *
     * @param name the key name
     */
    public void setName(final String name) {
        this.name = Assertions.validateStringParameter(NAME_TOKEN, name, NAME_REGEXP);
    }

    /**
     * Gets the key version.
     *
     * @return the key version
     */
    public String getVersion() {
        return version;
    }

    /**
     * Sets the key version.
     *
     * @param version the key version
     */
    public void setVersion(final String version) {
        this.version = Assertions.validateStringParameter(VERSION_TOKEN, version, VERSION_REGEXP);
    }

    @Override
    public PfKey.Compatibility getCompatibility(final PfKey otherKey) {
        if (!(otherKey instanceof PfConceptKey)) {
            return Compatibility.DIFFERENT;
        }
        final PfConceptKey otherArtifactKey = (PfConceptKey) otherKey;

        if (this.equals(otherArtifactKey)) {
            return Compatibility.IDENTICAL;
        }
        if (!this.getName().equals(otherArtifactKey.getName())) {
            return Compatibility.DIFFERENT;
        }

        final String[] thisVersionArray = getVersion().split("\\.");
        final String[] otherVersionArray = otherArtifactKey.getVersion().split("\\.");

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
    public boolean isCompatible(final PfKey otherKey) {
        if (!(otherKey instanceof PfConceptKey)) {
            return false;
        }
        final PfConceptKey otherArtifactKey = (PfConceptKey) otherKey;

        final Compatibility compatibility = this.getCompatibility(otherArtifactKey);

        return !(compatibility == Compatibility.DIFFERENT || compatibility == Compatibility.MAJOR);
    }

    @Override
    public PfValidationResult validate(final PfValidationResult result) {
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

        return result;
    }

    @Override
    public void clean() {
        name = Assertions.validateStringParameter(NAME_TOKEN, name, NAME_REGEXP);
        version = Assertions.validateStringParameter(VERSION_TOKEN, version, VERSION_REGEXP);
    }

    @Override
    public String toString() {
        final StringBuilder builder = new StringBuilder();
        builder.append(this.getClass().getSimpleName());
        builder.append(":(");
        builder.append("name=");
        builder.append(name);
        builder.append(",version=");
        builder.append(version);
        builder.append(")");
        return builder.toString();
    }

    @Override
    public PfConcept copyTo(final PfConcept target) {
        Assertions.argumentNotNull(target, "target may not be null");

        final PfConcept copyObject = target;
        Assertions.instanceOf(copyObject, PfConceptKey.class);

        final PfConceptKey copy = ((PfConceptKey) copyObject);
        copy.setName(name);
        copy.setVersion(version);

        return copyObject;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + name.hashCode();
        result = prime * result + version.hashCode();
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj == null) {
            return false;
        }
        if (this == obj) {
            return true;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }

        final PfConceptKey other = (PfConceptKey) obj;

        if (!name.equals(other.name)) {
            return false;
        }
        return version.equals(other.version);
    }

    @Override
    public int compareTo(final PfConcept otherObj) {
        Assertions.argumentNotNull(otherObj, "comparison object may not be null");

        if (this == otherObj) {
            return 0;
        }
        if (getClass() != otherObj.getClass()) {
            return this.hashCode() - otherObj.hashCode();
        }

        final PfConceptKey other = (PfConceptKey) otherObj;

        if (!name.equals(other.name)) {
            return name.compareTo(other.name);
        }
        return version.compareTo(other.version);
    }
}
