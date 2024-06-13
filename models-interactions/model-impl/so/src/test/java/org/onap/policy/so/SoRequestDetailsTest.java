/*-
 * ============LICENSE_START=======================================================
 * so
 * ================================================================================
 * Copyright (C) 2018 Ericsson. All rights reserved.
 * Modifications Copyright (C) 2019-2021 AT&T Intellectual Property. All rights reserved
 * Modifications Copyright (C) 2019, 2024 Nordix Foundation.
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

package org.onap.policy.so;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import org.junit.jupiter.api.Test;

class SoRequestDetailsTest {

    @Test
    void testConstructor() {
        SoRequestDetails obj = new SoRequestDetails();

        assertNull(obj.getCloudConfiguration());
        assertNull(obj.getModelInfo());
        assertNull(obj.getRequestInfo());
        assertNull(obj.getRequestParameters());
        assertNull(obj.getSubscriberInfo());

        assertNotNull(obj.getRelatedInstanceList());
        assertEquals(0, obj.getRelatedInstanceList().size());
    }

    @Test
    void testSetGet() {
        SoRequestDetails obj = new SoRequestDetails();

        SoCloudConfiguration cloudConfiguration = new SoCloudConfiguration();
        obj.setCloudConfiguration(cloudConfiguration);
        assertEquals(cloudConfiguration, obj.getCloudConfiguration());

        SoModelInfo modelInfo = new SoModelInfo();
        obj.setModelInfo(modelInfo);
        assertEquals(modelInfo, obj.getModelInfo());

        SoRequestInfo requestInfo = new SoRequestInfo();
        obj.setRequestInfo(requestInfo);
        assertEquals(requestInfo, obj.getRequestInfo());

        SoRequestParameters requestParameters = new SoRequestParameters();
        obj.setRequestParameters(requestParameters);
        assertEquals(requestParameters, obj.getRequestParameters());

        SoSubscriberInfo subscriberInfo = new SoSubscriberInfo();
        obj.setSubscriberInfo(subscriberInfo);
        assertEquals(subscriberInfo, obj.getSubscriberInfo());
    }

    @Test
    void testSoMRequestDetailsMethods() {
        SoRequestDetails details = new SoRequestDetails();
        assertNotNull(details);
        assertNotEquals(0, details.hashCode());

        SoCloudConfiguration cloudConfiguration = new SoCloudConfiguration();
        details.setCloudConfiguration(cloudConfiguration);
        assertEquals(cloudConfiguration, details.getCloudConfiguration());
        assertNotEquals(0, details.hashCode());

        SoModelInfo modelInfo = new SoModelInfo();
        details.setModelInfo(modelInfo);
        assertEquals(modelInfo, details.getModelInfo());
        assertNotEquals(0, details.hashCode());

        List<SoRelatedInstanceListElement> relatedInstanceList = new ArrayList<>();
        details.setRelatedInstanceList(relatedInstanceList);
        assertEquals(relatedInstanceList, details.getRelatedInstanceList());
        assertNotEquals(0, details.hashCode());

        SoRequestInfo requestInfo = new SoRequestInfo();
        details.setRequestInfo(requestInfo);
        assertEquals(requestInfo, details.getRequestInfo());
        assertNotEquals(0, details.hashCode());

        SoRequestParameters requestParameters = new SoRequestParameters();
        details.setRequestParameters(requestParameters);
        assertEquals(requestParameters, details.getRequestParameters());
        assertNotEquals(0, details.hashCode());

        SoSubscriberInfo subscriberInfo = new SoSubscriberInfo();
        details.setSubscriberInfo(subscriberInfo);
        assertEquals(subscriberInfo, details.getSubscriberInfo());
        assertNotEquals(0, details.hashCode());

        assertEquals("SoRequestDetails(modelInfo=org.onap.policy.so", details.toString().substring(0,  45));

        SoRequestDetails copiedDetails = new SoRequestDetails(details);

        assertEquals(details, (Object) details);
        assertEquals(details, copiedDetails);
        assertNotEquals(details, null);
        assertNotEquals(details, (Object) "Hello");

        checkField(cloudConfiguration, SoRequestDetails::setCloudConfiguration);
        checkField(modelInfo, SoRequestDetails::setModelInfo);
        checkField(requestInfo, SoRequestDetails::setRequestInfo);
        checkField(requestParameters, SoRequestDetails::setRequestParameters);
        checkField(subscriberInfo, SoRequestDetails::setSubscriberInfo);
        checkField(relatedInstanceList, SoRequestDetails::setRelatedInstanceList);
    }

    private <T> void checkField(T value, BiConsumer<SoRequestDetails, T> setter) {
        SoRequestDetails details1 = new SoRequestDetails();
        SoRequestDetails details2 = new SoRequestDetails(details1);

        setter.accept(details2, null);

        setter.accept(details1, value);
        assertNotEquals(details1, details2);

        setter.accept(details2, value);
        assertEquals(details1, details2);

        setter.accept(details1, null);
        assertNotEquals(details1, details2);
    }
}
