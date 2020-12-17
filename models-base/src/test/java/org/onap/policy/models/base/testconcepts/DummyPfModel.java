/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2019 Nordix Foundation.
 *  Modifications Copyright (C) 2019-2020 AT&T Intellectual Property. All rights reserved.
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

package org.onap.policy.models.base.testconcepts;

import java.util.ArrayList;
import java.util.List;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import org.onap.policy.common.parameters.BeanValidationResult;
import org.onap.policy.models.base.PfConcept;
import org.onap.policy.models.base.PfConceptKey;
import org.onap.policy.models.base.PfKey;
import org.onap.policy.models.base.PfModel;
import org.onap.policy.models.base.PfUtils;

@Data
@EqualsAndHashCode(callSuper = true)
public class DummyPfModel extends PfModel {
    private static final long serialVersionUID = 8800599637708309945L;

    private List<PfKey> keyList;

    /**
     * The Default Constructor creates a {@link DummyPfModel} object with a null concept key and
     * creates an empty TOSCA model.
     */
    public DummyPfModel() {
        super();
        super.setKey(new PfConceptKey());
        this.keyList = new ArrayList<>();
    }

    /**
     * The Key Constructor creates a {@link DummyPfModel} object with the given concept key and
     * creates an empty TOSCA model.
     *
     * @param key the TOSCA model key
     */
    public DummyPfModel(final PfConceptKey key) {
        super(key);
        this.keyList = new ArrayList<>();
    }

    /**
     * Constructor that initiates a {@link ToscaModel} with all its fields.
     *
     * @param key the TOSCA model key
     * @param keyList the service templates in the event model
     */
    public DummyPfModel(final PfConceptKey key, final List<PfKey> keyList) {
        super(key);
        this.keyList = keyList;
    }

    /**
     * Copy constructor.
     *
     * @param copyConcept the concept to copy from
     */
    public DummyPfModel(final DummyPfModel copyConcept) {
        super(copyConcept);

        this.keyList = new ArrayList<>();
        for (final PfKey pfKey : copyConcept.keyList) {
            keyList.add(PfUtils.makeCopy(pfKey));
        }
    }

    @Override
    public void register() {
        // nothing to do
    }

    @Override
    public List<PfKey> getKeys() {
        final List<PfKey> listOfKeys = super.getKeys();

        listOfKeys.addAll(keyList);

        return listOfKeys;
    }

    @Override
    public void clean() {
        super.clean();
        for (PfKey pfKey : keyList) {
            pfKey.clean();
        }
    }

    @Override
    public BeanValidationResult validate(@NonNull String fieldName) {
        BeanValidationResult result = super.validate(fieldName);

        int count = 0;
        for (PfKey pfKey : keyList) {
            result.addResult(pfKey.validate("keyList." + (count++)));
        }

        return result;
    }

    @Override
    public int compareTo(final PfConcept otherConcept) {
        if (super.compareTo(otherConcept) != 0) {
            return super.compareTo(otherConcept);
        }

        if (otherConcept == null) {
            return -1;
        }

        if (this == otherConcept) {
            return 0;
        }

        if (getClass() != otherConcept.getClass()) {
            return this.hashCode() - otherConcept.hashCode();
        }

        final DummyPfModel other = (DummyPfModel) otherConcept;
        if (!super.equals(other)) {
            return super.compareTo(other);
        }

        if (!keyList.equals(other.keyList)) {
            return (keyList.hashCode() - other.keyList.hashCode());
        }

        return 0;
    }
}
