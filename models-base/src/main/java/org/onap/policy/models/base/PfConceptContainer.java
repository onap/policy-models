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
import java.util.Map;
import java.util.Map.Entry;
import java.util.NavigableMap;
import java.util.Set;
import java.util.TreeMap;

import javax.persistence.CascadeType;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.ManyToMany;
import javax.persistence.Table;
import javax.ws.rs.core.Response;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;

import org.onap.policy.common.utils.validation.Assertions;
import org.onap.policy.models.base.PfValidationResult.ValidationResult;

/**
 * This class is a concept container and holds a map of concepts. The {@link PfConceptContainer}
 * class implements the helper methods of the {@link PfConceptGetter} interface to allow
 * {@link PfConceptContainer} instances to be retrieved by calling methods directly on this class
 * without referencing the contained map.
 *
 * <p>Validation checks that the container key is not null. An error is issued if no concepts are
 * defined in the container. Each concept entry is checked to ensure that its key and value are not
 * null and that the key matches the key in the map value. Each concept entry is then validated
 * individually.
 *
 * @param C the concept being contained
 */
@Entity
@Table(name = "PfConceptContainer")
@Data
@EqualsAndHashCode(callSuper = false)

public class PfConceptContainer<C extends PfConcept> extends PfConcept implements PfConceptGetter<C> {
    private static final long serialVersionUID = -324211738823208318L;

    @EmbeddedId
    private PfConceptKey key;

    @ManyToMany(cascade = CascadeType.ALL)
    private Map<PfConceptKey, C> conceptMap;

    /**
     * The Default Constructor creates a {@link PfConceptContainer} object with a null artifact key
     * and creates an empty concept map.
     */
    public PfConceptContainer() {
        this(new PfConceptKey());
    }

    /**
     * The Key Constructor creates a {@link PfConceptContainer} object with the given artifact key
     * and creates an empty concept map.
     *
     * @param key the concept key
     */
    public PfConceptContainer(@NonNull final PfConceptKey key) {
        this(key, new TreeMap<PfConceptKey, C>());
    }

    /**
     * This Constructor creates an concept container with all of its fields defined.
     *
     * @param key the concept container key
     * @param conceptMap the concepts to be stored in the concept container
     */
    public PfConceptContainer(@NonNull final PfConceptKey key, @NonNull final Map<PfConceptKey, C> conceptMap) {
        super();

        this.key = key;
        this.conceptMap = new TreeMap<>(conceptMap);
    }

    /**
     * Copy constructor.
     *
     * @param copyConcept the concept to copy from
     */
    public PfConceptContainer(@NonNull final PfConceptContainer<C> copyConcept) {
        super(copyConcept);
    }

    @Override
    public List<PfKey> getKeys() {
        final List<PfKey> keyList = key.getKeys();

        for (final C concept : conceptMap.values()) {
            keyList.addAll(concept.getKeys());
        }

        return keyList;
    }

    @Override
    public void clean() {
        key.clean();
        for (final Entry<PfConceptKey, C> conceptEntry : conceptMap.entrySet()) {
            conceptEntry.getKey().clean();
            conceptEntry.getValue().clean();
        }
    }

    @Override
    public PfValidationResult validate(@NonNull final PfValidationResult resultIn) {
        PfValidationResult result = resultIn;

        if (key.equals(PfConceptKey.getNullKey())) {
            result.addValidationMessage(
                    new PfValidationMessage(key, this.getClass(), ValidationResult.INVALID, "key is a null key"));
        }

        result = key.validate(result);

        if (conceptMap.isEmpty()) {
            result.addValidationMessage(new PfValidationMessage(key, this.getClass(), ValidationResult.INVALID,
                    "conceptMap may not be empty"));
        } else {
            result = validateConceptMap(result);
        }

        return result;
    }

    /**
     * Validate the concept map of the container.
     *
     * @param resultIn the incoming validation results so far
     * @return the validation results with the results of this validation added
     */
    private PfValidationResult validateConceptMap(final PfValidationResult resultIn) {
        PfValidationResult result = resultIn;

        for (final Entry<PfConceptKey, C> conceptEntry : conceptMap.entrySet()) {
            if (conceptEntry.getKey().equals(PfConceptKey.getNullKey())) {
                result.addValidationMessage(new PfValidationMessage(key, this.getClass(), ValidationResult.INVALID,
                        "key on concept entry " + conceptEntry.getKey() + " may not be the null key"));
            } else if (conceptEntry.getValue() == null) {
                result.addValidationMessage(new PfValidationMessage(key, this.getClass(), ValidationResult.INVALID,
                        "value on concept entry " + conceptEntry.getKey() + " may not be null"));
            } else if (!conceptEntry.getKey().equals(conceptEntry.getValue().getKey())) {
                result.addValidationMessage(new PfValidationMessage(key, this.getClass(),
                        ValidationResult.INVALID, "key on concept entry key " + conceptEntry.getKey()
                        + " does not equal concept value key " + conceptEntry.getValue().getKey()));
                result = conceptEntry.getValue().validate(result);
            } else {
                result = conceptEntry.getValue().validate(result);
            }
        }
        return result;
    }

    @Override
    public int compareTo(final PfConcept otherConcept) {
        if (otherConcept == null) {
            return -1;
        }
        if (this == otherConcept) {
            return 0;
        }
        if (getClass() != otherConcept.getClass()) {
            return this.hashCode() - otherConcept.hashCode();
        }

        @SuppressWarnings("unchecked")
        final PfConceptContainer<C> other = (PfConceptContainer<C>) otherConcept;
        int retVal = key.compareTo(other.key);
        if (retVal != 0) {
            return retVal;
        }

        if (!conceptMap.equals(other.conceptMap)) {
            return (conceptMap.hashCode() - other.conceptMap.hashCode());
        }

        return 0;
    }

    @Override
    public PfConcept copyTo(@NonNull final PfConcept target) {
        Assertions.instanceOf(target, PfConceptContainer.class);

        @SuppressWarnings("unchecked")
        final PfConceptContainer<C> copy = (PfConceptContainer<C>) target;
        copy.setKey(new PfConceptKey(key));
        final Map<PfConceptKey, C> newConceptMap = new TreeMap<>();
        for (final Entry<PfConceptKey, C> conceptMapEntry : conceptMap.entrySet()) {
            newConceptMap.put(new PfConceptKey(conceptMapEntry.getKey()),
                    new ConceptCloner().cloneConcept(conceptMapEntry.getValue()));
        }
        copy.setConceptMap(newConceptMap);

        return copy;
    }

    @Override
    public C get(final PfConceptKey conceptKey) {
        return new PfConceptGetterImpl<>((NavigableMap<PfConceptKey, C>) conceptMap).get(conceptKey);
    }

    @Override
    public C get(final String conceptKeyName) {
        return new PfConceptGetterImpl<>((NavigableMap<PfConceptKey, C>) conceptMap).get(conceptKeyName);
    }

    @Override
    public C get(final String conceptKeyName, final String conceptKeyVersion) {
        return new PfConceptGetterImpl<>((NavigableMap<PfConceptKey, C>) conceptMap).get(conceptKeyName,
                conceptKeyVersion);
    }

    @Override
    public Set<C> getAll(final String conceptKeyName) {
        return new PfConceptGetterImpl<>((NavigableMap<PfConceptKey, C>) conceptMap).getAll(conceptKeyName);
    }

    @Override
    public Set<C> getAll(final String conceptKeyName, final String conceptKeyVersion) {
        return new PfConceptGetterImpl<>((NavigableMap<PfConceptKey, C>) conceptMap).getAll(conceptKeyName,
                conceptKeyVersion);
    }

    /**
     * Private inner class that returns a clone of a concept by calling the copy constructor on the
     * original class.
     */
    private class ConceptCloner {
        @SuppressWarnings("unchecked")
        public C cloneConcept(final C originalConcept) {
            try {
                C clonedConcept = (C) originalConcept.getClass().newInstance();
                originalConcept.copyTo(clonedConcept);
                return clonedConcept;
            } catch (Exception ex) {
                throw new PfModelRuntimeException(Response.Status.INTERNAL_SERVER_ERROR,
                        "Failed to create a clone of class \"" + originalConcept.getClass().getCanonicalName() + "\"",
                        ex);
            }
        }
    }
}
