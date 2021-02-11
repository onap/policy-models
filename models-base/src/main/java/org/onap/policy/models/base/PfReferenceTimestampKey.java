/*-
 * ============LICENSE_START=======================================================
 * Copyright (C) 2021 Nordix Foundation.
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


import java.util.Collections;
import java.util.Date;
import java.util.List;
import javax.persistence.Column;
import javax.persistence.Embeddable;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
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
@Getter
@EqualsAndHashCode(callSuper = false)
public class PfReferenceTimestampKey extends PfReferenceKey {
    private static final String TIMESTAMP_TOKEN = "timeStamp";

    @Column(name = TIMESTAMP_TOKEN)
    @NotNull
    private Date timeStamp;

    /**
     * The default constructor creates a null reference timestamp key.
     */
    public PfReferenceTimestampKey() {
        super();
        this.timeStamp = new Date(0);
    }

    /**
     * The Copy Constructor creates a key by copying another key.
     *
     * @param referenceKey
     *        the reference key to copy from
     */
    public PfReferenceTimestampKey(final PfReferenceTimestampKey referenceKey) {
        super(referenceKey.getParentKeyName(), referenceKey.getParentKeyVersion(), referenceKey.getParentLocalName(),
            referenceKey.getLocalName());
        this.timeStamp = new Date(referenceKey.getTimeStamp().getTime());
    }

    /**
     * Constructor to create a null reference key for the specified parent concept key.
     *
     * @param pfConceptKey
     *        the parent concept key of this reference key
     */
    public PfReferenceTimestampKey(final PfConceptKey pfConceptKey) {
        super(pfConceptKey);
        this.timeStamp = new Date(0);
    }

    /**
     * Constructor to create a reference timestamp key for the given parent concept key with the given local name.
     *
     * @param pfConceptKey
     *        the parent concept key of this reference key
     * @param localName
     *        the local name of this reference key
     * @param date
     *        the timestamp for this reference key
     */
    public PfReferenceTimestampKey(final PfConceptKey pfConceptKey, final String localName, final Date date) {
        super(pfConceptKey, localName);
        this.timeStamp = new Date(date.getTime());
    }

    /**
     * Constructor to create a reference timestamp key for the given parent reference key with the given local name.
     *
     * @param parentReferenceKey
     *        the parent reference key of this reference key
     * @param localName
     *        the local name of this reference key
     * @param date
     *        the timestamp for this reference key
     */
    public PfReferenceTimestampKey(final PfReferenceKey parentReferenceKey, final String localName, final Date date) {
        super(parentReferenceKey, localName);
        this.timeStamp = new Date(date.getTime());
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
     * @param date
     *        the timestamp for this reference key
     */
    public PfReferenceTimestampKey(final PfConceptKey pfConceptKey, final String parentLocalName,
                                   final String localName, final Date date) {
        super(pfConceptKey, parentLocalName, localName);
        this.timeStamp = new Date(date.getTime());
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
     * @param date
     *        the timestamp for this reference key
     */
    public PfReferenceTimestampKey(final String parentKeyName, final String parentKeyVersion, final String localName,
                                   final Date date) {
        super(parentKeyName, parentKeyVersion, NULL_KEY_NAME, localName);
        this.timeStamp = new Date(date.getTime());
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
     * @param date
     *        the timestamp for this reference key
     */
    public PfReferenceTimestampKey(final String parentKeyName, final String parentKeyVersion,
                                   final String parentLocalName, final String localName, final Date date) {
        super(parentKeyName, parentKeyVersion, parentLocalName, localName);
        this.timeStamp = new Date(date.getTime());
    }


    /**
     * Constructor to create a key using the key and version from the specified key ID.
     *
     * @param id the key ID in a format that respects the KEY_ID_REGEXP
     */
    public PfReferenceTimestampKey(final String id) {
        super(id.substring(0, id.lastIndexOf(':')));
        this.timeStamp = new Date(Long.parseLong(id.substring(id.lastIndexOf(':') + 1)));
    }


    /**
     * Get a null reference timestamp key.
     *
     * @return a null reference key
     */
    public static PfReferenceTimestampKey getNullKey() {
        return new PfReferenceTimestampKey(PfKey.NULL_KEY_NAME, PfKey.NULL_KEY_VERSION, PfKey.NULL_KEY_NAME,
            PfKey.NULL_KEY_NAME, new Date(0));
    }


    @Override
    public PfReferenceTimestampKey getKey() {
        return this;
    }

    @Override
    public List<PfKey> getKeys() {
        return Collections.singletonList(getKey());
    }

    @Override
    public String getId() {
        return super.getId() + ':' + timeStamp;
    }

    public void setTimeStamp(@NonNull final Date timeStamp) {
        this.timeStamp = new Date(timeStamp.getTime());
    }

    @Override
    public boolean isNullKey() {
        return super.isNullKey() && getTimeStamp().getTime() == 0;
    }

    @Override
    public boolean isNewerThan(@NonNull PfKey otherKey) {
        Assertions.instanceOf(otherKey, PfTimestampKey.class);

        final PfReferenceTimestampKey otherConceptKey = (PfReferenceTimestampKey) otherKey;

        if (this.equals(otherConceptKey)) {
            return false;
        }

        if (!timeStamp.equals(otherConceptKey.timeStamp)) {
            return timeStamp.after(otherConceptKey.timeStamp);
        }

        return super.isNewerThan(otherKey);
    }

    @Override
    public String toString() {
        return "PfReferenceTimestampKey(parentKeyName=" + getParentKeyName() + ", parentKeyVersion="
            + getParentKeyVersion() + ", parentLocalName=" + getParentLocalName() + ", localName="
            + getLocalName() + ", timeStamp=" + getTimeStamp().getTime() + ")";
    }
}
