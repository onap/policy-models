/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2019, 2023 Nordix Foundation.
 *  Modifications Copyright (C) 2019-2020 AT&T Intellectual Property. All rights reserved.
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
import java.util.List;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;
import org.onap.policy.common.parameters.annotations.NotNull;
import org.onap.policy.common.utils.validation.Assertions;
import org.onap.policy.models.base.validation.annotations.VerifyKey;

/**
 * This class records a usage of a key in the system. When the list of keys being used by a concept
 * is built using the getKeys() method of the {@link PfConcept} class, an instance of this class is
 * created for every key occurrence. The list of keys returned by the getKeys() method is a list of
 * {@link PfKeyUse} objects.
 *
 * <p>Validation checks that each key is valid.
 */
@EqualsAndHashCode(callSuper = false)
@ToString
public class PfKeyUse extends PfKey {
    @Serial
    private static final long serialVersionUID = 2007147220109881705L;

    @VerifyKey
    @NotNull
    @Getter
    private PfKey usedKey;

    /**
     * The Default Constructor creates this concept with a null key.
     */
    public PfKeyUse() {
        this(new PfConceptKey());
    }

    /**
     * This constructor creates an instance of this class, and holds a reference to a used key.
     *
     * @param usedKey a used key
     */
    public PfKeyUse(@NonNull final PfKey usedKey) {
        this.usedKey = usedKey;
    }

    /**
     * Copy constructor.
     *
     * @param copyConcept the concept to copy from
     */
    public PfKeyUse(@NonNull final PfKeyUse copyConcept) {
        super(copyConcept);
        this.usedKey = PfUtils.makeCopy(copyConcept.usedKey);
    }

    @Override
    public PfKey getKey() {
        return usedKey;
    }

    @Override
    public List<PfKey> getKeys() {
        return usedKey.getKeys();
    }

    @Override
    public String getId() {
        return usedKey.getId();
    }

    @Override
    public boolean isNullKey() {
        return usedKey.isNullKey();
    }

    /**
     * Sets the key.
     *
     * @param key the key
     */
    public void setKey(@NonNull final PfKey key) {
        this.usedKey = key;
    }

    @Override
    public PfKey.Compatibility getCompatibility(@NonNull final PfKey otherKey) {
        return usedKey.getCompatibility(otherKey);
    }

    @Override
    public boolean isCompatible(@NonNull final PfKey otherKey) {
        return usedKey.isCompatible(otherKey);
    }

    @Override
    public boolean isNewerThan(@NonNull final PfKey otherKey) {
        return usedKey.isCompatible(otherKey);
    }

    @Override
    public int getMajorVersion() {
        return usedKey.getMajorVersion();
    }

    @Override
    public int getMinorVersion() {
        return usedKey.getMinorVersion();
    }

    @Override
    public int getPatchVersion() {
        return usedKey.getPatchVersion();
    }

    @Override
    public void clean() {
        usedKey.clean();
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

        final PfKeyUse other = (PfKeyUse) otherObj;

        return usedKey.compareTo(other.usedKey);
    }
}
