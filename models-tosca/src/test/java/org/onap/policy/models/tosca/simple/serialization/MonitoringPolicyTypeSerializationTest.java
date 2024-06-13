/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2019-2020, 2024 Nordix Foundation.
 *  Copyright (C) 2019-2020,2022 AT&T Intellectual Property. All rights reserved.
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.onap.policy.common.utils.coder.CoderException;
import org.onap.policy.common.utils.coder.StandardCoder;
import org.onap.policy.common.utils.resources.ResourceUtils;
import org.onap.policy.models.base.PfConceptKey;
import org.onap.policy.models.tosca.authorative.concepts.ToscaServiceTemplate;
import org.onap.policy.models.tosca.simple.concepts.JpaToscaConstraintLogical;
import org.onap.policy.models.tosca.simple.concepts.JpaToscaConstraintValidValues;
import org.onap.policy.models.tosca.simple.concepts.JpaToscaDataType;
import org.onap.policy.models.tosca.simple.concepts.JpaToscaPolicyType;
import org.onap.policy.models.tosca.simple.concepts.JpaToscaProperty;
import org.onap.policy.models.tosca.simple.concepts.JpaToscaServiceTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;

/**
 * Test serialization of monitoring policy types.
 *
 * @author Chenfei Gao (cgao@research.att.com)
 */
class MonitoringPolicyTypeSerializationTest {

    private static final String DATATYPE_ROOT = "tosca.datatypes.Root";

    private static final String STRING_TEXT = "string";

    private static final String DCAE = "onap.policies.monitoring.dcaegen2.collectors.datafile.datafile-app-server";

    private static final String MONITORING = "onap.policies.Monitoring";

    private static final String THRESHOLDS = "onap.datatypes.monitoring.thresholds";

    private static final String TCA_V1 = "onap.datatypes.monitoring.tca_policy";
    private static final String TCA_V2 = "list";

    private static final String METRICS = "onap.datatypes.monitoring.metricsPerEventName";

    private static final String VERSION_000 = "0.0.0";
    private static final String VERSION_100 = "1.0.0";
    private static final String VERSION_200 = "2.0.0";

    private static final Logger LOGGER = LoggerFactory.getLogger(MonitoringPolicyTypeSerializationTest.class);

    private static final String MONITORING_TCA_YAML = "policytypes/onap.policies.monitoring.tcagen2.yaml";
    private static final String MONITORING_TCA_V2_YAML = "policytypes/onap.policies.monitoring.tcagen2.v2.yaml";

    private static final String MONITORING_COLLECTORS_YAML =
            "policytypes/onap.policies.monitoring.dcaegen2.collectors.datafile.datafile-app-server.yaml";

    private StandardCoder coder;

    @BeforeEach
    void setUp() {
        coder = new StandardCoder();
    }

    @Test
    void testDeserialization() throws Exception {
        // TCA v1
        JpaToscaServiceTemplate serviceTemplateFromYaml = deserializeMonitoringInputYaml(MONITORING_TCA_YAML);
        verifyTcaInputDeserialization(serviceTemplateFromYaml, VERSION_100, TCA_V1);

        // TCA v2
        serviceTemplateFromYaml = deserializeMonitoringInputYaml(MONITORING_TCA_V2_YAML);
        verifyTcaInputDeserialization(serviceTemplateFromYaml, VERSION_200, TCA_V2);

        // Collector
        serviceTemplateFromYaml = deserializeMonitoringInputYaml(MONITORING_COLLECTORS_YAML);
        verifyCollectorInputDeserialization(serviceTemplateFromYaml);
    }

    @Test
    void testSerialization() throws Exception {
        // TCA v1
        JpaToscaServiceTemplate tcaServiceTemplateFromYaml = deserializeMonitoringInputYaml(MONITORING_TCA_YAML);
        String serializedServiceTemplateTca = serializeMonitoringServiceTemplate(tcaServiceTemplateFromYaml);

        ToscaServiceTemplate toscaServiceTemplateFromJsonTca =
                coder.decode(serializedServiceTemplateTca, ToscaServiceTemplate.class);

        JpaToscaServiceTemplate serviceTemplateFromJsonTca = new JpaToscaServiceTemplate();
        serviceTemplateFromJsonTca.fromAuthorative(toscaServiceTemplateFromJsonTca);
        String serializedServiceTemplateTcaOut = serializeMonitoringServiceTemplate(serviceTemplateFromJsonTca);
        assertEquals(serializedServiceTemplateTca, serializedServiceTemplateTcaOut);

        // TCA v2
        tcaServiceTemplateFromYaml = deserializeMonitoringInputYaml(MONITORING_TCA_V2_YAML);
        serializedServiceTemplateTca = serializeMonitoringServiceTemplate(tcaServiceTemplateFromYaml);

        toscaServiceTemplateFromJsonTca =
            coder.decode(serializedServiceTemplateTca, ToscaServiceTemplate.class);

        serviceTemplateFromJsonTca = new JpaToscaServiceTemplate();
        serviceTemplateFromJsonTca.fromAuthorative(toscaServiceTemplateFromJsonTca);
        serializedServiceTemplateTcaOut = serializeMonitoringServiceTemplate(serviceTemplateFromJsonTca);
        assertEquals(serializedServiceTemplateTca, serializedServiceTemplateTcaOut);

        // Collector
        JpaToscaServiceTemplate collectorServiceTemplateFromYaml =
                deserializeMonitoringInputYaml(MONITORING_COLLECTORS_YAML);
        String serializedServiceTemplateCollector =
                serializeMonitoringServiceTemplate(collectorServiceTemplateFromYaml);
        ToscaServiceTemplate toscaServiceTemplateFromJsonCollector =
                coder.decode(serializedServiceTemplateCollector, ToscaServiceTemplate.class);
        JpaToscaServiceTemplate serviceTemplateFromJsonCollector = new JpaToscaServiceTemplate();
        serviceTemplateFromJsonCollector.fromAuthorative(toscaServiceTemplateFromJsonCollector);
        String serializedServiceTemplateCollectorsOut =
                serializeMonitoringServiceTemplate(serviceTemplateFromJsonCollector);
        assertEquals(serializedServiceTemplateCollector, serializedServiceTemplateCollectorsOut);
    }

    private JpaToscaServiceTemplate deserializeMonitoringInputYaml(String resourcePath) throws Exception {
        Yaml yaml = new Yaml();
        String policyTypeYaml = ResourceUtils.getResourceAsString(resourcePath);
        Object yamlObject = yaml.load(policyTypeYaml);
        String yamlAsJsonString = coder.encode(yamlObject);
        ToscaServiceTemplate serviceTemplate = coder.decode(yamlAsJsonString, ToscaServiceTemplate.class);

        JpaToscaServiceTemplate jpaToscaServiceTemplate = new JpaToscaServiceTemplate();
        jpaToscaServiceTemplate.fromAuthorative(serviceTemplate);
        return jpaToscaServiceTemplate;
    }

    private void verifyTcaInputDeserialization(JpaToscaServiceTemplate serviceTemplate, String version, String tca) {

        // Sanity check the entire structure
        assertNotNull(serviceTemplate);
        LOGGER.info(serviceTemplate.validate("").toString());
        assertTrue(serviceTemplate.validate("").isValid());

        // Check tosca_definitions_version
        assertEquals("tosca_simple_yaml_1_1_0", serviceTemplate.getToscaDefinitionsVersion());

        // Check policy_types
        Map<PfConceptKey, JpaToscaPolicyType> policyTypesConceptMap = serviceTemplate.getPolicyTypes().getConceptMap();
        assertEquals(2, policyTypesConceptMap.size());
        Iterator<Entry<PfConceptKey, JpaToscaPolicyType>> policyTypesIter = policyTypesConceptMap.entrySet().iterator();

        Entry<PfConceptKey, JpaToscaPolicyType> firstPolicyType = policyTypesIter.next();
        assertEquals(MONITORING, firstPolicyType.getKey().getName());
        assertEquals(VERSION_100, firstPolicyType.getKey().getVersion());
        assertEquals("tosca.policies.Root", firstPolicyType.getValue().getDerivedFrom().getName());
        assertEquals("a base policy type for all policies that govern monitoring provisioning",
                firstPolicyType.getValue().getDescription());

        Entry<PfConceptKey, JpaToscaPolicyType> secondPolicyType = policyTypesIter.next();
        assertEquals("onap.policies.monitoring.tcagen2", secondPolicyType.getKey().getName());
        assertEquals(version, secondPolicyType.getKey().getVersion());
        assertEquals(MONITORING, secondPolicyType.getValue().getDerivedFrom().getName());
        assertEquals(1, secondPolicyType.getValue().getProperties().size());

        JpaToscaProperty property = secondPolicyType.getValue().getProperties().values().iterator().next();
        assertEquals("onap.policies.monitoring.tcagen2", property.getKey().getParentKeyName());
        assertEquals(version, property.getKey().getParentKeyVersion());
        assertEquals("tca.policy", property.getKey().getLocalName());
        assertEquals(tca, property.getType().getName());
        assertEquals("TCA Policy JSON", property.getDescription());

        // Check data_types
        Map<PfConceptKey, JpaToscaDataType> dataTypesConceptMap = serviceTemplate.getDataTypes().getConceptMap();
        assertEquals(3, dataTypesConceptMap.size());
        Iterator<Entry<PfConceptKey, JpaToscaDataType>> dataTypesIter = dataTypesConceptMap.entrySet().iterator();

        Entry<PfConceptKey, JpaToscaDataType> firstDataType = dataTypesIter.next();
        assertEquals(METRICS, firstDataType.getKey().getName());
        JpaToscaDataType firstDataTypeVal = firstDataType.getValue();
        assertEquals(DATATYPE_ROOT, firstDataTypeVal.getDerivedFrom().getName());
        assertEquals(VERSION_000, firstDataTypeVal.getDerivedFrom().getVersion());
        assertEquals(6, firstDataTypeVal.getProperties().size());
        Iterator<JpaToscaProperty> firstDataTypePropertiesIter = firstDataTypeVal.getProperties().values().iterator();

        JpaToscaProperty firstDataTypeFirstProperty = firstDataTypePropertiesIter.next();
        assertEquals(METRICS, firstDataTypeFirstProperty.getKey().getParentKeyName());
        assertEquals("controlLoopSchemaType", firstDataTypeFirstProperty.getKey().getLocalName());
        assertEquals(STRING_TEXT, firstDataTypeFirstProperty.getType().getName());
        assertTrue(firstDataTypeFirstProperty.isRequired());
        assertEquals("Specifies Control Loop Schema Type for the event Name e.g. VNF, VM",
                firstDataTypeFirstProperty.getDescription());
        assertEquals(1, firstDataTypeFirstProperty.getConstraints().size());
        assertEquals("org.onap.policy.models.tosca.simple.concepts.JpaToscaConstraintValidValues",
                firstDataTypeFirstProperty.getConstraints().iterator().next().getClass().getName());
        assertEquals(2,
                ((JpaToscaConstraintValidValues) (firstDataTypeFirstProperty.getConstraints().iterator().next()))
                        .getValidValues().size());

        JpaToscaProperty firstDataTypeSecondProperty = firstDataTypePropertiesIter.next();
        assertEquals(METRICS, firstDataTypeSecondProperty.getKey().getParentKeyName());
        assertEquals("eventName", firstDataTypeSecondProperty.getKey().getLocalName());
        assertEquals(STRING_TEXT, firstDataTypeSecondProperty.getType().getName());
        assertTrue(firstDataTypeSecondProperty.isRequired());
        assertEquals("Event name to which thresholds need to be applied", firstDataTypeSecondProperty.getDescription());

        JpaToscaProperty firstDataTypeThirdProperty = firstDataTypePropertiesIter.next();
        assertEquals(METRICS, firstDataTypeThirdProperty.getKey().getParentKeyName());
        assertEquals("policyName", firstDataTypeThirdProperty.getKey().getLocalName());
        assertEquals(STRING_TEXT, firstDataTypeThirdProperty.getType().getName());
        assertTrue(firstDataTypeThirdProperty.isRequired());
        assertEquals("TCA Policy Scope Name", firstDataTypeThirdProperty.getDescription());

        JpaToscaProperty firstDataTypeFourthProperty = firstDataTypePropertiesIter.next();
        assertEquals(METRICS, firstDataTypeFourthProperty.getKey().getParentKeyName());
        assertEquals("policyScope", firstDataTypeFourthProperty.getKey().getLocalName());
        assertEquals(STRING_TEXT, firstDataTypeFourthProperty.getType().getName());
        assertTrue(firstDataTypeFourthProperty.isRequired());
        assertEquals("TCA Policy Scope", firstDataTypeFourthProperty.getDescription());

        JpaToscaProperty firstDataTypeFifthProperty = firstDataTypePropertiesIter.next();
        assertEquals(METRICS, firstDataTypeFifthProperty.getKey().getParentKeyName());
        assertEquals("policyVersion", firstDataTypeFifthProperty.getKey().getLocalName());
        assertEquals(STRING_TEXT, firstDataTypeFifthProperty.getType().getName());
        assertTrue(firstDataTypeFifthProperty.isRequired());
        assertEquals("TCA Policy Scope Version", firstDataTypeFifthProperty.getDescription());

        JpaToscaProperty firstDataTypeSixthProperty = firstDataTypePropertiesIter.next();
        assertEquals(METRICS, firstDataTypeSixthProperty.getKey().getParentKeyName());
        assertEquals("thresholds", firstDataTypeSixthProperty.getKey().getLocalName());
        assertEquals("list", firstDataTypeSixthProperty.getType().getName());
        assertTrue(firstDataTypeSixthProperty.isRequired());
        assertEquals("Thresholds associated with eventName", firstDataTypeSixthProperty.getDescription());
        assertNotNull(firstDataTypeSixthProperty.getEntrySchema());
        assertEquals(THRESHOLDS, firstDataTypeSixthProperty.getEntrySchema().getType().getName());

        Entry<PfConceptKey, JpaToscaDataType> secondDataType = dataTypesIter.next();
        assertEquals(TCA_V1, secondDataType.getKey().getName());
        JpaToscaDataType secondDataTypeVal = secondDataType.getValue();
        assertEquals(DATATYPE_ROOT, secondDataTypeVal.getDerivedFrom().getName());
        assertEquals(VERSION_000, secondDataTypeVal.getDerivedFrom().getVersion());
        assertEquals(2, secondDataTypeVal.getProperties().size());
        Iterator<JpaToscaProperty> secondDataTypePropertiesIter = secondDataTypeVal.getProperties().values().iterator();

        JpaToscaProperty secondDataTypeFirstProperty = secondDataTypePropertiesIter.next();
        assertEquals(TCA_V1, secondDataTypeFirstProperty.getKey().getParentKeyName());
        assertEquals("domain", secondDataTypeFirstProperty.getKey().getLocalName());
        assertEquals(STRING_TEXT, secondDataTypeFirstProperty.getType().getName());
        assertTrue(secondDataTypeFirstProperty.isRequired());
        assertEquals("Domain name to which TCA needs to be applied", secondDataTypeFirstProperty.getDescription());
        assertEquals("measurementsForVfScaling", secondDataTypeFirstProperty.getDefaultValue());
        assertEquals(1, secondDataTypeFirstProperty.getConstraints().size());
        assertTrue(secondDataTypeFirstProperty.getConstraints().iterator().next() instanceof JpaToscaConstraintLogical);
        assertEquals("measurementsForVfScaling",
                ((JpaToscaConstraintLogical) (secondDataTypeFirstProperty.getConstraints().iterator().next()))
                        .getCompareTo());

        JpaToscaProperty secondDataTypeSecondProperty = secondDataTypePropertiesIter.next();
        assertEquals(TCA_V1, secondDataTypeSecondProperty.getKey().getParentKeyName());
        assertEquals("metricsPerEventName", secondDataTypeSecondProperty.getKey().getLocalName());
        assertEquals("list", secondDataTypeSecondProperty.getType().getName());
        assertTrue(secondDataTypeSecondProperty.isRequired());
        assertEquals("Contains eventName and threshold details that need to be applied to given eventName",
                secondDataTypeSecondProperty.getDescription());
        assertNotNull(secondDataTypeSecondProperty.getEntrySchema());
        assertEquals(METRICS, secondDataTypeSecondProperty.getEntrySchema().getType().getName());

        Entry<PfConceptKey, JpaToscaDataType> thirdDataType = dataTypesIter.next();
        assertEquals(THRESHOLDS, thirdDataType.getKey().getName());
        JpaToscaDataType thirdDataTypeVal = thirdDataType.getValue();
        assertEquals(DATATYPE_ROOT, thirdDataTypeVal.getDerivedFrom().getName());
        assertEquals(VERSION_000, thirdDataTypeVal.getDerivedFrom().getVersion());
        assertEquals(7, thirdDataTypeVal.getProperties().size());
        Iterator<JpaToscaProperty> thirdDataTypePropertiesIter = thirdDataTypeVal.getProperties().values().iterator();

        JpaToscaProperty thirdDataTypeFirstProperty = thirdDataTypePropertiesIter.next();
        assertEquals(THRESHOLDS, thirdDataTypeFirstProperty.getKey().getParentKeyName());
        assertEquals("closedLoopControlName", thirdDataTypeFirstProperty.getKey().getLocalName());
        assertEquals(STRING_TEXT, thirdDataTypeFirstProperty.getType().getName());
        assertTrue(thirdDataTypeFirstProperty.isRequired());
        assertEquals("Closed Loop Control Name associated with the threshold",
                thirdDataTypeFirstProperty.getDescription());

        JpaToscaProperty thirdDataTypeSecondProperty = thirdDataTypePropertiesIter.next();
        assertEquals(THRESHOLDS, thirdDataTypeSecondProperty.getKey().getParentKeyName());
        assertEquals("closedLoopEventStatus", thirdDataTypeSecondProperty.getKey().getLocalName());
        assertEquals(STRING_TEXT, thirdDataTypeSecondProperty.getType().getName());
        assertTrue(thirdDataTypeSecondProperty.isRequired());
        assertEquals("Closed Loop Event Status of the threshold", thirdDataTypeSecondProperty.getDescription());
        assertNotNull(thirdDataTypeSecondProperty.getConstraints());
        assertEquals(1, thirdDataTypeSecondProperty.getConstraints().size());
        assertEquals("JpaToscaConstraintValidValues(validValues=[ONSET, ABATED])",
                thirdDataTypeSecondProperty.getConstraints().iterator().next().toString());
        assertTrue(thirdDataTypeSecondProperty.getConstraints().iterator()
                .next() instanceof JpaToscaConstraintValidValues);
        assertEquals(2,
                ((JpaToscaConstraintValidValues) (thirdDataTypeSecondProperty.getConstraints().iterator().next()))
                    .getValidValues().size());

        JpaToscaProperty thirdDataTypeThirdProperty = thirdDataTypePropertiesIter.next();
        assertEquals(THRESHOLDS, thirdDataTypeThirdProperty.getKey().getParentKeyName());
        assertEquals("direction", thirdDataTypeThirdProperty.getKey().getLocalName());
        assertEquals(STRING_TEXT, thirdDataTypeThirdProperty.getType().getName());
        assertTrue(thirdDataTypeThirdProperty.isRequired());
        assertEquals("Direction of the threshold", thirdDataTypeThirdProperty.getDescription());
        assertNotNull(thirdDataTypeThirdProperty.getConstraints());
        assertEquals(1, thirdDataTypeThirdProperty.getConstraints().size());
        assertEquals(
                "JpaToscaConstraintValidValues(validValues=[LESS, LESS_OR_EQUAL, GREATER, GREATER_OR_EQUAL, EQUAL])",
                thirdDataTypeThirdProperty.getConstraints().iterator().next().toString());
        assertEquals(5,
                ((JpaToscaConstraintValidValues) (thirdDataTypeThirdProperty.getConstraints().iterator().next()))
                    .getValidValues().size());

        JpaToscaProperty thirdDataTypeFourthProperty = thirdDataTypePropertiesIter.next();
        assertEquals(THRESHOLDS, thirdDataTypeFourthProperty.getKey().getParentKeyName());
        assertEquals("fieldPath", thirdDataTypeFourthProperty.getKey().getLocalName());
        assertEquals(STRING_TEXT, thirdDataTypeFourthProperty.getType().getName());
        assertTrue(thirdDataTypeFourthProperty.isRequired());
        assertEquals("Json field Path as per CEF message which needs to be analyzed for TCA",
                thirdDataTypeFourthProperty.getDescription());
        assertNotNull(thirdDataTypeFourthProperty.getConstraints());
        assertEquals(1, thirdDataTypeFourthProperty.getConstraints().size());
        assertEquals(43,
                ((JpaToscaConstraintValidValues) (thirdDataTypeFourthProperty.getConstraints().iterator().next()))
                    .getValidValues().size());

        JpaToscaProperty thirdDataTypeFifthProperty = thirdDataTypePropertiesIter.next();
        assertEquals(THRESHOLDS, thirdDataTypeFifthProperty.getKey().getParentKeyName());
        assertEquals("severity", thirdDataTypeFifthProperty.getKey().getLocalName());
        assertEquals(STRING_TEXT, thirdDataTypeFifthProperty.getType().getName());
        assertTrue(thirdDataTypeFifthProperty.isRequired());
        assertEquals("Threshold Event Severity", thirdDataTypeFifthProperty.getDescription());
        assertNotNull(thirdDataTypeFifthProperty.getConstraints());
        assertEquals(1, thirdDataTypeFifthProperty.getConstraints().size());
        assertEquals("JpaToscaConstraintValidValues(validValues=[CRITICAL, MAJOR, MINOR, WARNING, NORMAL])",
                thirdDataTypeFifthProperty.getConstraints().iterator().next().toString());
        assertEquals(5,
                ((JpaToscaConstraintValidValues) (thirdDataTypeFifthProperty.getConstraints().iterator().next()))
                    .getValidValues().size());

        JpaToscaProperty thirdDataTypeSixthProperty = thirdDataTypePropertiesIter.next();
        assertEquals(THRESHOLDS, thirdDataTypeSixthProperty.getKey().getParentKeyName());
        assertEquals("thresholdValue", thirdDataTypeSixthProperty.getKey().getLocalName());
        assertEquals("integer", thirdDataTypeSixthProperty.getType().getName());
        assertTrue(thirdDataTypeSixthProperty.isRequired());
        assertEquals("Threshold value for the field Path inside CEF message",
                thirdDataTypeSixthProperty.getDescription());

        JpaToscaProperty thirdDataTypeSeventhProperty = thirdDataTypePropertiesIter.next();
        assertEquals(THRESHOLDS, thirdDataTypeSeventhProperty.getKey().getParentKeyName());
        assertEquals("version", thirdDataTypeSeventhProperty.getKey().getLocalName());
        assertEquals(STRING_TEXT, thirdDataTypeSeventhProperty.getType().getName());
        assertTrue(thirdDataTypeSeventhProperty.isRequired());
        assertEquals("Version number associated with the threshold", thirdDataTypeSeventhProperty.getDescription());
    }

    private void verifyCollectorInputDeserialization(JpaToscaServiceTemplate serviceTemplate) {

        // Sanity check the entire structure
        assertNotNull(serviceTemplate);
        LOGGER.info(serviceTemplate.validate("").toString());
        assertTrue(serviceTemplate.validate("").isValid());

        // Check tosca_definitions_version
        assertEquals("tosca_simple_yaml_1_1_0", serviceTemplate.getToscaDefinitionsVersion());

        // Check policy_types
        Map<PfConceptKey, JpaToscaPolicyType> policyTypesConceptMap = serviceTemplate.getPolicyTypes().getConceptMap();
        assertEquals(2, policyTypesConceptMap.size());
        Iterator<Entry<PfConceptKey, JpaToscaPolicyType>> policyTypesIter = policyTypesConceptMap.entrySet().iterator();

        Entry<PfConceptKey, JpaToscaPolicyType> firstPolicyType = policyTypesIter.next();
        assertEquals(MONITORING, firstPolicyType.getKey().getName());
        assertEquals(VERSION_100, firstPolicyType.getKey().getVersion());
        assertEquals("tosca.policies.Root", firstPolicyType.getValue().getDerivedFrom().getName());
        assertEquals("a base policy type for all policies that govern monitoring provisioning",
                firstPolicyType.getValue().getDescription());

        Entry<PfConceptKey, JpaToscaPolicyType> secondPolicyType = policyTypesIter.next();
        assertEquals(DCAE, secondPolicyType.getKey().getName());
        assertEquals(VERSION_100, secondPolicyType.getKey().getVersion());
        assertEquals("onap.policies.Monitoring", secondPolicyType.getValue().getDerivedFrom().getName());
        assertEquals(2, secondPolicyType.getValue().getProperties().size());

        Iterator<JpaToscaProperty> propertiesIter = secondPolicyType.getValue().getProperties().values().iterator();

        JpaToscaProperty firstProperty = propertiesIter.next();
        assertEquals(DCAE, firstProperty.getKey().getParentKeyName());
        assertEquals(VERSION_100, firstProperty.getKey().getParentKeyVersion());
        assertEquals("buscontroller_feed_publishing_endpoint", firstProperty.getKey().getLocalName());
        assertEquals(STRING_TEXT, firstProperty.getType().getName());
        assertEquals("Bus Controller feed endpoint", firstProperty.getDescription());

        JpaToscaProperty secondProperty = propertiesIter.next();
        assertEquals(DCAE, secondProperty.getKey().getParentKeyName());
        assertEquals(VERSION_100, secondProperty.getKey().getParentKeyVersion());
        assertEquals("datafile.policy", secondProperty.getKey().getLocalName());
        assertEquals(STRING_TEXT, secondProperty.getType().getName());
        assertEquals("datafile Policy JSON as string", secondProperty.getDescription());
    }

    private String serializeMonitoringServiceTemplate(JpaToscaServiceTemplate serviceTemplate) throws CoderException {
        ToscaServiceTemplate toscaServiceTemplate = serviceTemplate.toAuthorative();
        return coder.encode(toscaServiceTemplate);
    }
}
