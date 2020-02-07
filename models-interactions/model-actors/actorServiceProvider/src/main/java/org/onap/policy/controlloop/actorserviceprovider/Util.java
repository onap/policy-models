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

package org.onap.policy.controlloop.actorserviceprovider;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import org.onap.policy.common.endpoints.event.comm.Topic.CommInfrastructure;
import org.onap.policy.common.endpoints.utils.NetLoggerUtil;
import org.onap.policy.common.endpoints.utils.NetLoggerUtil.EventType;
import org.onap.policy.common.utils.coder.Coder;
import org.onap.policy.common.utils.coder.CoderException;
import org.onap.policy.common.utils.coder.StandardCoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Actor utilities.
 */
public class Util {
    private static final Logger logger = LoggerFactory.getLogger(Util.class);

    private Util() {
        // do nothing
    }

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
     * Logs a REST request. If the request is not of type, String, then it attempts to
     * pretty-print it into JSON before logging.
     *
     * @param url request URL
     * @param request request to be logged
     */
    public static <T> void logRestRequest(String url, T request) {
        logRestRequest(new StandardCoder(), url, request);
    }

    /**
     * Logs a REST request. If the request is not of type, String, then it attempts to
     * pretty-print it into JSON before logging.
     *
     * @param coder coder to be used to pretty-print the request
     * @param url request URL
     * @param request request to be logged
     */
    protected static <T> void logRestRequest(Coder coder, String url, T request) {
        String json;
        try {
            if (request instanceof String) {
                json = request.toString();
            } else {
                json = coder.encode(request, true);
            }

        } catch (CoderException e) {
            logger.warn("cannot pretty-print request", e);
            json = request.toString();
        }

        NetLoggerUtil.log(EventType.OUT, CommInfrastructure.REST, url, json);
        logger.info("[OUT|{}|{}|]{}{}", CommInfrastructure.REST, url, NetLoggerUtil.SYSTEM_LS, json);
    }

    /**
     * Logs a REST response. If the request is not of type, String, then it attempts to
     * pretty-print it into JSON before logging.
     *
     * @param url request URL
     * @param response response to be logged
     */
    public static <T> void logRestResponse(String url, T response) {
        logRestResponse(new StandardCoder(), url, response);
    }

    /**
     * Logs a REST response. If the request is not of type, String, then it attempts to
     * pretty-print it into JSON before logging.
     *
     * @param coder coder to be used to pretty-print the response
     * @param url request URL
     * @param response response to be logged
     */
    protected static <T> void logRestResponse(Coder coder, String url, T response) {
        String json;
        try {
            if (response == null) {
                json = null;
            } else if (response instanceof String) {
                json = response.toString();
            } else {
                json = coder.encode(response, true);
            }

        } catch (CoderException e) {
            logger.warn("cannot pretty-print response", e);
            json = response.toString();
        }

        NetLoggerUtil.log(EventType.IN, CommInfrastructure.REST, url, json);
        logger.info("[IN|{}|{}|]{}{}", CommInfrastructure.REST, url, NetLoggerUtil.SYSTEM_LS, json);
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
        Coder coder = new StandardCoder();

        try {
            String json = coder.encode(source);
            return coder.decode(json, clazz);

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
        if (source == null) {
            return null;
        }

        return translate(identifier, source, LinkedHashMap.class);
    }
}
