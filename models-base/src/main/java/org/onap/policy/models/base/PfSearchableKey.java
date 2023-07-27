/*
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2019, 2023 Nordix Foundation.
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

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serial;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import org.onap.policy.common.parameters.annotations.Pattern;
import org.onap.policy.common.utils.validation.Assertions;

/**
 * A key that is used to search for other concept keys. The name field may contain a
 * trailing ".*", to indicate wild-card matching.
 */
@Embeddable
@Getter
@EqualsAndHashCode(callSuper = false)
public class PfSearchableKey extends PfKeyImpl {
    @Serial
    private static final long serialVersionUID = 8932717618579392561L;

    /** Regular expression to specify the structure of key names. */
    public static final String WILDCARD_NAME_REGEXP = "^[A-Za-z0-9\\-_\\.]+(?:\\.\\*)?$";

    @Column(name = NAME_TOKEN, length = 120)
    @Pattern(regexp = WILDCARD_NAME_REGEXP)
    private String name;

    @Column(name = VERSION_TOKEN, length = 20)
    @Pattern(regexp = VERSION_REGEXP)
    private String version;

    /**
     * The default constructor creates a null key.
     */
    public PfSearchableKey() {
        this(NULL_KEY_NAME, NULL_KEY_VERSION);
    }

    /**
     * Copy constructor.
     *
     * @param copyKey the key to copy from
     */
    public PfSearchableKey(final PfSearchableKey copyKey) {
        super(copyKey);
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
     * Get a null key.
     *
     * @return a null key
     */
    public static PfSearchableKey getNullKey() {
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
