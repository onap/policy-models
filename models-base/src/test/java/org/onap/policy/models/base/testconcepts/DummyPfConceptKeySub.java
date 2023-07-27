/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2019, 2023 Nordix Foundation.
 *  Modifications Copyright (C) 2019 AT&T Intellectual Property. All rights reserved.
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

import jakarta.ws.rs.core.Response;
import lombok.NonNull;
import org.onap.policy.models.base.PfConceptKey;
import org.onap.policy.models.base.PfModelRuntimeException;

/**
 * KeyUse subclass that throws exception on default constructor for testing.
 *
 * @author Liam Fallon (liam.fallon@est.tech)
 */
public class DummyPfConceptKeySub extends PfConceptKey {
    private static final long serialVersionUID = 1L;

    /**
     * The Default Constructor creates this concept with a null key.
     */
    public DummyPfConceptKeySub() {
        throw new PfModelRuntimeException(Response.Status.BAD_GATEWAY, "Some error message");
    }

    /**
     * This constructor creates an instance of this class, and holds a reference to a used key.
     *
     * @param usedKey a used key
     */
    public DummyPfConceptKeySub(@NonNull final PfConceptKey usedKey) {
        super(usedKey);
    }

    /**
     * Copy constructor.
     *
     * @param source object to be copied
     */
    public DummyPfConceptKeySub(@NonNull final DummyPfConceptKeySub source) {
        super(source);
    }
}
