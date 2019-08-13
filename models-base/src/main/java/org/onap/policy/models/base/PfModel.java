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
import java.util.Set;
import java.util.TreeSet;

import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.Table;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;

import org.onap.policy.common.utils.validation.Assertions;
import org.onap.policy.models.base.PfValidationResult.ValidationResult;
import org.onap.policy.models.base.utils.BeanCopyUtils;

/**
 * This class is the base class for all models in the Policy Framework. All model classes inherit
 * from this model so all models must have a key and have key information.
 *
 * <p>Validation checks that the model key is valid. It goes on to check for null keys and checks
 * each key for uniqueness in the model. A check is carried out to ensure that an {@link PfKeyInfo}
 * instance exists for every {@link PfConceptKey} key. For each {@link PfReferenceKey} instance, a
 * check is made that its parent and local name are nut null and that a {@link PfKeyInfo} entry
 * exists for its parent. Then a check is made that each used {@link PfConceptKey} and
 * {@link PfReferenceKey} usage references a key that exists. Finally, a check is made to ensure
 * that an {@link PfConceptKey} instance exists for every {@link PfKeyInfo} instance.
 *
 * @param <C> the type of concept on which the interface is applied.
 */

@Entity
@Table(name = "PfModel")
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
@Data
@EqualsAndHashCode(callSuper = false)
public abstract class PfModel extends PfConcept {
    private static final String IS_A_NULL_KEY = " is a null key";

    private static final long serialVersionUID = -771659065637205430L;

    @EmbeddedId
    private PfConceptKey key;

    /**
     * The Default Constructor creates this concept with a NULL artifact key.
     */
    public PfModel() {
        this(new PfConceptKey());
    }

    /**
     * Constructor to create this concept with the specified key.
     *
     * @param key the key of this concept
     */
    public PfModel(@NonNull final PfConceptKey key) {
        super();
        Assertions.argumentNotNull(key, "key may not be null");

        this.key = key;
    }

    /**
     * Copy constructor.
     *
     * @param copyConcept the concept to copy from
     */
    public PfModel(@NonNull final PfModel copyConcept) {
        super(copyConcept);
    }

    /**
     * Registers this model with the {@link PfModelService}. All models are registered with the
     * model service so that models can be references from anywhere in the Policy Framework system
     * without being passed as references through deep call chains.
     */
    public abstract void register();

    @Override
    public List<PfKey> getKeys() {
        return key.getKeys();
    }

    @Override
    public void clean() {
        key.clean();
    }

    @Override
    public PfValidationResult validate(@NonNull final PfValidationResult resultIn) {
        PfValidationResult result = resultIn;

        if (key.isNullKey()) {
            result.addValidationMessage(
                    new PfValidationMessage(key, this.getClass(), ValidationResult.INVALID, "key is a null key"));
        }

        result = key.validate(result);

        // Key consistency check
        final Set<PfConceptKey> artifactKeySet = new TreeSet<>();
        final Set<PfReferenceKey> referenceKeySet = new TreeSet<>();
        final Set<PfKeyUse> usedKeySet = new TreeSet<>();

        for (final PfKey pfKey : this.getKeys()) {
            // Check for the two type of keys we have
            if (pfKey instanceof PfConceptKey) {
                result = validateArtifactKeyInModel((PfConceptKey) pfKey, artifactKeySet, result);
            } else if (pfKey instanceof PfReferenceKey) {
                result = validateReferenceKeyInModel((PfReferenceKey) pfKey, referenceKeySet, result);
            }
            // It must be a PfKeyUse, nothing else is legal
            else {
                usedKeySet.add((PfKeyUse) pfKey);
            }
        }

        // Check all reference keys have correct parent keys
        for (final PfReferenceKey referenceKey : referenceKeySet) {
            if (!artifactKeySet.contains(referenceKey.getParentConceptKey())) {
                result.addValidationMessage(new PfValidationMessage(key, this.getClass(), ValidationResult.INVALID,
                        "parent artifact key not found for reference key " + referenceKey));
            }
        }

        result = validateKeyUses(usedKeySet, artifactKeySet, referenceKeySet, result);

        return result;
    }

    /**
     * Check for consistent usage of an artifact key in the model.
     *
     * @param artifactKey The artifact key to check
     * @param artifactKeySet The set of artifact keys encountered so far, this key is appended to
     *        the set
     * @param result The validation result to append to
     * @return the result of the validation
     */
    private PfValidationResult validateArtifactKeyInModel(final PfConceptKey artifactKey,
            final Set<PfConceptKey> artifactKeySet, final PfValidationResult result) {
        // Null key check
        if (artifactKey.isNullKey()) {
            result.addValidationMessage(new PfValidationMessage(key, this.getClass(), ValidationResult.INVALID,
                    "key " + artifactKey + IS_A_NULL_KEY));
        }

        // Null key name start check
        if (artifactKey.getName().toUpperCase().startsWith(PfKey.NULL_KEY_NAME)) {
            result.addValidationMessage(new PfValidationMessage(key, this.getClass(), ValidationResult.INVALID,
                    "key " + artifactKey + " name starts with keyword " + PfKey.NULL_KEY_NAME));
        }

        // Unique key check
        if (artifactKeySet.contains(artifactKey)) {
            result.addValidationMessage(new PfValidationMessage(key, this.getClass(), ValidationResult.INVALID,
                    "duplicate key " + artifactKey + " found"));
        } else {
            artifactKeySet.add(artifactKey);
        }

        return result;
    }

    /**
     * Check for consistent usage of a reference key in the model.
     *
     * @param artifactKey The reference key to check
     * @param referenceKeySet The set of reference keys encountered so far, this key is appended to
     *        the set
     * @param result The validation result to append to
     * @return the result of the validation
     */
    private PfValidationResult validateReferenceKeyInModel(final PfReferenceKey referenceKey,
            final Set<PfReferenceKey> referenceKeySet, final PfValidationResult result) {
        // Null key check
        if (referenceKey.isNullKey()) {
            result.addValidationMessage(new PfValidationMessage(key, this.getClass(), ValidationResult.INVALID,
                    "key " + referenceKey + IS_A_NULL_KEY));
        }

        // Null parent key check
        if (referenceKey.getParentConceptKey().isNullKey()) {
            result.addValidationMessage(new PfValidationMessage(key, this.getClass(), ValidationResult.INVALID,
                    "parent artifact key of key " + referenceKey + IS_A_NULL_KEY));
        }

        // Null local name check
        if (referenceKey.getLocalName().equals(PfKey.NULL_KEY_NAME)) {
            result.addValidationMessage(new PfValidationMessage(key, this.getClass(), ValidationResult.INVALID,
                    "key " + referenceKey + " has a null local name"));
        }

        // Null key name start check
        if (referenceKey.getParentConceptKey().getName().toUpperCase().startsWith(PfKey.NULL_KEY_NAME)) {
            result.addValidationMessage(new PfValidationMessage(key, this.getClass(), ValidationResult.INVALID,
                    "key " + referenceKey + " parent name starts with keyword " + PfKey.NULL_KEY_NAME));
        }

        // Unique key check
        if (referenceKeySet.contains(referenceKey)) {
            result.addValidationMessage(new PfValidationMessage(key, this.getClass(), ValidationResult.INVALID,
                    "duplicate key " + referenceKey + " found"));
        } else {
            referenceKeySet.add(referenceKey);
        }

        return result;
    }

    /**
     * Check for consistent usage of cross-key references in the model.
     *
     * @param usedKeySet The set of all keys used in the model
     * @param artifactKeySet The set of artifact keys encountered so far, this key is appended to
     *        the set
     * @param referenceKeySet The set of reference keys encountered so far, this key is appended to
     *        the set
     * @param result The validation result to append to
     * @return the result of the validation
     */
    private PfValidationResult validateKeyUses(final Set<PfKeyUse> usedKeySet, final Set<PfConceptKey> artifactKeySet,
            final Set<PfReferenceKey> referenceKeySet, final PfValidationResult result) {
        // Check all key uses
        for (final PfKeyUse usedKey : usedKeySet) {
            if (usedKey.getKey() instanceof PfConceptKey) {
                // PfConceptKey usage, check the key exists
                if (!artifactKeySet.contains(usedKey.getKey())) {
                    result.addValidationMessage(new PfValidationMessage(usedKey.getKey(), this.getClass(),
                            ValidationResult.INVALID, "an artifact key used in the model is not defined"));
                }
            } else {
                // PfReferenceKey usage, check the key exists
                if (!referenceKeySet.contains(usedKey.getKey())) {
                    result.addValidationMessage(new PfValidationMessage(usedKey.getKey(), this.getClass(),
                            ValidationResult.INVALID, "a reference key used in the model is not defined"));
                }
            }
        }

        return result;
    }

    @Override
    public int compareTo(final PfConcept otherObj) {
        if (otherObj == null) {
            return -1;
        }
        if (this == otherObj) {
            return 0;
        }
        if (getClass() != otherObj.getClass()) {
            return getClass().getName().compareTo(otherObj.getClass().getName());
        }

        final PfModel other = (PfModel) otherObj;

        return key.compareTo(other.key);
    }

    @Override
    public PfConcept copyTo(@NonNull final PfConcept target) {
        return BeanCopyUtils.copyTo(this, target, this.getClass());
    }
}
