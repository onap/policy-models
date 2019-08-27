/*-
 * ============LICENSE_START=======================================================
 * ONAP Policy Model
 * ================================================================================
 * Copyright (C) 2019 AT&T Intellectual Property. All rights reserved.
 * Modifications Copyright (C) 2019 Nordix Foundation.
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
import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import org.apache.commons.lang3.ObjectUtils;
import org.onap.policy.models.base.PfConcept;
import org.onap.policy.models.base.PfConceptKey;
import org.onap.policy.models.base.PfKey;
import org.onap.policy.models.base.PfReferenceKey;
import org.onap.policy.models.base.PfValidationMessage;
import org.onap.policy.models.base.PfValidationResult;
import org.onap.policy.models.base.PfValidationResult.ValidationResult;

/**
 * Class to represent the EventFilter in TOSCA definition.
 *
 * @author Chenfei Gao (cgao@research.att.com)
 * @author Liam Fallon (liam.fallon@est.tech)
 */
@Entity
@Table(name = "ToscaEventFilter")
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
@Data
@EqualsAndHashCode(callSuper = false)
public class JpaToscaEventFilter extends PfConcept {
    private static final long serialVersionUID = 8769020537228210247L;

    @EmbeddedId
    private PfReferenceKey key;

    @Column
    private PfConceptKey node;

    @Column
    private String requirement;

    @Column
    private String capability;

    /**
     * The Default Constructor creates a {@link JpaToscaEventFilter} object with a null key.
     */
    public JpaToscaEventFilter() {
        this(new PfReferenceKey());
    }

    /**
     * The Key Constructor creates a {@link JpaToscaEventFilter} object with the given concept key.
     *
     * @param key the key
     */
    public JpaToscaEventFilter(@NonNull final PfReferenceKey key) {
        this(key, new PfConceptKey());
    }

    /**
     * The full Constructor creates a {@link JpaToscaEventFilter} object with the given concept key and node.
     *
     * @param key the key
     */
    public JpaToscaEventFilter(@NonNull final PfReferenceKey key, @NonNull final PfConceptKey node) {
        this.key = key;
        this.node = node;
    }

    /**
     * Copy constructor.
     *
     * @param copyConcept the concept to copy from
     */
    public JpaToscaEventFilter(final JpaToscaEventFilter copyConcept) {
        super(copyConcept);
        this.key = new PfReferenceKey(copyConcept.key);
        this.node = new PfConceptKey(copyConcept.node);
        this.requirement = copyConcept.requirement;
        this.capability = copyConcept.capability;
    }

    @Override
    public List<PfKey> getKeys() {
        final List<PfKey> keyList = getKey().getKeys();
        keyList.addAll(node.getKeys());
        return keyList;
    }

    @Override
    public void clean() {
        key.clean();
        node.clean();

        requirement = (requirement != null ? requirement.trim() : requirement);
        capability = (capability != null ? capability.trim() : capability);
    }

    @Override
    public PfValidationResult validate(@NonNull final PfValidationResult resultIn) {
        PfValidationResult result = resultIn;

        if (key.isNullKey()) {
            result.addValidationMessage(
                    new PfValidationMessage(key, this.getClass(), ValidationResult.INVALID, "key is a null key"));
        }

        result = key.validate(result);

        if (node == null || node.isNullKey()) {
            result.addValidationMessage(new PfValidationMessage(key, this.getClass(), ValidationResult.INVALID,
                    "node on an event filter may not be null"));
        }

        if (requirement != null && requirement.trim().length() == 0) {
            result.addValidationMessage(new PfValidationMessage(key, this.getClass(), ValidationResult.INVALID,
                    "event filter requirement may not be blank"));
        }

        if (capability != null && capability.trim().length() == 0) {
            result.addValidationMessage(new PfValidationMessage(key, this.getClass(), ValidationResult.INVALID,
                    "event filter capability may not be blank"));
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

        final JpaToscaEventFilter other = (JpaToscaEventFilter) otherConcept;
        if (!key.equals(other.key)) {
            return key.compareTo(other.key);
        }

        if (!node.equals(other.node)) {
            return node.compareTo(other.node);
        }

        int result = ObjectUtils.compare(requirement, other.requirement);
        if (result != 0) {
            return result;
        }

        return ObjectUtils.compare(capability, other.capability);
    }
}