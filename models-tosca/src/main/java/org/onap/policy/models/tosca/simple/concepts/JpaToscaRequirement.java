/*-
 * ============LICENSE_START=======================================================
 * ONAP Requirement Model
 * ================================================================================
 * Copyright (C) 2019-2021 AT&T Intellectual Property. All rights reserved.
 * Modifications Copyright (C) 2019-2021, 2023 Nordix Foundation.
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

import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.Table;
import java.io.Serial;
import java.util.ArrayList;
import java.util.List;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import org.onap.policy.common.parameters.annotations.NotNull;
import org.onap.policy.common.utils.coder.YamlJsonTranslator;
import org.onap.policy.models.base.PfConcept;
import org.onap.policy.models.base.PfConceptKey;
import org.onap.policy.models.base.PfUtils;
import org.onap.policy.models.base.validation.annotations.PfMin;
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
public class JpaToscaRequirement extends JpaToscaWithTypeAndStringProperties<ToscaRequirement> {

    @Serial
    private static final long serialVersionUID = 2785481541573683089L;
    private static final String AUTHORATIVE_UNBOUNDED_LITERAL = "UNBOUNDED";
    private static final Integer JPA_UNBOUNDED_VALUE = -1;
    private static final YamlJsonTranslator YAML_TRANSLATOR = new YamlJsonTranslator();

    @Column
    private String capability;

    @Column
    private String node;

    @Column
    private String relationship;

    @ElementCollection
    private List<@NotNull @PfMin(value = 0, allowed = -1) Integer> occurrences;

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
    }

    /**
     * Authorative constructor.
     *
     * @param authorativeConcept the authorative concept to copy from
     */
    public JpaToscaRequirement(final ToscaRequirement authorativeConcept) {
        super(authorativeConcept);
    }

    @Override
    public ToscaRequirement toAuthorative() {
        var toscaRequirement = new ToscaRequirement();
        super.setToscaEntity(toscaRequirement);
        super.toAuthorative();

        toscaRequirement.setCapability(capability);
        toscaRequirement.setNode(node);
        toscaRequirement.setRelationship(relationship);

        if (occurrences != null) {
            List<Object> occurrencesList = new ArrayList<>(occurrences);
            for (Integer occurrence : occurrences) {
                if (JPA_UNBOUNDED_VALUE.equals(occurrence)) {
                    occurrencesList.add(AUTHORATIVE_UNBOUNDED_LITERAL);
                } else {
                    occurrencesList.add(occurrence);
                }
            }
            toscaRequirement.setOccurrences(occurrencesList);
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
                    occurrences.add(((Number) occurrence).intValue());
                }
            }
        }
    }

    @Override
    protected Object deserializePropertyValue(String propValue) {
        return YAML_TRANSLATOR.fromYaml(propValue, Object.class);
    }

    @Override
    protected String serializePropertyValue(Object propValue) {
        return YAML_TRANSLATOR.toYaml(propValue);
    }

    @Override
    public void clean() {
        super.clean();

        capability = capability.trim();
        node = node.trim();
        relationship = relationship.trim();
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

        final JpaToscaRequirement other = (JpaToscaRequirement) otherConcept;

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

        return PfUtils.compareCollections(occurrences, other.occurrences);
    }
}
