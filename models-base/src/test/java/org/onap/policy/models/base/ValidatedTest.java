/*
 * ============LICENSE_START=======================================================
 * ONAP Policy Models
 * ================================================================================
 * Copyright (C) 2019 AT&T Intellectual Property. All rights reserved.
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

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;

public class ValidatedTest {
    private static final String ERROR_MESSAGE = "error message";
    private static final String COLLECTION_FIELD = "coll";
    private static final String VALID_VALUE = "abc123";
    private static final String PROPS_FIELD = "props";
    private static final String MY_NAME = "my.name";
    private static final String VALID_FIELD = "validField";
    private static final String INVALID_FIELD = "invalidField";
    private static final String NULL_FIELD = "nullField";
    private static final String WORD_PAT = "\\w*";
    private static final String MY_TO_STRING = "[some text]";
    private static final String VERSION = "1.2.3";

    private Validated validated;

    @Before
    public void setUp() {
        validated = new Validated();
    }

    @Test
    public void testValidate() {
        assertThatThrownBy(() -> validated.validate(null)).isInstanceOf(NullPointerException.class);

        PfValidationResult result = new PfValidationResult();
        assertSame(result, validated.validate(result));
        assertTrue(result.isValid());
        assertEquals(0, result.getMessageList().size());
    }

    @Test
    public void testValidateNotNull() {
        PfValidationResult result = new PfValidationResult();

        final PfValidationResult result2 = result;
        assertThatThrownBy(() -> validated.validateNotNull(null, VALID_FIELD, VALID_VALUE, result2)).isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> validated.validateNotNull(this, null, VALID_VALUE, result2)).isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> validated.validateNotNull(this, VALID_FIELD, VALID_VALUE, null)).isInstanceOf(NullPointerException.class);

        // null text
        result = validated.validateNotNull(this, NULL_FIELD, null, result);

        // invalid text
        result = validated.validateNotNull(this, INVALID_FIELD, "!!!", result);

        // valid text
        result = validated.validateNotNull(this, VALID_FIELD, VALID_VALUE, result);

        // different value
        result = validated.validateNotNull(this, VALID_FIELD, Integer.valueOf(10), result);

        assertFalse(result.isValid());
        assertEquals(1, result.getMessageList().size());

        // check result for null text
        PfValidationMessage msg = result.getMessageList().get(0);
        assertEquals(ValidatedTest.class.getName(), msg.getObservedClass());
        assertEquals(MY_TO_STRING, msg.getObservedKey().toString());
        assertTrue(msg.getMessage().contains("nullField invalid-null"));
    }

    @Test
    public void testValidateNotNullConceptKey() {
        PfValidationResult result = new PfValidationResult();

        // null key
        PfConceptKey key = new PfConceptKey();
        key.setVersion(VERSION);
        result = validated.validateNotNull(key, result);

        // null value
        key = new PfConceptKey();
        key.setName(MY_NAME);
        result = validated.validateNotNull(key, result);

        // both null
        key = new PfConceptKey();
        result = validated.validateNotNull(key, result);

        assertFalse(result.isValid());
        assertEquals(4, result.getMessageList().size());

        // valid key & value
        key = new PfConceptKey();
        key.setName(MY_NAME);
        key.setVersion(VERSION);
        result = validated.validateNotNull(key, result);

        // no change
        assertFalse(result.isValid());
        assertEquals(4, result.getMessageList().size());

        Iterator<PfValidationMessage> it = result.getMessageList().iterator();

        // check null key
        PfValidationMessage msg = it.next();
        assertEquals(PfConceptKey.class.getName(), msg.getObservedClass());
        assertTrue(msg.getMessage().contains("name invalid-null"));

        // check null value
        msg = it.next();
        assertEquals(PfConceptKey.class.getName(), msg.getObservedClass());
        assertTrue(msg.getMessage().contains("version invalid-null"));

        // check both null
        msg = it.next();
        assertEquals(PfConceptKey.class.getName(), msg.getObservedClass());
        assertTrue(msg.getMessage().contains("name invalid-null"));
        assertTrue(it.next().getMessage().contains("version invalid-null"));

        final PfConceptKey key2 = key;
        assertThatThrownBy(() -> validated.validateNotNull(key2, null)).isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> validated.validateNotNull(null, new PfValidationResult())).isInstanceOf(NullPointerException.class);
    }

    @Test
    public void testValidateText() {
        PfValidationResult result = new PfValidationResult();

        final PfValidationResult result2 = result;
        assertThatThrownBy(() -> validated.validateText(null, VALID_FIELD, VALID_VALUE, WORD_PAT, result2)).isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> validated.validateText(this, null, VALID_VALUE, WORD_PAT, result2)).isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> validated.validateText(this, VALID_FIELD, VALID_VALUE, null, result2)).isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> validated.validateText(this, VALID_FIELD, VALID_VALUE, WORD_PAT, null)).isInstanceOf(NullPointerException.class);

        // null text
        result = validated.validateText(this, NULL_FIELD, null, WORD_PAT, result);

        // invalid text
        result = validated.validateText(this, INVALID_FIELD, "!!!", WORD_PAT, result);

        // valid text
        result = validated.validateText(this, VALID_FIELD, VALID_VALUE, WORD_PAT, result);

        assertFalse(result.isValid());
        assertEquals(1, result.getMessageList().size());

        // check result for invalid text
        PfValidationMessage msg = result.getMessageList().get(0);
        assertEquals(ValidatedTest.class.getName(), msg.getObservedClass());
        assertEquals(MY_TO_STRING, msg.getObservedKey().toString());
        assertTrue(msg.getMessage().contains("invalidField invalid-parameter invalidField"));
    }

    @Test
    public void testValidatePropertiesNotNull() {
        PfValidationResult result = new PfValidationResult();
        result = validated.validatePropertiesNotNull(this, "properties", null, result);
        assertTrue(result.isValid());
        assertEquals(0, result.getMessageList().size());

        Map<String, Integer> map = new LinkedHashMap<>();

        // null key
        map.put(null, 10);

        // null value
        map.put("abc", null);

        // valid key & value
        map.put("def", 11);


        result = validated.validatePropertiesNotNull(this, PROPS_FIELD, map, result);

        assertFalse(result.isValid());
        assertEquals(2, result.getMessageList().size());

        Iterator<PfValidationMessage> it = result.getMessageList().iterator();

        // check null key
        PfValidationMessage msg = it.next();
        assertEquals(ValidatedTest.class.getName(), msg.getObservedClass());
        assertEquals(MY_TO_STRING, msg.getObservedKey().toString());
        assertTrue(msg.getMessage().contains("props.null invalid-null"));

        // check null value
        msg = it.next();
        assertEquals(ValidatedTest.class.getName(), msg.getObservedClass());
        assertEquals(MY_TO_STRING, msg.getObservedKey().toString());
        assertTrue(msg.getMessage().contains("props.abc invalid-null"));

        final PfValidationResult result2 = result;
        assertThatThrownBy(() -> validated.validatePropertiesNotNull(null, PROPS_FIELD, map, result2)).isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> validated.validatePropertiesNotNull(this, null, map, result2)).isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> validated.validatePropertiesNotNull(this, PROPS_FIELD, map, null)).isInstanceOf(NullPointerException.class);
    }

    @Test
    public void testValidateCollectionNotNull() {
        PfValidationResult result = new PfValidationResult();
        result = validated.validateCollectionNotNull(this, "collection", null, result);
        assertTrue(result.isValid());
        assertEquals(0, result.getMessageList().size());

        final List<String> lst = Arrays.asList("abc", null, "def", null);

        result = validated.validateCollectionNotNull(this, COLLECTION_FIELD, lst, result);

        assertFalse(result.isValid());
        assertEquals(2, result.getMessageList().size());

        Iterator<PfValidationMessage> it = result.getMessageList().iterator();

        // check first item
        PfValidationMessage msg = it.next();
        assertEquals(ValidatedTest.class.getName(), msg.getObservedClass());
        assertEquals(MY_TO_STRING, msg.getObservedKey().toString());
        assertTrue(msg.getMessage().contains("coll.1 invalid-null"));

        // check null value
        msg = it.next();
        assertEquals(ValidatedTest.class.getName(), msg.getObservedClass());
        assertEquals(MY_TO_STRING, msg.getObservedKey().toString());
        assertTrue(msg.getMessage().contains("coll.3 invalid-null"));

        final PfValidationResult result2 = result;
        assertThatThrownBy(() -> validated.validateCollectionNotNull(null, COLLECTION_FIELD, lst, result2)).isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> validated.validateCollectionNotNull(this, null, lst, result2)).isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> validated.validateCollectionNotNull(this, COLLECTION_FIELD, lst, null)).isInstanceOf(NullPointerException.class);
    }

    @Test
    public void testValidateCollection() {
        PfValidationResult result = new PfValidationResult();
        result = validated.validateCollection(this, "collection", null, result);
        assertTrue(result.isValid());
        assertEquals(0, result.getMessageList().size());

        List<MyValid> lst = Arrays.asList(new MyValid(0, false), new MyValid(1, true), null, new MyValid(2, false),
                        new MyValid(3, true));
        result = validated.validateCollection(this, COLLECTION_FIELD, lst, result);

        assertFalse(result.isValid());
        assertEquals(2, result.getMessageList().size());

        Iterator<PfValidationMessage> it = result.getMessageList().iterator();

        // check first item
        PfValidationMessage msg = it.next();
        assertEquals(MyValid.class.getName().replace('$', '.'), msg.getObservedClass());
        assertEquals(MY_TO_STRING, msg.getObservedKey().toString());
        assertTrue(msg.getMessage().contains("index.0 invalid-wrong value"));

        // check null value
        msg = it.next();
        assertEquals(MyValid.class.getName().replace('$', '.'), msg.getObservedClass());
        assertEquals(MY_TO_STRING, msg.getObservedKey().toString());
        assertTrue(msg.getMessage().contains("index.2 invalid-wrong value"));

        final PfValidationResult result2 = result;
        assertThatThrownBy(() -> validated.validateCollection(null, COLLECTION_FIELD, lst, result2)).isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> validated.validateCollection(this, null, lst, result2)).isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> validated.validateCollection(this, COLLECTION_FIELD, lst, null)).isInstanceOf(NullPointerException.class);
    }

    @Test
    public void testValidateConceptCollection() {
        PfValidationResult result = new PfValidationResult();
        result = validated.validateConceptCollection(this, "collection", null, result);
        assertTrue(result.isValid());
        assertEquals(0, result.getMessageList().size());

        List<MyConcept> lst = Arrays.asList(new MyConcept(0, false), new MyConcept(1, true), null,
                        new MyConcept(2, false), new MyConcept(3, true));
        result = validated.validateConceptCollection(this, COLLECTION_FIELD, lst, result);

        assertFalse(result.isValid());
        assertEquals(2, result.getMessageList().size());

        Iterator<PfValidationMessage> it = result.getMessageList().iterator();

        // check first item
        PfValidationMessage msg = it.next();
        assertEquals(MyConcept.class.getName().replace('$', '.'), msg.getObservedClass());
        assertEquals(MY_TO_STRING, msg.getObservedKey().toString());
        assertTrue(msg.getMessage().contains("index.0 invalid-wrong value"));

        // check null value
        msg = it.next();
        assertEquals(MyConcept.class.getName().replace('$', '.'), msg.getObservedClass());
        assertEquals(MY_TO_STRING, msg.getObservedKey().toString());
        assertTrue(msg.getMessage().contains("index.2 invalid-wrong value"));

        final PfValidationResult result2 = result;
        assertThatThrownBy(() -> validated.validateConceptCollection(null, COLLECTION_FIELD, lst, result2)).isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> validated.validateConceptCollection(this, null, lst, result2)).isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> validated.validateConceptCollection(this, COLLECTION_FIELD, lst, null)).isInstanceOf(NullPointerException.class);
    }

    @Test
    public void testAddError() {
        final PfValidationResult result = new PfValidationResult();
        final PfValidationResult result2 = result;

        assertThatThrownBy(() -> validated.addError(null, VALID_FIELD, result2, ERROR_MESSAGE)).isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> validated.addError(this, null, result2, ERROR_MESSAGE)).isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> validated.addError(this, VALID_FIELD, null, ERROR_MESSAGE)).isInstanceOf(NullPointerException.class);

        validated.addError(this, VALID_FIELD, result, "error-A");
        validated.addError(this, VALID_FIELD, result, null);
        validated.addError(this, VALID_FIELD, result, "error-B");

        assertFalse(result.isValid());
        assertEquals(2, result.getMessageList().size());

        Iterator<PfValidationMessage> it = result.getMessageList().iterator();

        PfValidationMessage msg = it.next();
        assertEquals(ValidatedTest.class.getName(), msg.getObservedClass());
        assertEquals(MY_TO_STRING, msg.getObservedKey().toString());
        assertTrue(msg.getMessage().contains("validField invalid-error-A"));

        msg = it.next();
        assertEquals(ValidatedTest.class.getName(), msg.getObservedClass());
        assertEquals(MY_TO_STRING, msg.getObservedKey().toString());
        assertTrue(msg.getMessage().contains("validField invalid-error-B"));
    }

    @Test
    public void testMakeKey() {
        assertThatThrownBy(() -> validated.makeKey(null)).isInstanceOf(NullPointerException.class);

        PfKey key = validated.makeKey(this);
        assertEquals(MY_TO_STRING, key.toString());
    }

    @Override
    public String toString() {
        return MY_TO_STRING;
    }

    private static class MyValid extends Validated {
        private boolean valid;
        private int index;

        public MyValid(int index, boolean valid) {
            this.index = index;
            this.valid = valid;
        }

        @Override
        public PfValidationResult validate(PfValidationResult result) {
            if (!valid) {
                this.addError(this, "index." + index, result, "wrong value");
            }

            return result;
        }

        @Override
        public String toString() {
            return MY_TO_STRING;
        }
    }

    private static class MyConcept extends PfConceptKey {
        private static final long serialVersionUID = 1L;

        private boolean valid;
        private int index;

        public MyConcept(int index, boolean valid) {
            this.index = index;
            this.valid = valid;
        }

        @Override
        public PfValidationResult validate(PfValidationResult result) {
            if (!valid) {
                new Validated().addError(this, "index." + index, result, "wrong value");
            }

            return result;
        }

        @Override
        public String toString() {
            return MY_TO_STRING;
        }
    }
}
