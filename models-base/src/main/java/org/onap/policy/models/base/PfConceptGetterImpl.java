/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2019-2020 Nordix Foundation.
 *  Modifications Copyright (C) 2021 AT&T Intellectual Property. All rights reserved.
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

import java.util.NavigableMap;
import java.util.Set;
import java.util.TreeSet;
import lombok.AllArgsConstructor;
import org.onap.policy.common.utils.validation.Assertions;

/**
 * Implements concept getting from navigable maps.
 *
 * @param <C> the type of concept on which the interface implementation is applied.
 */
@AllArgsConstructor
public class PfConceptGetterImpl<C> implements PfConceptGetter<C> {

    // The map from which to get concepts
    private final NavigableMap<PfConceptKey, C> conceptMap;


    @Override
    public C get(final PfConceptKey conceptKey) {
        return conceptMap.get(conceptKey);
    }

    @Override
    public C get(final String conceptKeyName) {
        Assertions.argumentNotNull(conceptKeyName, "conceptKeyName may not be null");

        // The very fist key that could have this name
        final var lowestArtifactKey = new PfConceptKey(conceptKeyName, PfKey.NULL_KEY_VERSION);

        // Check if we found a key for our name
        PfConceptKey foundKey = conceptMap.ceilingKey(lowestArtifactKey);
        if (foundKey == null || !foundKey.getName().equals(conceptKeyName)) {
            return null;
        }

        // Look for higher versions of the key
        do {
            final PfConceptKey nextkey = conceptMap.higherKey(foundKey);
            if (nextkey == null || !nextkey.getName().equals(conceptKeyName)) {
                break;
            }
            foundKey = nextkey;
        } while (true);

        return conceptMap.get(foundKey);
    }

    @Override
    public C get(final String conceptKeyName, final String conceptKeyVersion) {
        Assertions.argumentNotNull(conceptKeyName, "conceptKeyName may not be null");

        if (conceptKeyVersion != null) {
            return conceptMap.get(new PfConceptKey(conceptKeyName, conceptKeyVersion));
        } else {
            return this.get(conceptKeyName);
        }
    }

    @Override
    public Set<C> getAll(final String conceptKeyName) {
        return getAll(conceptKeyName, null);
    }

    @Override
    public Set<C> getAll(final String conceptKeyName, final String conceptKeyVersion) {
        final Set<C> returnSet = new TreeSet<>();

        if (conceptKeyName == null) {
            returnSet.addAll(conceptMap.values());
            return returnSet;
        }

        // The very fist key that could have this name
        final var lowestArtifactKey = new PfConceptKey(conceptKeyName, PfKey.NULL_KEY_VERSION);
        if (conceptKeyVersion != null) {
            lowestArtifactKey.setVersion(conceptKeyVersion);
        }

        // Check if we found a key for our name
        PfConceptKey foundKey = conceptMap.ceilingKey(lowestArtifactKey);
        if (foundKey == null || !foundKey.getName().equals(conceptKeyName)) {
            return returnSet;
        }
        returnSet.add(conceptMap.get(foundKey));

        // Look for higher versions of the key
        do {
            foundKey = conceptMap.higherKey(foundKey);
            if (foundKey == null || !foundKey.getName().equals(conceptKeyName)) {
                break;
            }
            returnSet.add(conceptMap.get(foundKey));
        } while (true);

        return returnSet;
    }
}
