/*-
 * ============LICENSE_START=======================================================
 * policy-yaml
 * ================================================================================
 * Copyright (C) 2017-2019 AT&T Intellectual Property. All rights reserved.
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
 * ============LICENSE_END=========================================================
 */

package org.onap.policy.controlloop.policy;

import java.io.Serializable;
import java.util.Collections;
import java.util.Map;
import java.util.UUID;

public class Policy implements Serializable {
    private static final long serialVersionUID = 1L;

    private String id = UUID.randomUUID().toString();
    private String name;
    private String description;
    private String actor;
    private String recipe;
    private Map<String, String> payload;
    private Target target;
    private OperationsAccumulateParams operationsAccumulateParams;
    private Integer retry = 0;
    private Integer timeout = 300;
    private String success = FinalResult.FINAL_SUCCESS.toString();
    private String failure = FinalResult.FINAL_FAILURE.toString();
    private String failureRetries = FinalResult.FINAL_FAILURE_RETRIES.toString();
    private String failureTimeout = FinalResult.FINAL_FAILURE_TIMEOUT.toString();
    private String failureException = FinalResult.FINAL_FAILURE_EXCEPTION.toString();
    private String failureGuard = FinalResult.FINAL_FAILURE_GUARD.toString();


    public Policy() {
        //Does Nothing Empty Constructor
    }

    public Policy(String id) {
        this.id = id;
    }

    /**
     * Constructor.
     *
     * @param name name
     * @param actor actor
     * @param recipe recipe
     * @param payload payload
     * @param target target
     */
    public Policy(String name, String actor, String recipe, Map<String, String> payload, Target target) {
        this.name = name;
        this.actor = actor;
        this.recipe = recipe;
        this.target = target;
        if (payload != null) {
            this.payload = Collections.unmodifiableMap(payload);
        }
    }

    /**
     * Constructor.
     *
     * @param name name
     * @param actor actor
     * @param recipe recipe
     * @param payload payload
     * @param target target
     * @param retries retries
     * @param timeout timeout
     */
    public Policy(String name, String actor, String recipe, Map<String, String> payload, Target target,
                  Integer retries, Integer timeout) {
        this(name, actor, recipe, payload, target);
        this.retry = retries;
        this.timeout = timeout;
    }

    /**
     * Constructor.
     *
     * @param policyParam provide parameter object
     */
    public Policy(PolicyParam policyParam) {
        this(policyParam.getName(), policyParam.getActor(), policyParam.getRecipe(), policyParam.getPayload(),
                policyParam.getTarget(), policyParam.getRetries(), policyParam.getTimeout());
        this.id = policyParam.getId();
        this.description = policyParam.getDescription();
    }

    /**
     * Constructor.
     *
     * @param policy copy object
     */
    public Policy(Policy policy) {
        this.id = policy.id;
        this.name = policy.name;
        this.description = policy.description;
        this.actor = policy.actor;
        this.recipe = policy.recipe;
        if (policy.payload != null) {
            this.payload = Collections.unmodifiableMap(policy.payload);
        }
        this.target = policy.target;
        this.operationsAccumulateParams = policy.operationsAccumulateParams;
        this.retry = policy.retry;
        this.timeout = policy.timeout;
        this.success = policy.success;
        this.failure = policy.failure;
        this.failureException = policy.failureException;
        this.failureGuard = policy.failureGuard;
        this.failureRetries = policy.failureRetries;
        this.failureTimeout = policy.failureTimeout;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getActor() {
        return actor;
    }

    public void setActor(String actor) {
        this.actor = actor;
    }

    public String getRecipe() {
        return recipe;
    }

    public void setRecipe(String recipe) {
        this.recipe = recipe;
    }

    public Map<String, String> getPayload() {
        return payload;
    }

    public void setPayload(Map<String, String> payload) {
        this.payload = payload;
    }

    public Target getTarget() {
        return target;
    }

    public void setTarget(Target target) {
        this.target = target;
    }

    public OperationsAccumulateParams getOperationsAccumulateParams() {
        return operationsAccumulateParams;
    }

    public void setOperationsAccumulateParams(OperationsAccumulateParams operationsAccumulateParams) {
        this.operationsAccumulateParams = operationsAccumulateParams;
    }

    public Integer getRetry() {
        return retry;
    }

    public void setRetry(Integer retry) {
        this.retry = retry;
    }

    public Integer getTimeout() {
        return timeout;
    }

    public void setTimeout(Integer timeout) {
        this.timeout = timeout;
    }

    public String getSuccess() {
        return success;
    }

    public void setSuccess(String success) {
        this.success = success;
    }

    public String getFailure() {
        return failure;
    }

    public void setFailure(String failure) {
        this.failure = failure;
    }

    public String getFailure_retries() {
        return failureRetries;
    }

    public void setFailure_retries(String failureRetries) {
        this.failureRetries = failureRetries;
    }

    public String getFailure_timeout() {
        return failureTimeout;
    }

    public void setFailure_timeout(String failureTimeout) {
        this.failureTimeout = failureTimeout;
    }

    public String getFailure_exception() {
        return failureException;
    }

    public void setFailure_exception(String failureException) {
        this.failureException = failureException;
    }

    public String getFailure_guard() {
        return failureGuard;
    }

    public void setFailure_guard(String failureGuard) {
        this.failureGuard = failureGuard;
    }

    public boolean isValid() {
        boolean isValid = id != null && name != null && actor != null;
        return isValid && recipe != null && target != null;
    }

    @Override
    public String toString() {
        return "Policy [id=" + id + ", name=" + name + ", description=" + description + ", actor=" + actor + ", recipe="
                + recipe + ", payload=" + payload + ", target=" + target + ", operationsAccumulateParams="
                + operationsAccumulateParams + ", retry=" + retry + ", timeout=" + timeout
                + ", success=" + success + ", failure=" + failure + ", failure_retries=" + failureRetries
                + ", failure_timeout=" + failureTimeout + ", failure_exception=" + failureException
                + ", failure_guard=" + failureGuard + "]";
    }

    @Override
    public int hashCode() {
        int result = 1;
        result = addHashCodeForField(result, actor);
        result = addHashCodeForField(result, description);
        result = addHashCodeForField(result, failure);
        result = addHashCodeForField(result, failureException);
        result = addHashCodeForField(result, failureGuard);
        result = addHashCodeForField(result, failureRetries);
        result = addHashCodeForField(result, failureTimeout);
        result = addHashCodeForField(result, id);
        result = addHashCodeForField(result, name);
        result = addHashCodeForField(result, payload);
        result = addHashCodeForField(result, recipe);
        result = addHashCodeForField(result, retry);
        result = addHashCodeForField(result, success);
        result = addHashCodeForField(result, target);
        result = addHashCodeForField(result, operationsAccumulateParams);
        result = addHashCodeForField(result, timeout);
        return result;
    }

    private int addHashCodeForField(int hashCode, Object field) {
        final int prime = 31;
        return prime * hashCode + ((field == null) ? 0 : field.hashCode());
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        Policy other = (Policy) obj;
        boolean isEq = equalsMayBeNull(actor, other.actor)
                && equalsMayBeNull(description, other.description)
                && equalsMayBeNull(failure, other.failure);
        isEq = isEq
                && equalsMayBeNull(failureException, other.failureException)
                && equalsMayBeNull(failureGuard, other.failureGuard);
        isEq = isEq
                && equalsMayBeNull(failureRetries, other.failureRetries)
                && equalsMayBeNull(id, other.id);
        isEq = isEq
                && equalsMayBeNull(name, other.name)
                && equalsMayBeNull(payload, other.payload);
        isEq = isEq
                && equalsMayBeNull(recipe, other.recipe)
                && equalsMayBeNull(retry, other.retry);
        isEq = isEq
                && equalsMayBeNull(success, other.success)
                && equalsMayBeNull(operationsAccumulateParams, other.operationsAccumulateParams);
        return isEq
                && equalsMayBeNull(target, other.target)
                && equalsMayBeNull(timeout, other.timeout);
    }

    private boolean equalsMayBeNull(final Object obj1, final Object obj2) {
        if ( obj1 == null ) {
            return obj2 == null;
        }
        return obj1.equals(obj2);
    }
}
