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

package org.onap.policy.models.tosca.simple.concepts;

import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;

import org.apache.commons.lang3.ObjectUtils;
import org.onap.policy.common.utils.validation.Assertions;
import org.onap.policy.models.base.PfAuthorative;
import org.onap.policy.models.base.PfConcept;
import org.onap.policy.models.base.PfKey;
import org.onap.policy.models.base.PfReferenceKey;
import org.onap.policy.models.base.PfValidationMessage;
import org.onap.policy.models.base.PfValidationResult;
import org.onap.policy.models.base.PfValidationResult.ValidationResult;
import org.onap.policy.models.tosca.authorative.concepts.ToscaTopologyTemplate;

/**
 * This class holds a TOSCA topology template. Note: Only the policy specific parts of the TOSCA topology template are
 * implemented.
 *
 * @author Liam Fallon (liam.fallon@est.tech)
 */
@Entity
@Table(name = "ToscaTopologyTemplate")
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
@Data
@EqualsAndHashCode(callSuper = false)
public class JpaToscaTopologyTemplate extends PfConcept implements PfAuthorative<ToscaTopologyTemplate> {
    private static final long serialVersionUID = 8969698734673232603L;

    public static final String DEFAULT_LOCAL_NAME = "ToscaTopologyTemplateSimple";

    @EmbeddedId
    private PfReferenceKey key;

    @Column(name = "description")
    private String description;

    @OneToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
    private JpaToscaPolicies policies;

    /**
     * The Default Constructor creates a {@link JpaToscaTopologyTemplate} object with a null key.
     */
    public JpaToscaTopologyTemplate() {
        this(new PfReferenceKey(JpaToscaServiceTemplate.DEFAULT_NAME, JpaToscaServiceTemplate.DEFAULT_VERSION,
                DEFAULT_LOCAL_NAME));
    }

    /**
     * The Key Constructor creates a {@link JpaToscaTopologyTemplate} object with the given concept
     * key.
     *
     * @param key the key
     */
    public JpaToscaTopologyTemplate(@NonNull final PfReferenceKey key) {
        this.key = key;
    }

    /**
     * Copy constructor.
     *
     * @param copyConcept the concept to copy from
     */
    public JpaToscaTopologyTemplate(final JpaToscaTopologyTemplate copyConcept) {
        super(copyConcept);
    }

    /**
     * Authorative constructor.
     *
     * @param authorativeConcept the authorative concept to copy from
     */
    public JpaToscaTopologyTemplate(final ToscaTopologyTemplate authorativeConcept) {
        this.fromAuthorative(authorativeConcept);
    }

    @Override
    public ToscaTopologyTemplate toAuthorative() {
        final ToscaTopologyTemplate toscaTopologyTemplate = new ToscaTopologyTemplate();

        toscaTopologyTemplate.setDescription(description);

        if (policies != null) {
            toscaTopologyTemplate.setPolicies(policies.toAuthorative());
        }

        return toscaTopologyTemplate;
    }

    @Override
    public void fromAuthorative(ToscaTopologyTemplate toscaTopologyTemplate) {
        description = toscaTopologyTemplate.getDescription();

        if (toscaTopologyTemplate.getPolicies() != null) {
            policies = new JpaToscaPolicies();
            policies.fromAuthorative(toscaTopologyTemplate.getPolicies());
        }
    }

    @Override
    public List<PfKey> getKeys() {
        final List<PfKey> keyList = getKey().getKeys();

        if (policies != null) {
            keyList.addAll(policies.getKeys());
        }

        return keyList;
    }

    @Override
    public void clean() {
        key.clean();

        description = (description != null ? description.trim() : null);

        if (policies != null) {
            policies.clean();
        }
    }

    @Override
    public PfValidationResult validate(PfValidationResult resultIn) {
        PfValidationResult result = resultIn;

        if (key.isNullKey()) {
            result.addValidationMessage(
                    new PfValidationMessage(key, this.getClass(), ValidationResult.INVALID, "key is a null key"));
        }

        result = key.validate(result);

        if (description != null && description.trim().length() == 0) {
            result.addValidationMessage(new PfValidationMessage(key, this.getClass(), ValidationResult.INVALID,
                    "property description may not be blank"));
        }

        if (policies != null) {
            result = policies.validate(result);
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

        final JpaToscaTopologyTemplate other = (JpaToscaTopologyTemplate) otherConcept;
        if (!key.equals(other.key)) {
            return key.compareTo(other.key);
        }

        int result = ObjectUtils.compare(description, other.description);
        if (result != 0) {
            return result;
        }

        return ObjectUtils.compare(policies, other.policies);
    }

    @Override
    public PfConcept copyTo(@NonNull PfConcept target) {
        final Object copyObject = target;
        Assertions.instanceOf(copyObject, PfConcept.class);

        final JpaToscaTopologyTemplate copy = ((JpaToscaTopologyTemplate) copyObject);
        copy.setKey(new PfReferenceKey(key));
        copy.setDescription(description);
        copy.setPolicies(policies != null ? new JpaToscaPolicies(policies) : null);

        return copy;
    }
}
