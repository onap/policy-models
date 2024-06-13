/*-
 * ============LICENSE_START=======================================================
 * ONAP
 * ================================================================================
 * Copyright (C) 2021 AT&T Intellectual Property. All rights reserved.
 * Modifications Copyright (C) 2023, 2024 Nordix Foundation.
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

package org.onap.policy.models.base;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import jakarta.validation.Valid;
import java.io.Serial;
import java.util.concurrent.atomic.AtomicBoolean;
import lombok.Getter;
import lombok.NonNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.onap.policy.common.parameters.BeanValidationResult;
import org.onap.policy.common.parameters.annotations.NotNull;
import org.onap.policy.common.utils.coder.CoderException;
import org.onap.policy.common.utils.coder.StandardCoder;
import org.onap.policy.models.base.validation.annotations.PfMin;
import org.onap.policy.models.base.validation.annotations.VerifyKey;

class PfValidatorTest {
    private static final String KEY_FIELD = "key";

    private static final String STRING_VALUE = "abc";

    private PfValidator validator;

    @BeforeEach
    void setUp() {
        validator = new PfValidator();
    }

    @Test
    void testAddValidatorsValueValidator() {
        // verify that standard annotations work
        StdAnnotation data = new StdAnnotation();
        data.strValue = STRING_VALUE;
        assertThat(validator.validateTop("", data).getResult()).isNull();

        data.strValue = null;
        assertThat(validator.validateTop("", data).getResult()).contains("strValue", "null");
    }

    @Test
    void testVerPfMin() {
        PfMinChecker data = new PfMinChecker();
        data.intValue = 10;
        assertThat(validator.validateTop("", data).getResult()).isNull();

        data.intValue = -2;
        assertThat(validator.validateTop("", data).getResult()).isNull();

        data.intValue = null;
        assertThat(validator.validateTop("", data).getResult()).isNull();

        data.intValue = STRING_VALUE;
        assertThat(validator.validateTop("", data).getResult()).isNull();

        data.intValue = -1;
        assertThat(validator.validateTop("", data).getResult()).contains("intValue", "-1");
    }

    @Test
    void testVerCascadeBeanValidationResultStringObject() {
        CascadeChecker checker = new CascadeChecker();
        checker.plain = new StdAnnotation();

        // valid
        checker.plain.strValue = STRING_VALUE;
        BeanValidationResult result = new BeanValidationResult("", this);
        assertThat(validator.verCascade(result, "", checker)).isTrue();

        // invalid
        checker.plain.strValue = null;

        result = new BeanValidationResult("", this);
        assertThat(validator.verCascade(result, "", checker.plain)).isFalse();
        assertThat(result.getResult()).contains("null");

        result = new BeanValidationResult("", this);
        assertThat(validator.verCascade(result, "", checker)).isFalse();
        assertThat(result.getResult()).contains("null").doesNotContain("plain");

        // validator returns null result - should be treated as valid
        checker = new CascadeChecker() {
            @Override
            public BeanValidationResult validate(@NonNull String fieldName) {
                return null;
            }
        };
        checker.plain = new StdAnnotation();
        result = new BeanValidationResult("", this);
        assertThat(validator.verCascade(result, "", checker)).isTrue();
    }

    @Test
    void testVerKey() throws CoderException {
        FullKeyAnnot data = new FullKeyAnnot();

        // not a key
        data.key = STRING_VALUE;
        assertThat(validator.validateTop("", data).getResult()).isNull();

        // null key
        data.key = new PfConceptKey();
        assertThat(validator.validateTop("", data).getResult())
            .contains(KEY_FIELD, "NULL:0.0.0", Validated.IS_A_NULL_KEY).doesNotContain("name", "version");

        // invalid version - should invoke verCascade() which will invoke key.validate()
        data.key = new StandardCoder().decode("{'name':'abc', 'version':'xyzzy'}".replace('\'', '"'),
            PfConceptKey.class);
        assertThat(validator.validateTop("", data).getResult())
            .contains(KEY_FIELD, "version", "xyzzy", "regular expression").doesNotContain("name");

        // not a PfKeyImpl - should not check individual fields
        PfKey pfkey = mock(PfKey.class);
        data.key = pfkey;
        assertThat(validator.validateTop("", data).getResult()).isNull();

        when(pfkey.isNullKey()).thenReturn(true);
        assertThat(validator.validateTop("", data).getResult()).contains(KEY_FIELD, Validated.IS_A_NULL_KEY);

        // null name
        data.key = new PfConceptKey(PfKey.NULL_KEY_NAME, "2.3.4");
        assertThat(validator.validateTop("", data).getResult()).contains(KEY_FIELD, "name", "null")
            .doesNotContain("version", "2.3.4");

        // null version
        data.key = new PfConceptKey(STRING_VALUE, PfKey.NULL_KEY_VERSION);
        assertThat(validator.validateTop("", data).getResult()).contains(KEY_FIELD, "version", "null")
            .doesNotContain("name", STRING_VALUE);

        // null name, invalid version - should get two messages
        data.key = new StandardCoder().decode("{'name':'NULL', 'version':'xyzzy'}".replace('\'', '"'),
            PfConceptKey.class);
        assertThat(validator.validateTop("", data).getResult()).contains(KEY_FIELD, "name", "null", "version", "xyzzy",
            "regular expression");

        /*
         * Tests with all flags set to "false" (i.e., no validations).
         */

        EmptyKeyAnnot data2 = new EmptyKeyAnnot();

        // build a key that is totally invalid
        AtomicBoolean called = new AtomicBoolean();

        data2.key = new PfConceptKey() {
            @Serial
            private static final long serialVersionUID = 1L;

            @Override
            public BeanValidationResult validate(@NonNull String fieldName) {
                called.set(true);
                return null;
            }
        };

        // should be ok, since no validations are performed
        assertThat(validator.validateTop("", data2).getResult()).isNull();
        assertThat(called.get()).isFalse();
    }

    @Test
    void testXlateObject() {
        assertThat(validator.xlate(null)).isNull();
        assertThat(validator.xlate("hello")).isEqualTo("hello");

        PfConceptKey key = new PfConceptKey("hello", "1.2.3");
        assertThat(validator.xlate(key)).isEqualTo("hello:1.2.3");
    }

    public static class StdAnnotation {
        @Getter
        @NotNull
        private String strValue;
    }

    public static class PfMinChecker {
        @Getter
        @PfMin(value = 5, allowed = -2)
        private Object intValue;
    }

    public static class CascadeChecker extends Validated {
        @Getter
        @Valid
        private StdAnnotation plain;

        @Override
        public BeanValidationResult validate(@NonNull String fieldName) {
            // directly validates "plain"
            return new PfValidator().validateTop(fieldName, plain);
        }
    }

    public static class FullKeyAnnot {
        @Getter
        @VerifyKey(keyNotNull = true, nameNotNull = true, versionNotNull = true, valid = true)
        private Object key;
    }

    public static class EmptyKeyAnnot {
        @Getter
        @VerifyKey(keyNotNull = false, nameNotNull = false, versionNotNull = false, valid = false)
        private PfKey key;
    }
}
