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

package org.onap.policy.models.tosca.concepts;

import com.google.gson.annotations.SerializedName;

import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;

import org.apache.commons.lang3.ObjectUtils;
import org.onap.policy.common.utils.validation.Assertions;
import org.onap.policy.common.utils.validation.ParameterValidationUtils;
import org.onap.policy.models.base.PfConcept;
import org.onap.policy.models.base.PfConceptKey;
import org.onap.policy.models.base.PfKey;
import org.onap.policy.models.base.PfValidationMessage;
import org.onap.policy.models.base.PfValidationResult;
import org.onap.policy.models.base.PfValidationResult.ValidationResult;

/**
 * This class holds a full TOSCA service template. Note: Only the policy specific parts of the TOSCA
 * service template are implemented.
 *
 * @author Liam Fallon (liam.fallon@est.tech)
 */
@Entity
@Table(name = "ToscaServiceTemplate")
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
@Data
@EqualsAndHashCode(callSuper = true)
public class ToscaServiceTemplate extends ToscaEntityType {
    private static final long serialVersionUID = 8084846046148349401L;

    @Column
    @SerializedName("tosca_definitions_version")
    private String toscaDefinitionsVersion;

    @OneToOne(cascade = CascadeType.ALL)
    @SerializedName("data_types")
    private ToscaDataTypes dataTypes;

    @OneToOne(cascade = CascadeType.ALL)
    @SerializedName("policy_types")
    private ToscaPolicyTypes policyTypes;

    @SerializedName("topology_template")
    private ToscaTopologyTemplate topologyTemplate;


    /**
     * The Default Constructor creates a {@link ToscaServiceTemplate} object with a null key.
     */
    public ToscaServiceTemplate() {
        this(new PfConceptKey());
    }

    /**
     * The Key Constructor creates a {@link ToscaServiceTemplate} object with the given concept key.
     *
     * @param key the key
     */
    public ToscaServiceTemplate(@NonNull final PfConceptKey key) {
        this(key, "");
    }

    /**
     * The full constructor creates a {@link ToscaServiceTemplate} object with all mandatory
     * parameters.
     *
     * @param key the key
     * @param toscaDefinitionsVersion the TOSCA version string
     */
    public ToscaServiceTemplate(@NonNull final PfConceptKey key, @NonNull final String toscaDefinitionsVersion) {
        super(key);
        this.toscaDefinitionsVersion = toscaDefinitionsVersion;
    }

    /**
     * Copy constructor.
     *
     * @param copyConcept the concept to copy from
     */
    public ToscaServiceTemplate(final ToscaServiceTemplate copyConcept) {
        super(copyConcept);
    }

    @Override
    public List<PfKey> getKeys() {
        final List<PfKey> keyList = super.getKeys();

        if (dataTypes != null) {
            keyList.addAll(dataTypes.getKeys());
        }

        if (policyTypes != null) {
            keyList.addAll(policyTypes.getKeys());
        }

        if (topologyTemplate != null) {
            keyList.addAll(topologyTemplate.getKeys());
        }

        return keyList;
    }

    @Override
    public void clean() {
        toscaDefinitionsVersion = toscaDefinitionsVersion.trim();

        if (dataTypes != null) {
            dataTypes.clean();
        }

        if (policyTypes != null) {
            policyTypes.clean();
        }

        if (topologyTemplate != null) {
            topologyTemplate.clean();
        }
    }

    @Override
    public PfValidationResult validate(final PfValidationResult resultIn) {
        PfValidationResult result = super.validate(resultIn);

        if (!ParameterValidationUtils.validateStringParameter(toscaDefinitionsVersion)) {
            result.addValidationMessage(new PfValidationMessage(getKey(), this.getClass(), ValidationResult.INVALID,
                    "service template tosca definitions version may not be null"));
        }

        if (dataTypes != null) {
            result = dataTypes.validate(result);
        }

        if (policyTypes != null) {
            result = policyTypes.validate(result);
        }

        return (topologyTemplate != null ? topologyTemplate.validate(result) : result);
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

        final ToscaServiceTemplate other = (ToscaServiceTemplate) otherConcept;
        if (!super.equals(other)) {
            return super.compareTo(other);
        }

        int result = ObjectUtils.compare(toscaDefinitionsVersion, other.toscaDefinitionsVersion);
        if (result != 0) {
            return result;
        }

        result = ObjectUtils.compare(dataTypes, other.dataTypes);
        if (result != 0) {
            return result;
        }

        result = ObjectUtils.compare(policyTypes, other.policyTypes);
        if (result != 0) {
            return result;
        }

        return ObjectUtils.compare(topologyTemplate, other.topologyTemplate);
    }

    @Override
    public PfConcept copyTo(@NonNull PfConcept target) {
        final Object copyObject = target;
        Assertions.instanceOf(copyObject, PfConcept.class);

        final ToscaServiceTemplate copy = ((ToscaServiceTemplate) copyObject);
        super.copyTo(target);
        copy.setToscaDefinitionsVersion(toscaDefinitionsVersion);

        copy.setDataTypes(dataTypes != null ? new ToscaDataTypes(dataTypes) : null);
        copy.setPolicyTypes(policyTypes != null ? new ToscaPolicyTypes(policyTypes) : null);
        copy.setTopologyTemplate(topologyTemplate != null ? new ToscaTopologyTemplate(topologyTemplate) : null);

        return copy;
    }
}
