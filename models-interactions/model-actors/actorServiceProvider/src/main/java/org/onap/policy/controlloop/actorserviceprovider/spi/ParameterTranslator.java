/*-
 * ============LICENSE_START=======================================================
 * ONAP
 * ================================================================================
 * Copyright (C) 2020 AT&T Intellectual Property. All rights reserved.
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

package org.onap.policy.controlloop.actorserviceprovider.spi;

import java.util.Map;
import org.onap.policy.common.utils.coder.Coder;
import org.onap.policy.common.utils.coder.CoderException;
import org.onap.policy.common.utils.coder.StandardCoder;

/**
 * Parameter translation utilities.
 */
public class ParameterTranslator {

    private ParameterTranslator() {
        // do nothing
    }

    /**
     * Translates parameters from one class to another, typically from a Map to a POJO or
     * vice versa.
     *
     * @param <T> type of object into which the source should be translated
     * @param identifier identifier of the actor/operation being translated; used to build
     *        an exception message
     * @param source source object to be translated
     * @param clazz target class
     * @return the translated object
     */
    public static <T> T translate(String identifier, Object source, Class<T> clazz) {
        Coder coder = new StandardCoder();

        try {
            String json = coder.encode(source);
            return coder.decode(json, clazz);

        } catch (CoderException e) {
            throw new IllegalArgumentException("cannot translate parameters for " + identifier, e);
        }
    }

    /**
     * Casts parameters to a Map.
     *
     * @param identifier identifier of the actor/operation being translated; used to build
     *        an exception message
     * @param source source parameters
     * @return the parameters, as a Map
     */
    @SuppressWarnings("unchecked")
    public static Map<String, Object> translateToMap(String identifier, Object source) {
        if (source == null) {
            return null;
        }

        try {
            return (Map<String, Object>) source;

        } catch (ClassCastException e) {
            throw new IllegalArgumentException("cannot translate parameters for " + identifier, e);
        }
    }
}
