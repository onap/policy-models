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

import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import org.junit.Before;
import org.junit.Test;
import org.onap.policy.common.utils.coder.CoderException;
import org.onap.policy.common.utils.coder.StandardCoder;
import org.onap.policy.common.utils.resources.ResourceUtils;
import org.onap.policy.models.base.PfConceptKey;
import org.onap.policy.models.base.PfValidationResult;
import org.onap.policy.models.tosca.authorative.concepts.ToscaServiceTemplate;
import org.onap.policy.models.tosca.simple.concepts.JpaToscaConstraintLogical;
import org.onap.policy.models.tosca.simple.concepts.JpaToscaConstraintValidValues;
import org.onap.policy.models.tosca.simple.concepts.JpaToscaDataType;
import org.onap.policy.models.tosca.simple.concepts.JpaToscaEntrySchema;
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
public class MonitoringPolicyTypeSerializationTest {

    private static final String DATATYPE_ROOT = "tosca.datatypes.Root";

    private static final String STRING_TEXT = "string";

    private static final String DCAE = "onap.policies.monitoring.dcaegen2.collectors.datafile.datafile-app-server";

    private static final String MONITORING = "onap.policies.Monitoring";

    private static final String THRESHOLDS = "onap.datatypes.monitoring.thresholds";

    private static final String TCA = "onap.datatypes.monitoring.tca_policy";

    private static final String METRICS = "onap.datatypes.monitoring.metricsPerEventName";

    private static final String VERSION_100 = "1.0.0";

    private static final String VERSION_000 = "0.0.0";

    private static final Logger LOGGER = LoggerFactory.getLogger(MonitoringPolicyTypeSerializationTest.class);

    private static final String MONITORING_TCA_YAML = "policytypes/onap.policies.monitoring.cdap.tca.hi.lo.app.yaml";
    private static final String MONITORING_COLLECTORS_YAML =
            "policytypes/onap.policies.monitoring.dcaegen2.collectors.datafile.datafile-app-server.yaml";

    private StandardCoder coder;

    @Before
    public void setUp() {
        coder = new StandardCoder();
    }

    @Test
    public void testDeserialization() throws Exception {
        // TCA
        JpaToscaServiceTemplate serviceTemplateFromYaml = deserializeMonitoringInputYaml(MONITORING_TCA_YAML);
        verifyTcaInputDeserialization(serviceTemplateFromYaml);

        // Collector
        serviceTemplateFromYaml = deserializeMonitoringInputYaml(MONITORING_COLLECTORS_YAML);
        verifyCollectorInputDeserialization(serviceTemplateFromYaml);
    }

    @Test
    public void testSerialization() {
        try {
            // TCA
            JpaToscaServiceTemplate tcaServiceTemplateFromYaml = deserializeMonitoringInputYaml(MONITORING_TCA_YAML);
            String serializedServiceTemplateTca = serializeMonitoringServiceTemplate(tcaServiceTemplateFromYaml);

            ToscaServiceTemplate toscaServiceTemplateFromJsonTca =
                    coder.decode(serializedServiceTemplateTca, ToscaServiceTemplate.class);

            JpaToscaServiceTemplate serviceTemplateFromJsonTca = new JpaToscaServiceTemplate();
            serviceTemplateFromJsonTca.fromAuthorative(toscaServiceTemplateFromJsonTca);
            String serializedServiceTemplateTcaOut = serializeMonitoringServiceTemplate(serviceTemplateFromJsonTca);
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

        } catch (Exception e) {
            LOGGER.warn("No exception should be thrown", e);
            fail("No exception should be thrown");
        }
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

    private void verifyTcaInputDeserialization(JpaToscaServiceTemplate serviceTemplate) {

        // Sanity check the entire structure
        assertNotNull(serviceTemplate);
        LOGGER.info(serviceTemplate.validate(new PfValidationResult()).toString());
        assertTrue(serviceTemplate.validate(new PfValidationResult()).isValid());

        // Check tosca_definitions_version
        assertEquals("tosca_simple_yaml_1_0_0", serviceTemplate.getToscaDefinitionsVersion());

        // Check policy_types
        Map<PfConceptKey, JpaToscaPolicyType> policyTypesConceptMap = serviceTemplate.getPolicyTypes().getConceptMap();
        assertTrue(policyTypesConceptMap.size() == 2);
        Iterator<Entry<PfConceptKey, JpaToscaPolicyType>> policyTypesIter = policyTypesConceptMap.entrySet().iterator();

        Entry<PfConceptKey, JpaToscaPolicyType> firstPolicyType = policyTypesIter.next();
        assertEquals(MONITORING, firstPolicyType.getKey().getName());
        assertEquals(VERSION_000, firstPolicyType.getKey().getVersion());
        assertEquals("tosca.policies.Root", firstPolicyType.getValue().getDerivedFrom().getName());
        assertEquals("a base policy type for all policies that governs monitoring provisioning",
                firstPolicyType.getValue().getDescription());

        Entry<PfConceptKey, JpaToscaPolicyType> secondPolicyType = policyTypesIter.next();
        assertEquals("onap.policies.monitoring.cdap.tca.hi.lo.app", secondPolicyType.getKey().getName());
        assertEquals(VERSION_100, secondPolicyType.getKey().getVersion());
        assertEquals(MONITORING, secondPolicyType.getValue().getDerivedFrom().getName());
        assertTrue(secondPolicyType.getValue().getProperties().size() == 1);

        JpaToscaProperty property = secondPolicyType.getValue().getProperties().values().iterator().next();
        assertEquals("onap.policies.monitoring.cdap.tca.hi.lo.app", property.getKey().getParentKeyName());
        assertEquals(VERSION_100, property.getKey().getParentKeyVersion());
        assertEquals("tca_policy", property.getKey().getLocalName());
        assertEquals("map", property.getType().getName());
        assertEquals("TCA Policy JSON", property.getDescription());

        JpaToscaEntrySchema entrySchema = property.getEntrySchema();
        assertEquals(TCA, entrySchema.getType().getName());

        // Check data_types
        Map<PfConceptKey, JpaToscaDataType> dataTypesConceptMap = serviceTemplate.getDataTypes().getConceptMap();
        assertTrue(dataTypesConceptMap.size() == 3);
        Iterator<Entry<PfConceptKey, JpaToscaDataType>> dataTypesIter = dataTypesConceptMap.entrySet().iterator();

        Entry<PfConceptKey, JpaToscaDataType> firstDataType = dataTypesIter.next();
        assertEquals(METRICS, firstDataType.getKey().getName());
        JpaToscaDataType firstDataTypeVal = firstDataType.getValue();
        assertEquals(DATATYPE_ROOT, firstDataTypeVal.getDerivedFrom().getName());
        assertEquals(VERSION_000, firstDataTypeVal.getDerivedFrom().getVersion());
        assertTrue(firstDataTypeVal.getProperties().size() == 6);
        Iterator<JpaToscaProperty> firstDataTypePropertiesIter = firstDataTypeVal.getProperties().values().iterator();

        JpaToscaProperty firstDataTypeFirstProperty = firstDataTypePropertiesIter.next();
        assertEquals(METRICS,
                firstDataTypeFirstProperty.getKey().getParentKeyName());
        assertEquals("controlLoopSchemaType", firstDataTypeFirstProperty.getKey().getLocalName());
        assertEquals(STRING_TEXT, firstDataTypeFirstProperty.getType().getName());
        assertTrue(firstDataTypeFirstProperty.isRequired());
        assertEquals("Specifies Control Loop Schema Type for the event Name e.g. VNF, VM",
                firstDataTypeFirstProperty.getDescription());
        assertTrue(firstDataTypeFirstProperty.getConstraints().size() == 1);
        assertEquals("org.onap.policy.models.tosca.simple.concepts.JpaToscaConstraintValidValues",
                firstDataTypeFirstProperty.getConstraints().iterator().next().getClass().getCanonicalName());
        assertTrue(((JpaToscaConstraintValidValues) (firstDataTypeFirstProperty.getConstraints().iterator().next()))
                .getValidValues().size() == 2);

        JpaToscaProperty firstDataTypeSecondProperty = firstDataTypePropertiesIter.next();
        assertEquals(METRICS,
                firstDataTypeSecondProperty.getKey().getParentKeyName());
        assertEquals("eventName", firstDataTypeSecondProperty.getKey().getLocalName());
        assertEquals(STRING_TEXT, firstDataTypeSecondProperty.getType().getName());
        assertTrue(firstDataTypeSecondProperty.isRequired());
        assertEquals("Event name to which thresholds need to be applied", firstDataTypeSecondProperty.getDescription());

        JpaToscaProperty firstDataTypeThirdProperty = firstDataTypePropertiesIter.next();
        assertEquals(METRICS,
                firstDataTypeThirdProperty.getKey().getParentKeyName());
        assertEquals("policyName", firstDataTypeThirdProperty.getKey().getLocalName());
        assertEquals(STRING_TEXT, firstDataTypeThirdProperty.getType().getName());
        assertTrue(firstDataTypeThirdProperty.isRequired());
        assertEquals("TCA Policy Scope Name", firstDataTypeThirdProperty.getDescription());

        JpaToscaProperty firstDataTypeFourthProperty = firstDataTypePropertiesIter.next();
        assertEquals(METRICS,
                firstDataTypeFourthProperty.getKey().getParentKeyName());
        assertEquals("policyScope", firstDataTypeFourthProperty.getKey().getLocalName());
        assertEquals(STRING_TEXT, firstDataTypeFourthProperty.getType().getName());
        assertTrue(firstDataTypeFourthProperty.isRequired());
        assertEquals("TCA Policy Scope", firstDataTypeFourthProperty.getDescription());

        JpaToscaProperty firstDataTypeFifthProperty = firstDataTypePropertiesIter.next();
        assertEquals(METRICS,
                firstDataTypeFifthProperty.getKey().getParentKeyName());
        assertEquals("policyVersion", firstDataTypeFifthProperty.getKey().getLocalName());
        assertEquals(STRING_TEXT, firstDataTypeFifthProperty.getType().getName());
        assertTrue(firstDataTypeFifthProperty.isRequired());
        assertEquals("TCA Policy Scope Version", firstDataTypeFifthProperty.getDescription());

        JpaToscaProperty firstDataTypeSixthProperty = firstDataTypePropertiesIter.next();
        assertEquals(METRICS,
                firstDataTypeSixthProperty.getKey().getParentKeyName());
        assertEquals("thresholds", firstDataTypeSixthProperty.getKey().getLocalName());
        assertEquals("list", firstDataTypeSixthProperty.getType().getName());
        assertTrue(firstDataTypeSixthProperty.isRequired());
        assertEquals("Thresholds associated with eventName", firstDataTypeSixthProperty.getDescription());
        assertNotNull(firstDataTypeSixthProperty.getEntrySchema());
        assertEquals(THRESHOLDS,
                firstDataTypeSixthProperty.getEntrySchema().getType().getName());

        Entry<PfConceptKey, JpaToscaDataType> secondDataType = dataTypesIter.next();
        assertEquals(TCA, secondDataType.getKey().getName());
        JpaToscaDataType secondDataTypeVal = secondDataType.getValue();
        assertEquals(DATATYPE_ROOT, secondDataTypeVal.getDerivedFrom().getName());
        assertEquals(VERSION_000, secondDataTypeVal.getDerivedFrom().getVersion());
        assertTrue(secondDataTypeVal.getProperties().size() == 2);
        Iterator<JpaToscaProperty> secondDataTypePropertiesIter = secondDataTypeVal.getProperties().values().iterator();

        JpaToscaProperty secondDataTypeFirstProperty = secondDataTypePropertiesIter.next();
        assertEquals(TCA, secondDataTypeFirstProperty.getKey().getParentKeyName());
        assertEquals("domain", secondDataTypeFirstProperty.getKey().getLocalName());
        assertEquals(STRING_TEXT, secondDataTypeFirstProperty.getType().getName());
        assertTrue(secondDataTypeFirstProperty.isRequired());
        assertEquals("Domain name to which TCA needs to be applied", secondDataTypeFirstProperty.getDescription());
        assertEquals("measurementsForVfScaling", secondDataTypeFirstProperty.getDefaultValue());
        assertTrue(secondDataTypeFirstProperty.getConstraints().size() == 1);
        assertTrue(secondDataTypeFirstProperty.getConstraints().iterator().next() instanceof JpaToscaConstraintLogical);
        assertEquals("measurementsForVfScaling",
                ((JpaToscaConstraintLogical) (secondDataTypeFirstProperty.getConstraints().iterator().next()))
                        .getCompareTo());

        JpaToscaProperty secondDataTypeSecondProperty = secondDataTypePropertiesIter.next();
        assertEquals(TCA, secondDataTypeSecondProperty.getKey().getParentKeyName());
        assertEquals("metricsPerEventName", secondDataTypeSecondProperty.getKey().getLocalName());
        assertEquals("list", secondDataTypeSecondProperty.getType().getName());
        assertTrue(secondDataTypeSecondProperty.isRequired());
        assertEquals("Contains eventName and threshold details that need to be applied to given eventName",
                secondDataTypeSecondProperty.getDescription());
        assertNotNull(secondDataTypeSecondProperty.getEntrySchema());
        assertEquals(METRICS,
                secondDataTypeSecondProperty.getEntrySchema().getType().getName());

        Entry<PfConceptKey, JpaToscaDataType> thirdDataType = dataTypesIter.next();
        assertEquals(THRESHOLDS, thirdDataType.getKey().getName());
        JpaToscaDataType thirdDataTypeVal = thirdDataType.getValue();
        assertEquals(DATATYPE_ROOT, thirdDataTypeVal.getDerivedFrom().getName());
        assertEquals(VERSION_000, thirdDataTypeVal.getDerivedFrom().getVersion());
        assertTrue(thirdDataTypeVal.getProperties().size() == 7);
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
        assertTrue(thirdDataTypeSecondProperty.getConstraints().size() == 1);
        assertEquals("JpaToscaConstraintValidValues(validValues=[ONSET, ABATED])",
                thirdDataTypeSecondProperty.getConstraints().iterator().next().toString());
        assertTrue(thirdDataTypeSecondProperty.getConstraints().iterator()
                .next() instanceof JpaToscaConstraintValidValues);
        assertTrue(((JpaToscaConstraintValidValues) (thirdDataTypeSecondProperty.getConstraints().iterator().next()))
                .getValidValues().size() == 2);

        JpaToscaProperty thirdDataTypeThirdProperty = thirdDataTypePropertiesIter.next();
        assertEquals(THRESHOLDS, thirdDataTypeThirdProperty.getKey().getParentKeyName());
        assertEquals("direction", thirdDataTypeThirdProperty.getKey().getLocalName());
        assertEquals(STRING_TEXT, thirdDataTypeThirdProperty.getType().getName());
        assertTrue(thirdDataTypeThirdProperty.isRequired());
        assertEquals("Direction of the threshold", thirdDataTypeThirdProperty.getDescription());
        assertNotNull(thirdDataTypeThirdProperty.getConstraints());
        assertTrue(thirdDataTypeThirdProperty.getConstraints().size() == 1);
        assertEquals(
                "JpaToscaConstraintValidValues(validValues=[LESS, LESS_OR_EQUAL, GREATER, GREATER_OR_EQUAL, EQUAL])",
                thirdDataTypeThirdProperty.getConstraints().iterator().next().toString());
        assertTrue(((JpaToscaConstraintValidValues) (thirdDataTypeThirdProperty.getConstraints().iterator().next()))
                .getValidValues().size() == 5);

        JpaToscaProperty thirdDataTypeFourthProperty = thirdDataTypePropertiesIter.next();
        assertEquals(THRESHOLDS, thirdDataTypeFourthProperty.getKey().getParentKeyName());
        assertEquals("fieldPath", thirdDataTypeFourthProperty.getKey().getLocalName());
        assertEquals(STRING_TEXT, thirdDataTypeFourthProperty.getType().getName());
        assertTrue(thirdDataTypeFourthProperty.isRequired());
        assertEquals("Json field Path as per CEF message which needs to be analyzed for TCA",
                thirdDataTypeFourthProperty.getDescription());
        assertNotNull(thirdDataTypeFourthProperty.getConstraints());
        assertTrue(thirdDataTypeFourthProperty.getConstraints().size() == 1);
        assertTrue(((JpaToscaConstraintValidValues) (thirdDataTypeFourthProperty.getConstraints().iterator().next()))
                .getValidValues().size() == 43);

        JpaToscaProperty thirdDataTypeFifthProperty = thirdDataTypePropertiesIter.next();
        assertEquals(THRESHOLDS, thirdDataTypeFifthProperty.getKey().getParentKeyName());
        assertEquals("severity", thirdDataTypeFifthProperty.getKey().getLocalName());
        assertEquals(STRING_TEXT, thirdDataTypeFifthProperty.getType().getName());
        assertTrue(thirdDataTypeFifthProperty.isRequired());
        assertEquals("Threshold Event Severity", thirdDataTypeFifthProperty.getDescription());
        assertNotNull(thirdDataTypeFifthProperty.getConstraints());
        assertTrue(thirdDataTypeFifthProperty.getConstraints().size() == 1);
        assertEquals("JpaToscaConstraintValidValues(validValues=[CRITICAL, MAJOR, MINOR, WARNING, NORMAL])",
                thirdDataTypeFifthProperty.getConstraints().iterator().next().toString());
        assertTrue(((JpaToscaConstraintValidValues) (thirdDataTypeFifthProperty.getConstraints().iterator().next()))
                .getValidValues().size() == 5);;

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
        LOGGER.info(serviceTemplate.validate(new PfValidationResult()).toString());
        assertTrue(serviceTemplate.validate(new PfValidationResult()).isValid());

        // Check tosca_definitions_version
        assertEquals("tosca_simple_yaml_1_0_0", serviceTemplate.getToscaDefinitionsVersion());

        // Check policy_types
        Map<PfConceptKey, JpaToscaPolicyType> policyTypesConceptMap = serviceTemplate.getPolicyTypes().getConceptMap();
        assertTrue(policyTypesConceptMap.size() == 2);
        Iterator<Entry<PfConceptKey, JpaToscaPolicyType>> policyTypesIter = policyTypesConceptMap.entrySet().iterator();

        Entry<PfConceptKey, JpaToscaPolicyType> firstPolicyType = policyTypesIter.next();
        assertEquals(MONITORING, firstPolicyType.getKey().getName());
        assertEquals(VERSION_100, firstPolicyType.getKey().getVersion());
        assertEquals("tosca.policies.Root", firstPolicyType.getValue().getDerivedFrom().getName());
        assertEquals("a base policy type for all policies that govern monitoring provision",
                firstPolicyType.getValue().getDescription());

        Entry<PfConceptKey, JpaToscaPolicyType> secondPolicyType = policyTypesIter.next();
        assertEquals(DCAE,
                secondPolicyType.getKey().getName());
        assertEquals(VERSION_100, secondPolicyType.getKey().getVersion());
        assertEquals("policy.nodes.Root", secondPolicyType.getValue().getDerivedFrom().getName());
        assertTrue(secondPolicyType.getValue().getProperties().size() == 2);

        Iterator<JpaToscaProperty> propertiesIter = secondPolicyType.getValue().getProperties().values().iterator();

        JpaToscaProperty firstProperty = propertiesIter.next();
        assertEquals(DCAE,
                firstProperty.getKey().getParentKeyName());
        assertEquals(VERSION_100, firstProperty.getKey().getParentKeyVersion());
        assertEquals("buscontroller_feed_publishing_endpoint", firstProperty.getKey().getLocalName());
        assertEquals(STRING_TEXT, firstProperty.getType().getName());
        assertEquals("DMAAP Bus Controller feed endpoint", firstProperty.getDescription());

        JpaToscaProperty secondProperty = propertiesIter.next();
        assertEquals(DCAE,
                secondProperty.getKey().getParentKeyName());
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
