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

package org.onap.policy.models.tosca.simple.concepts;

/**
 * ENUM for TOSCA constraint operations.
 *
 * @author Liam Fallon (liam.fallon@est.tech)
 */
public enum JpaToscaConstraintOperation {
    // @formatter:off
    EQ("equal_to"),
    GT("greater_than"),
    GE("greater_or_equal"),
    LT("less_than"),
    LE("less_or_equal");
    // @formatter:on

    private final String toscaToken;

    private JpaToscaConstraintOperation(final String toscaToken) {
        this.toscaToken = toscaToken;
    }

    public String getToscaToken() {
        return toscaToken;
    }
}
