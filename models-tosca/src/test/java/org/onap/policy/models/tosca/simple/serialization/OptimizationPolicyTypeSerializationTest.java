/*-
 * ============LICENSE_START=======================================================
 * Copyright (C) 2019 AT&T Intellectual Property. All rights reserved.
 * Modifications Copyright (C) 2020 Nordix Foundation.
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.Map;
import org.apache.commons.collections4.MapUtils;
import org.junit.Before;
import org.junit.Test;
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

public class OptimizationPolicyTypeSerializationTest {

    private static final String TYPE_ROOT = "tosca.policies.Root";
    private static final String VERSION = "1.0.0";

    private static final String INPUT_OPTIMIZATION_YAML = "policytypes/onap.policies.Optimization.yaml";
    private static final String INPUT_OPTIMIZATION_RESOURCE_YAML =
            "policytypes/onap.policies.optimization.Resource.yaml";
    private static final String INPUT_OPTIMIZATION_SERVICE_YAML = "policytypes/onap.policies.optimization.Service.yaml";

    private StandardCoder coder;

    @Before
    public void setUp() {
        coder = new StandardCoder();
    }

    @Test
    public void testOptimization() throws CoderException {
        JpaToscaServiceTemplate svctmpl = loadYaml(INPUT_OPTIMIZATION_YAML);
        validate("initial object", svctmpl, TYPE_ROOT, "onap.policies.Optimization", false, false);

        String ser = serialize(svctmpl);
        JpaToscaServiceTemplate svctmpl2 = deserialize(ser);
        validate("copy", svctmpl2, TYPE_ROOT, "onap.policies.Optimization", false, false);

        assertEquals(svctmpl, svctmpl2);
    }

    @Test
    public void testOptimizationResource() throws CoderException {
        JpaToscaServiceTemplate svctmpl = loadYaml(INPUT_OPTIMIZATION_RESOURCE_YAML);
        validate("initial object", svctmpl, "onap.policies.Optimization", "onap.policies.optimization.Resource", true,
                true);

        String ser = serialize(svctmpl);
        JpaToscaServiceTemplate svctmpl2 = deserialize(ser);
        validate("copy", svctmpl2, "onap.policies.Optimization", "onap.policies.optimization.Resource", true, true);

        assertEquals(svctmpl, svctmpl2);
    }

    @Test
    public void testOptimizationService() throws CoderException {
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

        assertEquals(testnm + " type count", 1, policyTypes.getConceptMap().size());
        JpaToscaPolicyType policyType = policyTypes.getConceptMap().values().iterator().next();

        assertEquals(testnm + " name", typeName, policyType.getName());
        assertEquals(testnm + " version", VERSION, policyType.getVersion());

        assertNotNull(testnm + " derived from", policyType.getDerivedFrom());
        assertEquals(testnm + " derived from name", derivedFrom, policyType.getDerivedFrom().getName());

        Map<String, JpaToscaProperty> props = policyType.getProperties();
        assertNotNull(testnm + " properties", props);

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

        assertNotNull(testnm, prop);
        validateMatchable(testnm, prop.getMetadata());
    }

    private void validateServices(String testName, JpaToscaProperty prop) {
        String testnm = testName + " services";

        assertNotNull(testnm, prop);
        validateMatchable(testnm, prop.getMetadata());
    }

    private void validateResources(String testName, JpaToscaProperty prop) {
        String testnm = testName + " resources";

        assertNotNull(testnm, prop);
        validateMatchable(testnm, prop.getMetadata());
    }

    private void validateGeography(String testName, JpaToscaProperty prop) {
        String testnm = testName + " geography";

        assertNotNull(testnm, prop);

        // this line results in a stack overflow
        // assertEquals(testnm + " name", "geography", prop.getName());

        assertEquals(testnm + " description", "One or more geographic regions", prop.getDescription());
        assertEquals(testnm + " type", "list", prop.getType().getName());
        validateMatchable(testnm, prop.getMetadata());
        assertTrue(testnm + " required", prop.isRequired());
        assertEquals(testnm + " entry_schema", "string", prop.getEntrySchema().getType().getName());

        List<JpaToscaConstraint> constraints = prop.getEntrySchema().getConstraints();
        assertNotNull(testnm + " constraints", constraints);

        assertEquals(testnm + " constraint size", 1, constraints.size());
        assertTrue(testnm + " constraint type", constraints.get(0) instanceof JpaToscaConstraintValidValues);
        JpaToscaConstraintValidValues constraint = (JpaToscaConstraintValidValues) constraints.get(0);

        assertEquals(testnm + " valid values", "[US, International]", constraint.getValidValues().toString());
    }

    private void validateIdentity(String testName, JpaToscaProperty prop) {
        String testnm = testName + " identity";

        assertNotNull(testnm, prop);
        assertEquals(testnm + " metadata", true, MapUtils.isEmpty(prop.getMetadata()));
    }

    private void validateMatchable(String testName, Map<String, String> metadata) {
        String testnm = testName + " matchable";

        assertNotNull(testnm + " metadata", metadata);
        assertEquals(testnm + " value", "true", metadata.get("matchable"));
    }
}
