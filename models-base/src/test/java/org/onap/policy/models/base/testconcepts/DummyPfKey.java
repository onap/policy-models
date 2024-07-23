/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2019, 2023 Nordix Foundation.
 *  Modifications Copyright (C) 2019-2020 AT&T Intellectual Property. All rights reserved.
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

package org.onap.policy.models.base.testconcepts;

import java.io.Serial;
import java.util.List;
import lombok.NonNull;
import org.onap.policy.common.parameters.BeanValidationResult;
import org.onap.policy.models.base.PfConcept;
import org.onap.policy.models.base.PfKey;

public class DummyPfKey extends PfKey {
    @Serial
    private static final long serialVersionUID = 1L;

    public DummyPfKey() {
        // Default constructor
    }

    public DummyPfKey(DummyPfKey source) {
        super(source);
    }

    @Override
    public int compareTo(@NonNull PfConcept arg0) {
        return 0;
    }

    @Override
    public String getId() {
        return null;
    }

    @Override
    public boolean isNullKey() {
        return false;
    }

    @Override
    public Compatibility getCompatibility(@NonNull PfKey otherKey) {
        return null;
    }

    @Override
    public boolean isCompatible(@NonNull PfKey otherKey) {
        return false;
    }

    @Override
    public PfKey getKey() {
        return null;
    }

    @Override
    public List<PfKey> getKeys() {
        return List.of(getKey());
    }

    @Override
    public BeanValidationResult validate(@NonNull String fieldName) {
        return null;
    }

    @Override
    public void clean() {
        // nothing to do
    }

    @Override
    public boolean equals(Object otherObject) {
        return false;
    }

    @Override
    public String toString() {
        return "";
    }

    @Override
    public int hashCode() {
        return 0;
    }

    @Override
    public boolean isNewerThan(@NonNull PfKey otherKey) {
        return false;
    }

    @Override
    public int getMajorVersion() {
        return 0;
    }

    @Override
    public int getMinorVersion() {
        return 0;
    }

    @Override
    public int getPatchVersion() {
        return 0;
    }
}
