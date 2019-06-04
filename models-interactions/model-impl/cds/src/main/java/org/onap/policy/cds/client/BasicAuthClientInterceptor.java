/*-
 * ============LICENSE_START=======================================================
 * Copyright (C) 2017 - 2019 Bell Canada.
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
import org.onap.policy.cds.properties.CdsProperties;

/**
 * An interceptor to handle client authHeader.
 *
 * <p>The {@link BasicAuthClientInterceptor} implements {@link ClientInterceptor} in order to insert the authorization
 * header data provided by {@link CdsProperties#getBasicAuth()} to all the outgoing
 * calls.</p>
 *
 * <p>On the client context, we add metadata with "Authorization" as the key and "Basic" followed by base64 encoded
 * username:password as its value.
 * On the server side, CDS BasicAuthServerInterceptor (1) gets the client metadata from the server context, (2) extract
 * the "Authorization" header key and finally (3) decodes the username and password from the authHeader.</p>
 */
public class BasicAuthClientInterceptor implements ClientInterceptor {

    private CdsProperties props;

    BasicAuthClientInterceptor(CdsProperties props) {
        this.props = props;
    }

    @Override
    public <ReqT, RespT> ClientCall<ReqT, RespT> interceptCall(MethodDescriptor<ReqT, RespT> method,
        CallOptions callOptions, Channel channel) {
        Key<String> authHeader = Key.of("Authorization", Metadata.ASCII_STRING_MARSHALLER);
        return new ForwardingClientCall.SimpleForwardingClientCall<ReqT, RespT>(channel.newCall(method, callOptions)) {
            @Override
            public void start(Listener<RespT> responseListener, Metadata headers) {
                headers.put(authHeader, props.getBasicAuth());
                super.start(responseListener, headers);
            }
        };
    }
}

