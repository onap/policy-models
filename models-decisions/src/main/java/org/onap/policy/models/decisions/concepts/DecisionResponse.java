/*
 * ============LICENSE_START=======================================================
 * ONAP Policy Decision Models
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

package org.onap.policy.models.decisions.concepts;

import java.util.Map;
import lombok.Data;

/**
 * Generic class for handling Decision Response objects.
 *
 * @author pameladragosh
 *
 */
@Data
public class DecisionResponse {
    private String status;
    private String message;
    private Map<String, Object> advice;
    private Map<String, Object> obligations;
    private Map<String, Object> policies;
    private Map<String, Object> attributes;
}
