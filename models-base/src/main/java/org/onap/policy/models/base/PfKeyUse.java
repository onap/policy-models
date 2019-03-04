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

import java.util.List;

import lombok.EqualsAndHashCode;
import lombok.ToString;

import org.onap.policy.common.utils.validation.Assertions;
import org.onap.policy.models.base.PfValidationResult.ValidationResult;

/**
 * This class records a usage of a key in the system. When the list of keys being used by a concept is built using the
 * getKeys() method of the {@link PfConcept} class, an instance of this class is created for every key occurrence. The
 * list of keys returned by the getKeys() method is a list of {@link PfKeyUse} objects.
 *
 * <p>Validation checks that each key is valid.
 */
@EqualsAndHashCode(callSuper = false)
@ToString
public class PfKeyUse extends PfKey {
    private static final long serialVersionUID = 2007147220109881705L;

    private PfKey usedKey;

    /**
     * The Default Constructor creates this concept with a null key.
     */
    public PfKeyUse() {
        this(new PfConceptKey());
    }

    /**
     * Copy constructor.
     *
     * @param copyConcept the concept to copy from
     */
    public PfKeyUse(final PfKeyUse copyConcept) {
        super(copyConcept);
    }

    /**
     * This constructor creates an instance of this class, and holds a reference to a used key.
     *
     * @param usedKey a used key
     */
    public PfKeyUse(final PfKey usedKey) {
        Assertions.argumentNotNull(usedKey, "usedKey may not be null");
        this.usedKey = usedKey;
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

    /**
     * Sets the key.
     *
     * @param key the key
     */
    public void setKey(final PfKey key) {
        Assertions.argumentNotNull(key, "usedKey may not be null");
        this.usedKey = key;
    }

    @Override
    public PfKey.Compatibility getCompatibility(final PfKey otherKey) {
        return usedKey.getCompatibility(otherKey);
    }

    @Override
    public boolean isCompatible(final PfKey otherKey) {
        return usedKey.isCompatible(otherKey);
    }

    @Override
    public PfValidationResult validate(final PfValidationResult result) {
        if (usedKey.equals(PfConceptKey.getNullKey())) {
            result.addValidationMessage(new PfValidationMessage(usedKey, this.getClass(), ValidationResult.INVALID,
                    "usedKey is a null key"));
        }
        return usedKey.validate(result);
    }

    @Override
    public void clean() {
        usedKey.clean();
    }

    @Override
    public PfConcept copyTo(final PfConcept target) {
        Assertions.argumentNotNull(target, "target may not be null");

        final Object copyObject = target;
        Assertions.instanceOf(copyObject, PfKeyUse.class);

        final PfKeyUse copy = ((PfKeyUse) copyObject);
        try {
            copy.usedKey = usedKey.getClass().newInstance();
        } catch (final Exception e) {
            throw new PfModelRuntimeException("error copying concept key: " + e.getMessage(), e);
        }
        usedKey.copyTo(copy.usedKey);

        return copy;
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

        final PfKeyUse other = (PfKeyUse) otherObj;

        return usedKey.compareTo(other.usedKey);
    }
}
