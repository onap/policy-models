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

import lombok.AllArgsConstructor;

/**
 * Object whose {@link #toString()} method invokes {@link Object#toString()} on another
 * object, on-demand. This assumes that the other object's method returns an object
 * identifier. This is typically used to include an object's identifier in a log message.
 */
@AllArgsConstructor
public class DelayedIdentString {
    /**
     * String to return for null objects or null object identifiers.
     */
    public static final String NULL_STRING = "null";

    private final Object object;

    /**
     * Gets the object's identifier, after stripping anything appearing before '@'.
     */
    @Override
    public String toString() {
        if (object == null) {
            return NULL_STRING;
        }

        var ident = objectToString();
        if (ident == null) {
            return NULL_STRING;
        }

        int index = ident.indexOf('@');
        return (index > 0 ? ident.substring(index) : ident);
    }

    /**
     * Invokes the object's {@link Object#toString()} method.
     *
     * @return the output from the object's {@link Object#toString()} method
     */
    protected String objectToString() {
        return object.toString();
    }
}
