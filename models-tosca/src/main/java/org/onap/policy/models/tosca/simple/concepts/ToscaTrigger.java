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

import java.time.Duration;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.Table;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;

import org.apache.commons.lang3.ObjectUtils;
import org.onap.policy.common.utils.validation.Assertions;
import org.onap.policy.common.utils.validation.ParameterValidationUtils;
import org.onap.policy.models.base.PfConcept;
import org.onap.policy.models.base.PfKey;
import org.onap.policy.models.base.PfReferenceKey;
import org.onap.policy.models.base.PfValidationMessage;
import org.onap.policy.models.base.PfValidationResult;
import org.onap.policy.models.base.PfValidationResult.ValidationResult;

/**
 * Class to represent the trigger of policy type in TOSCA definition.
 *
 * @author Chenfei Gao (cgao@research.att.com)
 * @author Liam Fallon (liam.fallon@est.tech)
 */
@Entity
@Table(name = "ToscaTrigger")
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
@Data
@EqualsAndHashCode(callSuper = false)
public class ToscaTrigger extends PfConcept {
    private static final long serialVersionUID = -6515211640208986971L;

    @EmbeddedId
    private PfReferenceKey key;

    @Column
    private String description;

    @Column
    @SerializedName("event_type")
    private String eventType;

    @Column
    @SerializedName("schedule")
    private ToscaTimeInterval schedule;

    @Column
    @SerializedName("target_filter")
    private ToscaEventFilter targetFilter;

    @Column
    private ToscaConstraint condition;

    @Column
    private ToscaConstraint constraint;

    @Column
    @SerializedName("period")
    private Duration period;

    @Column
    private int evaluations = 0;

    @Column
    private String method;

    @Column
    private String action;

    /**
     * The Default Constructor creates a {@link ToscaTrigger} object with a null key.
     */
    public ToscaTrigger() {
        this(new PfReferenceKey());
    }

    /**
     * The Key Constructor creates a {@link ToscaTrigger} object with the given concept key.
     *
     * @param key the key
     */
    public ToscaTrigger(@NonNull final PfReferenceKey key) {
        this(key, "", "");
    }

    /**
     * The full Constructor creates a {@link ToscaTrigger} object with all mandatory objects.
     *
     * @param key the key
     * @param eventType the event type
     * @param action the trigger action
     */
    public ToscaTrigger(@NonNull final PfReferenceKey key, @NonNull final String eventType,
            @NonNull final String action) {
        this.key = key;
        this.eventType = eventType;
        this.action = action;
    }

    /**
     * Copy constructor.
     *
     * @param copyConcept the concept to copy from
     */
    public ToscaTrigger(final ToscaTrigger copyConcept) {
        super(copyConcept);
    }

    @Override
    public List<PfKey> getKeys() {
        final List<PfKey> keyList = getKey().getKeys();
        if (schedule != null) {
            keyList.addAll(schedule.getKeys());
        }
        if (targetFilter != null) {
            keyList.addAll(targetFilter.getKeys());
        }
        if (condition != null) {
            keyList.addAll(condition.getKeys());
        }
        if (constraint != null) {
            keyList.addAll(constraint.getKeys());
        }
        return keyList;
    }

    @Override
    public void clean() {
        key.clean();

        description = (description != null ? description.trim() : description);
        eventType = eventType.trim();

        if (schedule != null) {
            schedule.clean();
        }
        if (targetFilter != null) {
            targetFilter.clean();
        }
        if (condition != null) {
            condition.clean();
        }
        if (constraint != null) {
            constraint.clean();
        }

        method = (method != null ? method.trim() : method);
        action = action.trim();
    }

    @Override
    public PfValidationResult validate(@NonNull final PfValidationResult resultIn) {
        PfValidationResult result = resultIn;

        if (key.isNullKey()) {
            result.addValidationMessage(
                    new PfValidationMessage(key, this.getClass(), ValidationResult.INVALID, "key is a null key"));
        }

        result = key.validate(result);

        if (description != null && description.trim().length() == 0) {
            result.addValidationMessage(new PfValidationMessage(key, this.getClass(), ValidationResult.INVALID,
                    "trigger description may not be blank"));
        }

        if (!ParameterValidationUtils.validateStringParameter(eventType)) {
            result.addValidationMessage(new PfValidationMessage(key, this.getClass(), ValidationResult.INVALID,
                    "event type on trigger must be defined"));
        }

        result = validateOptionalFields(result);

        if (evaluations < 0) {
            result.addValidationMessage(new PfValidationMessage(key, this.getClass(), ValidationResult.INVALID,
                    "evaluations on trigger must be zero or a positive integer"));
        }

        if (method != null && method.trim().length() == 0) {
            result.addValidationMessage(new PfValidationMessage(key, this.getClass(), ValidationResult.INVALID,
                    "method on trigger may not be blank"));
        }

        if (!ParameterValidationUtils.validateStringParameter(action)) {
            result.addValidationMessage(new PfValidationMessage(key, this.getClass(), ValidationResult.INVALID,
                    "action on trigger must be defined"));
        }

        return result;
    }

    /**
     * Validate optional fields.
     *
     * @param resultIn the validation result so far
     * @return the validation resutls including these fields
     */
    private PfValidationResult validateOptionalFields(final PfValidationResult resultIn) {
        PfValidationResult result = resultIn;

        result = (schedule != null ? schedule.validate(result) : result);
        result = (targetFilter != null ? targetFilter.validate(result) : result);
        result = (condition != null ? condition.validate(result) : result);
        result = (constraint != null ? constraint.validate(result) : result);

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
            return this.hashCode() - otherConcept.hashCode();
        }

        final ToscaTrigger other = (ToscaTrigger) otherConcept;
        if (!key.equals(other.key)) {
            return key.compareTo(other.key);
        }

        return compareFields(other);
    }

    /**
     * Compare the fields of this ToscaTrigger object with the fields of the other ToscaProperty
     * object.
     *
     * @param other the other ToscaTrigger object
     */
    private int compareFields(final ToscaTrigger other) {
        int result = ObjectUtils.compare(description, other.description);
        if (result != 0) {
            return result;
        }

        result = ObjectUtils.compare(eventType, other.eventType);
        if (result != 0) {
            return result;
        }

        result = ObjectUtils.compare(schedule, other.schedule);
        if (result != 0) {
            return result;
        }

        result = ObjectUtils.compare(targetFilter, other.targetFilter);
        if (result != 0) {
            return result;
        }

        result = ObjectUtils.compare(condition, other.condition);
        if (result != 0) {
            return result;
        }

        result = ObjectUtils.compare(constraint, other.constraint);
        if (result != 0) {
            return result;
        }

        result = ObjectUtils.compare(period, other.period);
        if (result != 0) {
            return result;
        }

        if (evaluations != other.evaluations) {
            return evaluations - other.evaluations;
        }

        result = ObjectUtils.compare(method, other.method);
        if (result != 0) {
            return result;
        }

        return ObjectUtils.compare(action, other.action);
    }

    @Override
    public PfConcept copyTo(@NonNull final PfConcept target) {
        Assertions.instanceOf(target, ToscaTrigger.class);

        final ToscaTrigger copy = ((ToscaTrigger) target);
        copy.setKey(new PfReferenceKey(key));
        copy.setDescription(description);
        copy.setEventType(eventType);
        copy.setSchedule(schedule != null ? new ToscaTimeInterval(schedule) : null);
        copy.setTargetFilter(targetFilter != null ? new ToscaEventFilter(targetFilter) : null);
        copy.setCondition(condition);
        copy.setConstraint(constraint);
        copy.setPeriod(period);
        copy.setEvaluations(evaluations);
        copy.setMethod(method);
        copy.setAction(action);

        return copy;
    }
}
