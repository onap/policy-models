/*-
 * ============LICENSE_START=======================================================
 * ONAP Policy Model
 * ================================================================================
 * Copyright (C) 2019-2021, 2023 Nordix Foundation.
 * Modifications Copyright (C) 2020-2021 AT&T Intellectual Property. All rights reserved.
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

package org.onap.policy.models.base;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import java.io.Serial;
import java.time.Instant;
import java.util.Date;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import org.onap.policy.common.parameters.annotations.Pattern;
import org.onap.policy.common.utils.validation.Assertions;

@Embeddable
@Data
@EqualsAndHashCode(callSuper = false)
public class PfTimestampKey extends PfKeyImpl {
    @Serial
    private static final long serialVersionUID = -8410208962541783805L;

    private static final String TIMESTAMP_TOKEN = "timeStamp";

    @Column(name = NAME_TOKEN, length = 120)
    @Pattern(regexp = NAME_REGEXP)
    private String name;

    @Column(name = VERSION_TOKEN, length = 20)
    @Pattern(regexp = VERSION_REGEXP)
    private String version;

    @Column(name = TIMESTAMP_TOKEN, precision = 3)
    @Temporal(TemporalType.TIMESTAMP)
    @NonNull
    private Date timeStamp;


    /**
     * The default constructor creates a null concept key.
     */
    public PfTimestampKey() {
        this(NULL_KEY_NAME, NULL_KEY_VERSION, Instant.EPOCH);
    }

    /**
     * Copy constructor.
     *
     * @param copyConcept the concept to copy from
     */
    public PfTimestampKey(@NonNull final PfTimestampKey copyConcept) {
        super(copyConcept);
        this.timeStamp = copyConcept.getTimeStamp();
    }

    /**
     * Constructor to create a key with the specified name and version.
     *
     * @param name    the key name
     * @param version the key version
     * @param instant the time stamp of key
     */
    public PfTimestampKey(@NonNull final String name, @NonNull final String version,
                          @NonNull final Instant instant) {
        super(name, version);
        this.timeStamp = Date.from(instant);
    }

    /**
     * Constructor to create a key using the key and version from the specified key ID.
     *
     * @param id the key ID in a format that respects the KEY_ID_REGEXP
     */
    public PfTimestampKey(final String id) {
        super(id.substring(0, id.lastIndexOf(':')));
        this.timeStamp = new Date(Long.parseLong(id.substring(id.lastIndexOf(':') + 1)));
    }

    @Override
    public String getId() {
        return getName() + ':' + getVersion() + ':' + getTimeStamp().getTime();
    }

    /**
     * Get a null key.
     *
     * @return a null key
     */
    public static PfTimestampKey getNullKey() {
        return new PfTimestampKey(PfKey.NULL_KEY_NAME, PfKey.NULL_KEY_VERSION, Instant.EPOCH);
    }

    public Instant getInstant() {
        return timeStamp.toInstant();
    }

    public void setInstant(final Instant instant) {
        setTimeStamp(Date.from(instant));
    }

    @Override
    public boolean isNewerThan(@NonNull PfKey otherKey) {
        Assertions.instanceOf(otherKey, PfTimestampKey.class);

        final PfTimestampKey otherConceptKey = (PfTimestampKey) otherKey;

        if (this.equals(otherConceptKey)) {
            return false;
        }

        if (!timeStamp.equals(otherConceptKey.timeStamp)) {
            return timeStamp.after(otherConceptKey.timeStamp);
        }

        return super.isNewerThan(otherKey);
    }

    @Override
    public boolean isNullKey() {
        return super.isNullKey() && getTimeStamp().getTime() == 0;
    }

    @Override
    public int compareTo(@NonNull final PfConcept otherObj) {
        int result = super.compareTo(otherObj);
        if (0 == result) {
            final PfTimestampKey other = (PfTimestampKey) otherObj;
            return timeStamp.compareTo(other.timeStamp);
        }
        return result;
    }

    @Override
    public void setName(@NonNull String name) {
        this.name = Assertions.validateStringParameter(NAME_TOKEN, name, getNameRegEx());
    }

    @Override
    public void setVersion(@NonNull String version) {
        this.version = Assertions.validateStringParameter(VERSION_TOKEN, version, getVersionRegEx());
    }

}
