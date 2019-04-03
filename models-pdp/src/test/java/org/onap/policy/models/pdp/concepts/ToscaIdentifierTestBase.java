/*
 * ============LICENSE_START=======================================================
 * ONAP Policy Models
 * ================================================================================
 * Copyright (C) 2019 AT&T Intellectual Property. All rights reserved.
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

package org.onap.policy.models.pdp.concepts;

import org.onap.policy.common.utils.coder.Coder;
import org.onap.policy.common.utils.coder.CoderException;
import org.onap.policy.common.utils.coder.StandardCoder;

/**
 * Super class to test identity keys.
 *
 * @param <T> type of key being tested
 */
public class ToscaIdentifierTestBase<T> {

    private static final Coder coder = new StandardCoder();

    private final Class<T> clazz;


    /**
     * Constructs the object.
     * @param clazz the type of class being tested
     */
    public ToscaIdentifierTestBase(Class<T> clazz) {
        this.clazz = clazz;
    }

    /**
     * Makes an identifier. Uses JSON which does no error checking.
     *
     * @param name name to put into the identifier
     * @param version version to put into the identifier
     * @return a new identifier
     * @throws CoderException if the JSON cannot be decoded
     */
    public T makeIdent(String name, String version) throws CoderException {
        StringBuilder bldr = new StringBuilder();
        bldr.append("{");

        if (name != null) {
            bldr.append("'name':'");
            bldr.append(name);
            bldr.append("'");
        }

        if (version != null) {
            if (name != null) {
                bldr.append(',');
            }

            bldr.append("'version':'");
            bldr.append(version);
            bldr.append("'");
        }

        bldr.append("}");

        String json = bldr.toString().replace('\'', '"');

        return coder.decode(json, clazz);
    }
}
