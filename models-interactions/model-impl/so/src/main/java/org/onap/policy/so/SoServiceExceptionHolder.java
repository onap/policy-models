/*-
 * ============LICENSE_START=======================================================
 * so
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

package org.onap.policy.so;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

public class SoServiceExceptionHolder implements Serializable {

    private static final long serialVersionUID = -3283942659786236032L;

    @SerializedName("messageId")
    private String messageId;

    @SerializedName("text")
    private String text;

    @SerializedName("variables")
    private List<String> variables = new LinkedList<>();

    public SoServiceExceptionHolder() {
        // required by author
    }

    public String getMessageId() {
        return messageId;
    }

    public String getText() {
        return text;
    }

    public List<String> getVariables() {
        return variables;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public void setText(String text) {
        this.text = text;
    }

}
