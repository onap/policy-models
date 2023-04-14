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


import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface ModifyCllEventCall {
    @Headers({
        "Accept: application/json",
        "Content-Type: application/json"
    })
    @POST("/event/{topicName}")
    Call<ResponseBody> CreateModifyCllRequest(@Body RequestBody body);
}
