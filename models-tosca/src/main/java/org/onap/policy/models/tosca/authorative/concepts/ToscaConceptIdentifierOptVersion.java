/*
 * ============LICENSE_START=======================================================
 * ONAP Policy Models
 * ================================================================================
 * Copyright (C) 2019, 2021 AT&T Intellectual Property. All rights reserved.
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

import java.io.Serializable;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;

/**
 * Concept identifier with an optional name and version.
 */
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class ToscaConceptIdentifierOptVersion extends ToscaNameVersion
                implements Serializable, Comparable<ToscaConceptIdentifierOptVersion> {
    private static final long serialVersionUID = 8010649773816325786L;

    public ToscaConceptIdentifierOptVersion(@NonNull String name, String version) {
        super(name, version);
    }

    public ToscaConceptIdentifierOptVersion(ToscaConceptIdentifierOptVersion source) {
        super(source);
    }

    public ToscaConceptIdentifierOptVersion(ToscaConceptIdentifier source) {
        super(source.getName(), source.getVersion());
    }

    @Override
    public int compareTo(ToscaConceptIdentifierOptVersion other) {
        return commonCompareTo(other);
    }
}
