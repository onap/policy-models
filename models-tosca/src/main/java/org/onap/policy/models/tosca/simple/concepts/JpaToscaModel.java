/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2019-2020, 2023 Nordix Foundation.
 *  Modifications Copyright (C) 2019-2020 AT&T Intellectual Property. All rights reserved.
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

package org.onap.policy.models.tosca.simple.concepts;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinColumns;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.util.List;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import org.onap.policy.common.parameters.annotations.Valid;
import org.onap.policy.models.base.PfConcept;
import org.onap.policy.models.base.PfConceptKey;
import org.onap.policy.models.base.PfKey;
import org.onap.policy.models.base.PfModel;
import org.onap.policy.models.base.PfModelService;

/**
 * A container class for a TOSCA model with multiple service templates. This class is a container class that allows a
 * model with many service templates to be constructed that contains a well-formed overall TOSCA model.
 *
 * <p>Validation runs {@link JpaToscaModel} validation on the model and all its sub concepts.
 */

@Entity
@Table(name = "ToscaModel")
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
@Data
@EqualsAndHashCode(callSuper = true)
public class JpaToscaModel extends PfModel {
    private static final long serialVersionUID = 8800599637708309945L;

    @OneToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "serviceTemplatesName", referencedColumnName = "name")
    @JoinColumn(name = "serviceTemplatesVersion", referencedColumnName = "version")
    @Valid
    private JpaToscaServiceTemplates serviceTemplates;

    /**
     * The Default Constructor creates a {@link JpaToscaModel} object with a null concept key and creates an empty TOSCA
     * model.
     */
    public JpaToscaModel() {
        this(new PfConceptKey());
    }

    /**
     * The Key Constructor creates a {@link JpaToscaModel} object with the given concept key and creates an empty TOSCA
     * model.
     *
     * @param key the TOSCA model key
     */
    public JpaToscaModel(final PfConceptKey key) {
        this(key, new JpaToscaServiceTemplates(new PfConceptKey()));
    }

    /**
     * Constructor that initiates a {@link JpaToscaModel} with all its fields.
     *
     * @param key the TOSCA model key
     * @param serviceTemplates the service templates in the event model
     */
    public JpaToscaModel(@NonNull final PfConceptKey key, @NonNull final JpaToscaServiceTemplates serviceTemplates) {
        super(key);
        this.serviceTemplates = serviceTemplates;
    }

    /**
     * Copy constructor.
     *
     * @param copyConcept the concept to copy from
     */
    public JpaToscaModel(@NonNull final JpaToscaModel copyConcept) {
        super(copyConcept);
        this.serviceTemplates = new JpaToscaServiceTemplates(copyConcept.serviceTemplates);
    }

    @Override
    public void register() {
        PfModelService.registerModel(serviceTemplates.getId(), getServiceTemplates());
    }

    @Override
    public List<PfKey> getKeys() {
        final List<PfKey> keyList = super.getKeys();

        keyList.addAll(serviceTemplates.getKeys());

        return keyList;
    }

    @Override
    public void clean() {
        super.clean();
        serviceTemplates.clean();
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

        final JpaToscaModel other = (JpaToscaModel) otherConcept;
        int result = super.compareTo(other);
        if (result != 0) {
            return result;
        }

        return serviceTemplates.compareTo(other.serviceTemplates);
    }
}
