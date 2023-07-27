/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2019, 2023 Nordix Foundation.
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

package org.onap.policy.models.dao;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.io.Serial;
import java.util.ArrayList;
import java.util.List;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import org.onap.policy.common.parameters.BeanValidationResult;
import org.onap.policy.common.utils.validation.Assertions;
import org.onap.policy.models.base.PfConcept;
import org.onap.policy.models.base.PfKey;
import org.onap.policy.models.base.PfReferenceKey;

@Entity
@Table(name = "DummyReferenceEntity")
@Data
@EqualsAndHashCode(callSuper = false)
public class DummyReferenceEntity extends PfConcept {
    @Serial
    private static final long serialVersionUID = -2962570563281067894L;

    @EmbeddedId()
    @NonNull
    private PfReferenceKey key;

    @Column
    private double doubleValue;

    /**
     * Default constructor.
     */
    public DummyReferenceEntity() {
        this.key = new PfReferenceKey();
        this.doubleValue = 123.45;
    }

    public DummyReferenceEntity(DummyReferenceEntity source) {
        this.key = source.key;
        this.doubleValue = source.doubleValue;
    }

    /**
     * Constructor.
     *
     * @param key the key
     * @param doubleValue the double value
     */
    public DummyReferenceEntity(final PfReferenceKey key, final double doubleValue) {
        this.key = key;
        this.doubleValue = doubleValue;
    }

    @Override
    public List<PfKey> getKeys() {
        final List<PfKey> keyList = new ArrayList<>();
        keyList.add(getKey());
        return keyList;
    }

    @Override
    public BeanValidationResult validate(@NonNull String fieldName) {
        BeanValidationResult result = new BeanValidationResult(fieldName, this);
        result.addResult(key.validate("key"));
        return result;
    }

    @Override
    public void clean() {
        key.clean();
    }


    @Override
    public int compareTo(final PfConcept otherObj) {
        Assertions.argumentNotNull(otherObj, "comparison object may not be null");

        if (this == otherObj) {
            return 0;
        }
        if (getClass() != otherObj.getClass()) {
            return this.hashCode() - otherObj.hashCode();
        }

        final DummyReferenceEntity other = (DummyReferenceEntity) otherObj;

        return Double.compare(doubleValue, other.doubleValue);
    }
}
