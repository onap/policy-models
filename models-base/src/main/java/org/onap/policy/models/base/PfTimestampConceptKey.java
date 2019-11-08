/*-
 * ============LICENSE_START=======================================================
 * ONAP Policy Model
 * ================================================================================
 * Copyright (C) 2019 Nordix Foundation.
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

import java.util.Date;
import javax.persistence.Column;
import javax.persistence.Embeddable;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;
import org.onap.policy.common.utils.validation.Assertions;
import org.onap.policy.models.base.PfValidationResult.ValidationResult;

@Embeddable
@Getter
@ToString
@EqualsAndHashCode(callSuper = true)
public class PfTimestampConceptKey extends PfConceptKey {
    private static final long serialVersionUID = -8410208962541783805L;

    private static final String NAME_TOKEN = "name";
    private static final String VERSION_TOKEN = "version";
    private static final String TIMESTAMP_TOKEN = "timeStamp";

    @Column(name = NAME_TOKEN, length = 120)
    private String name;

    @Column(name = VERSION_TOKEN, length = 20)
    private String version;

    @Column(name = TIMESTAMP_TOKEN)
    private Date timeStamp;


    /**
     * The default constructor creates a null concept key.
     */
    public PfTimestampConceptKey() {
        this(NULL_KEY_NAME, NULL_KEY_VERSION, new Date(0));
    }

    /**
     * Copy constructor.
     *
     * @param copyConcept the concept to copy from
     */
    public PfTimestampConceptKey(@NonNull final PfTimestampConceptKey copyConcept) {
        super(copyConcept);
        this.name = copyConcept.name;
        this.version = copyConcept.version;
        this.timeStamp = new Date(copyConcept.getTimeStamp().getTime());
    }

    /**
     * Constructor to create a key with the specified name and version.
     *
     * @param name the key name
     * @param version the key version
     * @param timeStamp the timestamp of key
     */
    public PfTimestampConceptKey(@NonNull final String name, @NonNull final String version,
            @NonNull final Date timeStamp) {
        super(name, version);
        this.name = name;
        this.version = version;
        this.timeStamp = new Date(timeStamp.getTime());
    }

    @Override
    public String getId() {
        return getName() + ':' + getVersion() + ':' + getTimeStamp().getTime();
    }

    @Override
    public boolean isNewerThan(@NonNull PfKey otherKey) {
        Assertions.instanceOf(otherKey, PfTimestampConceptKey.class);

        final PfTimestampConceptKey otherConceptKey = (PfTimestampConceptKey) otherKey;

        if (this.equals(otherConceptKey)) {
            return false;
        }

        if (!timeStamp.equals(otherConceptKey.timeStamp)) {
            return timeStamp.after(otherConceptKey.timeStamp);
        }

        return super.isNewerThan(otherKey);
    }

    public void setTimeStamp(@NonNull final Date timeStamp) {
        this.timeStamp = new Date(timeStamp.getTime());
    }

    @Override
    public boolean isNullKey() {
        return this.equals(new PfTimestampConceptKey(PfKey.NULL_KEY_NAME, PfKey.NULL_KEY_VERSION, new Date(0)));
    }

    @Override
    public PfValidationResult validate(@NonNull final PfValidationResult result) {
        super.validate(result);
        try {
            Assertions.argumentNotNull(timeStamp, "timeStamp must not be null");
        } catch (IllegalArgumentException e) {
            result.addValidationMessage(new PfValidationMessage(this, this.getClass(), ValidationResult.INVALID,
                    "timeStamp invalid-" + e.getMessage()));
        }
        return result;
    }

    @Override
    public int compareTo(@NonNull final PfConcept otherObj) {
        int result = super.compareTo(otherObj);
        if (0 == result) {
            final PfTimestampConceptKey other = (PfTimestampConceptKey) otherObj;
            return timeStamp.compareTo(other.timeStamp);
        }
        return result;
    }

}
