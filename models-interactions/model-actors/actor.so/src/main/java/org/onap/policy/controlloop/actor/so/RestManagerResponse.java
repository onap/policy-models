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

package org.onap.policy.controlloop.actor.so;

import java.lang.annotation.Annotation;
import java.net.URI;
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import javax.ws.rs.core.EntityTag;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.Link;
import javax.ws.rs.core.Link.Builder;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.NewCookie;
import javax.ws.rs.core.Response;
import lombok.Getter;
import org.onap.policy.common.utils.coder.Coder;
import org.onap.policy.common.utils.coder.CoderException;
import org.onap.policy.rest.RestManager.Pair;

/**
 * RestManager Response suitable for use with subclasses of HttpOperation. Only a couple
 * of methods are implemented; the rest throw {@link UnsupportedOperationException}.
 */
public class RestManagerResponse extends Response {
    // TODO move to actorServices

    @Getter
    private final int status;

    private final String body;
    private final Coder coder;

    /**
     * Constructs the object.
     *
     * @param result the result from the RestManager request
     * @param coder coder to decode the entity body
     */
    public RestManagerResponse(Pair<Integer, String> result, Coder coder) {
        this.status = result.first;
        this.body = result.second;
        this.coder = coder;
    }

    @Override
    public <T> T readEntity(Class<T> entityType) {
        if (entityType == String.class) {
            return entityType.cast(body);
        }

        try {
            return coder.decode(body, entityType);
        } catch (CoderException e) {
            throw new IllegalArgumentException("cannot decode response", e);
        }
    }

    @Override
    public <T> T readEntity(GenericType<T> entityType) {
        throw new UnsupportedOperationException();
    }

    @Override
    public <T> T readEntity(Class<T> entityType, Annotation[] annotations) {
        throw new UnsupportedOperationException();
    }

    @Override
    public <T> T readEntity(GenericType<T> entityType, Annotation[] annotations) {
        throw new UnsupportedOperationException();
    }

    @Override
    public StatusType getStatusInfo() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Object getEntity() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean hasEntity() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean bufferEntity() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void close() {
        // do nothing
    }

    @Override
    public MediaType getMediaType() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Locale getLanguage() {
        throw new UnsupportedOperationException();
    }

    @Override
    public int getLength() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Set<String> getAllowedMethods() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Map<String, NewCookie> getCookies() {
        throw new UnsupportedOperationException();
    }

    @Override
    public EntityTag getEntityTag() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Date getDate() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Date getLastModified() {
        throw new UnsupportedOperationException();
    }

    @Override
    public URI getLocation() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Set<Link> getLinks() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean hasLink(String relation) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Link getLink(String relation) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Builder getLinkBuilder(String relation) {
        throw new UnsupportedOperationException();
    }

    @Override
    public MultivaluedMap<String, Object> getMetadata() {
        throw new UnsupportedOperationException();
    }

    @Override
    public MultivaluedMap<String, String> getStringHeaders() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getHeaderString(String name) {
        throw new UnsupportedOperationException();
    }
}
