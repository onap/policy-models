/*-
 * ============LICENSE_START=======================================================
 * aai
 * ================================================================================
 * Copyright (C) 2017-2019 AT&T Intellectual Property. All rights reserved.
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

package org.onap.policy.aai;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AaiNqResponseWrapper implements Serializable {
    private static final long serialVersionUID = 8411407444051746101L;
    
    private static final Logger logger = LoggerFactory.getLogger(AaiNqResponseWrapper.class);

    private static final Pattern VF_MODULE_NAME_PAT = Pattern.compile("(.*_)(\\d+)");

    private UUID requestId;
    private AaiNqResponse aaiNqResponse;

    public AaiNqResponseWrapper() {}

    public AaiNqResponseWrapper(UUID requestId, AaiNqResponse aaiNqResponse) {
        this.requestId = requestId;
        this.aaiNqResponse = aaiNqResponse;
    }

    public UUID getRequestId() {
        return requestId;
    }

    public void setRequestId(UUID requestId) {
        this.requestId = requestId;
    }

    public AaiNqResponse getAaiNqResponse() {
        return aaiNqResponse;
    }

    public void setAaiNqResponse(AaiNqResponse aaiNqResponse) {
        this.aaiNqResponse = aaiNqResponse;
    }

    /**
     * Counts the number of VF modules, if any, in the response.
     *
     * @return the number of VF modules, or {@code 0} if there are none
     */
    public int countVfModules() {
        return getVfModuleItems(false).size();
    }

    /**
     * Generates the name for the next VF module.
     *
     * @return the name of the next VF module, or {@code null} if the name could not be
     *         generated (i.e., because the response has no matching VF module names on
     *         which to model it)
     */
    public String genVfModuleName() {
        /*
         * Loop through the VF modules, extracting the name prefix and the largest number
         * suffix
         */
        String prefix = null;
        int maxSuffix = -1;

        for (AaiNqInventoryResponseItem item : getVfModuleItems(false)) {
            String name = item.getVfModule().getVfModuleName();
            Matcher matcher = VF_MODULE_NAME_PAT.matcher(name);
            if (matcher.matches()) {
                int suffix = Integer.parseInt(matcher.group(2));
                if (suffix > maxSuffix) {
                    maxSuffix = suffix;
                    prefix = matcher.group(1);
                }
            }
        }

        ++maxSuffix;

        return (prefix == null ? null : prefix + maxSuffix);
    }

    /**
     * Gets a list of VF modules. If the non-base VF modules are requested, then only
     * those whose names match the name pattern, {@link #VF_MODULE_NAME_PAT}, are
     * returned.
     *
     * @param wantBaseModule {@code true} if the the base VF module(s) is desired,
     *        {@code false} otherwise
     * @return the list of VF module items
     */
    public List<AaiNqInventoryResponseItem> getVfModuleItems(boolean wantBaseModule) {
        // get the list of items
        List<AaiNqInventoryResponseItem> itemList;
        try {
            itemList = aaiNqResponse.getInventoryResponseItems().get(0).getItems().getInventoryResponseItems().get(0)
                            .getItems().getInventoryResponseItems();

        } catch (NullPointerException | IndexOutOfBoundsException e) {
            logger.debug("no VF modules in AAI response", e);
            return Collections.emptyList();
        }

        if (itemList == null) {
            return Collections.emptyList();
        }

        /*
         * Walk the items looking for VF modules, allocating the list only when an item is
         * found.
         */
        List<AaiNqInventoryResponseItem> vfModuleItems = new ArrayList<>(itemList.size());

        for (AaiNqInventoryResponseItem inventoryResponseItem : itemList) {
            AaiNqVfModule vfmod = inventoryResponseItem.getVfModule();
            if (vfmod == null) {
                continue;
            }

            if (vfmod.getIsBaseVfModule() == wantBaseModule
                            && (wantBaseModule || VF_MODULE_NAME_PAT.matcher(vfmod.getVfModuleName()).matches())) {
                vfModuleItems.add(inventoryResponseItem);
            }
        }

        return vfModuleItems;
    }
}
