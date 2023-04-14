/*
 * ============LICENSE_START=======================================================
 * ONAP
 * ================================================================================
 *  Copyright (C) 2022 Huawei, Inc. Limited.
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
package org.onap.policy.controlloop.actor.so;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;
import java.util.UUID;
import javax.swing.text.html.parser.Entity;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ModifyCllEvent implements Serializable {

    private static final long serialVersionUID = 2391252138583119195L;

    @SerializedName("requestID")
    protected UUID requestId;
    @SerializedName("target_type")
    protected String source;
    protected String timestamp;
    protected Entity entity;

    /**
     * Construct an instance from an existing instance.
     *
     * @param event the existing instance
     */
    protected ModifyCllEvent(ModifyCllEvent event) {
        if (event == null) {
            return;
        }
        this.source =  event.source;
        this.timestamp = event.timestamp;
        this.entity = event.entity;
    }

}