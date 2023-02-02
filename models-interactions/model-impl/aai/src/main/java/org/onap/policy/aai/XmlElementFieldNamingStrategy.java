/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2023 Nordix Foundation.
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

package org.onap.policy.aai;

import com.google.gson.FieldNamingStrategy;
import java.lang.reflect.Field;
import javax.xml.bind.annotation.XmlElement;

public class XmlElementFieldNamingStrategy implements FieldNamingStrategy {
    @Override
    public String translateName(Field field) {
        XmlElement annotatedFieldName = field.getAnnotation(XmlElement.class);

        if (annotatedFieldName != null) {
            return annotatedFieldName.name();
        } else {
            return field.getName();
        }
    }
}