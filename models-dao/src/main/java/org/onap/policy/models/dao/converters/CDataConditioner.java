/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2019, 2023 Nordix Foundation.
 *  Modifications Copyright (C) 2021 AT&T Intellectual Property. All rights reserved.
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

package org.onap.policy.models.dao.converters;

import com.google.re2j.Pattern;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import jakarta.xml.bind.annotation.adapters.XmlAdapter;

/**
 * The Class CDataConditioner converts a CDATA String to and from database format by removing spaces
 * at the ends of lines and platform-specific new line endings.
 */
@Converter
public class CDataConditioner extends XmlAdapter<String, String> implements AttributeConverter<String, String> {

    private static final Pattern TRAILING_SPACE_PAT = Pattern.compile("\\s+$");
    private static final String NL = "\n";

    @Override
    public String convertToDatabaseColumn(final String raw) {
        return clean(raw);
    }

    @Override
    public String convertToEntityAttribute(final String db) {
        return clean(db);
    }

    @Override
    public String unmarshal(final String value) throws Exception {
        return this.convertToEntityAttribute(value);
    }

    @Override
    public String marshal(final String value) throws Exception {
        return this.convertToDatabaseColumn(value);
    }

    /**
     * Clean.
     *
     * @param in the in
     * @return the string
     */
    public static String clean(final String in) {
        if (in == null) {
            return null;
        } else {
            return TRAILING_SPACE_PAT.matcher(in).replaceAll("").replaceAll("\\r?\\n", NL);
        }
    }
}
