/*-
 * ============LICENSE_START=======================================================
 * policy-yaml
 * ================================================================================
 * Copyright (C) 2017-2018 AT&T Intellectual Property. All rights reserved.
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

package org.onap.policy.controlloop.policy.builder.impl;

import java.util.LinkedList;
import java.util.List;

import org.onap.policy.controlloop.policy.builder.Message;
import org.onap.policy.controlloop.policy.builder.Results;

public class ResultsImpl implements Results {

    private String specification;
    private List<Message> messages = new LinkedList<>();

    @Override
    public List<Message> getMessages() {
        return messages;
    }

    @Override
    public String getSpecification() {
        return specification;
    }

    @Override
    public boolean isValid() {
        return (this.specification != null);
    }

    public void addMessage(Message message) {
        this.messages.add(message);
    }

    public void setSpecification(String spec) {
        this.specification = spec;
    }
}
