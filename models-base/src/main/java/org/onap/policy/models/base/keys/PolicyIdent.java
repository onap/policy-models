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

package org.onap.policy.models.base.keys;

import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.onap.policy.models.base.PfConceptKey;

/**
 * Identifies a policy. Both the name and version must be non-null.
 */
@NonNull
@NoArgsConstructor
public class PolicyIdent extends PfConceptKey {
    private static final long serialVersionUID = 1L;

    public PolicyIdent(String name, String version) {
        super(name, version);
    }

    public PolicyIdent(PolicyIdent source) {
        super(source);
    }
}
