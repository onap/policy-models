/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2019 Nordix Foundation.
 *  Copyright (C) 2019 AT&T Intellectual Property. All rights reserved.
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

package org.onap.policy.models.tosca.simple.serialization;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import org.junit.Before;
import org.junit.Test;
import org.onap.policy.common.utils.resources.ResourceUtils;
import org.onap.policy.models.base.PfConceptKey;
import org.onap.policy.models.base.PfValidationResult;
import org.onap.policy.models.tosca.simple.concepts.ToscaConstraintLogicalString;
import org.onap.policy.models.tosca.simple.concepts.ToscaConstraintValidValues;
import org.onap.policy.models.tosca.simple.concepts.ToscaDataType;
import org.onap.policy.models.tosca.simple.concepts.ToscaEntrySchema;
import org.onap.policy.models.tosca.simple.concepts.ToscaPolicyType;
import org.onap.policy.models.tosca.simple.concepts.ToscaProperty;
import org.onap.policy.models.tosca.simple.concepts.ToscaServiceTemplate;
import org.onap.policy.models.tosca.simple.serialization.ToscaServiceTemplateMessageBodyHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;

/**
 * Test serialization of monitoring policy types.
 *
 * @author Chenfei Gao (cgao@research.att.com)
 */
public class MonitoringPolicyTypeSerializationTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(MonitoringPolicyTypeSerializationTest.class);

    private static final String MONITORING_TCA_YAML = "policytypes/onap.policy.monitoring.cdap.tca.hi.lo.app.yaml";
    private static final String MONITORING_COLLECTORS_YAML =
            "policytypes/onap.policies.monitoring.dcaegen2.collectors.datafile.datafile-app-server.yaml";

    private Gson gson;

    @Before
    public void setUp() {
        gson = new ToscaServiceTemplateMessageBodyHandler().getGson();
    }

    @Test
    public void testDeserialization() {
        try {
            // TCA
            ToscaServiceTemplate serviceTemplateFromYaml = deserializeMonitoringInputYaml(MONITORING_TCA_YAML);
            verifyTcaInputDeserialization(serviceTemplateFromYaml);

            // Collector
            serviceTemplateFromYaml = deserializeMonitoringInputYaml(MONITORING_COLLECTORS_YAML);
            verifyCollectorInputDeserialization(serviceTemplateFromYaml);

        } catch (Exception e) {
            fail("No exception should be thrown");
        }
    }

    @Test
    public void testSerialization() {
        try {
            // TCA
            ToscaServiceTemplate serviceTemplateFromYaml = deserializeMonitoringInputYaml(MONITORING_TCA_YAML);
            String serializedServiceTemplate1 = serializeMonitoringServiceTemplate(serviceTemplateFromYaml);
            ToscaServiceTemplate serviceTemplateFromJson = gson.fromJson(serializedServiceTemplate1,
                    ToscaServiceTemplate.class);
            String serializedServiceTemplate2 = serializeMonitoringServiceTemplate(serviceTemplateFromJson);
            assertEquals(serializedServiceTemplate1, serializedServiceTemplate2);

            // Collector
            serviceTemplateFromYaml = deserializeMonitoringInputYaml(MONITORING_COLLECTORS_YAML);
            serializedServiceTemplate1 = serializeMonitoringServiceTemplate(serviceTemplateFromYaml);
            serviceTemplateFromJson = gson.fromJson(serializedServiceTemplate1, ToscaServiceTemplate.class);
            serializedServiceTemplate2 = serializeMonitoringServiceTemplate(serviceTemplateFromJson);
            assertEquals(serializedServiceTemplate1, serializedServiceTemplate2);

        } catch (Exception e) {
            fail("No exception should be thrown");
        }
    }

    private ToscaServiceTemplate deserializeMonitoringInputYaml(String resourcePath)
            throws JsonSyntaxException, IOException {

        Yaml yaml = new Yaml();
        String policyTypeYaml = ResourceUtils.getResourceAsString(resourcePath);
        Object yamlObject = yaml.load(policyTypeYaml);
        String yamlAsJsonString = new Gson().toJson(yamlObject);
        ToscaServiceTemplate serviceTemplate = gson.fromJson(yamlAsJsonString, ToscaServiceTemplate.class);
        return serviceTemplate;
    }

    private void verifyTcaInputDeserialization(ToscaServiceTemplate serviceTemplate) {

        // Sanity check the entire structure
        assertNotNull(serviceTemplate);
        LOGGER.info(serviceTemplate.validate(new PfValidationResult()).toString());
        assertTrue(serviceTemplate.validate(new PfValidationResult()).isValid());

        // Check tosca_definitions_version
        assertEquals("tosca_simple_yaml_1_0_0", serviceTemplate.getToscaDefinitionsVersion());

        // Check policy_types
        Map<PfConceptKey, ToscaPolicyType> policyTypesConceptMap = serviceTemplate.getPolicyTypes().getConceptMap();
        assertTrue(policyTypesConceptMap.size() == 2);
        Iterator<Entry<PfConceptKey, ToscaPolicyType>> policyTypesIter = policyTypesConceptMap.entrySet().iterator();

        Entry<PfConceptKey, ToscaPolicyType> firstPolicyType = policyTypesIter.next();
        assertEquals("onap.policies.Monitoring", firstPolicyType.getKey().getName());
        assertEquals("1.0.0", firstPolicyType.getKey().getVersion());
        assertEquals("tosca.policies.Root", firstPolicyType.getValue().getDerivedFrom().getName());
        assertEquals("a base policy type for all policies that governs monitoring provisioning",
                firstPolicyType.getValue().getDescription());

        Entry<PfConceptKey, ToscaPolicyType> secondPolicyType = policyTypesIter.next();
        assertEquals("onap.policy.monitoring.cdap.tca.hi.lo.app", secondPolicyType.getKey().getName());
        assertEquals("1.0.0", secondPolicyType.getKey().getVersion());
        assertEquals("onap.policies.Monitoring", secondPolicyType.getValue().getDerivedFrom().getName());
        assertTrue(secondPolicyType.getValue().getProperties().size() == 1);

        ToscaProperty property = secondPolicyType.getValue().getProperties().iterator().next();
        assertEquals("onap.policy.monitoring.cdap.tca.hi.lo.app", property.getKey().getParentKeyName());
        assertEquals("1.0.0", property.getKey().getParentKeyVersion());
        assertEquals("tca_policy", property.getKey().getLocalName());
        assertEquals("map", property.getType().getName());
        assertEquals("TCA Policy JSON", property.getDescription());

        ToscaEntrySchema entrySchema = property.getEntrySchema();
        assertEquals("map", entrySchema.getKey().getParentKeyName());
        assertEquals("1.0.0", entrySchema.getKey().getParentKeyVersion());
        assertEquals("entry_schema", entrySchema.getKey().getLocalName());
        assertEquals("onap.datatypes.monitoring.tca_policy", entrySchema.getType().getName());

        // Check data_types
        Map<PfConceptKey, ToscaDataType> dataTypesConceptMap = serviceTemplate.getDataTypes().getConceptMap();
        assertTrue(dataTypesConceptMap.size() == 3);
        Iterator<Entry<PfConceptKey, ToscaDataType>> dataTypesIter = dataTypesConceptMap.entrySet().iterator();

        Entry<PfConceptKey, ToscaDataType> firstDataType = dataTypesIter.next();
        assertEquals("onap.datatypes.monitoring.metricsPerEventName", firstDataType.getKey().getName());
        ToscaDataType firstDataTypeVal = firstDataType.getValue();
        assertEquals("tosca.datatypes.Root", firstDataTypeVal.getDerivedFrom().getName());
        assertEquals("1.0.0", firstDataTypeVal.getDerivedFrom().getVersion());
        assertTrue(firstDataTypeVal.getProperties().size() == 6);
        Iterator<ToscaProperty> firstDataTypePropertiesIter = firstDataTypeVal.getProperties().iterator();

        ToscaProperty firstDataTypeFirstProperty = firstDataTypePropertiesIter.next();
        assertEquals("onap.datatypes.monitoring.metricsPerEventName", firstDataTypeFirstProperty.getKey()
                .getParentKeyName());
        assertEquals("controlLoopSchemaType", firstDataTypeFirstProperty.getKey().getLocalName());
        assertEquals("string", firstDataTypeFirstProperty.getType().getName());
        assertTrue(firstDataTypeFirstProperty.isRequired());
        assertEquals("Specifies Control Loop Schema Type for the event Name e.g. VNF, VM",
                firstDataTypeFirstProperty.getDescription());
        assertTrue(firstDataTypeFirstProperty.getConstraints().size() == 1);
        assertEquals("valid_values", firstDataTypeFirstProperty.getConstraints().iterator().next().getKey()
                .getLocalName());
        assertEquals("string", firstDataTypeFirstProperty.getConstraints().iterator().next().getKey()
                .getParentKeyName());
        assertTrue(firstDataTypeFirstProperty.getConstraints().iterator().next()
                instanceof ToscaConstraintValidValues);
        assertTrue(((ToscaConstraintValidValues)(firstDataTypeFirstProperty.getConstraints().iterator().next()))
                .getValidValues().size() == 2);

        ToscaProperty firstDataTypeSecondProperty = firstDataTypePropertiesIter.next();
        assertEquals("onap.datatypes.monitoring.metricsPerEventName", firstDataTypeSecondProperty.getKey()
                .getParentKeyName());
        assertEquals("eventName", firstDataTypeSecondProperty.getKey().getLocalName());
        assertEquals("string", firstDataTypeSecondProperty.getType().getName());
        assertTrue(firstDataTypeSecondProperty.isRequired());
        assertEquals("Event name to which thresholds need to be applied", firstDataTypeSecondProperty
                .getDescription());

        ToscaProperty firstDataTypeThirdProperty = firstDataTypePropertiesIter.next();
        assertEquals("onap.datatypes.monitoring.metricsPerEventName", firstDataTypeThirdProperty.getKey()
                .getParentKeyName());
        assertEquals("policyName", firstDataTypeThirdProperty.getKey().getLocalName());
        assertEquals("string", firstDataTypeThirdProperty.getType().getName());
        assertTrue(firstDataTypeThirdProperty.isRequired());
        assertEquals("TCA Policy Scope Name", firstDataTypeThirdProperty.getDescription());

        ToscaProperty firstDataTypeFourthProperty = firstDataTypePropertiesIter.next();
        assertEquals("onap.datatypes.monitoring.metricsPerEventName", firstDataTypeFourthProperty.getKey()
                .getParentKeyName());
        assertEquals("policyScope", firstDataTypeFourthProperty.getKey().getLocalName());
        assertEquals("string", firstDataTypeFourthProperty.getType().getName());
        assertTrue(firstDataTypeFourthProperty.isRequired());
        assertEquals("TCA Policy Scope", firstDataTypeFourthProperty.getDescription());

        ToscaProperty firstDataTypeFifthProperty = firstDataTypePropertiesIter.next();
        assertEquals("onap.datatypes.monitoring.metricsPerEventName", firstDataTypeFifthProperty.getKey()
                .getParentKeyName());
        assertEquals("policyVersion", firstDataTypeFifthProperty.getKey().getLocalName());
        assertEquals("string", firstDataTypeFifthProperty.getType().getName());
        assertTrue(firstDataTypeFifthProperty.isRequired());
        assertEquals("TCA Policy Scope Version", firstDataTypeFifthProperty.getDescription());

        ToscaProperty firstDataTypeSixthProperty = firstDataTypePropertiesIter.next();
        assertEquals("onap.datatypes.monitoring.metricsPerEventName", firstDataTypeSixthProperty.getKey()
                .getParentKeyName());
        assertEquals("thresholds", firstDataTypeSixthProperty.getKey().getLocalName());
        assertEquals("list", firstDataTypeSixthProperty.getType().getName());
        assertTrue(firstDataTypeSixthProperty.isRequired());
        assertEquals("Thresholds associated with eventName", firstDataTypeSixthProperty.getDescription());
        assertNotNull(firstDataTypeSixthProperty.getEntrySchema());
        assertEquals("entry_schema", firstDataTypeSixthProperty.getEntrySchema().getKey().getLocalName());
        assertEquals("list", firstDataTypeSixthProperty.getEntrySchema().getKey().getParentKeyName());
        assertEquals("onap.datatypes.monitoring.thresholds", firstDataTypeSixthProperty.getEntrySchema().getType()
                .getName());

        Entry<PfConceptKey, ToscaDataType> secondDataType = dataTypesIter.next();
        assertEquals("onap.datatypes.monitoring.tca_policy", secondDataType.getKey().getName());
        ToscaDataType secondDataTypeVal = secondDataType.getValue();
        assertEquals("tosca.datatypes.Root", secondDataTypeVal.getDerivedFrom().getName());
        assertEquals("1.0.0", secondDataTypeVal.getDerivedFrom().getVersion());
        assertTrue(secondDataTypeVal.getProperties().size() == 2);
        Iterator<ToscaProperty> secondDataTypePropertiesIter = secondDataTypeVal.getProperties().iterator();

        ToscaProperty secondDataTypeFirstProperty = secondDataTypePropertiesIter.next();
        assertEquals("onap.datatypes.monitoring.tca_policy", secondDataTypeFirstProperty.getKey().getParentKeyName());
        assertEquals("domain", secondDataTypeFirstProperty.getKey().getLocalName());
        assertEquals("string", secondDataTypeFirstProperty.getType().getName());
        assertTrue(secondDataTypeFirstProperty.isRequired());
        assertEquals("Domain name to which TCA needs to be applied", secondDataTypeFirstProperty.getDescription());
        assertEquals("measurementsForVfScaling", secondDataTypeFirstProperty.getDefaultValue());
        assertTrue(secondDataTypeFirstProperty.getConstraints().size() == 1);
        assertEquals("string", secondDataTypeFirstProperty.getConstraints().iterator().next().getKey()
                .getParentKeyName());
        assertEquals("equal", secondDataTypeFirstProperty.getConstraints().iterator().next().getKey().getLocalName());
        assertTrue(secondDataTypeFirstProperty.getConstraints().iterator().next()
                instanceof ToscaConstraintLogicalString);
        assertEquals("measurementsForVfScaling", ((ToscaConstraintLogicalString)(secondDataTypeFirstProperty
                .getConstraints().iterator().next())).getCompareToString());

        ToscaProperty secondDataTypeSecondProperty = secondDataTypePropertiesIter.next();
        assertEquals("onap.datatypes.monitoring.tca_policy", secondDataTypeSecondProperty.getKey().getParentKeyName());
        assertEquals("metricsPerEventName", secondDataTypeSecondProperty.getKey().getLocalName());
        assertEquals("list", secondDataTypeSecondProperty.getType().getName());
        assertTrue(secondDataTypeSecondProperty.isRequired());
        assertEquals("Contains eventName and threshold details that need to be applied to given eventName",
                secondDataTypeSecondProperty.getDescription());
        assertNotNull(secondDataTypeSecondProperty.getEntrySchema());
        assertEquals("list", secondDataTypeSecondProperty.getEntrySchema().getKey().getParentKeyName());
        assertEquals("onap.datatypes.monitoring.metricsPerEventName",
                secondDataTypeSecondProperty.getEntrySchema().getType().getName());
        assertEquals("entry_schema", secondDataTypeSecondProperty.getEntrySchema().getKey().getLocalName());

        Entry<PfConceptKey, ToscaDataType> thirdDataType = dataTypesIter.next();
        assertEquals("onap.datatypes.monitoring.thresholds", thirdDataType.getKey().getName());
        ToscaDataType thirdDataTypeVal = thirdDataType.getValue();
        assertEquals("tosca.datatypes.Root", thirdDataTypeVal.getDerivedFrom().getName());
        assertEquals("1.0.0", thirdDataTypeVal.getDerivedFrom().getVersion());
        assertTrue(thirdDataTypeVal.getProperties().size() == 7);
        Iterator<ToscaProperty> thirdDataTypePropertiesIter = thirdDataTypeVal.getProperties().iterator();

        ToscaProperty thirdDataTypeFirstProperty = thirdDataTypePropertiesIter.next();
        assertEquals("onap.datatypes.monitoring.thresholds", thirdDataTypeFirstProperty.getKey().getParentKeyName());
        assertEquals("closedLoopControlName", thirdDataTypeFirstProperty.getKey().getLocalName());
        assertEquals("string", thirdDataTypeFirstProperty.getType().getName());
        assertTrue(thirdDataTypeFirstProperty.isRequired());
        assertEquals("Closed Loop Control Name associated with the threshold", thirdDataTypeFirstProperty
                .getDescription());

        ToscaProperty thirdDataTypeSecondProperty = thirdDataTypePropertiesIter.next();
        assertEquals("onap.datatypes.monitoring.thresholds", thirdDataTypeSecondProperty.getKey().getParentKeyName());
        assertEquals("closedLoopEventStatus", thirdDataTypeSecondProperty.getKey().getLocalName());
        assertEquals("string", thirdDataTypeSecondProperty.getType().getName());
        assertTrue(thirdDataTypeSecondProperty.isRequired());
        assertEquals("Closed Loop Event Status of the threshold", thirdDataTypeSecondProperty.getDescription());
        assertNotNull(thirdDataTypeSecondProperty.getConstraints());
        assertTrue(thirdDataTypeSecondProperty.getConstraints().size() == 1);
        assertEquals("string", thirdDataTypeSecondProperty.getConstraints().iterator().next().getKey()
                .getParentKeyName());
        assertEquals("valid_values", thirdDataTypeSecondProperty.getConstraints().iterator().next().getKey()
                .getLocalName());
        assertTrue(thirdDataTypeSecondProperty.getConstraints().iterator().next()
                instanceof ToscaConstraintValidValues);
        assertTrue(((ToscaConstraintValidValues)(thirdDataTypeSecondProperty.getConstraints().iterator().next()))
                .getValidValues().size() == 2);

        ToscaProperty thirdDataTypeThirdProperty = thirdDataTypePropertiesIter.next();
        assertEquals("onap.datatypes.monitoring.thresholds", thirdDataTypeThirdProperty.getKey().getParentKeyName());
        assertEquals("direction", thirdDataTypeThirdProperty.getKey().getLocalName());
        assertEquals("string", thirdDataTypeThirdProperty.getType().getName());
        assertTrue(thirdDataTypeThirdProperty.isRequired());
        assertEquals("Direction of the threshold", thirdDataTypeThirdProperty.getDescription());
        assertNotNull(thirdDataTypeThirdProperty.getConstraints());
        assertTrue(thirdDataTypeThirdProperty.getConstraints().size() == 1);
        assertEquals("string", thirdDataTypeThirdProperty.getConstraints().iterator().next().getKey()
                .getParentKeyName());
        assertEquals("valid_values", thirdDataTypeThirdProperty.getConstraints().iterator().next().getKey()
                .getLocalName());
        assertTrue(((ToscaConstraintValidValues)(thirdDataTypeThirdProperty.getConstraints().iterator().next()))
                .getValidValues().size() == 5);

        ToscaProperty thirdDataTypeFourthProperty = thirdDataTypePropertiesIter.next();
        assertEquals("onap.datatypes.monitoring.thresholds", thirdDataTypeFourthProperty.getKey().getParentKeyName());
        assertEquals("fieldPath", thirdDataTypeFourthProperty.getKey().getLocalName());
        assertEquals("string", thirdDataTypeFourthProperty.getType().getName());
        assertTrue(thirdDataTypeFourthProperty.isRequired());
        assertEquals("Json field Path as per CEF message which needs to be analyzed for TCA",
                thirdDataTypeFourthProperty.getDescription());
        assertNotNull(thirdDataTypeFourthProperty.getConstraints());
        assertTrue(thirdDataTypeFourthProperty.getConstraints().size() == 1);
        assertEquals("string", thirdDataTypeFourthProperty.getConstraints().iterator().next().getKey()
                .getParentKeyName());
        assertEquals("valid_values", thirdDataTypeFourthProperty.getConstraints().iterator().next().getKey()
                .getLocalName());
        assertTrue(((ToscaConstraintValidValues)(thirdDataTypeFourthProperty.getConstraints().iterator().next()))
                .getValidValues().size() == 43);

        ToscaProperty thirdDataTypeFifthProperty = thirdDataTypePropertiesIter.next();
        assertEquals("onap.datatypes.monitoring.thresholds", thirdDataTypeFifthProperty.getKey().getParentKeyName());
        assertEquals("severity", thirdDataTypeFifthProperty.getKey().getLocalName());
        assertEquals("string", thirdDataTypeFifthProperty.getType().getName());
        assertTrue(thirdDataTypeFifthProperty.isRequired());
        assertEquals("Threshold Event Severity", thirdDataTypeFifthProperty.getDescription());
        assertNotNull(thirdDataTypeFifthProperty.getConstraints());
        assertTrue(thirdDataTypeFifthProperty.getConstraints().size() == 1);
        assertEquals("string", thirdDataTypeFifthProperty.getConstraints().iterator().next().getKey()
                .getParentKeyName());
        assertEquals("valid_values", thirdDataTypeFifthProperty.getConstraints().iterator().next().getKey()
                .getLocalName());
        assertTrue(((ToscaConstraintValidValues)(thirdDataTypeFifthProperty.getConstraints().iterator().next()))
                .getValidValues().size() == 5);;

        ToscaProperty thirdDataTypeSixthProperty = thirdDataTypePropertiesIter.next();
        assertEquals("onap.datatypes.monitoring.thresholds", thirdDataTypeSixthProperty.getKey().getParentKeyName());
        assertEquals("thresholdValue", thirdDataTypeSixthProperty.getKey().getLocalName());
        assertEquals("integer", thirdDataTypeSixthProperty.getType().getName());
        assertTrue(thirdDataTypeSixthProperty.isRequired());
        assertEquals("Threshold value for the field Path inside CEF message", thirdDataTypeSixthProperty
                .getDescription());

        ToscaProperty thirdDataTypeSeventhProperty = thirdDataTypePropertiesIter.next();
        assertEquals("onap.datatypes.monitoring.thresholds", thirdDataTypeSeventhProperty.getKey().getParentKeyName());
        assertEquals("version", thirdDataTypeSeventhProperty.getKey().getLocalName());
        assertEquals("string", thirdDataTypeSeventhProperty.getType().getName());
        assertTrue(thirdDataTypeSeventhProperty.isRequired());
        assertEquals("Version number associated with the threshold", thirdDataTypeSeventhProperty.getDescription());
    }

    private void verifyCollectorInputDeserialization(ToscaServiceTemplate serviceTemplate) {

        // Sanity check the entire structure
        assertNotNull(serviceTemplate);
        LOGGER.info(serviceTemplate.validate(new PfValidationResult()).toString());
        assertTrue(serviceTemplate.validate(new PfValidationResult()).isValid());

        // Check tosca_definitions_version
        assertEquals("tosca_simple_yaml_1_0_0", serviceTemplate.getToscaDefinitionsVersion());

        // Check policy_types
        Map<PfConceptKey, ToscaPolicyType> policyTypesConceptMap = serviceTemplate.getPolicyTypes().getConceptMap();
        assertTrue(policyTypesConceptMap.size() == 2);
        Iterator<Entry<PfConceptKey, ToscaPolicyType>> policyTypesIter = policyTypesConceptMap.entrySet().iterator();

        Entry<PfConceptKey, ToscaPolicyType> firstPolicyType = policyTypesIter.next();
        assertEquals("onap.policies.Monitoring", firstPolicyType.getKey().getName());
        assertEquals("1.0.0", firstPolicyType.getKey().getVersion());
        assertEquals("tosca.policies.Root", firstPolicyType.getValue().getDerivedFrom().getName());
        assertEquals("a base policy type for all policies that govern monitoring provision",
                firstPolicyType.getValue().getDescription());

        Entry<PfConceptKey, ToscaPolicyType> secondPolicyType = policyTypesIter.next();
        assertEquals("onap.policies.monitoring.dcaegen2.collectors.datafile.datafile-app-server",
                secondPolicyType.getKey().getName());
        assertEquals("1.0.0", secondPolicyType.getKey().getVersion());
        assertEquals("policy.nodes.Root", secondPolicyType.getValue().getDerivedFrom().getName());
        assertTrue(secondPolicyType.getValue().getProperties().size() == 2);

        Iterator<ToscaProperty> propertiesIter = secondPolicyType.getValue().getProperties().iterator();

        ToscaProperty firstProperty = propertiesIter.next();
        assertEquals("onap.policies.monitoring.dcaegen2.collectors.datafile.datafile-app-server",
                firstProperty.getKey().getParentKeyName());
        assertEquals("1.0.0", firstProperty.getKey().getParentKeyVersion());
        assertEquals("buscontroller_feed_publishing_endpoint", firstProperty.getKey().getLocalName());
        assertEquals("string", firstProperty.getType().getName());
        assertEquals("DMAAP Bus Controller feed endpoint", firstProperty.getDescription());

        ToscaProperty secondProperty = propertiesIter.next();
        assertEquals("onap.policies.monitoring.dcaegen2.collectors.datafile.datafile-app-server",
                secondProperty.getKey().getParentKeyName());
        assertEquals("1.0.0", secondProperty.getKey().getParentKeyVersion());
        assertEquals("datafile.policy", secondProperty.getKey().getLocalName());
        assertEquals("string", secondProperty.getType().getName());
        assertEquals("datafile Policy JSON as string", secondProperty.getDescription());
    }

    private String serializeMonitoringServiceTemplate(ToscaServiceTemplate serviceTemplate) {
        return gson.toJson(serviceTemplate);
    }
}