/*-
 * ============LICENSE_START=======================================================
 * Copyright (C) 2021, 2023 Nordix Foundation.
 * Modifications Copyright (C) 2021 AT&T Intellectual Property. All rights reserved.
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
import jakarta.persistence.Embedded;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import java.io.Serial;
import java.time.Instant;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import org.onap.policy.common.parameters.annotations.NotNull;
import org.onap.policy.common.utils.validation.Assertions;

/**
 * This class is an extension of PfReferenceKey. It has similar behaviour as of PfReferenceKey with an
 * additional option to have timestamp as a parameter.
 *
 */

@Embeddable
@Data
@EqualsAndHashCode(callSuper = false)
public class PfReferenceTimestampKey extends PfKey {
    @Serial
    private static final long serialVersionUID = 1130918285832617215L;

    private static final String TIMESTAMP_TOKEN = "timeStamp";

    @Column(name = TIMESTAMP_TOKEN, precision = 3)
    @Temporal(TemporalType.TIMESTAMP)
    @NotNull
    private Date timeStamp;

    @Embedded
    @Column
    private PfReferenceKey referenceKey;

    /**
     * The default constructor creates a null reference timestamp key.
     */
    public PfReferenceTimestampKey() {
        this.referenceKey = new PfReferenceKey();
        this.timeStamp = new Date(0);
    }

    /**
     * The Copy Constructor creates a key by copying another key.
     *
     * @param referenceTimestampKey
     *        the reference key to copy from
     */
    public PfReferenceTimestampKey(final PfReferenceTimestampKey referenceTimestampKey) {
        this.referenceKey = referenceTimestampKey.getReferenceKey();
        this.timeStamp = referenceTimestampKey.getTimeStamp();
    }

    /**
     * Constructor to create a null reference key for the specified parent concept key.
     *
     * @param pfConceptKey
     *        the parent concept key of this reference key
     */
    public PfReferenceTimestampKey(final PfConceptKey pfConceptKey) {
        this.referenceKey = new PfReferenceKey(pfConceptKey);
        this.timeStamp = new Date(0);
    }

    /**
     * Constructor to create a reference timestamp key for the given parent concept key with the given local name.
     *
     * @param pfConceptKey
     *        the parent concept key of this reference key
     * @param localName
     *        the local name of this reference key
     * @param instant
     *        the time stamp for this reference key
     */
    public PfReferenceTimestampKey(final PfConceptKey pfConceptKey, final String localName, final Instant instant) {
        this.referenceKey = new PfReferenceKey(pfConceptKey, localName);
        this.timeStamp = Date.from(instant);
    }

    /**
     * Constructor to create a reference timestamp key for the given parent reference key with the given local name.
     *
     * @param parentReferenceKey
     *        the parent reference key of this reference key
     * @param localName
     *        the local name of this reference key
     * @param instant
     *        the time stamp for this reference key
     */
    public PfReferenceTimestampKey(final PfReferenceKey parentReferenceKey, final String localName,
                                   final Instant instant) {
        this.referenceKey = new PfReferenceKey(parentReferenceKey, localName);
        this.timeStamp = Date.from(instant);
    }

    /**
     * Constructor to create a reference timestamp key for the given parent reference key (specified by the parent
     * reference key's concept key and local name) with the given local name.
     *
     * @param pfConceptKey
     *        the concept key of the parent reference key of this reference key
     * @param parentLocalName
     *        the local name of the parent reference key of this reference key
     * @param localName
     *        the local name of this reference key
     * @param instant
     *        the time stamp for this reference key
     */
    public PfReferenceTimestampKey(final PfConceptKey pfConceptKey, final String parentLocalName,
                                   final String localName, final Instant instant) {
        this.referenceKey = new PfReferenceKey(pfConceptKey, parentLocalName, localName);
        this.timeStamp = Date.from(instant);
    }

    /**
     * Constructor to create a reference timestamp key for the given parent concept key (specified by the
     * parent concept key's name and version) with the given local name.
     *
     * @param parentKeyName
     *        the name of the parent concept key of this reference key
     * @param parentKeyVersion
     *        the version of the parent concept key of this reference key
     * @param localName
     *        the local name of this reference key
     * @param instant
     *        the time stamp for this reference key
     */
    public PfReferenceTimestampKey(final String parentKeyName, final String parentKeyVersion, final String localName,
                                   final Instant instant) {
        this.referenceKey = new PfReferenceKey(parentKeyName, parentKeyVersion, PfKey.NULL_KEY_NAME, localName);
        this.timeStamp = Date.from(instant);
    }

    /**
     * Constructor to create a reference timestamp key for the given parent key (specified by the parent key's name,
     * version and local name) with the given local name.
     *
     * @param parentKeyName
     *        the parent key name of this reference key
     * @param parentKeyVersion
     *        the parent key version of this reference key
     * @param parentLocalName
     *        the parent local name of this reference key
     * @param localName
     *        the local name of this reference key
     * @param instant
     *        the instant for this reference key
     */
    public PfReferenceTimestampKey(final String parentKeyName, final String parentKeyVersion,
                                   final String parentLocalName, final String localName, final Instant instant) {
        this.referenceKey = new PfReferenceKey(parentKeyName, parentKeyVersion, parentLocalName, localName);
        this.timeStamp = Date.from(instant);
    }


    /**
     * Constructor to create a key using the key and version from the specified key ID.
     *
     * @param id the key ID in a format that respects the KEY_ID_REGEXP
     */
    public PfReferenceTimestampKey(final String id) {
        this.referenceKey = new PfReferenceKey(id.substring(0, id.lastIndexOf(':')));
        this.timeStamp = new Date(Long.parseLong(id.substring(id.lastIndexOf(':') + 1)));
    }


    /**
     * Get a null reference timestamp key.
     *
     * @return a null reference key
     */
    public static PfReferenceTimestampKey getNullKey() {
        return new PfReferenceTimestampKey(PfKey.NULL_KEY_NAME, PfKey.NULL_KEY_VERSION, PfKey.NULL_KEY_NAME,
            PfKey.NULL_KEY_NAME, Instant.EPOCH);
    }

    public Instant getInstant() {
        return timeStamp.toInstant();
    }

    public void setInstant(final Instant instant) {
        setTimeStamp(Date.from(instant));
    }

    /**
     * Get the key of this reference.
     *
     * @return the pfReferenceTimestamp key
     */
    @Override
    public PfReferenceTimestampKey getKey() {
        return this;
    }

    /**
     * Get the key as a string.
     * @return pfReferenceTimestamp key.
     */
    @Override
    public String getId() {
        return getReferenceKey().getId() + ':' + getTimeStamp().getTime();
    }


    /**
     * Check if this key is a newer version than the other key.
     *
     * @param otherKey the key to check against
     * @return true, if this key is newer than the other key
     */
    @Override
    public boolean isNewerThan(@NonNull PfKey otherKey) {
        Assertions.instanceOf(otherKey, PfReferenceTimestampKey.class);
        final PfReferenceTimestampKey otherReferenceKey = (PfReferenceTimestampKey) otherKey;
        if (!getTimeStamp().equals(otherReferenceKey.timeStamp)) {
            return timeStamp.after(otherReferenceKey.timeStamp);
        }
        return getReferenceKey().isNewerThan(otherReferenceKey.getReferenceKey());
    }

    @Override
    public boolean isNullKey() {
        return getReferenceKey().isNullKey() && getTimeStamp().getTime() == 0;
    }

    @Override
    public int getMajorVersion() {
        return getReferenceKey().getMajorVersion();
    }

    @Override
    public int getMinorVersion() {
        return getReferenceKey().getMinorVersion();
    }

    @Override
    public int getPatchVersion() {
        return getReferenceKey().getPatchVersion();
    }


    @Override
    public int compareTo(@NonNull final PfConcept otherObj) {
        if (this == otherObj) {
            return 0;
        }
        if (getClass() != otherObj.getClass()) {
            return getClass().getName().compareTo(otherObj.getClass().getName());
        }
        int result = getReferenceKey().compareTo(((PfReferenceTimestampKey) otherObj).getReferenceKey());
        if (0 == result) {
            return getTimeStamp().compareTo(((PfReferenceTimestampKey) otherObj).timeStamp);
        }
        return result;
    }

    @Override
    public List<PfKey> getKeys() {
        return Collections.singletonList(getKey());
    }

    @Override
    public void clean() {
        getReferenceKey().clean();
    }

    @Override
    public Compatibility getCompatibility(@NonNull PfKey otherKey) {
        return getReferenceKey().getCompatibility(otherKey);
    }

    @Override
    public boolean isCompatible(@NonNull PfKey otherKey) {
        if (!(otherKey instanceof PfReferenceTimestampKey otherReferenceKey)) {
            return false;
        }

        return this.getReferenceKey().getParentConceptKey().isCompatible(otherReferenceKey.getReferenceKey()
            .getParentConceptKey());
    }
}
