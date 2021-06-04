/*-
 * ============LICENSE_START=======================================================
 * ONAP
 * ================================================================================
 * Copyright (C) 2021 AT&T Intellectual Property. All rights reserved.
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

package org.onap.policy.models.tosca.simple.provider;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.onap.policy.models.base.PfConceptKey;
import org.onap.policy.models.base.PfKey;

@Getter
@AllArgsConstructor
@EqualsAndHashCode
public class EntityKey {
    public enum NodeType {
        POLICY, POLICY_TYPE, DATA_TYPE
    }

    private final NodeType type;
    private final PfConceptKey ident;

    public EntityKey withNullVersion() {
        return new EntityKey(type, new PfConceptKey(ident.getName(), PfKey.NULL_KEY_VERSION));
    }

    public boolean isNewerThan(EntityKey other) {
        return ident.isNewerThan(other.ident);
    }
}
