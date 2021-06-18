/*-
 * ============LICENSE_START=======================================================
 * Copyright (C) 2019 Bell Canada.
 * Modifications Copyright (C) 2019, 2021 AT&T Intellectual Property. All rights reserved.
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

package org.onap.policy.cds.client;

import io.grpc.CallOptions;
import io.grpc.Channel;
import io.grpc.ClientCall;
import io.grpc.ClientInterceptor;
import io.grpc.ForwardingClientCall;
import io.grpc.Metadata;
import io.grpc.Metadata.Key;
import io.grpc.MethodDescriptor;
import lombok.AllArgsConstructor;
import org.onap.policy.cds.properties.CdsServerProperties;

/**
 * An interceptor to insert the client authHeader.
 *
 * <p>The {@link BasicAuthClientHeaderInterceptor} implements {@link ClientInterceptor} to insert authorization
 * header data provided by {@link CdsServerProperties#getBasicAuth()} to all the outgoing calls.</p>
 *
 * <p>On the client context, we add metadata with "Authorization" as the key and "Basic" followed by base64 encoded
 * username:password as its value.
 * On the server side, CDS BasicAuthServerInterceptor (1) gets the client metadata from the server context, (2) extract
 * the "Authorization" header key and finally (3) decodes the username and password from the authHeader.</p>
 */
@AllArgsConstructor
public class BasicAuthClientHeaderInterceptor implements ClientInterceptor {

    static final String BASIC_AUTH_HEADER_KEY = "Authorization";
    private CdsServerProperties props;

    @Override
    public <Q, P> ClientCall<Q, P> interceptCall(MethodDescriptor<Q, P> method,
        CallOptions callOptions, Channel channel) {
        Key<String> authHeader = Key.of(BASIC_AUTH_HEADER_KEY, Metadata.ASCII_STRING_MARSHALLER);
        return new ForwardingClientCall.SimpleForwardingClientCall<Q, P>(channel.newCall(method, callOptions)) {
            @Override
            public void start(Listener<P> responseListener, Metadata headers) {
                headers.put(authHeader, props.getBasicAuth());
                super.start(responseListener, headers);
            }
        };
    }
}

