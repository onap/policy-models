/*-
 * ============LICENSE_START=======================================================
 * Copyright (C) 2020 Nordix Foundation.
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

package org.onap.policy.models.tosca.simple.concepts;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.Lob;
import javax.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import org.onap.policy.common.utils.coder.YamlJsonTranslator;
import org.onap.policy.models.base.PfAuthorative;
import org.onap.policy.models.base.PfConcept;
import org.onap.policy.models.base.PfConceptKey;
import org.onap.policy.models.base.PfUtils;
import org.onap.policy.models.base.PfValidationMessage;
import org.onap.policy.models.base.PfValidationResult;
import org.onap.policy.models.base.PfValidationResult.ValidationResult;
import org.onap.policy.models.tosca.authorative.concepts.ToscaCapabilityAssignment;

/**
 * Class to represent the parameter in TOSCA definition.
 */
@Entity
@Table(name = "ToscaCapabilityAssignment")
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
@Data
@EqualsAndHashCode(callSuper = false)
public class JpaToscaCapabilityAssignment extends JpaToscaEntityType<ToscaCapabilityAssignment>
        implements PfAuthorative<ToscaCapabilityAssignment> {

    private static final long serialVersionUID = 1675770231921107988L;

    private static final String AUTHORATIVE_UNBOUNDED_LITERAL = "UNBOUNDED";
    private static final Integer JPA_UNBOUNDED_VALUE = -1;

    private static final YamlJsonTranslator YAML_JSON_TRANSLATOR = new YamlJsonTranslator();

    @ElementCollection
    @Lob
    private Map<String, String> properties = new LinkedHashMap<>();

    @ElementCollection
    @Lob
    private Map<String, String> attributes = new LinkedHashMap<>();

    @ElementCollection
    private List<Integer> occurrences = new ArrayList<>();

    /**
     * The Default Constructor creates a {@link JpaToscaCapabilityAssignment} object with a null key.
     */
    public JpaToscaCapabilityAssignment() {
        this(new PfConceptKey());
    }

    /**
     * The Key Constructor creates a {@link JpaToscaCapabilityAssignment} object with the given concept key.
     *
     * @param key the key
     */
    public JpaToscaCapabilityAssignment(@NonNull final PfConceptKey key) {
        super(key);
    }

    /**
     * Copy constructor.
     *
     * @param copyConcept the concept to copy from
     */
    public JpaToscaCapabilityAssignment(final JpaToscaCapabilityAssignment copyConcept) {
        super(copyConcept);
        this.properties = copyConcept.properties == null ? null : new LinkedHashMap<>(copyConcept.properties);
        this.attributes = copyConcept.attributes == null ? null : new LinkedHashMap<>(copyConcept.attributes);
        this.occurrences = copyConcept.occurrences == null ? null : new ArrayList<>(copyConcept.occurrences);
    }

    /**
     * Authorative constructor.
     *
     * @param authorativeConcept the authorative concept to copy from
     */
    public JpaToscaCapabilityAssignment(@NonNull final ToscaCapabilityAssignment authorativeConcept) {
        super(new PfConceptKey());
        this.fromAuthorative(authorativeConcept);
    }

    @Override
    public ToscaCapabilityAssignment toAuthorative() {
        ToscaCapabilityAssignment toscaCapabilityAssignment = new ToscaCapabilityAssignment();
        super.setToscaEntity(toscaCapabilityAssignment);
        super.toAuthorative();

        toscaCapabilityAssignment.setProperties(
                PfUtils.mapMap(properties, property -> YAML_JSON_TRANSLATOR.fromYaml(property, Object.class)));

        toscaCapabilityAssignment.setAttributes(
                PfUtils.mapMap(attributes, attribute -> YAML_JSON_TRANSLATOR.fromYaml(attribute, Object.class)));

        toscaCapabilityAssignment.setOccurrences(PfUtils.mapList(occurrences, occurrence -> {
            if (occurrence.equals(JPA_UNBOUNDED_VALUE)) {
                return AUTHORATIVE_UNBOUNDED_LITERAL;
            } else {
                return occurrence;
            }
        }));

        return toscaCapabilityAssignment;
    }

    @Override
    public void fromAuthorative(ToscaCapabilityAssignment toscaCapabilityAssignment) {
        super.fromAuthorative(toscaCapabilityAssignment);


        properties = PfUtils.mapMap(toscaCapabilityAssignment.getProperties(), YAML_JSON_TRANSLATOR::toYaml);
        attributes = PfUtils.mapMap(toscaCapabilityAssignment.getAttributes(), YAML_JSON_TRANSLATOR::toYaml);

        occurrences = PfUtils.mapList(toscaCapabilityAssignment.getOccurrences(), occurrence -> {
            if (occurrence.equals(AUTHORATIVE_UNBOUNDED_LITERAL)) {
                return JPA_UNBOUNDED_VALUE;
            } else {
                return ((Number) occurrence).intValue();
            }
        });
    }

    @Override
    public void clean() {
        super.clean();

        properties = PfUtils.mapMap(properties, String::trim);
        attributes = PfUtils.mapMap(attributes, String::trim);
    }

    @Override
    public PfValidationResult validate(final PfValidationResult resultIn) {
        PfValidationResult result = super.validate(resultIn);

        if (properties != null) {
            result.append(validateProperties(new PfValidationResult()));
        }

        if (attributes != null) {
            result.append(validateAttributes(new PfValidationResult()));
        }

        if (occurrences != null) {
            result.append(validateOccurrences(new PfValidationResult()));
        }

        return result;
    }

    /**
     * Validate the properties.
     *
     * @param resultIn The result of validations up to now
     * @return the validation result
     */
    private PfValidationResult validateProperties(final PfValidationResult resultIn) {
        PfValidationResult result = resultIn;

        for (Entry<String, String> propertyEntry : properties.entrySet()) {
            if (propertyEntry.getValue() == null) {
                result.addValidationMessage(new PfValidationMessage(getKey(), this.getClass(), ValidationResult.INVALID,
                        "capability assignment property " + propertyEntry.getKey() + " value may not be null"));
            }
        }
        return result;
    }

    /**
     * Validate the attributes.
     *
     * @param resultIn The result of validations up to now
     * @return the validation result
     */
    private PfValidationResult validateAttributes(final PfValidationResult resultIn) {
        PfValidationResult result = resultIn;

        for (Entry<String, String> attributeEntry : attributes.entrySet()) {
            if (attributeEntry.getValue() == null) {
                result.addValidationMessage(new PfValidationMessage(getKey(), this.getClass(), ValidationResult.INVALID,
                        "capability assignment attribute " + attributeEntry.getKey() + " value may not be null"));
            }
        }
        return result;
    }

    /**
     * Validate the occurrences.
     *
     * @param resultIn The result of validations up to now
     * @return the validation result
     */
    private PfValidationResult validateOccurrences(final PfValidationResult resultIn) {
        PfValidationResult result = resultIn;

        for (Integer occurrence : occurrences) {
            if (occurrence == null) {
                result.addValidationMessage(new PfValidationMessage(getKey(), this.getClass(), ValidationResult.INVALID,
                        "capability assignment occurrence value may not be null "));
            } else if (occurrence < 0 && !occurrence.equals(JPA_UNBOUNDED_VALUE)) {
                result.addValidationMessage(new PfValidationMessage(getKey(), this.getClass(), ValidationResult.INVALID,
                        "capability assignment occurrence value may not be negative"));
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
            return getClass().getName().compareTo(otherConcept.getClass().getName());
        }

        final JpaToscaCapabilityAssignment other = (JpaToscaCapabilityAssignment) otherConcept;
        int result = super.compareTo(other);
        if (result != 0) {
            return result;
        }

        result = PfUtils.compareObjects(properties, other.properties);
        if (result != 0) {
            return result;
        }

        result = PfUtils.compareObjects(attributes, other.attributes);
        if (result != 0) {
            return result;
        }

        return PfUtils.compareObjects(occurrences, other.occurrences);
    }
}
