/*-
 * ============LICENSE_START=======================================================
 * ONAP Policy Model
 * ================================================================================
 * Copyright (C) 2019-2020 AT&T Intellectual Property. All rights reserved.
 * Modifications Copyright (C) 2019-2021, 2023 Nordix Foundation.
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

import com.google.gson.annotations.SerializedName;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.Table;
import java.io.Serial;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import org.onap.policy.common.parameters.BeanValidationResult;
import org.onap.policy.common.parameters.annotations.NotNull;
import org.onap.policy.models.base.PfConcept;
import org.onap.policy.models.base.PfKey;
import org.onap.policy.models.base.PfReferenceKey;
import org.onap.policy.models.base.PfUtils;
import org.onap.policy.models.base.validation.annotations.VerifyKey;

/**
 * Class to represent the TimeInterval in TOSCA definition.
 *
 * @author Chenfei Gao (cgao@research.att.com)
 * @author Liam Fallon (liam.fallon@est.tech)
 *
 */
@Entity
@Table(name = "ToscaTimeInterval")
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
@Data
@EqualsAndHashCode(callSuper = false)
public class JpaToscaTimeInterval extends PfConcept {
    @Serial
    private static final long serialVersionUID = 9151467029611969980L;

    @EmbeddedId
    @VerifyKey
    @NotNull
    private PfReferenceKey key;

    @SerializedName("start_time")
    private Timestamp startTime;

    @SerializedName("end_time")
    private Timestamp endTime;

    /**
     * The Default Constructor creates a {@link JpaToscaTimeInterval} object with a null key.
     */
    public JpaToscaTimeInterval() {
        this(new PfReferenceKey());
    }

    /**
     * The Key Constructor creates a {@link JpaToscaTimeInterval} object with the given concept key.
     *
     * @param key the key
     */
    public JpaToscaTimeInterval(@NonNull final PfReferenceKey key) {
        this(key, Instant.EPOCH, Instant.EPOCH);
    }

    /**
     * The full constructor creates a {@link JpaToscaTimeInterval} object with all fields.
     *
     * @param key the key
     */
    public JpaToscaTimeInterval(@NonNull final PfReferenceKey key, @NonNull final Instant startTime,
            @NonNull final Instant endTime) {
        this.key = key;
        this.startTime = Timestamp.from(startTime);
        this.endTime = Timestamp.from(endTime);
    }

    /**
     * Copy constructor.
     *
     * @param copyConcept the concept to copy from
     */
    public JpaToscaTimeInterval(final JpaToscaTimeInterval copyConcept) {
        super(copyConcept);
        this.key = new PfReferenceKey(copyConcept.key);
        this.startTime = copyConcept.startTime;
        this.endTime = copyConcept.endTime;
    }

    @Override
    public List<PfKey> getKeys() {
        return getKey().getKeys();
    }

    @Override
    public void clean() {
        key.clean();
    }

    @Override
    public BeanValidationResult validate(@NonNull String fieldName) {
        BeanValidationResult result = super.validate(fieldName);

        if (startTime == null || startTime.getTime() == 0) {
            addResult(result, "startTime", startTime, "is null or zero");
        }

        if (endTime == null || endTime.getTime() == 0) {
            addResult(result, "endTime", endTime, "is null or zero");
        }

        if (startTime != null && endTime != null && endTime.before(startTime)) {
            addResult(result, "endTime", endTime, "is before startTime");
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

        final JpaToscaTimeInterval other = (JpaToscaTimeInterval) otherConcept;
        int result = key.compareTo(other.key);
        if (result != 0) {
            return result;
        }

        result = PfUtils.compareObjects(startTime, other.startTime);
        if (result != 0) {
            return result;
        }

        return PfUtils.compareObjects(endTime, other.endTime);
    }
}
