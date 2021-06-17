/*-
 * ============LICENSE_START=======================================================
 * ONAP
 * ================================================================================
 * Copyright (C) 2020-2021 AT&T Intellectual Property. All rights reserved.
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

package org.onap.policy.controlloop.actorserviceprovider;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.onap.policy.common.utils.coder.Coder;
import org.onap.policy.common.utils.coder.CoderException;
import org.onap.policy.common.utils.coder.StandardCoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Actor utilities.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class Util {
    private static final Logger logger = LoggerFactory.getLogger(Util.class);
    private static final Coder coder = new StandardCoder();

    /**
     * Extracts an object's identity by invoking {@link Object#toString()} and returning
     * the portion starting with "@". Extraction is done on-demand, when toString() is
     * called on the result. This is typically used when logging.
     *
     * @param object object whose identity is to be extracted
     * @return an object that will extract the source object's identity when this object's
     *         toString() method is called
     */
    public static Object ident(Object object) {
        return new DelayedIdentString(object);
    }

    /**
     * Runs a function and logs a message if it throws an exception. Does <i>not</i>
     * re-throw the exception.
     *
     * @param function function to be run
     * @param exceptionMessage message to log if an exception is thrown
     * @param exceptionArgs arguments to be passed to the logger
     */
    public static void runFunction(Runnable function, String exceptionMessage, Object... exceptionArgs) {
        try {
            function.run();

        } catch (RuntimeException ex) {
            // create a new array containing the original arguments plus the exception
            Object[] allArgs = Arrays.copyOf(exceptionArgs, exceptionArgs.length + 1);
            allArgs[exceptionArgs.length] = ex;

            logger.warn(exceptionMessage, allArgs);
        }
    }

    /**
     * Translates parameters from one class to another, typically from a Map to a POJO or
     * vice versa.
     *
     * @param identifier identifier of the actor/operation being translated; used to build
     *        an exception message
     * @param source source object to be translated
     * @param clazz target class
     * @return the translated object
     */
    public static <T> T translate(String identifier, Object source, Class<T> clazz) {
        try {
            return coder.convert(source, clazz);

        } catch (CoderException | RuntimeException e) {
            throw new IllegalArgumentException("cannot translate parameters for " + identifier, e);
        }
    }

    /**
     * Translates parameters to a Map.
     *
     * @param identifier identifier of the actor/operation being translated; used to build
     *        an exception message
     * @param source source parameters
     * @return the parameters, as a Map
     */
    @SuppressWarnings("unchecked")
    public static Map<String, Object> translateToMap(String identifier, Object source) {
        return translate(identifier, source, LinkedHashMap.class);
    }
}
