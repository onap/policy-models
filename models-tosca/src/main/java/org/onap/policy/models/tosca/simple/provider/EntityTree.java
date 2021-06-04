/*-
 * ============LICENSE_START=======================================================
 * ONAP
 * ================================================================================
 * Copyright (C) 2021 AT&T Intellectual Property. All rights reserved.
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

package org.onap.policy.models.tosca.simple.provider;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.onap.policy.models.base.PfConceptKey;
import org.onap.policy.models.base.PfKey;
import org.onap.policy.models.tosca.simple.provider.EntityKey.NodeType;

/**
 * Tree of entities, containing cross references.
 */
public class EntityTree {
    private final Map<EntityKey, Object> key2data = new HashMap<>();
    private final Map<EntityKey, Set<EntityKey>> references = new HashMap<>();
    private final Map<EntityKey, Set<EntityKey>> referencedBy = new HashMap<>();

    /**
     * Note: key.version is always null, while value.version is the latest version number.
     */
    private final Map<EntityKey, EntityKey> latest = new HashMap<>();

    /**
     * Constructs the object.
     */
    public EntityTree() {
        // do nothing
    }

    /**
     * Gets the data associated with an entity key.
     *
     * @param <T> class of entity to get
     * @param key desired entity
     * @return the entity, or {@code null} if it is not found
     */
    @SuppressWarnings("unchecked")
    public <T> T get(EntityKey key) {
        return (T) getOptVersion(key, key2data);
    }

    private <T> T getOptVersion(EntityKey key, Map<EntityKey, T> map) {
        T value = map.get(key);
        if (value != null) {
            return value;
        }

        if (key.getIdent().getVersion() == null || key.getIdent().isNullVersion()) {
            return map.get(latest.get(key.withNullVersion()));
        }

        PfConceptKey keyWithNullVersion = new PfConceptKey(key.getIdent().getName(), PfKey.NULL_KEY_VERSION);
        return map.get(new EntityKey(key.getType(), keyWithNullVersion));
    }

    /**
     * Gets the set of entities that the given entity references.
     *
     * @param key source entity key
     * @param types types of entities to retrieve
     * @return the entities that the entity references
     */
    public Set<EntityKey> getUses(EntityKey key, NodeType...types) {
        var result = getOptVersion(key, references);
        if (result == null) {
            return Collections.emptySet();
        }

        List<NodeType> ltypes = Arrays.asList(types);
        return result.stream().filter(key2 -> ltypes.contains(key2.getType())).collect(Collectors.toSet());
    }

    /**
     * Gets the set of entities that reference the given entity.
     *
     * @param key referenced entity key
     * @param types types of entities to retrieve
     * @return the entities that reference the entity
     */
    public Set<EntityKey> getUsedBy(EntityKey key, NodeType...types) {
        var result = getOptVersion(key, referencedBy);
        if (result == null) {
            return Collections.emptySet();
        }

        List<NodeType> ltypes = Arrays.asList(types);
        return result.stream().filter(key2 -> ltypes.contains(key2.getType())).collect(Collectors.toSet());
    }

    /**
     * Adds a new entity to the tree.
     *
     * @param key entity to be added
     */
    public void add(EntityKey key, Object data) {
        key2data.put(key, data);
        latest.merge(key.withNullVersion(), key, (unused, oldValue) -> key.isNewerThan(oldValue) ? key : oldValue);
    }

    /**
     * Specifies the a source entity references a target entity.
     *
     * @param source source entity
     * @param target target entity
     */
    public void addReference(EntityKey source, EntityKey target) {
        references.computeIfAbsent(source, key -> new HashSet<>()).add(target);
        referencedBy.computeIfAbsent(target, key -> new HashSet<>()).add(source);
    }
}
