/*
 * ============LICENSE_START=======================================================
 *
 * ================================================================================
 * Copyright (C) 2018 Nokia Intellectual Property. All rights reserved.
 * Modifications Copyright (C) 2018, 2021 AT&T Intellectual Property. All rights reserved.
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

package org.onap.policy.controlloop.actor.appclcm;

import com.google.common.collect.Lists;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import org.apache.commons.lang3.StringUtils;

@AllArgsConstructor
class AppcLcmRecipeFormatter {

    private final String dashCasedRecipe;

    String getUrlRecipe() {
        return dashCasedRecipe.toLowerCase();
    }

    String getBodyRecipe() {
        return Lists.newArrayList(dashCasedRecipe.split("-"))
                .stream()
                .map(String::toLowerCase)
                .map(StringUtils::capitalize)
                .collect(Collectors.joining(""));
    }
}
