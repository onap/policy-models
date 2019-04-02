/*-
 * ============LICENSE_START=======================================================
 * TestActorServiceProvider
 * ================================================================================
 * Copyright (C) 2018 Ericsson. All rights reserved.
 * Modifications Copyright (C) 2019 Nordix Foundation.
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

package org.onap.policy.controlloop.actorserviceprovider;

import java.util.ArrayList;
import java.util.List;

import org.onap.policy.controlloop.actorserviceprovider.spi.Actor;

public class DummyActor implements Actor {
    @Override
    public String actor() {
        return this.getClass().getSimpleName();
    }

    @Override
    public List<String> recipes() {
        List<String> recipeList = new ArrayList<>();
        recipeList.add("Dorothy");
        recipeList.add("Wizard");

        return recipeList;
    }

    @Override
    public List<String> recipeTargets(String recipe) {
        List<String> recipeTargetList = new ArrayList<>();
        recipeTargetList.add("Wicked Witch");
        recipeTargetList.add("Wizard of Oz");

        return recipeTargetList;
    }

    @Override
    public List<String> recipePayloads(String recipe) {
        List<String> recipePayloadList = new ArrayList<>();
        recipePayloadList.add("Dorothy");
        recipePayloadList.add("Toto");

        return recipePayloadList;
    }
}
