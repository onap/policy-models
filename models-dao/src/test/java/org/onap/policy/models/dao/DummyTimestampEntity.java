/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2020 Nordix Foundation.
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

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Table;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;

import org.onap.policy.common.utils.validation.Assertions;
import org.onap.policy.models.base.PfConcept;
import org.onap.policy.models.base.PfKey;
import org.onap.policy.models.base.PfTimestampKey;
import org.onap.policy.models.base.PfValidationResult;

@Entity
@Table(name = "DummyTimestampEntity")
@Data
@EqualsAndHashCode(callSuper = false)
public class DummyTimestampEntity extends PfConcept {
    private static final long serialVersionUID = -2962570563281067895L;

    @EmbeddedId()
    @NonNull
    private PfTimestampKey key;

    @Column
    private double doubleValue;

    /**
     * Default constructor.
     */
    public DummyTimestampEntity() {
        this.key = new PfTimestampKey();
        this.doubleValue = 123.45;
    }

    public DummyTimestampEntity(DummyTimestampEntity source) {
        this.key = source.key;
        this.doubleValue = source.doubleValue;
    }

    /**
     * Constructor.
     *
     * @param key the key
     * @param doubleValue the double value
     */
    public DummyTimestampEntity(final PfTimestampKey key, final double doubleValue) {
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
    public PfValidationResult validate(final PfValidationResult result) {
        return key.validate(result);
    }

    @Override
    public void clean() {
        key.clean();
    }


    @Override
    public int compareTo(@NonNull final PfConcept otherObj) {
        if (this == otherObj) {
            return 0;
        }
        if (getClass() != otherObj.getClass()) {
            return this.getClass().getName().compareTo(otherObj.getClass().getName());
        }

        final DummyTimestampEntity other = (DummyTimestampEntity) otherObj;

        return Double.compare(doubleValue, other.doubleValue);
    }
}
