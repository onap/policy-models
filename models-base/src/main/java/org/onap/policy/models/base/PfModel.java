/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2019, 2023-2024 Nordix Foundation.
 *  Modifications Copyright (C) 2019-2021 AT&T Intellectual Property. All rights reserved.
 *  Modifications Copyright (C) 2022 Bell Canada. All rights reserved.
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

import jakarta.persistence.EmbeddedId;
import jakarta.persistence.MappedSuperclass;
import java.io.Serial;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import org.onap.policy.common.parameters.BeanValidationResult;
import org.onap.policy.common.parameters.ValidationStatus;
import org.onap.policy.common.parameters.annotations.NotNull;
import org.onap.policy.common.utils.validation.Assertions;
import org.onap.policy.models.base.validation.annotations.VerifyKey;

/**
 * This class is the base class for all models in the Policy Framework. All model classes inherit
 * from this model so all models must have a key and have key information.
 *
 * <p>Validation checks that the model key is valid. It goes on to check for null keys and checks
 * each key for uniqueness in the model. A check is carried out to ensure that an {@link PfKey}
 * instance exists for every {@link PfConceptKey} key. For each {@link PfReferenceKey} instance, a
 * check is made that its parent and local name are not null and that a {@link PfKey} entry
 * exists for its parent. Then a check is made that each used {@link PfConceptKey} and
 * {@link PfReferenceKey} usage references a key that exists. Finally, a check is made to ensure
 * that an {@link PfConceptKey} instance exists for every {@link PfKey} instance.
 *
 * @param <C> the type of concept on which the interface is applied.
 */

@MappedSuperclass
@Data
@EqualsAndHashCode(callSuper = false)
public abstract class PfModel extends PfConcept {
    private static final String KEYS_TOKEN = "keys";

    @Serial
    private static final long serialVersionUID = -771659065637205430L;

    @EmbeddedId
    @VerifyKey
    @NotNull
    private PfConceptKey key;

    /**
     * The Default Constructor creates this concept with a NULL artifact key.
     */
    protected PfModel() {
        this(new PfConceptKey());
    }

    /**
     * Constructor to create this concept with the specified key.
     *
     * @param key the key of this concept
     */
    protected PfModel(@NonNull final PfConceptKey key) {
        super();
        Assertions.argumentNotNull(key, "key may not be null");

        this.key = key;
    }

    /**
     * Copy constructor.
     *
     * @param copyConcept the concept to copy from
     */
    protected PfModel(@NonNull final PfModel copyConcept) {
        super(copyConcept);
        this.key = new PfConceptKey(copyConcept.key);
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
    public BeanValidationResult validate(@NonNull String fieldName) {
        BeanValidationResult result = new PfValidator().validateTop(fieldName, this);

        // Key consistency check
        final Set<PfConceptKey> artifactKeySet = new TreeSet<>();
        final Set<PfReferenceKey> referenceKeySet = new TreeSet<>();
        final Set<PfKeyUse> usedKeySet = new TreeSet<>();

        for (final PfKey pfKey : this.getKeys()) {
            // Check for the two type of keys we have
            if (pfKey instanceof PfConceptKey pfConceptKey) {
                validateArtifactKeyInModel(pfConceptKey, artifactKeySet, result);
            } else if (pfKey instanceof PfReferenceKey pfReferenceKey) {
                validateReferenceKeyInModel(pfReferenceKey, referenceKeySet, result);
            } else {
                // It must be a PfKeyUse, nothing else is legal
                usedKeySet.add((PfKeyUse) pfKey);
            }
        }

        // Check all reference keys have correct parent keys
        for (final PfReferenceKey referenceKey : referenceKeySet) {
            if (!artifactKeySet.contains(referenceKey.getParentConceptKey())) {
                addResult(result, "reference key", referenceKey, "parent artifact key not found");
            }
        }

        validateKeyUses(usedKeySet, artifactKeySet, referenceKeySet, result);

        return result;
    }

    /**
     * Check for consistent usage of an artifact key in the model.
     *
     * @param artifactKey    The artifact key to check
     * @param artifactKeySet The set of artifact keys encountered so far, this key is appended to
     *                       the set
     * @param result         where to add the results
     */
    private void validateArtifactKeyInModel(final PfConceptKey artifactKey,
                                            final Set<PfConceptKey> artifactKeySet, final BeanValidationResult result) {

        validateKeyNotNull(result, KEYS_TOKEN, artifactKey);

        var result2 = new BeanValidationResult(KEYS_TOKEN, artifactKey);

        // Null key name start check
        if (artifactKey.getName().toUpperCase().startsWith(PfKey.NULL_KEY_NAME)) {
            addResult(result2, "name of " + artifactKey.getId(), artifactKey.getName(),
                "starts with keyword " + PfKey.NULL_KEY_NAME);
        }

        // Unique key check
        if (artifactKeySet.contains(artifactKey)) {
            addResult(result, KEYS_TOKEN, artifactKey, "duplicate key");
        } else {
            artifactKeySet.add(artifactKey);
        }
    }

    /**
     * Check for consistent usage of a reference key in the model.
     *
     * @param referenceKey    The reference key to check
     * @param referenceKeySet The set of reference keys encountered so far, this key is appended to
     *                        the set
     * @param result          where to add the results
     */
    private void validateReferenceKeyInModel(final PfReferenceKey referenceKey,
                                             final Set<PfReferenceKey> referenceKeySet,
                                             final BeanValidationResult result) {
        // Null key check
        if (referenceKey.isNullKey()) {
            addResult(result, KEYS_TOKEN, referenceKey, IS_A_NULL_KEY);
        }

        var result2 = new BeanValidationResult(KEYS_TOKEN, referenceKey);

        // Null parent key check
        if (referenceKey.getParentConceptKey().isNullKey()) {
            addResult(result2, "parent key of " + referenceKey.getId(), referenceKey.getParentConceptKey().getId(),
                IS_A_NULL_KEY);
        }

        // Null local name check
        if (referenceKey.getLocalName().equals(PfKey.NULL_KEY_NAME)) {
            addResult(result2, "local name of " + referenceKey.getId(), referenceKey.getLocalName(), IS_NULL);
        }

        // Null key name start check
        if (referenceKey.getParentConceptKey().getName().toUpperCase().startsWith(PfKey.NULL_KEY_NAME)) {
            addResult(result2, "parent name of " + referenceKey.getId(), referenceKey.getParentConceptKey().getName(),
                "starts with keyword " + PfKey.NULL_KEY_NAME);
        }

        // Unique key check
        if (referenceKeySet.contains(referenceKey)) {
            addResult(result, KEYS_TOKEN, referenceKey, "duplicate key");
        } else {
            referenceKeySet.add(referenceKey);
        }
    }

    /**
     * Check for consistent usage of cross-key references in the model.
     *
     * @param usedKeySet      The set of all keys used in the model
     * @param artifactKeySet  The set of artifact keys encountered so far, this key is appended to
     *                        the set
     * @param referenceKeySet The set of reference keys encountered so far, this key is appended to
     *                        the set
     * @param result          where to add the results
     */
    private void validateKeyUses(final Set<PfKeyUse> usedKeySet, final Set<PfConceptKey> artifactKeySet,
                                 final Set<PfReferenceKey> referenceKeySet, final BeanValidationResult result) {
        // Check all key uses
        for (final PfKeyUse usedKey : usedKeySet) {
            if (usedKey.getKey() instanceof PfConceptKey) {
                // PfConceptKey usage, check the key exists
                if (!artifactKeySet.contains(usedKey.getKey())) {
                    result.addResult("artifact key", usedKey.getId(), ValidationStatus.INVALID, NOT_DEFINED);
                }
            } else {
                // PfReferenceKey usage, check the key exists
                if (!referenceKeySet.contains(usedKey.getKey())) {
                    result.addResult("reference key", usedKey.getId(), ValidationStatus.INVALID, NOT_DEFINED);
                }
            }
        }
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
}
