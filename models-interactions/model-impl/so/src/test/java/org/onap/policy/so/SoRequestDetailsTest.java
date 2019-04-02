/*-
 * ============LICENSE_START=======================================================
 * so
 * ================================================================================
 * Copyright (C) 2018 Ericsson. All rights reserved.
 * ================================================================================
 * Modifications Copyright (C) 2019 AT&T Intellectual Property. All rights reserved
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

package org.onap.policy.so;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

public class SoRequestDetailsTest {

    @Test
    public void testConstructor() {
        SoRequestDetails obj = new SoRequestDetails();

        assertTrue(obj.getCloudConfiguration() == null);
        assertTrue(obj.getModelInfo() == null);
        assertTrue(obj.getRequestInfo() == null);
        assertTrue(obj.getRequestParameters() == null);
        assertTrue(obj.getSubscriberInfo() == null);

        assertTrue(obj.getRelatedInstanceList() != null);
        assertEquals(0, obj.getRelatedInstanceList().size());
    }

    @Test
    public void testSetGet() {
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
    public void testSoMRequestDetailsMethods() {
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

        assertEquals("SORequestDetails [modelInfo=org.onap.policy.so", details.toString().substring(0,  46));

        SoRequestDetails copiedDetails = new SoRequestDetails(details);

        assertTrue(details.equals(details));
        assertTrue(details.equals(copiedDetails));
        assertFalse(details.equals(null));
        assertFalse(details.equals("Hello"));
        
        details.setCloudConfiguration(null);
        assertFalse(details.equals(copiedDetails));
        copiedDetails.setCloudConfiguration(null);
        assertTrue(details.equals(copiedDetails));
        details.setCloudConfiguration(cloudConfiguration);
        assertFalse(details.equals(copiedDetails));
        copiedDetails.setCloudConfiguration(cloudConfiguration);
        assertTrue(details.equals(copiedDetails));

        details.setModelInfo(null);
        assertFalse(details.equals(copiedDetails));
        copiedDetails.setModelInfo(null);
        assertTrue(details.equals(copiedDetails));
        details.setModelInfo(modelInfo);
        assertFalse(details.equals(copiedDetails));
        copiedDetails.setModelInfo(modelInfo);
        assertTrue(details.equals(copiedDetails));
        
        details.setRequestInfo(null);
        assertFalse(details.equals(copiedDetails));
        copiedDetails.setRequestInfo(null);
        assertTrue(details.equals(copiedDetails));
        details.setRequestInfo(requestInfo);
        assertFalse(details.equals(copiedDetails));
        copiedDetails.setRequestInfo(requestInfo);
        assertTrue(details.equals(copiedDetails));

        details.setRequestParameters(null);
        assertFalse(details.equals(copiedDetails));
        copiedDetails.setRequestParameters(null);
        assertTrue(details.equals(copiedDetails));
        details.setRequestParameters(requestParameters);
        assertFalse(details.equals(copiedDetails));
        copiedDetails.setRequestParameters(requestParameters);
        assertTrue(details.equals(copiedDetails));

        details.setSubscriberInfo(null);
        assertFalse(details.equals(copiedDetails));
        copiedDetails.setSubscriberInfo(null);
        assertTrue(details.equals(copiedDetails));
        details.setSubscriberInfo(subscriberInfo);
        assertFalse(details.equals(copiedDetails));
        copiedDetails.setSubscriberInfo(subscriberInfo);
        assertTrue(details.equals(copiedDetails));

        details.setRelatedInstanceList(null);
        assertFalse(details.equals(copiedDetails));
        copiedDetails.setRelatedInstanceList(null);
        assertTrue(details.equals(copiedDetails));
        details.setRelatedInstanceList(relatedInstanceList);
        assertFalse(details.equals(copiedDetails));
        copiedDetails.setRelatedInstanceList(relatedInstanceList);
        assertTrue(details.equals(copiedDetails));
    }
}
