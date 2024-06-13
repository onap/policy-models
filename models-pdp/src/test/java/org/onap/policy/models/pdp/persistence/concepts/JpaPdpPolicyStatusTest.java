/*-
 * ============LICENSE_START=======================================================
 * ONAP
 * ================================================================================
 * Copyright (C) 2021 AT&T Intellectual Property. All rights reserved.
 * Modifications Copyright (C) 2024 Nordix Foundation
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

package org.onap.policy.models.pdp.persistence.concepts;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.UnaryOperator;
import org.assertj.core.api.AbstractStringAssert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.onap.policy.models.base.PfKey;
import org.onap.policy.models.base.PfReferenceKey;
import org.onap.policy.models.base.Validated;
import org.onap.policy.models.pdp.concepts.PdpPolicyStatus;
import org.onap.policy.models.pdp.concepts.PdpPolicyStatus.PdpPolicyStatusBuilder;
import org.onap.policy.models.pdp.concepts.PdpPolicyStatus.State;
import org.onap.policy.models.tosca.authorative.concepts.ToscaConceptIdentifier;

class JpaPdpPolicyStatusTest {
    private static final String MY_PDP = "MyPdp";
    private static final String MY_GROUP = "MyGroup";
    private static final String MY_PDP_TYPE = "MyPdpType";
    private static final ToscaConceptIdentifier POLICY = new ToscaConceptIdentifier("MyPolicy", "1.2.3");
    private static final ToscaConceptIdentifier POLICY_TYPE = new ToscaConceptIdentifier("MyPolicyType", "1.2.4");

    private PdpPolicyStatusBuilder builder;


    /**
     * Set up Policy Status builder.
     */
    @BeforeEach
    void setup() {
        // @formatter:off
        builder = PdpPolicyStatus.builder()
                        .deploy(true)
                        .pdpGroup(MY_GROUP)
                        .pdpId(MY_PDP)
                        .pdpType(MY_PDP_TYPE)
                        .policy(POLICY)
                        .policyType(POLICY_TYPE)
                        .state(State.SUCCESS);
        // @formatter:on
    }

    @Test
    void testJpaPdpPolicyStatus() {
        JpaPdpPolicyStatus jpa = new JpaPdpPolicyStatus();

        assertThat(jpa.getKey()).isNotNull();
        assertThat(jpa.getKey().isNullKey()).isTrue();
        assertThat(jpa.getPdpGroup()).isEqualTo(PfKey.NULL_KEY_NAME);
        assertThat(jpa.getPdpType()).isEqualTo(PfKey.NULL_KEY_NAME);
        assertThat(jpa.getPolicyType()).isNotNull();
        assertThat(jpa.getPolicyType().isNullKey()).isTrue();
        assertThat(jpa.isDeploy()).isFalse();
        assertThat(jpa.getState()).isEqualTo(State.WAITING);
    }

    @Test
    void testJpaPdpPolicyStatusJpaPdpPolicyStatus() {
        JpaPdpPolicyStatus jpa = new JpaPdpPolicyStatus(builder.build());

        assertThat(new JpaPdpPolicyStatus(jpa)).isEqualTo(jpa);
    }

    @Test
    void testJpaPdpPolicyStatusPdpPolicyStatus() {
        JpaPdpPolicyStatus jpa = new JpaPdpPolicyStatus(builder.build());

        assertThat(jpa.getKey()).isNotNull();
        PfReferenceKey key = jpa.getKey();
        assertThat(key.getParentKeyName()).isEqualTo(POLICY.getName());
        assertThat(key.getParentKeyVersion()).isEqualTo(POLICY.getVersion());
        assertThat(key.getParentLocalName()).isEqualTo(PfKey.NULL_KEY_NAME);
        assertThat(key.getLocalName()).isEqualTo(MY_PDP);

        assertThat(jpa.getPdpGroup()).isEqualTo(MY_GROUP);
        assertThat(jpa.getPdpType()).isEqualTo(MY_PDP_TYPE);

        assertThat(jpa.getPolicyType()).isNotNull();
        assertThat(jpa.getPolicyType().getName()).isEqualTo(POLICY_TYPE.getName());
        assertThat(jpa.getPolicyType().getVersion()).isEqualTo(POLICY_TYPE.getVersion());

        assertThat(jpa.isDeploy()).isTrue();
        assertThat(jpa.getState()).isEqualTo(State.SUCCESS);
    }

    @Test
    void testGetKeys() {
        JpaPdpPolicyStatus jpa = new JpaPdpPolicyStatus(builder.build());

        assertThat(jpa.getKeys()).isEqualTo(List.of(jpa.getKey()));
    }

    @Test
    void testClean() {
        JpaPdpPolicyStatus jpa =
                        new JpaPdpPolicyStatus(builder.pdpGroup(MY_GROUP + " ").pdpType(MY_PDP_TYPE + " ").build());

        jpa.clean();

        assertThat(jpa.getPdpGroup()).isEqualTo(MY_GROUP);
        assertThat(jpa.getPdpType()).isEqualTo(MY_PDP_TYPE);
    }

    @Test
    @SuppressWarnings("serial")
    void testCompareTo() {
        JpaPdpPolicyStatus jpa = new JpaPdpPolicyStatus(builder.build());

        assertNotEquals(0, jpa.compareTo(null));
        assertEquals(0, jpa.compareTo(jpa));
        assertNotEquals(0, jpa.compareTo(new JpaPdpPolicyStatus(builder.build()) {}));

        assertNotEquals(0, checkCompareTo(bldr -> bldr.pdpId("AnotherPdp")));
        assertNotEquals(0, checkCompareTo(bldr -> bldr.pdpGroup("AnotherGroup")));
        assertNotEquals(0, checkCompareTo(bldr -> bldr.pdpType("AnotherType")));
        assertNotEquals(0, checkCompareTo(
            bldr -> bldr.policyType(new ToscaConceptIdentifier("AnotherPolicyType", "1.2.4"))));
        assertNotEquals(0, checkCompareTo(bldr -> bldr.deploy(false)));
        assertNotEquals(0, checkCompareTo(bldr -> bldr.state(State.FAILURE)));
    }

    private int checkCompareTo(UnaryOperator<PdpPolicyStatusBuilder> fieldModifier) {
        JpaPdpPolicyStatus jpa1 = new JpaPdpPolicyStatus(builder.build());
        JpaPdpPolicyStatus jpa2 = new JpaPdpPolicyStatus(fieldModifier.apply(builder).build());

        return jpa1.compareTo(jpa2);
    }

    @Test
    void testToAuthorative() {
        PdpPolicyStatus data = builder.build();

        assertThat(new JpaPdpPolicyStatus(data).toAuthorative()).isEqualTo(data);
    }

    @Test
    void testFromAuthorative() {
        PdpPolicyStatus data = builder.build();
        JpaPdpPolicyStatus jpa = new JpaPdpPolicyStatus();

        jpa.fromAuthorative(data);

        assertThat(jpa).isEqualTo(new JpaPdpPolicyStatus(data));
    }

    @Test
    void testValidate() {
        assertThat(new JpaPdpPolicyStatus(builder.build()).validate("").getResult()).isNull();

        assertThatThrownBy(() -> new JpaPdpPolicyStatus(builder.build()).validate(null))
                        .hasMessageContaining("fieldName").hasMessageContaining("is null");

        checkValidate(jpa -> jpa.getKey().setParentKeyName(PfKey.NULL_KEY_NAME)).contains("policy name",
                        Validated.IS_NULL);

        checkValidate(jpa -> jpa.getKey().setParentKeyVersion(PfKey.NULL_KEY_VERSION)).contains("policy version",
                        Validated.IS_NULL);

        checkValidate(jpa -> jpa.getKey().setParentLocalName("SomeName")).contains("parent local name", "must be NULL");

        checkValidate(jpa -> jpa.getKey().setLocalName(PfKey.NULL_KEY_NAME)).contains("pdpId", Validated.IS_NULL);
    }

    private AbstractStringAssert<?> checkValidate(Consumer<JpaPdpPolicyStatus> fieldModifier) {
        JpaPdpPolicyStatus jpa = new JpaPdpPolicyStatus(builder.build());
        fieldModifier.accept(jpa);

        return assertThat(jpa.validate("").getResult());
    }
}
