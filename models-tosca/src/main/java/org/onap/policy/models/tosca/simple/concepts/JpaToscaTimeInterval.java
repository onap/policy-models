/*-
 * ============LICENSE_START=======================================================
 * ONAP Policy Model
 * ================================================================================
 * Copyright (C) 2019 AT&T Intellectual Property. All rights reserved.
 * Modifications Copyright (C) 2019 Nordix Foundation.
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

import java.util.Date;
import java.util.List;

import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.Table;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;

import org.onap.policy.common.utils.validation.Assertions;
import org.onap.policy.models.base.PfConcept;
import org.onap.policy.models.base.PfKey;
import org.onap.policy.models.base.PfReferenceKey;
import org.onap.policy.models.base.PfUtils;
import org.onap.policy.models.base.PfValidationMessage;
import org.onap.policy.models.base.PfValidationResult;
import org.onap.policy.models.base.PfValidationResult.ValidationResult;
import org.onap.policy.models.base.utils.BeanCopyUtils;

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
    private static final long serialVersionUID = 9151467029611969980L;

    @EmbeddedId
    private PfReferenceKey key;

    @SerializedName("start_time")
    private Date startTime;

    @SerializedName("end_time")
    private Date endTime;

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
        this(key, new Date(0), new Date(0));
    }

    /**
     * The full constructor creates a {@link JpaToscaTimeInterval} object with all fields.
     *
     * @param key the key
     */
    public JpaToscaTimeInterval(@NonNull final PfReferenceKey key, @NonNull final Date startTime,
            @NonNull final Date endTime) {
        this.key = key;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    /**
     * Copy constructor.
     *
     * @param copyConcept the concept to copy from
     */
    public JpaToscaTimeInterval(final JpaToscaTimeInterval copyConcept) {
        super(copyConcept);
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
    public PfValidationResult validate(@NonNull final PfValidationResult resultIn) {
        PfValidationResult result = resultIn;

        if (key.isNullKey()) {
            result.addValidationMessage(
                    new PfValidationMessage(key, this.getClass(), ValidationResult.INVALID, "key is a null key"));
        }

        result = key.validate(result);

        if (startTime == null || startTime.getTime() == 0) {
            result.addValidationMessage(new PfValidationMessage(key, this.getClass(), ValidationResult.INVALID,
                    "start time on time interval may not be null or zero"));
        }

        if (endTime == null || endTime.getTime() == 0) {
            result.addValidationMessage(new PfValidationMessage(key, this.getClass(), ValidationResult.INVALID,
                    "end time on time interval may not be null or zero"));
        }

        if (startTime != null && endTime != null && endTime.before(startTime)) {
            result.addValidationMessage(new PfValidationMessage(key, this.getClass(), ValidationResult.INVALID,
                    "end time \"" + endTime.toString() + "\" on time interval may not be before start time \""
                            + startTime.toString() + "\""));
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
        if (!key.equals(other.key)) {
            return key.compareTo(other.key);
        }

        int returnVal = PfUtils.compareObjects(startTime, other.startTime);
        if (returnVal != 0) {
            return returnVal;
        }

        return PfUtils.compareObjects(endTime, other.endTime);
    }

    @Override
    public PfConcept copyTo(@NonNull final PfConcept target) {
        return BeanCopyUtils.copyTo(this, target, this.getClass());
    }
}
