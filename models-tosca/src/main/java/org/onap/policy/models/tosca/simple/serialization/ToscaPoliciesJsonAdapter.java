/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2019 Nordix Foundation.
 *  Copyright (C) 2019 AT&T Intellectual Property. All rights reserved.
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

package org.onap.policy.models.tosca.simple.serialization;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import java.lang.reflect.Type;
import java.util.Iterator;
import lombok.NonNull;

import org.onap.policy.models.base.PfConceptKey;
import org.onap.policy.models.tosca.simple.concepts.ToscaPolicies;
import org.onap.policy.models.tosca.simple.concepts.ToscaPolicy;

/**
 * GSON type adapter for TOSCA policies.
 *
 * @author Liam Fallon (liam.fallon@est.tech)
 * @author Chenfei Gao (cgao@research.att.com)
 */
public class ToscaPoliciesJsonAdapter implements JsonSerializer<ToscaPolicies>, JsonDeserializer<ToscaPolicies> {

    @Override
    public ToscaPolicies deserialize(@NonNull final JsonElement policiesElement, @NonNull final Type type,
            @NonNull final JsonDeserializationContext context) {
        // The incoming JSON
        final JsonArray policiesJsonArray = policiesElement.getAsJsonArray();

        // The outgoing object
        final PfConceptKey policiesKey = new PfConceptKey("IncomingPolicies", "0.0.1");
        final ToscaPolicies policies = new ToscaPolicies(policiesKey);

        // Get the policies
        for (Iterator<JsonElement> policiesIterator = policiesJsonArray.iterator(); policiesIterator.hasNext(); ) {
            ToscaPolicy policy = new ToscaPolicyJsonAdapter()
                    .deserialize(policiesIterator.next(), ToscaPolicy.class, context);

            policies.getConceptMap().put(policy.getKey(), policy);
        }

        return policies;
    }

    @Override
    public JsonElement serialize(@NonNull final ToscaPolicies policies, @NonNull final Type type,
            @NonNull final JsonSerializationContext context) {

        JsonArray policiesJsonArray = new JsonArray();

        for (ToscaPolicy policy: policies.getConceptMap().values()) {
            policiesJsonArray.add(new ToscaPolicyJsonAdapter().serialize(policy, type, context));
        }
        return policiesJsonArray;
    }
}
