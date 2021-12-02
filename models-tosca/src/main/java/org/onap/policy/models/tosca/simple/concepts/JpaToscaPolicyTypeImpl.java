/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2021 Nordix Foundation.
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


import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.persistence.AttributeOverride;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.Lob;
import javax.persistence.Table;
import javax.ws.rs.core.Response;
import lombok.Data;
import lombok.NonNull;
import org.onap.policy.common.utils.coder.StandardCoder;
import org.onap.policy.models.base.PfAuthorative;
import org.onap.policy.models.base.PfConcept;
import org.onap.policy.models.base.PfConceptKey;
import org.onap.policy.models.base.PfKey;
import org.onap.policy.models.base.PfModelRuntimeException;
import org.onap.policy.models.base.PfUtils;
import org.onap.policy.models.base.validation.annotations.VerifyKey;
import org.onap.policy.models.tosca.authorative.concepts.ToscaConceptIdentifier;
import org.onap.policy.models.tosca.authorative.concepts.ToscaPolicyTypeImpl;

/**
 * Class to represent the policy type impl definition.
 */
@Entity
@Table(name = "ToscaPolicyTypeImpl")
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
@Data
public class JpaToscaPolicyTypeImpl extends PfConcept implements PfAuthorative<ToscaPolicyTypeImpl>, Serializable {

    private static final long serialVersionUID = 3856535825361196553L;

    private static final StandardCoder STANDARD_CODER = new StandardCoder();

    @EmbeddedId
    @VerifyKey
    @AttributeOverride(name = "name", column = @Column(name = "policy_impl_name"))
    @AttributeOverride(name = "version", column = @Column(name = "policy_impl_version"))
    private PfConceptKey policyTypeImplKey;

    private PfConceptKey policyTypeRef;

    private String pdpType;

    @ElementCollection
    @Lob
    private Map<String, String> policyModel;

    /**
     * The Default Constructor creates a {@link JpaToscaPolicyTypeImpl} object with a null key.
     */
    public JpaToscaPolicyTypeImpl() {
        this(new PfConceptKey());
    }

    /**
     * The Key Constructor creates a {@link JpaToscaPolicyTypeImpl} object with the given concept key.
     *
     * @param key the key
     */
    public JpaToscaPolicyTypeImpl(@NonNull final PfConceptKey key) {
        this.policyTypeImplKey = key;
    }

    /**
     * Copy constructor.
     *
     * @param copyConcept the concept to copy from
     */
    public JpaToscaPolicyTypeImpl(@NonNull final JpaToscaPolicyTypeImpl copyConcept) {
        super(copyConcept);
        this.policyTypeImplKey = copyConcept.getPolicyTypeImplKey();
        this.policyTypeRef = copyConcept.getPolicyTypeRef();
        this.pdpType = copyConcept.getPdpType();
        this.policyModel =  copyConcept.getPolicyModel();
    }

    /**
     * Authorative constructor.
     *
     * @param authorativeConcept the authorative concept to copy from
     */
    public JpaToscaPolicyTypeImpl(final ToscaPolicyTypeImpl authorativeConcept) {
        this.fromAuthorative(authorativeConcept);
    }

    /**
     *  To Authorative.
     * @return ToscaPolicyTypeIMpl
     */
    public ToscaPolicyTypeImpl toAuthorative() {
        var policyTypeImpl = new ToscaPolicyTypeImpl();
        policyTypeImpl.setPolicyTypeImplRef(new ToscaConceptIdentifier(getKey().getName(), getKey().getVersion()));

        if (getPolicyTypeRef() != null) {
            policyTypeImpl.setPolicyTypeRef(new ToscaConceptIdentifier(getPolicyTypeRef().getName(),
                getPolicyTypeRef().getVersion()));
        }

        if (getPdpType() != null) {
            policyTypeImpl.setPdpType(getPdpType());
        }

        policyTypeImpl.setPolicyModel(getPolicyModel().entrySet().stream()
            .collect(Collectors.toMap(Map.Entry::getKey, e -> (Object) e.getValue())));

        return policyTypeImpl;
    }

    /**
     * Set an instance of the persist concept to the equivalent values as the other concept.
     *
     * @param authorativeConcept the authorative concept
     */
    @Override
    public void fromAuthorative(ToscaPolicyTypeImpl authorativeConcept) {
        policyTypeImplKey = new PfConceptKey();
        pdpType = authorativeConcept.getPdpType();

        if (authorativeConcept.getPolicyTypeImplRef().getName() != null) {
            policyTypeImplKey.setName(authorativeConcept.getPolicyTypeImplRef().getName());
        } else {
            throw new PfModelRuntimeException(Response.Status.BAD_REQUEST,
                "Name not specified, the field must be specified in the key ");
        }
        if (authorativeConcept.getPolicyTypeImplRef().getVersion() != null) {
            policyTypeImplKey.setVersion(authorativeConcept.getPolicyTypeImplRef().getVersion());
        } else {
            throw new PfModelRuntimeException(Response.Status.BAD_REQUEST,
                "version not specified, the field must be specified in the key ");
        }

        if (authorativeConcept.getPolicyTypeRef() != null) {
            policyTypeRef = new PfConceptKey(authorativeConcept.getPolicyTypeRef().getName(),
                authorativeConcept.getPolicyTypeRef().getVersion());
        }

        if (authorativeConcept.getPolicyModel() != null) {
            policyModel = authorativeConcept.getPolicyModel().entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().toString()));
        }

    }


    /**
     * Gets the key of this concept.
     *
     * @return the concept key
     */
    @Override
    public PfConceptKey getKey() {
        return policyTypeImplKey;
    }

    /**
     * Gets a list of all keys for this concept and all concepts that are defined or referenced by this concept and its
     * sub-concepts.
     *
     * @return the keys used by this concept and its contained concepts
     */
    @Override
    public List<PfKey> getKeys() {
        final List<PfKey> keyList = getKey().getKeys();
        return keyList;
    }

    public void clean() {
        policyTypeImplKey.clean();
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

        @SuppressWarnings("unchecked")
        final JpaToscaPolicyTypeImpl other = (JpaToscaPolicyTypeImpl) otherConcept;

        int result = policyTypeImplKey.compareTo(other.policyTypeImplKey);
        if (result != 0) {
            return result;
        }

        return PfUtils.compareMaps(policyModel, other.policyModel);

    }
}
