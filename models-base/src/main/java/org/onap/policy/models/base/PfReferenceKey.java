/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2019, 2021, 2023 Nordix Foundation.
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

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serial;
import java.util.ArrayList;
import java.util.List;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import org.onap.policy.common.parameters.annotations.NotNull;
import org.onap.policy.common.parameters.annotations.Pattern;
import org.onap.policy.common.utils.validation.Assertions;

/**
 * A reference key identifies entities in the system that are contained in other entities. Every contained concept in
 * the system must have an {@link PfReferenceKey} to identify it. Non-contained first order concepts are identified
 * using an {@link PfConceptKey} key.
 *
 * <p>An {@link PfReferenceKey} contains an {@link PfConceptKey} key reference to the first order entity that contains
 * it. The local name of the reference key must uniquely identify the referenced concept among those concepts contained
 * in the reference key's parent. In other words, if a parent concept has more than one child, the local name in the key
 * of all its children must be unique.
 *
 * <p>If a reference key's parent is itself a reference key, then the parent's local name must be set in the reference
 * key. If the parent is a first order concept, then the parent's local name in the key will be set to NULL.
 *
 * <p>Key validation checks that the parent name and parent version fields match the NAME_REGEXP and
 * VERSION_REGEXP regular expressions respectively and that the local name fields match the
 * LOCAL_NAME_REGEXP regular expression.
 */
@Embeddable
@Data
@EqualsAndHashCode(callSuper = false)
public class PfReferenceKey extends PfKey {
    private static final String PARENT_KEY_NAME = "parentKeyName";
    private static final String PARENT_KEY_VERSION = "parentKeyVersion";
    private static final String PARENT_LOCAL_NAME = "parentLocalName";
    private static final String LOCAL_NAME = "localName";

    @Serial
    private static final long serialVersionUID = 8932717618579392561L;

    /**
     * Regular expression to specify the structure of local names in reference keys.
     */
    public static final String LOCAL_NAME_REGEXP = "[A-Za-z0-9\\-_\\.]+|^$";

    /**
     * Regular expression to specify the structure of IDs in reference keys.
     */
    public static final String REFERENCE_KEY_ID_REGEXP =
        "[A-Za-z0-9\\-_]+:[0-9].[0-9].[0-9]:[A-Za-z0-9\\-_]+:[A-Za-z0-9\\-_]+";

    private static final int PARENT_NAME_FIELD = 0;
    private static final int PARENT_VERSION_FIELD = 1;
    private static final int PARENT_LOCAL_NAME_FIELD = 2;
    private static final int LOCAL_NAME_FIELD = 3;

    @Column(name = PARENT_KEY_NAME, length = 120)
    @NotNull
    @Pattern(regexp = NAME_REGEXP)
    private String parentKeyName;

    @Column(name = PARENT_KEY_VERSION, length = 15)
    @NotNull
    @Pattern(regexp = VERSION_REGEXP)
    private String parentKeyVersion;

    @Column(name = PARENT_LOCAL_NAME, length = 120)
    @NotNull
    @Pattern(regexp = LOCAL_NAME_REGEXP)
    private String parentLocalName;

    @Column(name = LOCAL_NAME, length = 120)
    @NotNull
    @Pattern(regexp = LOCAL_NAME_REGEXP)
    private String localName;

    /**
     * The default constructor creates a null reference key.
     */
    public PfReferenceKey() {
        this(NULL_KEY_NAME, NULL_KEY_VERSION, NULL_KEY_NAME, NULL_KEY_NAME);
    }

    /**
     * The Copy Constructor creates a key by copying another key.
     *
     * @param referenceKey the reference key to copy from
     */
    public PfReferenceKey(final PfReferenceKey referenceKey) {
        this(referenceKey.getParentKeyName(), referenceKey.getParentKeyVersion(), referenceKey.getParentLocalName(),
            referenceKey.getLocalName());
    }

    /**
     * Constructor to create a null reference key for the specified parent concept key.
     *
     * @param pfConceptKey the parent concept key of this reference key
     */
    public PfReferenceKey(final PfConceptKey pfConceptKey) {
        this(pfConceptKey.getName(), pfConceptKey.getVersion(), NULL_KEY_NAME, NULL_KEY_NAME);
    }

    /**
     * Constructor to create a reference key for the given parent concept key with the given local name.
     *
     * @param pfConceptKey the parent concept key of this reference key
     * @param localName    the local name of this reference key
     */
    public PfReferenceKey(final PfConceptKey pfConceptKey, final String localName) {
        this(pfConceptKey, NULL_KEY_NAME, localName);
    }

    /**
     * Constructor to create a reference key for the given parent reference key with the given local name.
     *
     * @param parentReferenceKey the parent reference key of this reference key
     * @param localName          the local name of this reference key
     */
    public PfReferenceKey(final PfReferenceKey parentReferenceKey, final String localName) {
        this(parentReferenceKey.getParentConceptKey(), parentReferenceKey.getLocalName(), localName);
    }

    /**
     * Constructor to create a reference key for the given parent reference key (specified by the parent reference key's
     * concept key and local name) with the given local name.
     *
     * @param pfConceptKey    the concept key of the parent reference key of this reference key
     * @param parentLocalName the local name of the parent reference key of this reference key
     * @param localName       the local name of this reference key
     */
    public PfReferenceKey(final PfConceptKey pfConceptKey, final String parentLocalName, final String localName) {
        this(pfConceptKey.getName(), pfConceptKey.getVersion(), parentLocalName, localName);
    }

    /**
     * Constructor to create a reference key for the given parent concept key (specified by the parent concept key's
     * name and version) with the given local name.
     *
     * @param parentKeyName    the name of the parent concept key of this reference key
     * @param parentKeyVersion the version of the parent concept key of this reference key
     * @param localName        the local name of this reference key
     */
    public PfReferenceKey(final String parentKeyName, final String parentKeyVersion, final String localName) {
        this(parentKeyName, parentKeyVersion, NULL_KEY_NAME, localName);
    }

    /**
     * Constructor to create a reference key for the given parent key (specified by the parent key's name, version nad
     * local name) with the given local name.
     *
     * @param parentKeyName    the parent key name of this reference key
     * @param parentKeyVersion the parent key version of this reference key
     * @param parentLocalName  the parent local name of this reference key
     * @param localName        the local name of this reference key
     */
    public PfReferenceKey(final String parentKeyName, final String parentKeyVersion, final String parentLocalName,
                          final String localName) {
        super();
        this.parentKeyName = Assertions.validateStringParameter(PARENT_KEY_NAME, parentKeyName, NAME_REGEXP);
        this.parentKeyVersion = Assertions.validateStringParameter(PARENT_KEY_VERSION, parentKeyVersion,
            VERSION_REGEXP);
        this.parentLocalName = Assertions.validateStringParameter(PARENT_LOCAL_NAME, parentLocalName,
            LOCAL_NAME_REGEXP);
        this.localName = Assertions.validateStringParameter(LOCAL_NAME, localName, LOCAL_NAME_REGEXP);
    }

    /**
     * Constructor to create a key from the specified key ID.
     *
     * @param id the key ID in a format that respects the KEY_ID_REGEXP
     */
    public PfReferenceKey(final String id) {
        final var conditionedId = Assertions.validateStringParameter("id", id, REFERENCE_KEY_ID_REGEXP);

        // Split on colon, if the id passes the regular expression test above
        // it'll have just three colons separating the parent name,
        // parent version, parent local name, and and local name
        // No need for range checks or size checks on the array
        final String[] nameVersionNameArray = conditionedId.split(":");

        // Initiate the new key
        parentKeyName = Assertions.validateStringParameter(PARENT_KEY_NAME, nameVersionNameArray[PARENT_NAME_FIELD],
            NAME_REGEXP);
        parentKeyVersion = Assertions.validateStringParameter(PARENT_KEY_VERSION,
            nameVersionNameArray[PARENT_VERSION_FIELD], VERSION_REGEXP);
        parentLocalName = Assertions.validateStringParameter(PARENT_LOCAL_NAME,
            nameVersionNameArray[PARENT_LOCAL_NAME_FIELD], LOCAL_NAME_REGEXP);
        localName = Assertions.validateStringParameter(LOCAL_NAME, nameVersionNameArray[LOCAL_NAME_FIELD],
            LOCAL_NAME_REGEXP);
    }

    /**
     * Get a null reference key.
     *
     * @return a null reference key
     */
    public static PfReferenceKey getNullKey() {
        return new PfReferenceKey(PfKey.NULL_KEY_NAME, PfKey.NULL_KEY_VERSION, PfKey.NULL_KEY_NAME,
            PfKey.NULL_KEY_NAME);
    }

    @Override
    public PfReferenceKey getKey() {
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
        return parentKeyName + ':' + parentKeyVersion + ':' + parentLocalName + ':' + localName;
    }

    @Override
    public boolean isNullKey() {
        return (PfKey.NULL_KEY_NAME.equals(this.getParentKeyName()) && PfKey.NULL_KEY_VERSION
            .equals(this.getParentKeyVersion()) && PfKey.NULL_KEY_NAME.equals(this.getParentLocalName())
            && PfKey.NULL_KEY_NAME.equals(this.getLocalName()));
    }

    /**
     * Gets the parent concept key of this reference key.
     *
     * @return the parent concept key of this reference key
     */
    public PfConceptKey getParentConceptKey() {
        return new PfConceptKey(parentKeyName, parentKeyVersion);
    }

    /**
     * Gets the parent reference key of this reference key.
     *
     * @return the parent reference key of this reference key
     */
    public PfReferenceKey getParentReferenceKey() {
        return new PfReferenceKey(parentKeyName, parentKeyVersion, parentLocalName);
    }

    /**
     * Sets the parent concept key of this reference key.
     *
     * @param parentKey the parent concept key of this reference key
     */
    public void setParentConceptKey(final PfConceptKey parentKey) {
        Assertions.argumentNotNull(parentKey, "parentKey may not be null");

        parentKeyName = parentKey.getName();
        parentKeyVersion = parentKey.getVersion();
        parentLocalName = NULL_KEY_NAME;
    }

    /**
     * Sets the parent reference key of this reference key.
     *
     * @param parentKey the parent reference key of this reference key
     */
    public void setParentReferenceKey(final PfReferenceKey parentKey) {
        Assertions.argumentNotNull(parentKey, "parentKey may not be null");

        parentKeyName = parentKey.getParentKeyName();
        parentKeyVersion = parentKey.getParentKeyVersion();
        parentLocalName = parentKey.getLocalName();
    }

    @Override
    public PfKey.Compatibility getCompatibility(@NonNull final PfKey otherKey) {
        if (!(otherKey instanceof PfReferenceKey otherReferenceKey)) {
            return Compatibility.DIFFERENT;
        }

        return this.getParentConceptKey().getCompatibility(otherReferenceKey.getParentConceptKey());
    }

    @Override
    public boolean isCompatible(@NonNull final PfKey otherKey) {
        if (!(otherKey instanceof PfReferenceKey otherReferenceKey)) {
            return false;
        }

        return this.getParentConceptKey().isCompatible(otherReferenceKey.getParentConceptKey());
    }

    @Override
    public int getMajorVersion() {
        return this.getParentConceptKey().getMajorVersion();
    }

    @Override
    public int getMinorVersion() {
        return this.getParentConceptKey().getMinorVersion();
    }

    @Override
    public int getPatchVersion() {
        return this.getParentConceptKey().getPatchVersion();
    }


    @Override
    public boolean isNewerThan(@NonNull final PfKey otherKey) {
        Assertions.instanceOf(otherKey, PfReferenceKey.class);

        final PfReferenceKey otherReferenceKey = (PfReferenceKey) otherKey;

        return this.getParentConceptKey().isNewerThan(otherReferenceKey.getParentConceptKey());
    }

    @Override
    public void clean() {
        parentKeyName = Assertions.validateStringParameter(PARENT_KEY_NAME, parentKeyName, NAME_REGEXP);
        parentKeyVersion = Assertions.validateStringParameter(PARENT_KEY_VERSION, parentKeyVersion, VERSION_REGEXP);
        parentLocalName = Assertions.validateStringParameter(PARENT_LOCAL_NAME, parentLocalName, LOCAL_NAME_REGEXP);
        localName = Assertions.validateStringParameter(LOCAL_NAME, localName, LOCAL_NAME_REGEXP);
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

        final PfReferenceKey other = (PfReferenceKey) otherObj;
        if (!parentKeyName.equals(other.parentKeyName)) {
            return parentKeyName.compareTo(other.parentKeyName);
        }
        if (!parentKeyVersion.equals(other.parentKeyVersion)) {
            return parentKeyVersion.compareTo(other.parentKeyVersion);
        }
        if (!parentLocalName.equals(other.parentLocalName)) {
            return parentLocalName.compareTo(other.parentLocalName);
        }
        return localName.compareTo(other.localName);
    }
}
