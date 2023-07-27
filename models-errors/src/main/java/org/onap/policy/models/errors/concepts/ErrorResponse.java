/*
 * ============LICENSE_START=======================================================
 * ONAP Policy Decision Models
 * ================================================================================
 * Copyright (C) 2019 AT&T Intellectual Property. All rights reserved.
 * Modifications Copyright (C) 2023 Nordix Foundation.
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

package org.onap.policy.models.errors.concepts;

import com.google.gson.annotations.SerializedName;
import jakarta.ws.rs.core.Response;
import java.io.Serial;
import java.io.Serializable;
import java.util.List;
import lombok.Data;

/**
 * This class is for defining a common error/warning
 * response from API calls in the Policy Framework.
 *
 * @author pameladragosh
 */
@Data
public class ErrorResponse implements Serializable {
    @Serial
    private static final long serialVersionUID = 6760066094588944729L;

    @SerializedName("code")
    private Response.Status responseCode;
    @SerializedName("error")
    private String errorMessage;
    @SerializedName("details")
    private List<String> errorDetails;
    @SerializedName("warnings")
    private List<String> warningDetails;

}
