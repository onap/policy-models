/*-
 * ============LICENSE_START=======================================================
 * ONAP
 * ================================================================================
 * Copyright (C) 2020 AT&T Intellectual Property. All rights reserved.
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

package org.onap.policy.models.sim.dmaap.filter;

import org.onap.policy.common.utils.coder.CoderException;
import org.onap.policy.common.utils.coder.StandardCoder;
import org.onap.policy.common.utils.coder.StandardCoderObject;

/**
 * Support utilities used by various Filter tests.
 */
public class FilterSupport {
    public static final String SCO_STR =
                    "{'name':'john', 'text':'some data', 'indirect':'name', 'nested':{'name':'joe'}}".replace('\'',
                                    '"');
    public static final String INVALID_MESSAGE = "{'invalid message";

    private FilterSupport() {
        // do nothing
    }

    public static StandardCoderObject makeSco() throws CoderException {
        return new StandardCoder().decode(SCO_STR, StandardCoderObject.class);
    }
}
