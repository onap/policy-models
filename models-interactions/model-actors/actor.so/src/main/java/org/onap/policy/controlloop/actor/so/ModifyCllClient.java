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

import java.io.IOException;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import okio.BufferedSink;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.onap.policy.controlloop.actorserviceprovider.parameters.HttpConfig;
import org.onap.policy.controlloop.actorserviceprovider.parameters.HttpPollingConfig;
import org.onap.policy.so.SoResponse;

public class ModifyCllClient {

    private ModifyCllEventCall modifyCllEventCall;
    public ModifyCllEventCall getModifyCllEventCall() {
        return this.modifyCllEventCall;
    }

    protected boolean NotifyResponsetoUUI(SoResponse soResponse) {
        int responseCode = soResponse.getHttpResponseCode();
        RequestBody body = null;
        if (responseCode == 200) {
            //init body;
            getModifyCllEventCall().CreateModifyCllRequest(body);
        } else if (responseCode == 400) {
            //init body;
            getModifyCllEventCall().CreateModifyCllRequest(body);
        } else if (responseCode == 401) {
            //init body;
            getModifyCllEventCall().CreateModifyCllRequest(body);
        } else if (responseCode == 403) {
            //init body;
            getModifyCllEventCall().CreateModifyCllRequest(body);
        } else if (responseCode == 404) {
            //init body;
            getModifyCllEventCall().CreateModifyCllRequest(body);
        } else if (responseCode == 500) {
            //init body;
            getModifyCllEventCall().CreateModifyCllRequest(body);
        }
        return true;
    }

}
