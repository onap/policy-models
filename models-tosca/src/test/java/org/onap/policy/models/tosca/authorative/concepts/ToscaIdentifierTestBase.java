/*
 * ============LICENSE_START=======================================================
 * ONAP Policy Models
 * ================================================================================
 * Copyright (C) 2019-2020 AT&T Intellectual Property. All rights reserved.
 * Modifications Copyright (C) 2024 Nordix Foundation
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

package org.onap.policy.models.tosca.authorative.concepts;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.onap.policy.common.utils.coder.Coder;
import org.onap.policy.common.utils.coder.CoderException;
import org.onap.policy.common.utils.coder.StandardCoder;

/**
 * Super class to test identity keys.
 *
 * @param <T> type of key being tested
 */
class ToscaIdentifierTestBase<T extends Comparable<T>> {
    public static final String NAME = "my-name";
    public static final String VERSION = "1.2.3";

    private static final Coder coder = new StandardCoder();

    private final Class<T> clazz;
    private final String nameField;
    private final String versionField;


    /**
     * Constructs the object.
     *
     * @param clazz the type of class being tested
     * @param nameField name of the field containing the "name"
     * @param versionField name of the field containing the "version"
     */
    public ToscaIdentifierTestBase(Class<T> clazz, String nameField, String versionField) {
        this.clazz = clazz;
        this.nameField = nameField;
        this.versionField = versionField;
    }

    /**
     * Tests the compareTo() method.
     *
     * @throws Exception if an error occurs
     */
    void testCompareTo() throws Exception {
        T ident = makeIdent(NAME, VERSION);
        assertEquals(0, ident.compareTo(ident));

        assertTrue(ident.compareTo(null) > 0);

        assertEquals(0, ident.compareTo(makeIdent(NAME, VERSION)));
        assertTrue(ident.compareTo(makeIdent(NAME, null)) > 0);
        assertTrue(ident.compareTo(makeIdent(null, VERSION)) > 0);
        assertTrue(ident.compareTo(makeIdent(NAME, VERSION + "a")) < 0);
        assertTrue(ident.compareTo(makeIdent(NAME + "a", VERSION)) < 0);

        // name takes precedence over version
        assertTrue(makeIdent(NAME, VERSION + "a").compareTo(makeIdent(NAME + "a", VERSION)) < 0);
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
            bldr.append("'");
            bldr.append(nameField);
            bldr.append("':'");
            bldr.append(name);
            bldr.append("'");
        }

        if (version != null) {
            if (name != null) {
                bldr.append(',');
            }

            bldr.append("'");
            bldr.append(versionField);
            bldr.append("':'");
            bldr.append(version);
            bldr.append("'");
        }

        bldr.append("}");

        String json = bldr.toString().replace('\'', '"');

        return coder.decode(json, clazz);
    }
}
