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
import java.util.UUID;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import org.onap.policy.common.parameters.BeanValidationResult;
import org.onap.policy.common.utils.validation.Assertions;
import org.onap.policy.models.base.PfConcept;
import org.onap.policy.models.base.PfConceptKey;
import org.onap.policy.models.base.PfKey;

@Entity
@Table(name = "DummyConceptEntity")
@Data
@EqualsAndHashCode(callSuper = false)
public class DummyConceptEntity extends PfConcept {
    @Serial
    private static final long serialVersionUID = -2962570563281067894L;

    @EmbeddedId()
    @NonNull
    private PfConceptKey key;

    @Column
    @NonNull
    private UUID uuid;

    @Column
    @NonNull
    private String description;

    public DummyConceptEntity() {
        this.key = new PfConceptKey();
    }

    /**
     * Copy constructor.
     *
     * @param source object from which to copy
     */
    public DummyConceptEntity(DummyConceptEntity source) {
        this.key = source.key;
        this.uuid = source.uuid;
        this.description = source.description;
    }

    public DummyConceptEntity(final Double doubleValue) {
        this.key = new PfConceptKey();
    }

    public DummyConceptEntity(final PfConceptKey key, final Double doubleValue) {
        this.key = key;
    }

    /**
     * Constructor.
     *
     * @param key the key
     * @param uuid the uuid
     * @param description the description
     */
    public DummyConceptEntity(PfConceptKey key, UUID uuid, String description) {
        this.key = key;
        this.uuid = uuid;
        this.description = description;
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

        final DummyConceptEntity other = (DummyConceptEntity) otherObj;

        if (!key.equals(other.key)) {
            return key.compareTo(other.key);
        }
        if (!uuid.equals(other.uuid)) {
            return uuid.compareTo(other.uuid);
        }
        return description.compareTo(other.description);
    }
}
