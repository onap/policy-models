/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2019 Nordix Foundation.
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

package org.onap.policy.models.base;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The model service makes Policy Framework models available to all classes in a JVM.
 *
 * <p>The reason for having a model service is to avoid having to pass concept and model definitions
 * down long call chains in modules such as the Policy Framework engine and editor. The model
 * service makes the model and concept definitions available statically.
 *
 * <p>Note that the use of the model service means that only a single Policy Framework model of a
 * particular type may exist in Policy Framework (particularly the engine) at any time. Of course
 * the model in a JVM can be changed at any time provided all users of the model are stopped and
 * restarted in an orderly manner.
 */
public abstract class PfModelService {
    // The map holding the models
    private static Map<Class<?>, PfConcept> modelMap = new ConcurrentHashMap<>();

    /**
     * This class is an abstract static class that cannot be extended.
     */
    private PfModelService() {}

    /**
     * Register a model with the model service.
     *
     * @param <M> the generic type
     * @param modelClass the class of the model, used to index the model
     * @param model The model
     */
    public static <M extends PfConcept> void registerModel(final Class<M> modelClass, final M model) {
        modelMap.put(modelClass, model);
    }

    /**
     * Remove a model from the model service.
     *
     * @param <M> the generic type
     * @param modelClass the class of the model, used to index the model
     */
    public static <M extends PfConcept> void deregisterModel(final Class<M> modelClass) {
        modelMap.remove(modelClass);
    }

    /**
     * Get a model from the model service.
     *
     * @param <M> the generic type
     * @param modelClass the class of the model, used to index the model
     * @return The model
     */
    @SuppressWarnings("unchecked")
    public static <M extends PfConcept> M getModel(final Class<M> modelClass) {
        final M model = (M) modelMap.get(modelClass);

        if (model == null) {
            throw new PfModelRuntimeException(
                    "Model for " + modelClass.getCanonicalName() + " not found in model service");
        }

        return model;
    }

    /**
     * Check if a model is defined on the model service.
     *
     * @param <M> the generic type
     * @param modelClass the class of the model, used to index the model
     * @return true if the model is defined
     */
    public static <M extends PfConcept> boolean existsModel(final Class<M> modelClass) {
        return modelMap.get(modelClass) != null;
    }

    /**
     * Clear all models in the model service.
     */
    public static void clear() {
        modelMap.clear();
    }
}
