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

package org.onap.policy.models.sim.dmaap.filter;

import com.google.re2j.Matcher;
import com.google.re2j.Pattern;
import org.onap.policy.common.utils.coder.StandardCoderObject;

/**
 * Utilities used by the Filter classes.
 */
public class FilterUtil {
    /**
     * Pattern to match "${xxx}" notations within a specification string.
     */
    private static final Pattern FIELD_PAT = Pattern.compile("[$][{]([^}]+)[}]");

    private FilterUtil() {
        // do nothing
    }

    /**
     * Builds a value using the specification. The source may contain "${}" notations,
     * which are interpreted as fields within the coder object. Everything else is treated
     * as a literal string.
     *
     * @param sco object from which fields are to be extracted
     * @param specification value specification
     * @return the value
     */
    public static String build(StandardCoderObject sco, String specification) {
        if (specification == null) {
            return null;
        }

        StringBuilder builder = new StringBuilder();

        int ilast = 0;
        Matcher mat = FIELD_PAT.matcher(specification);
        while (mat.find(ilast)) {
            int ibeg = mat.start();
            builder.append(specification.subSequence(ilast, ibeg));

            ilast = mat.end();

            String extract = sco.getString(splitName(mat.group(1)));
            if (extract != null) {
                builder.append(extract);
            }
        }

        // include any text remaining after the final "}"
        builder.append(specification.substring(ilast));

        return builder.toString();
    }

    /**
     * Same as @{@link #build(StandardCoderObject, String)}, but the output is treated as
     * the name of a field to be extracted from the coder object.
     *
     * @param sco object from which fields are to be extracted
     * @param specification value specification
     * @return the extracted value
     */
    public static String extract(StandardCoderObject sco, String specification) {
        String fieldName = build(sco, specification);
        return (fieldName == null ? null : sco.getString(splitName(fieldName)));
    }

    /**
     * Splits a field name into separate components using "." as the delimiter.
     * @param fieldName name to be split
     * @return an array of components
     */
    public static Object[] splitName(String fieldName) {
        String[] components = fieldName.split("\\.");
        Object[] arr = new Object[components.length];
        System.arraycopy(components, 0, arr, 0, components.length);
        return arr;
    }
}
