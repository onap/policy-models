/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2019-2021 Nordix Foundation.
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


import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.GeneratedValue;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import org.onap.policy.common.parameters.annotations.Pattern;
import org.onap.policy.common.utils.validation.Assertions;

/**
 * An concept key uniquely identifies every first order entity in the system. Every first order concept in the system
 * must have an {@link PfGeneratedIdKey} to identify it. Concepts that are wholly contained in another concept are
 * identified using a {@link PfReferenceKey} key.
 *
 * <p>Key validation checks that the name and version fields match the NAME_REGEXP and VERSION_REGEXP
 * regular expressions respectively.
 */
@Embeddable
@Data
@EqualsAndHashCode(callSuper = false)
public class PfGeneratedIdKey extends PfKeyImpl {

    private static final long serialVersionUID = 1L;

    private static final String ID_TOKEN = "ID";

    @Column(name = NAME_TOKEN, length = 120)
    @Pattern(regexp = NAME_REGEXP)
    private String name;

    @Column(name = VERSION_TOKEN, length = 20)
    @Pattern(regexp = VERSION_REGEXP)
    private String version;

    @Column(name = ID_TOKEN)
    @GeneratedValue
    private Long generatedId;

    /**
     * The default constructor creates a null concept key.
     */
    public PfGeneratedIdKey() {
        this(NULL_KEY_NAME, NULL_KEY_VERSION);
    }

    /**
     * Constructor to create a key with the specified name and version.
     *
     * @param name the key name
     * @param version the key version
     */
    public PfGeneratedIdKey(final String name, final String version) {
        super(name, version);
    }

    /**
     * Copy constructor.
     *
     * @param copyConcept the concept to copy from
     */
    public PfGeneratedIdKey(final PfGeneratedIdKey copyConcept) {
        super(copyConcept);
        this.generatedId = copyConcept.getGeneratedId();
    }

    /**
     * Constructor to create a key with the specified name and version.
     *
     * @param name the key name
     * @param version the key version
     * @param generatedId the conceptId of key
     */
    public PfGeneratedIdKey(@NonNull final String name, @NonNull final String version,
            final Long generatedId) {
        super(name, version);
        this.generatedId = generatedId;
    }

    /**
     * Constructor to create a key using the key and version from the specified key ID.
     *
     * @param id the key ID in a format that respects the KEY_ID_REGEXP
     */
    public PfGeneratedIdKey(final String id) {
        super(id.substring(0, id.lastIndexOf(':')));
        this.generatedId = Long.parseLong(id.substring(id.lastIndexOf(':') + 1));
    }

    @Override
    public int compareTo(@NonNull final PfConcept otherObj) {
        int result = super.compareTo(otherObj);
        if (0 == result) {
            final PfGeneratedIdKey other = (PfGeneratedIdKey) otherObj;
            return generatedId.compareTo(other.generatedId);
        }
        return result;
    }

    @Override
    public String getId() {
        return getName() + ':' + getVersion() + ':' + getGeneratedId();
    }

    @Override
    public boolean isNewerThan(@NonNull PfKey otherKey) {
        Assertions.instanceOf(otherKey, PfGeneratedIdKey.class);

        final PfGeneratedIdKey otherConceptKey = (PfGeneratedIdKey) otherKey;

        if (this.equals(otherConceptKey)) {
            return false;
        }

        if (!generatedId.equals(otherConceptKey.generatedId)) {
            return generatedId.compareTo(otherConceptKey.generatedId) >= 1;
        }

        return super.isNewerThan(otherKey);
    }

    @Override
    public boolean isNullKey() {
        return super.isNullKey() && getGeneratedId() == null;
    }

    public void setName(@NonNull final String name) {
        this.name = Assertions.validateStringParameter(NAME_TOKEN, name, getNameRegEx());
    }

    public void setVersion(@NonNull final String version) {
        this.version = Assertions.validateStringParameter(VERSION_TOKEN, version, getVersionRegEx());
    }

    /**
     * Get a null concept key.
     *
     * @return a null concept key
     */
    public static final PfGeneratedIdKey getNullKey() {
        return new PfGeneratedIdKey(PfKey.NULL_KEY_NAME, PfKey.NULL_KEY_VERSION);
    }

}
