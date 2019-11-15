/*
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2019 Nordix Foundation.
 *  Modifications Copyright (C) 2019 AT&T Intellectual Property. All rights reserved.
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
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import org.onap.policy.common.utils.validation.Assertions;

/**
 * An concept key uniquely identifies every first order entity in the system. Every first order concept in the system
 * must have an {@link PfSearchableKey} to identify it. Concepts that are wholly contained in another concept are
 * identified using a {@link PfReferenceKey} key.
 *
 * <p>Key validation checks that the name and version fields match the NAME_REGEXP and VERSION_REGEXP
 * regular expressions respectively.
 */
@Embeddable
@Getter
@EqualsAndHashCode(callSuper = false)
public class PfSearchableKey extends PfKeyImpl {
    private static final long serialVersionUID = 8932717618579392561L;

    /** Regular expression to specify the structure of key names. */
    public static final String WILDCARD_NAME_REGEXP = "^[A-Za-z0-9\\-_\\.]+(?:\\.\\*)?$";

    @Column(name = NAME_TOKEN, length = 120)
    private String name;

    @Column(name = VERSION_TOKEN, length = 20)
    private String version;

    /**
     * The default constructor creates a null concept key.
     */
    public PfSearchableKey() {
        this(NULL_KEY_NAME, NULL_KEY_VERSION);
    }

    /**
     * Copy constructor.
     *
     * @param copyConcept the concept to copy from
     */
    public PfSearchableKey(final PfSearchableKey copyConcept) {
        super(copyConcept);
    }

    /**
     * Constructor to create a key with the specified name and version.
     *
     * @param name the key name
     * @param version the key version
     */
    public PfSearchableKey(final String name, final String version) {
        super(name, version);
    }

    /**
     * Constructor to create a key using the key and version from the specified key ID.
     *
     * @param id the key ID in a format that respects the KEY_ID_REGEXP
     */
    public PfSearchableKey(final String id) {
        super(id);
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
    public static final PfSearchableKey getNullKey() {
        return new PfSearchableKey(PfKey.NULL_KEY_NAME, PfKey.NULL_KEY_VERSION);
    }

    @Override
    protected String getNameRegEx() {
        return WILDCARD_NAME_REGEXP;
    }

    @Override
    public String toString() {
        return "PfSearchableKey(name=" + getName() + ", version=" + getVersion() + ")";
    }
}
