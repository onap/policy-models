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

package org.onap.policy.controlloop.actorserviceprovider.topic;

import lombok.EqualsAndHashCode;
import org.onap.policy.common.utils.coder.StandardCoderObject;

/**
 * Selector key, which contains a hierarchical list of Strings and Integers that are used
 * to extract the content of a field, typically from a {@link StandardCoderObject}.
 */
@EqualsAndHashCode
public class SelectorKey {

    /**
     * Names and indices used to extract the field's value from the
     * {@link StandardCoderObject}.
     */
    private final Object[] fieldIdentifiers;

    /**
     * Constructs the object.
     *
     * @param fieldIdentifiers names and indices used to extract the field's value from
     *        the {@link StandardCoderObject}
     */
    public SelectorKey(Object... fieldIdentifiers) {
        this.fieldIdentifiers = fieldIdentifiers;
    }

    /**
     * Extracts the given field from an object.
     *
     * @param object object from which to extract the field
     * @return the extracted value, or {@code null} if the object does not contain the
     *         field
     */
    public String extractField(StandardCoderObject object) {
        return object.getString(fieldIdentifiers);
    }
}
