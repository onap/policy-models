/*-
 * ============LICENSE_START=======================================================
 * ONAP Policy Models
 * ================================================================================
 * Copyright (C) 2021 AT&T Intellectual Property. All rights reserved.
 * Modifications Copyright (C) 2025 OpenInfra Foundation Europe. All rights reserved.
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

import java.io.Serial;
import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.apache.commons.lang3.ObjectUtils;
import org.onap.policy.models.base.PfConceptKey;
import org.onap.policy.models.base.PfKey;

/**
 * Concept with an optional name and version.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ToscaNameVersion implements Serializable {

    @Serial
    private static final long serialVersionUID = 8010649773816325786L;

    private String name;

    private String version;

    public ToscaNameVersion(@NonNull PfKey key) {
        this.name = key.getName();
        this.version = key.getVersion();
    }

    public ToscaNameVersion(ToscaNameVersion source) {
        this.name = source.name;
        this.version = source.version;
    }

    /**
     * Create a PfConceptKey from the TOSCA identifier.
     *
     * @return the key
     */
    public PfConceptKey asConceptKey() {
        return new PfConceptKey(name, version);
    }

    protected int commonCompareTo(ToscaNameVersion other) {
        if (this == other) {
            return 0;
        }

        if (other == null) {
            return 1;
        }

        if (getClass() != other.getClass()) {
            return getClass().getSimpleName().compareTo(other.getClass().getSimpleName());
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
