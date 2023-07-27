/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2019, 2023 Nordix Foundation.
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

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import jakarta.xml.bind.annotation.adapters.XmlAdapter;
import java.util.UUID;

/**
 * The Class UuidConverter converts a UUID to and from database format.
 *
 * @author Liam Fallon (liam.fallon@est.tech)
 */
@Converter
public class Uuid2String extends XmlAdapter<String, UUID> implements AttributeConverter<UUID, String> {

    @Override
    public String convertToDatabaseColumn(final UUID uuid) {
        String returnString;
        if (uuid == null) {
            returnString = "";
        } else {
            returnString = uuid.toString();
        }
        return returnString;
    }

    @Override
    public UUID convertToEntityAttribute(final String uuidString) {
        return UUID.fromString(uuidString);
    }

    @Override
    public UUID unmarshal(final String value) throws Exception {
        return this.convertToEntityAttribute(value);
    }

    @Override
    public String marshal(final UUID value) throws Exception {
        return this.convertToDatabaseColumn(value);
    }
}
