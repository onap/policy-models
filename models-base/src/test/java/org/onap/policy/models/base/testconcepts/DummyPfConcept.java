/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2019, 2023 Nordix Foundation.
 *  Modifications Copyright (C) 2019-2020 AT&T Intellectual Property. All rights reserved.
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

package org.onap.policy.models.base.testconcepts;

import jakarta.persistence.EmbeddedId;
import java.io.Serial;
import java.util.List;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import org.apache.commons.lang3.ObjectUtils;
import org.onap.policy.common.parameters.annotations.NotBlank;
import org.onap.policy.common.parameters.annotations.NotNull;
import org.onap.policy.models.base.PfAuthorative;
import org.onap.policy.models.base.PfConcept;
import org.onap.policy.models.base.PfConceptKey;
import org.onap.policy.models.base.PfKey;
import org.onap.policy.models.base.validation.annotations.VerifyKey;

@Data
@EqualsAndHashCode(callSuper = false)
public class DummyPfConcept extends PfConcept implements PfAuthorative<DummyAuthorativeConcept> {
    @Serial
    private static final long serialVersionUID = 1L;

    @EmbeddedId
    @VerifyKey
    @NotNull
    private PfConceptKey key;

    @NotBlank
    private String description;


    /**
     * The Default Constructor creates a {@link DummyPfConcept} object with a null key.
     */
    public DummyPfConcept() {
        this(new PfConceptKey());
    }

    /**
     * The Key Constructor creates a {@link DummyPfConcept} object with the given concept key.
     *
     * @param key the key
     */
    public DummyPfConcept(@NonNull final PfConceptKey key) {
        this.key = key;
    }

    /**
     * Copy constructor.
     *
     * @param copyConcept the concept to copy from
     */
    public DummyPfConcept(final DummyPfConcept copyConcept) {
        super(copyConcept);
        this.key = new PfConceptKey(copyConcept.key);
        this.description = copyConcept.description;
    }

    @Override
    public DummyAuthorativeConcept toAuthorative() {
        DummyAuthorativeConcept dac = new DummyAuthorativeConcept();
        dac.setName(key.getName());
        dac.setVersion(key.getVersion());
        dac.setDescription(description);

        return dac;
    }

    @Override
    public void fromAuthorative(DummyAuthorativeConcept dac) {
        key.setName(dac.getName());
        key.setVersion(dac.getVersion());
        description = dac.getDescription();
    }

    @Override
    public List<PfKey> getKeys() {
        return getKey().getKeys();
    }

    @Override
    public void clean() {
        key.clean();

        description = (description != null ? description.trim() : null);
    }

    @Override
    public int compareTo(final PfConcept otherConcept) {
        if (otherConcept == null) {
            return -1;
        }
        if (this == otherConcept) {
            return 0;
        }
        if (getClass() != otherConcept.getClass()) {
            return this.hashCode() - otherConcept.hashCode();
        }

        final DummyPfConcept other = (DummyPfConcept) otherConcept;
        if (!key.equals(other.key)) {
            return key.compareTo(other.key);
        }

        return ObjectUtils.compare(description, other.description);
    }
}
