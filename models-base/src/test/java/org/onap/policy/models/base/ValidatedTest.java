/*-
 * ============LICENSE_START=======================================================
 * ONAP
 * ================================================================================
 * Copyright (C) 2020-2021 AT&T Intellectual Property. All rights reserved.
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

package org.onap.policy.models.base;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import lombok.AllArgsConstructor;
import lombok.NonNull;
import org.junit.jupiter.api.Test;
import org.onap.policy.common.parameters.BeanValidationResult;
import org.onap.policy.common.parameters.ValidationResult;
import org.onap.policy.common.parameters.ValidationStatus;
import org.onap.policy.common.utils.coder.CoderException;
import org.onap.policy.common.utils.coder.StandardCoder;

class ValidatedTest {
    private static final @NonNull String MY_FIELD = "myField";
    private static final @NonNull String Q_KEY = "\"" + Validated.KEY_TOKEN + "\"";
    private static final @NonNull String Q_VALUE = "\"" + Validated.VALUE_TOKEN + "\"";
    private static final String NOT_SAME = "not same";
    private static final String TEXT = "some text";
    private static final String OTHER = "other text";
    private static final String NAME = "myKey";
    private static final String VERSION = "1.0.0";

    @Test
    void testAddResult() {
        BeanValidationResult result = new BeanValidationResult("", this);
        Validated.addResult(result, MY_FIELD, TEXT, "some message");
        assertThat(result.getResult()).contains(MY_FIELD).contains(TEXT).contains("some message");

        assertThatThrownBy(() -> Validated.addResult(null, MY_FIELD, TEXT, OTHER))
                        .isInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> Validated.addResult(result, null, TEXT, OTHER))
                        .isInstanceOf(NullPointerException.class);

        assertThatCode(() -> Validated.addResult(result, MY_FIELD, null, OTHER)).doesNotThrowAnyException();

        assertThatThrownBy(() -> Validated.addResult(result, MY_FIELD, TEXT, null))
                        .isInstanceOf(NullPointerException.class);
    }

    @Test
    void testMakeNullResult() {
        ValidationResult rnull = Validated.makeNullResult(MY_FIELD, TEXT);
        assertEquals(MY_FIELD, rnull.getName());
        assertThat(rnull.getResult()).contains(MY_FIELD).contains(TEXT).contains(Validated.IS_NULL);
        assertFalse(rnull.isValid());

        assertThatThrownBy(() -> Validated.makeNullResult(null, TEXT)).isInstanceOf(NullPointerException.class);

        assertThatCode(() -> Validated.makeNullResult(MY_FIELD, null)).doesNotThrowAnyException();
    }

    @Test
    void testValidateKeyNotNull() throws CoderException {
        BeanValidationResult result = new BeanValidationResult("", this);
        Validated.validateKeyNotNull(result, MY_FIELD, new PfConceptKey(NAME, VERSION));
        assertThat(result.getResult()).isNull();

        result = new BeanValidationResult("", this);
        Validated.validateKeyNotNull(result, MY_FIELD, new PfConceptKey(NAME, PfConceptKey.NULL_KEY_VERSION));
        assertThat(result.getResult()).isNull();

        result = new BeanValidationResult("", this);
        Validated.validateKeyNotNull(result, MY_FIELD, new PfConceptKey(PfConceptKey.NULL_KEY_NAME, VERSION));
        assertThat(result.getResult()).isNull();

        // key is null
        result = new BeanValidationResult("", this);
        Validated.validateKeyNotNull(result, MY_FIELD, new PfConceptKey());
        assertThat(result.getResult()).contains(MY_FIELD, Validated.IS_A_NULL_KEY)
                        .doesNotContain("\"name\"", "\"version\"");

        /*
         * Key is not null, but key.validate() should fail due to an invalid version.
         * Note: have to create the key by decoding from json, as the class will prevent
         * an invalid version from being assigned.
         */
        PfConceptKey key = new StandardCoder().decode("{'name':'myKey','version':'bogus'}".replace('\'', '"'),
                        PfConceptKey.class);
        result = new BeanValidationResult("", this);
        Validated.validateKeyNotNull(result, MY_FIELD, key);
        assertThat(result.getResult()).contains(MY_FIELD, "version", "does not match regular expression");

        BeanValidationResult result2 = new BeanValidationResult("", this);

        // null parameter tests
        PfConceptKey conceptKey = new PfConceptKey();
        assertThatThrownBy(() -> Validated.validateKeyNotNull(result2, null, conceptKey))
                        .isInstanceOf(NullPointerException.class);

        assertThatCode(() -> Validated.validateKeyNotNull(result2, MY_FIELD, null)).doesNotThrowAnyException();
    }

    @Test
    void testValidateKeyVersionNotNull() {
        BeanValidationResult result = new BeanValidationResult("", this);
        Validated.validateKeyVersionNotNull(result, MY_FIELD, null);
        assertThat(result.getResult()).isNull();

        result = new BeanValidationResult("", this);
        Validated.validateKeyVersionNotNull(result, MY_FIELD, new PfConceptKey(NAME, VERSION));
        assertThat(result.getResult()).isNull();

        result = new BeanValidationResult("", this);
        Validated.validateKeyVersionNotNull(result, MY_FIELD, new PfConceptKey(NAME, PfConceptKey.NULL_KEY_VERSION));
        assertThat(result.getResult()).contains(MY_FIELD).contains("version").contains(Validated.IS_NULL);

        BeanValidationResult result2 = new BeanValidationResult("", this);
        PfConceptKey conceptKey = new PfConceptKey();
        assertThatThrownBy(() -> Validated.validateKeyVersionNotNull(result2, null, conceptKey))
                        .isInstanceOf(NullPointerException.class);

        assertThatCode(() -> Validated.validateKeyVersionNotNull(result2, MY_FIELD, null)).doesNotThrowAnyException();
    }

    @Test
    void testGetKeyId() {
        // not a key field - should just use the given value
        BeanValidationResult result = new BeanValidationResult("", this);
        Validated.addResult(result, MY_FIELD, TEXT, "some message");
        assertThat(result.getResult()).contains(MY_FIELD).contains(TEXT).contains("some message");

        // repeat with a key field - should use the key's ID
        result = new BeanValidationResult("", this);
        Validated.addResult(result, MY_FIELD, new PfConceptKey(NAME, VERSION), "some message");
        assertThat(result.getResult()).contains(MY_FIELD).contains("myKey:1.0.0").contains("some message");
    }

    @AllArgsConstructor
    private static class MyString extends Validated {
        private final String text;

        @Override
        public BeanValidationResult validate(String fieldName) {
            if (TEXT.equals(text)) {
                return null;
            }

            BeanValidationResult result = new BeanValidationResult(fieldName, this);
            result.addResult(fieldName, text, ValidationStatus.INVALID, NOT_SAME);
            return result;
        }
    }
}
