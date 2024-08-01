/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2019-2020, 2023-2024 Nordix Foundation.
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

import com.google.re2j.Pattern;
import jakarta.persistence.CascadeType;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.MappedSuperclass;
import jakarta.ws.rs.core.Response;
import java.io.Serial;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NavigableMap;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.function.Function;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import org.apache.commons.lang3.StringUtils;
import org.onap.policy.common.parameters.BeanValidationResult;
import org.onap.policy.common.parameters.ValidationResult;
import org.onap.policy.common.parameters.annotations.NotNull;
import org.onap.policy.models.base.validation.annotations.VerifyKey;

// @formatter:off
/**
 * This class is a concept container and holds a map of concepts. The {@link PfConceptContainer} class implements the
 * helper methods of the {@link PfConceptGetter} interface to allow {@link PfConceptContainer} instances to be retrieved
 * by calling methods directly on this class without referencing the contained map.
 *
 * <p>Validation checks that a container key is not null. An error is issued if no concepts are defined in a container.
 * Each concept entry is checked to ensure that its key and value are not null and that the key matches the key in the
 * map value. Each concept entry is then validated individually.
 *
 * @param <C> the concept being contained
 */
//@formatter:on
@MappedSuperclass
@Data
@EqualsAndHashCode(callSuper = false)
public class PfConceptContainer<C extends PfConcept, A extends PfNameVersion> extends PfConcept
    implements PfConceptGetter<C>, PfAuthorative<List<Map<String, A>>> {
    @Serial
    private static final long serialVersionUID = -324211738823208318L;

    private static final String VALUE_FIELD = "value";
    private static final Pattern KEY_ID_PATTERN = Pattern.compile(PfKey.KEY_ID_REGEXP);

    @EmbeddedId
    @VerifyKey
    @NotNull
    private PfConceptKey key;

    @ManyToMany(cascade = CascadeType.ALL)
    // @formatter:off
    @JoinTable(
            joinColumns = {
                @JoinColumn(name = "conceptContainerMapName",    referencedColumnName = "name"),
                @JoinColumn(name = "concpetContainerMapVersion", referencedColumnName = "version")
            },
            inverseJoinColumns = {
                @JoinColumn(name = "conceptContainerName",    referencedColumnName = "name"),
                @JoinColumn(name = "conceptContainerVersion", referencedColumnName = "version")
            }
        )
    // @formatter:on
    private Map<PfConceptKey, C> conceptMap;

    /**
     * The Default Constructor creates a {@link PfConceptContainer} object with a null artifact key and creates an empty
     * concept map.
     */
    public PfConceptContainer() {
        this(new PfConceptKey());
    }

    /**
     * The Key Constructor creates a {@link PfConceptContainer} object with the given artifact key and creates an empty
     * concept map.
     *
     * @param key the concept key
     */
    public PfConceptContainer(@NonNull final PfConceptKey key) {
        this(key, new TreeMap<>());
    }

    /**
     * This Constructor creates a concept container with all of its fields defined.
     *
     * @param key        the concept container key
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
    public PfConceptContainer(@NonNull final PfConceptContainer<C, A> copyConcept) {
        super(copyConcept);
        this.key = new PfConceptKey(copyConcept.key);

        this.conceptMap = new TreeMap<>();
        for (final Entry<PfConceptKey, C> conceptMapEntry : copyConcept.conceptMap.entrySet()) {
            var newK = new PfConceptKey(conceptMapEntry.getKey());
            var newC = PfUtils.makeCopy(conceptMapEntry.getValue());
            this.conceptMap.put(newK, newC);
        }
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
    public List<Map<String, A>> toAuthorative() {
        // The returned list is a list of map singletons with one map for each map
        // entry in the concept container
        List<Map<String, A>> toscaConceptMapList = new ArrayList<>();

        for (Entry<PfConceptKey, C> conceptEntry : getConceptMap().entrySet()) {
            // Create a map to hold this entry
            Map<String, A> toscaPolicyMap = new LinkedHashMap<>(1);

            // Add the concept container entry to the singleton map
            @SuppressWarnings("unchecked")
            PfAuthorative<A> authoritiveImpl = (PfAuthorative<A>) conceptEntry.getValue();
            toscaPolicyMap.put(conceptEntry.getKey().getName(), authoritiveImpl.toAuthorative());

            // Add the map to the returned list
            toscaConceptMapList.add(toscaPolicyMap);
        }

        return toscaConceptMapList;
    }

    @Override
    public void fromAuthorative(List<Map<String, A>> authorativeList) {
        // Clear any existing map entries
        conceptMap.clear();

        // Concepts are in lists of maps
        for (Map<String, A> incomingConceptMap : authorativeList) {
            // Add the map entries one by one
            for (Entry<String, A> incomingConceptEntry : incomingConceptMap.entrySet()) {

                var conceptKey = new PfConceptKey();
                if (KEY_ID_PATTERN.matches(incomingConceptEntry.getKey())) {
                    conceptKey = new PfConceptKey(incomingConceptEntry.getKey());
                } else {
                    conceptKey.setName(incomingConceptEntry.getKey());
                    if (incomingConceptEntry.getValue().getVersion() != null) {
                        conceptKey.setVersion(incomingConceptEntry.getValue().getVersion());
                    } else {
                        conceptKey.setVersion(PfKey.NULL_KEY_VERSION);
                    }
                }

                incomingConceptEntry.getValue().setName(findConceptField(conceptKey, conceptKey.getName(),
                    incomingConceptEntry.getValue(), PfNameVersion::getDefinedName));
                incomingConceptEntry.getValue().setVersion(findConceptField(conceptKey, conceptKey.getVersion(),
                    incomingConceptEntry.getValue(), PfNameVersion::getDefinedVersion));

                var jpaConcept = getConceptNewInstance();
                // This cast allows us to call the fromAuthorative method
                @SuppressWarnings("unchecked")
                PfAuthorative<A> authoritiveImpl = (PfAuthorative<A>) jpaConcept;

                // Set the key name and the rest of the values on the concept
                authoritiveImpl.fromAuthorative(incomingConceptEntry.getValue());

                // After all that, save the map entry
                conceptMap.put(conceptKey, jpaConcept);
            }
        }

        if (conceptMap.isEmpty()) {
            throw new PfModelRuntimeException(Response.Status.BAD_REQUEST,
                "An incoming list of concepts must have at least one entry");
        }
    }

    /**
     * Get an authorative list of the concepts in this container.
     *
     * @return the authorative list of concepts
     */
    public List<A> toAuthorativeList() {
        List<A> toscaConceptList = new ArrayList<>();

        for (Map<String, A> toscaConceptMap : toAuthorative()) {
            toscaConceptList.addAll(toscaConceptMap.values());
        }

        return toscaConceptList;
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
    public BeanValidationResult validate(@NonNull String fieldName) {
        BeanValidationResult result = new PfValidator().validateTop(fieldName, this);
        result.addResult(validateConceptMap());

        return result;
    }

    /**
     * Validate the concept map of the container.
     *
     * @return the validation result
     */
    private ValidationResult validateConceptMap() {
        var result = new BeanValidationResult("conceptMap", conceptMap);

        for (final Entry<PfConceptKey, C> conceptEntry : conceptMap.entrySet()) {
            BeanValidationResult result2 = null;

            if (conceptEntry.getKey().equals(PfConceptKey.getNullKey())) {
                addResult(result, "key on concept entry", conceptEntry.getKey(), IS_A_NULL_KEY);
            } else if (conceptEntry.getValue() == null) {
                result2 = new BeanValidationResult(conceptEntry.getKey().getId(), conceptEntry.getKey());
                addResult(result2, VALUE_FIELD, conceptEntry.getValue(), IS_NULL);
            } else if (!conceptEntry.getKey().equals(conceptEntry.getValue().getKey())) {
                result2 = new BeanValidationResult(conceptEntry.getKey().getId(), conceptEntry.getKey());
                addResult(result2, VALUE_FIELD, conceptEntry.getValue(), "does not equal concept key");
                result2.addResult(conceptEntry.getValue().validate(VALUE_FIELD));
            } else {
                result2 = new BeanValidationResult(conceptEntry.getKey().getId(), conceptEntry.getKey());
                result2.addResult(conceptEntry.getValue().validate(VALUE_FIELD));
            }

            result.addResult(result2);
        }
        return (result.isClean() ? null : result);
    }

    @Override
    public int compareTo(@NonNull final PfConcept otherConcept) {
        if (this == otherConcept) {
            return 0;
        }
        if (getClass() != otherConcept.getClass()) {
            return getClass().getName().compareTo(otherConcept.getClass().getName());
        }

        @SuppressWarnings("unchecked") final PfConceptContainer<C, A> other = (PfConceptContainer<C, A>) otherConcept;
        int retVal = key.compareTo(other.key);
        if (retVal != 0) {
            return retVal;
        }

        return PfUtils.compareMaps(conceptMap, other.conceptMap);
    }

    /**
     * Get all the concepts that match the given name and version.
     *
     * @param conceptKeyName    the name of the concept, if null, return all names
     * @param conceptKeyVersion the version of the concept, if null, return all versions
     * @return conceptKeyVersion
     */
    public Set<C> getAllNamesAndVersions(final String conceptKeyName, final String conceptKeyVersion) {
        if (conceptKeyName == null || conceptKeyVersion == null || PfKey.NULL_KEY_VERSION.equals(conceptKeyVersion)) {
            return getAll(conceptKeyName, conceptKeyVersion);
        } else {
            final Set<C> returnSet = new TreeSet<>();
            var foundConcept = get(conceptKeyName, conceptKeyVersion);
            if (foundConcept != null) {
                returnSet.add(foundConcept);
            }
            return returnSet;
        }
    }

    @Override
    public C get(final PfConceptKey conceptKey) {
        if (conceptKey.isNullVersion()) {
            return get(conceptKey.getName());
        } else {
            return new PfConceptGetterImpl<>(getNavigableConceptMap()).get(conceptKey);
        }
    }

    @Override
    public C get(final String conceptKeyName) {
        return new PfConceptGetterImpl<>(getNavigableConceptMap()).get(conceptKeyName);
    }

    @Override
    public C get(final String conceptKeyName, final String conceptKeyVersion) {
        return new PfConceptGetterImpl<>(getNavigableConceptMap()).get(conceptKeyName, conceptKeyVersion);
    }

    @Override
    public Set<C> getAll(final String conceptKeyName) {
        return new PfConceptGetterImpl<>(getNavigableConceptMap()).getAll(conceptKeyName);
    }

    @Override
    public Set<C> getAll(final String conceptKeyName, final String conceptKeyVersion) {
        return new PfConceptGetterImpl<>(getNavigableConceptMap()).getAll(conceptKeyName, conceptKeyVersion);
    }

    /**
     * Get the concept map as a NavigableMap object.
     *
     * @return NavigableMap conceptMap instance.
     */
    private NavigableMap<PfConceptKey, C> getNavigableConceptMap() {
        return new TreeMap<>(conceptMap);
    }

    /**
     * Get a new empty instance of a concept for this concept map.
     *
     * @return the new instance
     */
    @SuppressWarnings("unchecked")
    private C getConceptNewInstance() {
        try {
            String conceptClassName =
                ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[0].getTypeName();
            return (C) Class.forName(conceptClassName).getDeclaredConstructor().newInstance();
        } catch (Exception ex) {
            throw new PfModelRuntimeException(Response.Status.INTERNAL_SERVER_ERROR,
                "failed to instantiate instance of container concept class", ex);
        }
    }

    private String findConceptField(final PfConceptKey conceptKey, final String keyFieldValue,
                                    final PfNameVersion concept,
                                    final Function<PfNameVersion, String> fieldGetterFunction) {

        String conceptField = fieldGetterFunction.apply(concept);

        if (StringUtils.isBlank(conceptField) || keyFieldValue.equals(conceptField)) {
            return keyFieldValue;
        } else {
            throw new PfModelRuntimeException(Response.Status.BAD_REQUEST, "Key " + conceptKey.getId() + " field "
                + keyFieldValue + " does not match the value " + conceptField + " in the concept field");
        }
    }
}