/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2019 Nordix Foundation.
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

import java.util.Arrays;
import java.util.List;

import lombok.NonNull;

import org.onap.policy.models.base.PfConcept;
import org.onap.policy.models.base.PfKey;
import org.onap.policy.models.base.PfValidationResult;

public class DummyPfKey extends PfKey {
    private static final long serialVersionUID = 1L;

    @Override
    public int compareTo(PfConcept arg0) {
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
    public Compatibility getCompatibility(PfKey otherKey) {
        return null;
    }

    @Override
    public boolean isCompatible(PfKey otherKey) {
        return false;
    }

    @Override
    public PfKey getKey() {
        return null;
    }

    @Override
    public List<PfKey> getKeys() {
        return Arrays.asList(getKey());
    }

    @Override
    public PfValidationResult validate(PfValidationResult result) {
        return null;
    }

    @Override
    public void clean() {

    }

    @Override
    public boolean equals(Object otherObject) {
        return false;
    }

    @Override
    public String toString() {
        return null;
    }

    @Override
    public int hashCode() {
        return 0;
    }

    @Override
    public PfConcept copyTo(PfConcept target) {
        return null;
    }

    @Override
    public boolean isNewerThan(@NonNull PfKey otherKey) {
        // TODO Auto-generated method stub
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
