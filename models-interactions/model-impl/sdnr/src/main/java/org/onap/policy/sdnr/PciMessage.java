/*-
 * ============LICENSE_START=======================================================
 * sdnr
 * ================================================================================
 * Copyright (C) 2018 Wipro Limited Intellectual Property. All rights reserved.
 * Modifications Copyright (C) 2019 Nordix Foundation.
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

package org.onap.policy.sdnr;

import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Generic PCI message, which can contain a request or a response.
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class PciMessage extends PciWrapper implements Serializable {
    private static final long serialVersionUID = 879766924715980798L;

    private PciBody body;
}
