/*-
 * ============LICENSE_START=======================================================
 * Copyright (C) 2019 AT&T Intellectual Property. All rights reserved.
 * Modifications Copyright (C) 2020, 2024 Nordix Foundation.
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

package org.onap.policy.models.tosca.simple.serialization;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Map;
import org.apache.commons.collections4.MapUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.onap.policy.common.utils.coder.CoderException;
import org.onap.policy.common.utils.coder.StandardCoder;
import org.onap.policy.common.utils.resources.ResourceUtils;
import org.onap.policy.models.tosca.authorative.concepts.ToscaServiceTemplate;
import org.onap.policy.models.tosca.simple.concepts.JpaToscaConstraint;
import org.onap.policy.models.tosca.simple.concepts.JpaToscaConstraintValidValues;
import org.onap.policy.models.tosca.simple.concepts.JpaToscaPolicyType;
import org.onap.policy.models.tosca.simple.concepts.JpaToscaPolicyTypes;
import org.onap.policy.models.tosca.simple.concepts.JpaToscaProperty;
import org.onap.policy.models.tosca.simple.concepts.JpaToscaServiceTemplate;
import org.yaml.snakeyaml.Yaml;

class OptimizationPolicyTypeSerializationTest {

    private static final String TYPE_ROOT = "tosca.policies.Root";
    private static final String VERSION = "1.0.0";

    private static final String INPUT_OPTIMIZATION_YAML = "policytypes/onap.policies.Optimization.yaml";
    private static final String INPUT_OPTIMIZATION_RESOURCE_YAML =
            "policytypes/onap.policies.optimization.Resource.yaml";
    private static final String INPUT_OPTIMIZATION_SERVICE_YAML = "policytypes/onap.policies.optimization.Service.yaml";

    private StandardCoder coder;

    @BeforeEach
    void setUp() {
        coder = new StandardCoder();
    }

    @Test
    void testOptimization() throws CoderException {
        JpaToscaServiceTemplate svctmpl = loadYaml(INPUT_OPTIMIZATION_YAML);
        validate("initial object", svctmpl, TYPE_ROOT, "onap.policies.Optimization", false, false);

        String ser = serialize(svctmpl);
        JpaToscaServiceTemplate svctmpl2 = deserialize(ser);
        validate("copy", svctmpl2, TYPE_ROOT, "onap.policies.Optimization", false, false);

        assertEquals(svctmpl, svctmpl2);
    }

    @Test
    void testOptimizationResource() throws CoderException {
        JpaToscaServiceTemplate svctmpl = loadYaml(INPUT_OPTIMIZATION_RESOURCE_YAML);
        validate("initial object", svctmpl, "onap.policies.Optimization", "onap.policies.optimization.Resource", true,
                true);

        String ser = serialize(svctmpl);
        JpaToscaServiceTemplate svctmpl2 = deserialize(ser);
        validate("copy", svctmpl2, "onap.policies.Optimization", "onap.policies.optimization.Resource", true, true);

        assertEquals(svctmpl, svctmpl2);
    }

    @Test
    void testOptimizationService() throws CoderException {
        JpaToscaServiceTemplate svctmpl = loadYaml(INPUT_OPTIMIZATION_SERVICE_YAML);
        validate("initial object", svctmpl, "onap.policies.Optimization", "onap.policies.optimization.Service", false,
                true);

        String ser = serialize(svctmpl);
        JpaToscaServiceTemplate svctmpl2 = deserialize(ser);
        validate("copy", svctmpl2, "onap.policies.Optimization", "onap.policies.optimization.Service", false, true);

        assertEquals(svctmpl, svctmpl2);
    }

    private JpaToscaServiceTemplate loadYaml(String yamlFileName) throws CoderException {
        Yaml yaml = new Yaml();
        String policyTypeYaml = ResourceUtils.getResourceAsString(yamlFileName);
        Object yamlObject = yaml.load(policyTypeYaml);
        String yamlAsJsonString = coder.encode(yamlObject);
        return deserialize(yamlAsJsonString);
    }

    private JpaToscaServiceTemplate deserialize(String json) throws CoderException {
        ToscaServiceTemplate auth = coder.decode(json, ToscaServiceTemplate.class);

        JpaToscaServiceTemplate svctmpl = new JpaToscaServiceTemplate();
        svctmpl.fromAuthorative(auth);
        return svctmpl;
    }

    private String serialize(JpaToscaServiceTemplate svctmpl) throws CoderException {
        ToscaServiceTemplate auth = svctmpl.toAuthorative();
        return coder.encode(auth);
    }

    private void validate(String testnm, JpaToscaServiceTemplate svctmpl, String derivedFrom, String typeName,
            boolean checkResource, boolean checkService) {
        JpaToscaPolicyTypes policyTypes = svctmpl.getPolicyTypes();

        assertEquals(1, policyTypes.getConceptMap().size(), testnm + " type count");
        JpaToscaPolicyType policyType = policyTypes.getConceptMap().values().iterator().next();

        assertEquals(typeName, policyType.getName(), testnm + " name");
        assertEquals(VERSION, policyType.getVersion(), testnm + " version");

        assertNotNull(String.valueOf(policyType.getDerivedFrom()), testnm + " derived from");
        assertEquals(derivedFrom, policyType.getDerivedFrom().getName(), testnm + " derived from name");

        Map<String, JpaToscaProperty> props = policyType.getProperties();
        assertNotNull(props.toString(), testnm + " properties");

        if (checkResource && checkService) {
            validateResources(testnm, props.get("resources"));
            validateServices(testnm, props.get("services"));
        } else if (checkService && !checkResource) {
            validateServices(testnm, props.get("services"));
        } else {
            validateScope(testnm, props.get("scope"));
            validateGeography(testnm, props.get("geography"));
            validateIdentity(testnm, props.get("identity"));
        }
    }

    // only need to validate deep match of one of these; geography is the most interesting

    private void validateScope(String testName, JpaToscaProperty prop) {
        String testnm = testName + " scope";

        assertNotNull(prop, testnm);
        validateMatchable(testnm, prop.getMetadata());
    }

    private void validateServices(String testName, JpaToscaProperty prop) {
        String testnm = testName + " services";

        assertNotNull(prop, testnm);
        validateMatchable(testnm, prop.getMetadata());
    }

    private void validateResources(String testName, JpaToscaProperty prop) {
        String testnm = testName + " resources";

        assertNotNull(prop, testnm);
        validateMatchable(testnm, prop.getMetadata());
    }

    private void validateGeography(String testName, JpaToscaProperty prop) {
        String testnm = testName + " geography";

        assertNotNull(prop, testnm);

        assertEquals("One or more geographic regions", prop.getDescription(), testnm + " description");
        assertEquals("list", prop.getType().getName(), testnm + " description");
        validateMatchable(testnm, prop.getMetadata());
        assertTrue(prop.isRequired(), testnm + " required");
        assertEquals("string", prop.getEntrySchema().getType().getName(), testnm + " description");

        List<JpaToscaConstraint> constraints = prop.getEntrySchema().getConstraints();
        assertNotNull(constraints, testnm + " constraints");

        assertEquals(1, constraints.size(), testnm + " constraint size");
        assertInstanceOf(JpaToscaConstraintValidValues.class, constraints.get(0), testnm + " constraint type");
        JpaToscaConstraintValidValues constraint = (JpaToscaConstraintValidValues) constraints.get(0);

        assertEquals("[US, International]", constraint.getValidValues().toString(), testnm + " valid values");
    }

    private void validateIdentity(String testName, JpaToscaProperty prop) {
        String testnm = testName + " identity";

        assertNotNull(prop, testnm);
        assertTrue(MapUtils.isEmpty(prop.getMetadata()), testnm + " metadata");
    }

    private void validateMatchable(String testName, Map<String, String> metadata) {
        String testnm = testName + " matchable";

        assertNotNull(metadata, testnm + " metadata");
        assertEquals("true", metadata.get("matchable"), testnm + " value");
    }
}
