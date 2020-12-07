/*-
 * ============LICENSE_START=======================================================
 * ONAP Requirement Model
 * ================================================================================
 * Copyright (C) 2019-2020 AT&T Intellectual Property. All rights reserved.
 * Modifications Copyright (C) 2019-2020 Nordix Foundation.
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
import javax.persistence.Column;
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
import org.onap.policy.models.base.PfKey;
import org.onap.policy.models.base.PfUtils;
import org.onap.policy.models.base.PfValidationMessage;
import org.onap.policy.models.base.PfValidationResult;
import org.onap.policy.models.tosca.authorative.concepts.ToscaRequirement;

/**
 * Class to represent the requirement in TOSCA definition.
 *
 * @author Chenfei Gao (cgao@research.att.com)
 * @author Liam Fallon (liam.fallon@est.tech)
 */
@Entity
@Table(name = "ToscaRequirement")
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
@Data
@EqualsAndHashCode(callSuper = true)
public class JpaToscaRequirement extends JpaToscaEntityType<ToscaRequirement>
        implements PfAuthorative<ToscaRequirement> {

    private static final long serialVersionUID = 2785481541573683089L;
    private static final String AUTHORATIVE_UNBOUNDED_LITERAL = "UNBOUNDED";
    private static final Double JPA_UNBOUNDED_VALUE = -1.0;

    @Column
    private String capability;

    @Column
    private String node;

    @Column
    private String relationship;

    @ElementCollection
    private List<Double> occurrences;

    @ElementCollection
    @Lob
    private Map<String, String> properties;

    /**
     * The Default Constructor creates a {@link JpaToscaRequirement} object with a null key.
     */
    public JpaToscaRequirement() {
        this(new PfConceptKey());
    }

    /**
     * The Key Constructor creates a {@link JpaToscaRequirement} object with the given concept key.
     *
     * @param key the key
     */
    public JpaToscaRequirement(@NonNull final PfConceptKey key) {
        super(key);
    }

    /**
     * Copy constructor.
     *
     * @param copyConcept the concept to copy from
     */
    public JpaToscaRequirement(@NonNull final JpaToscaRequirement copyConcept) {
        super(copyConcept);
        this.capability = copyConcept.capability;
        this.node = copyConcept.node;
        this.relationship = copyConcept.relationship;
        this.occurrences = new ArrayList<>(copyConcept.occurrences);
        this.properties = PfUtils.mapMap(copyConcept.properties, String::new);
    }

    /**
     * Authorative constructor.
     *
     * @param authorativeConcept the authorative concept to copy from
     */
    public JpaToscaRequirement(final ToscaRequirement authorativeConcept) {
        super(new PfConceptKey());
        this.fromAuthorative(authorativeConcept);
    }

    @Override
    public ToscaRequirement toAuthorative() {
        ToscaRequirement toscaRequirement = new ToscaRequirement();
        super.setToscaEntity(toscaRequirement);
        super.toAuthorative();

        toscaRequirement.setCapability(capability);
        toscaRequirement.setNode(node);
        toscaRequirement.setRelationship(relationship);

        if (occurrences != null) {
            List<Object> occurrencesList = new ArrayList<>(occurrences);
            for (Double occurrence : occurrences) {
                if (occurrence == JPA_UNBOUNDED_VALUE) {
                    occurrencesList.add(AUTHORATIVE_UNBOUNDED_LITERAL);
                } else {
                    occurrencesList.add(occurrence.doubleValue());
                }
            }
            toscaRequirement.setOccurrences(occurrencesList);
        }

        if (properties != null) {
            Map<String, Object> propertiesMap = new LinkedHashMap<>();

            for (Map.Entry<String, String> entry : properties.entrySet()) {
                propertiesMap.put(entry.getKey(), new YamlJsonTranslator().fromYaml(entry.getValue(), Object.class));
            }

            toscaRequirement.setProperties(propertiesMap);
        }

        return toscaRequirement;
    }

    @Override
    public void fromAuthorative(@NonNull final ToscaRequirement toscaRequirement) {
        super.fromAuthorative(toscaRequirement);

        capability = toscaRequirement.getCapability();
        node = toscaRequirement.getNode();
        relationship = toscaRequirement.getRelationship();

        if (toscaRequirement.getOccurrences() != null) {
            occurrences = new ArrayList<>();
            for (Object occurrence : toscaRequirement.getOccurrences()) {
                if (occurrence.equals(AUTHORATIVE_UNBOUNDED_LITERAL)) {
                    occurrences.add(JPA_UNBOUNDED_VALUE);
                } else {
                    occurrences.add((Double) occurrence);
                }
            }
        }

        if (toscaRequirement.getProperties() != null) {
            properties = new LinkedHashMap<>();
            for (Map.Entry<String, Object> toscaPropertyEntry : toscaRequirement.getProperties().entrySet()) {
                String jpaProperty = new YamlJsonTranslator().toYaml(toscaPropertyEntry.getValue());
                properties.put(toscaPropertyEntry.getKey(), jpaProperty);
            }
        }

    }

    @Override
    public List<PfKey> getKeys() {
        return super.getKeys();
    }

    @Override
    public void clean() {
        super.clean();

        capability = capability.trim();
        node = node.trim();
        relationship = relationship.trim();

        properties = PfUtils.mapMap(properties, String::trim);
    }

    @Override
    public PfValidationResult validate(@NonNull final PfValidationResult resultIn) {
        PfValidationResult result = super.validate(resultIn);

        if (properties != null) {
            result = validateProperties(result);
        }

        if (occurrences != null) {
            result = validateOccurrences(result);
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

        for (String property : properties.values()) {
            if (property == null) {
                result.addValidationMessage(new PfValidationMessage(getKey(), this.getClass(),
                        PfValidationResult.ValidationResult.INVALID, "topology template property may not be null "));
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

        for (Double occurrence : occurrences) {
            if (occurrence == null) {
                result.addValidationMessage(new PfValidationMessage(getKey(), this.getClass(),
                        PfValidationResult.ValidationResult.INVALID, "requirement occurrence value may not be null "));
            }
            if (occurrence < -1.0) {
                result.addValidationMessage(
                        new PfValidationMessage(getKey(), this.getClass(), PfValidationResult.ValidationResult.INVALID,
                                "requirement occurrence value may not be negative"));
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

        final JpaToscaRequirement other = (JpaToscaRequirement) otherConcept;
        int result = super.compareTo(other);
        if (result != 0) {
            return result;
        }

        result = capability.compareTo(other.capability);
        if (result != 0) {
            return result;
        }

        result = node.compareTo(other.node);
        if (result != 0) {
            return result;
        }

        result = relationship.compareTo(other.relationship);
        if (result != 0) {
            return result;
        }

        result = PfUtils.compareCollections(occurrences, other.occurrences);
        if (result != 0) {
            return result;
        }

        return PfUtils.compareMaps(properties, other.properties);
    }
}
