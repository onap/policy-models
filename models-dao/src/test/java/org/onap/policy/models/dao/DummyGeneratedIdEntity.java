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

package org.onap.policy.models.dao;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import org.onap.policy.common.parameters.BeanValidationResult;
import org.onap.policy.models.base.PfConcept;
import org.onap.policy.models.base.PfGeneratedIdKey;
import org.onap.policy.models.base.PfKey;


@Entity
@Table(name = "DummyGeneratedIdEntity")
@Data
@EqualsAndHashCode(callSuper = false)
public class DummyGeneratedIdEntity extends PfConcept {
    private static final long serialVersionUID = -2962570563281067896L;

    @EmbeddedId()
    @NonNull
    private PfGeneratedIdKey key;

    @Column(precision = 3)
    @Temporal(TemporalType.TIMESTAMP)
    private Date timeStamp;

    @Column
    private double doubleValue;

    /**
     * Default constructor.
     */
    public DummyGeneratedIdEntity() {
        this.key = new PfGeneratedIdKey();
        this.timeStamp = new Date();
    }

    public DummyGeneratedIdEntity(DummyGeneratedIdEntity source) {
        this.key = source.key;
        this.timeStamp = source.timeStamp;
    }

    /**
     * Constructor.
     *
     * @param key the key
     * @param timeStamp the date value
     */
    public DummyGeneratedIdEntity(final PfGeneratedIdKey key, final Date timeStamp) {
        this.key = key;
        this.timeStamp = timeStamp;
    }

    /**
     * Constructor.
     *
     * @param key the key
     * @param timeStamp the date value
     * @param doubleValue the double value     *
     */
    public DummyGeneratedIdEntity(final PfGeneratedIdKey key, final Date timeStamp, final double doubleValue) {
        this.key = key;
        this.timeStamp = timeStamp;
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
    public int compareTo(@NonNull final PfConcept otherObj) {
        if (this == otherObj) {
            return 0;
        }

        if (getClass() != otherObj.getClass()) {
            return this.getClass().getName().compareTo(otherObj.getClass().getName());
        }

        final DummyGeneratedIdEntity other = (DummyGeneratedIdEntity) otherObj;
        if (this.timeStamp != other.timeStamp) {
            return  timeStamp.compareTo(other.timeStamp);
        }
        return  Double.compare(doubleValue, other.doubleValue);
    }

}
