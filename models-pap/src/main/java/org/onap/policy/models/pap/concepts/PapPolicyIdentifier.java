/*-
 * ============LICENSE_START=======================================================
 * ONAP Policy Models
 * ================================================================================
 * Copyright (C) 2021 Nordix Foundation.
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

package org.onap.policy.models.pap.concepts;

import com.google.gson.annotations.SerializedName;
import lombok.NonNull;
import org.onap.policy.models.tosca.authorative.concepts.ToscaConceptIdentifier;
import org.onap.policy.models.tosca.authorative.concepts.ToscaConceptIdentifierOptVersion;

/**
 * Policy identifier with an optional version; only the "name" is required.
 *
 * <p>Note that there are deliberately no setters or getters on this class, it is use purely for GSON serialization and
 * deserializaiton
 */
public class PapPolicyIdentifier {
    @NonNull
    @SerializedName("policy-id")
    private String name;

    @SerializedName("policy-version")
    private String version;

    public PapPolicyIdentifier(final String name, final String version) {
        this.name = name;
        this.version = version;
    }

    public PapPolicyIdentifier(@NonNull final ToscaConceptIdentifier identifier) {
        this(identifier.getName(), identifier.getVersion());
    }

    public PapPolicyIdentifier(@NonNull final ToscaConceptIdentifierOptVersion identifier) {
        this(identifier.getName(), identifier.getVersion());
    }

    public ToscaConceptIdentifierOptVersion getGenericIdentifier() {
        return name == null ? new ToscaConceptIdentifierOptVersion()
                : new ToscaConceptIdentifierOptVersion(name, version);
    }
}
