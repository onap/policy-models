/*-
 * ============LICENSE_START=======================================================
 * aai
 * ================================================================================
 * Copyright (C) 2017-2019 AT&T Intellectual Property. All rights reserved.
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

package org.onap.policy.aai;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class AaiNqServiceExcept implements Serializable {
    private static final long serialVersionUID = 2858343404484338546L;

    @SerializedName("messageId")
    private String messageId;

    @SerializedName("text")
    private String text;

    @SerializedName("variables")
    private String[] variables;

    public String getMessageId() {
        return messageId;
    }

    public String getText() {
        return text;
    }

    public String[] getVariables() {
        return variables;
    }
}
