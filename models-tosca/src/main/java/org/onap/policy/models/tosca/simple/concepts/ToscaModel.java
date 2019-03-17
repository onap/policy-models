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
import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;

import org.onap.policy.common.utils.validation.Assertions;
import org.onap.policy.models.base.PfConcept;
import org.onap.policy.models.base.PfConceptKey;
import org.onap.policy.models.base.PfKey;
import org.onap.policy.models.base.PfModel;
import org.onap.policy.models.base.PfModelService;
import org.onap.policy.models.base.PfValidationResult;

/**
 * A container class for a TOSCA model with multiple service templates. This class is a container
 * class that allows a model with many service templates to be constructed that contains a well
 * formed overall TOSCA model.
 *
 * <p>Validation runs {@link ToscaModel} validation on the model and all its sub concepts.
 */

@Entity
@Table(name = "ToscaModel")
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
@Data
@EqualsAndHashCode(callSuper = true)
public class ToscaModel extends PfModel {
    private static final long serialVersionUID = 8800599637708309945L;

    @OneToOne(cascade = CascadeType.ALL)
    private ToscaServiceTemplates serviceTemplates;

    /**
     * The Default Constructor creates a {@link ToscaModel} object with a null concept key and
     * creates an empty TOSCA model.
     */
    public ToscaModel() {
        this(new PfConceptKey());
    }

    /**
     * The Key Constructor creates a {@link ToscaModel} object with the given concept key and
     * creates an empty TOSCA model.
     *
     * @param key the TOSCA model key
     */
    public ToscaModel(final PfConceptKey key) {
        this(key, new ToscaServiceTemplates(new PfConceptKey()));
    }

    /**
     * Constructor that initiates a {@link ToscaModel} with all its fields.
     *
     * @param key the TOSCA model key
     * @param serviceTemplates the service templates in the event model
     */
    public ToscaModel(@NonNull final PfConceptKey key, @NonNull final ToscaServiceTemplates serviceTemplates) {
        super(key);
        this.serviceTemplates = serviceTemplates;
    }

    /**
     * Copy constructor.
     *
     * @param copyConcept the concept to copy from
     */
    public ToscaModel(@NonNull final ToscaModel copyConcept) {
        super(copyConcept);
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
    public PfValidationResult validate(@NonNull final PfValidationResult resultIn) {
        PfValidationResult result = super.validate(resultIn);

        return serviceTemplates.validate(result);
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

        final ToscaModel other = (ToscaModel) otherConcept;
        if (!super.equals(other)) {
            return super.compareTo(other);
        }

        return serviceTemplates.compareTo(other.serviceTemplates);
    }

    @Override
    public PfConcept copyTo(@NonNull final PfConcept targetObject) {
        Assertions.instanceOf(targetObject, ToscaModel.class);

        final ToscaModel copy = ((ToscaModel) targetObject);
        super.copyTo(targetObject);
        copy.setServiceTemplates(new ToscaServiceTemplates(serviceTemplates));

        return copy;
    }
}
