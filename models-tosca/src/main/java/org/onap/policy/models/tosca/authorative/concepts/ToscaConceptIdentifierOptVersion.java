/*
 * ============LICENSE_START=======================================================
 * ONAP Policy Models
 * ================================================================================
 * Copyright (C) 2019 AT&T Intellectual Property. All rights reserved.
 * Modifications Copyright (C) 2020-2021 Nordix Foundation.
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

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.apache.commons.lang3.ObjectUtils;

/**
 * Concept identifier with an optional version; only the "name" is required.
 */
@Data
@NoArgsConstructor
public class ToscaConceptIdentifierOptVersion implements Comparable<ToscaConceptIdentifierOptVersion> {

    @NonNull
    private String name;

    private String version;

    public ToscaConceptIdentifierOptVersion(@NonNull String name, String version) {
        this.name = name;
        this.version = version;
    }

    public ToscaConceptIdentifierOptVersion(ToscaConceptIdentifierOptVersion source) {
        this.name = source.name;
        this.version = source.version;
    }

    public ToscaConceptIdentifierOptVersion(ToscaConceptIdentifier source) {
        this.name = source.getName();
        this.version = source.getVersion();
    }

    @Override
    public int compareTo(ToscaConceptIdentifierOptVersion other) {
        if (this == other) {
            return 0;
        }

        if (other == null) {
            return 1;
        }

        int result = ObjectUtils.compare(getName(), other.getName());
        if (result != 0) {
            return result;
        }

        return ObjectUtils.compare(getVersion(), other.getVersion());
    }

    @Override
    public String toString() {
        return this.name + " " + this.version;
    }
}
