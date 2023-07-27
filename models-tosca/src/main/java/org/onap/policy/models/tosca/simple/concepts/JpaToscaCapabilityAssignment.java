/*-
 * ============LICENSE_START=======================================================
 * Copyright (C) 2020-2021, 2023 Nordix Foundation.
 * Modifications Copyright (C) 2020-2021 AT&T Intellectual Property. All rights reserved.
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

import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import java.io.Serial;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import org.onap.policy.common.parameters.annotations.NotNull;
import org.onap.policy.common.utils.coder.YamlJsonTranslator;
import org.onap.policy.models.base.PfConcept;
import org.onap.policy.models.base.PfConceptKey;
import org.onap.policy.models.base.PfUtils;
import org.onap.policy.models.base.validation.annotations.PfMin;
import org.onap.policy.models.tosca.authorative.concepts.ToscaCapabilityAssignment;

/**
 * Class to represent the parameter in TOSCA definition.
 */
@Entity
@Table(name = "ToscaCapabilityAssignment")
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
@Data
@EqualsAndHashCode(callSuper = false)
public class JpaToscaCapabilityAssignment extends JpaToscaWithTypeAndStringProperties<ToscaCapabilityAssignment> {

    @Serial
    private static final long serialVersionUID = 1675770231921107988L;

    private static final String AUTHORATIVE_UNBOUNDED_LITERAL = "UNBOUNDED";
    private static final Integer JPA_UNBOUNDED_VALUE = -1;

    private static final YamlJsonTranslator YAML_JSON_TRANSLATOR = new YamlJsonTranslator();

    @ElementCollection
    @Lob
    private Map<@NotNull String, @NotNull String> attributes;

    @ElementCollection
    private List<@NotNull @PfMin(value = 0, allowed = -1) Integer> occurrences;

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
        this.attributes = copyConcept.attributes == null ? null : new LinkedHashMap<>(copyConcept.attributes);
        this.occurrences = copyConcept.occurrences == null ? null : new ArrayList<>(copyConcept.occurrences);
    }

    /**
     * Authorative constructor.
     *
     * @param authorativeConcept the authorative concept to copy from
     */
    public JpaToscaCapabilityAssignment(@NonNull final ToscaCapabilityAssignment authorativeConcept) {
        super(authorativeConcept);
    }

    @Override
    public ToscaCapabilityAssignment toAuthorative() {
        var toscaCapabilityAssignment = new ToscaCapabilityAssignment();
        super.setToscaEntity(toscaCapabilityAssignment);
        super.toAuthorative();

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
    protected Object deserializePropertyValue(String propValue) {
        return YAML_JSON_TRANSLATOR.fromYaml(propValue, Object.class);
    }

    @Override
    protected String serializePropertyValue(Object propValue) {
        return YAML_JSON_TRANSLATOR.toYaml(propValue);
    }

    @Override
    public void clean() {
        super.clean();

        attributes = PfUtils.mapMap(attributes, String::trim);
    }

    @Override
    public int compareTo(final PfConcept otherConcept) {
        if (this == otherConcept) {
            return 0;
        }

        int result = super.compareTo(otherConcept);
        if (result != 0) {
            return result;
        }

        final JpaToscaCapabilityAssignment other = (JpaToscaCapabilityAssignment) otherConcept;

        result = PfUtils.compareMaps(attributes, other.attributes);
        if (result != 0) {
            return result;
        }

        return PfUtils.compareCollections(occurrences, other.occurrences);
    }
}
