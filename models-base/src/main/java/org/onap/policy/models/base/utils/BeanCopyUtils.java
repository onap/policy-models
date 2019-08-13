/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2019 Nordix Foundation.
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

package org.onap.policy.models.base.utils;

import static java.lang.reflect.Modifier.isStatic;
import static java.util.Arrays.stream;
import static java.util.Objects.nonNull;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import javax.ws.rs.core.Response;
import lombok.NonNull;
import org.onap.policy.common.utils.validation.Assertions;
import org.onap.policy.models.base.PfModelRuntimeException;
import org.onap.policy.models.base.utils.annotation.ArrayCopy;
import org.onap.policy.models.base.utils.annotation.IgnoreCopy;
import org.onap.policy.models.base.utils.annotation.NewInstance;

/**
 * Utility class for Policy Framework concept utilities. It is responsible for copy function.
 */
public class BeanCopyUtils {
    /**
     * Regex for identify dots into a string.
     */
    private static final String DOT_SPLIT_REGEX = "\\.";

    /**
     * Default constructor.
     */
    private BeanCopyUtils() {}

    /**
     * Copy function.
     *
     * @param source source which is the type of the class invoked to be copied
     * @param target invoked class to be copied to
     * @param clz class from which gets the field
     * @return the new copied target
     */
    @SuppressWarnings("rawtypes")
    public static <T> T copyTo(T source, @NonNull final T target, Class<?> clz) {
        Assertions.instanceOf(target, clz);
        BeanCopyUtils copyUtils = new BeanCopyUtils();
        List<Field> fields = copyUtils.getDeclaredFields(target.getClass(), true).stream()
                .filter(field -> !field.isAnnotationPresent(IgnoreCopy.class)).collect(Collectors.toList());
        fields.stream().forEach(field -> {

            if (field.isAnnotationPresent(NewInstance.class)) {
                NewInstance instanceParameter = copyUtils.getFieldAnnotation(field, NewInstance.class);
                Class<?> typeOfInstance = instanceParameter.instanceType();

                if (instanceParameter.nullable() && copyUtils.getFieldValue(source, field) == null) {
                    copyUtils.setFieldValue(target, field, null);
                } else {

                    try {
                        Constructor constructor = typeOfInstance.getConstructor(typeOfInstance);
                        copyUtils.setFieldValue(target, field,
                                constructor.newInstance(copyUtils.getFieldValue(source, field)));
                    } catch (InstantiationException | IllegalAccessException | IllegalArgumentException
                                | InvocationTargetException | NoSuchMethodException | SecurityException e) {
                        throw new PfModelRuntimeException(Response.Status.INTERNAL_SERVER_ERROR,
                                "error copying: " + e.getMessage(), e);
                    }
                }
            } else if (field.isAnnotationPresent(ArrayCopy.class)) {
                copyUtils.setFieldValue(target, field, deepCopy(copyUtils.getFieldValue(source, field)));
            } else {
                copyUtils.setFieldValue(target, field, copyUtils.getFieldValue(source, field));
            }
        });
        return target;
    }

    /**
     * Make a deep copy.
     *
     * @param obj the obj to be copied
     * @return the new copied obj
     */
    @SuppressWarnings("unchecked")
    public static <T> T deepCopy(T obj) {
        try {
            if (obj == null) {
                return null;
            }
            Class<?> clazz = obj.getClass();
            T clone = (T) clazz.newInstance();
            Field[] fields = clazz.getDeclaredFields();

            for (int i = 0; i < fields.length; i++) {
                Field field = fields[i];
                field.setAccessible(true);

                if (!Modifier.isFinal(field.getModifiers())) {
                    if (field.get(obj) instanceof List<?>) {
                        List<?> copiedList = deepCopyList((List<?>) field.get(obj));
                        field.set(clone, copiedList);
                    } else if (field.get(obj) instanceof Map<?, ?>) {
                        Map<?, ?> copiedMap = deepCopyMap((Map<?, ?>) field.get(obj));
                        field.set(clone, copiedMap);
                    } else {
                        field.set(clone, field.get(obj));
                    }
                }
            }

            while (true) {
                if (Object.class.equals(clazz)) {
                    break;
                }
                clazz = clazz.getSuperclass();
                Field[] declaredFields = clazz.getDeclaredFields();

                for (int i = 0; i < declaredFields.length; i++) {
                    Field field = declaredFields[i];
                    field.setAccessible(true);
                    if (!Modifier.isFinal(field.getModifiers())) {
                        if (field.get(obj) instanceof List<?>) {
                            List<?> copiedList = deepCopyList((List<?>) field.get(obj));
                            field.set(clone, copiedList);
                        } else {
                            field.set(clone, field.get(obj));
                        }
                    }
                }
            }
            return clone;
        } catch (InstantiationException | IllegalAccessException e) {
            return null;
        }
    }

    /**
     * Copy the list.
     *
     * @param arg the list to be copied
     * @return the copied list
     */
    public static <T> List<T> deepCopyList(List<T> arg) {
        if (arg == null) {
            return null;
        }
        List<T> retList = new ArrayList<T>();

        for (T each : arg) {
            retList.add(deepCopy(each));
        }
        return retList;
    }

    /**
     * Copy the map.
     *
     * @param arg the map to be copied
     * @return the copied map
     */
    public static <T, E> Map<T, E> deepCopyMap(Map<T, E> arg) {
        if (arg == null) {
            return null;
        }
        Map<T, E> retMap = new TreeMap<>();

        for (final Entry<T, E> entry : arg.entrySet()) {
            retMap.put(deepCopy(entry.getKey()), deepCopy(entry.getValue()));
        }
        return retMap;
    }

    /**
     * Return the fields of a class.
     *
     * @param clazz class from which gets the field
     * @param skipStatic if true it skips the static fields otherwise all private fields are retrieved.
     * @return a list of class fields.
     */
    @SuppressWarnings("unchecked")
    public List<Field> getDeclaredFields(final Class<?> clazz, final boolean skipStatic) {
        final List<Field> res = new ArrayList<>();
        if (nonNull(clazz.getSuperclass()) && !clazz.getSuperclass().equals(Object.class)) {
            res.addAll(getDeclaredFields(clazz.getSuperclass(), skipStatic));
        }
        stream(getDeclaredFields(clazz)).filter(field -> !skipStatic || !isStatic(field.getModifiers()))
                .forEach(field -> {
                    field.setAccessible(true);
                    res.add(field);
                });
        return res;
    }

    /**
     * Returns the class fields.
     *
     * @param clazz the class from which gets the field.
     * @return a list of class fields
     */
    private Field[] getDeclaredFields(final Class<?> clazz) {
        Field[] res = clazz.getDeclaredFields();
        return res;
    }

    /**
     * Gets the value of a field.
     *
     * @param target the field's class
     * @param field the field {@link Field}
     * @return the field value
     */
    public Object getFieldValue(final Object target, final Field field) {
        return getFieldValue(target, field.getName(), field.getType());
    }

    /**
     * Gets the value of a field through getter method.
     *
     * @param target the field's class
     * @param fieldName the field name
     * @param fieldType the field type
     * @return the field value
     */
    public Object getFieldValue(final Object target, final String fieldName, final Class<?> fieldType) {
        Object fieldValue = getRealTarget(target);
        for (String currFieldName : fieldName.split(DOT_SPLIT_REGEX)) {
            if (fieldValue == null) {
                break;
            }
            fieldValue = getFieldValueDirectAccess(fieldValue, currFieldName);
        }
        return fieldValue;
    }

    /**
     * Returns (if existing) the field's given type annotation.
     *
     * @param field the field that should have the annotation
     * @param annotationClazz the annotation type
     * @param <A> the annotation type object
     * @return the annotation
     */
    public <A extends Annotation> A getFieldAnnotation(final Field field, final Class<A> annotationClazz) {
        A annotation = null;
        if (field.isAnnotationPresent(annotationClazz)) {
            annotation = field.getAnnotation(annotationClazz);
        }
        return annotation;
    }

    /**
     * Gets the value of a field.
     *
     * @param target the field's class
     * @param fieldName the field name
     * @return the field value
     */
    private Object getFieldValueDirectAccess(final Object target, final String fieldName) {
        try {
            Field field = getDeclaredField(fieldName, target.getClass());
            return field.get(target);
        } catch (final Exception e) {
            throw new IllegalStateException(e);
        }
    }

    /**
     * Return the field of the given class.
     *
     * @param fieldName the name of the field to retrieve.
     * @param targetClass the field's class
     * @return the field corresponding to the given name.
     */
    public Field getDeclaredField(final String fieldName, final Class<?> targetClass) {
        Field field;
        try {
            field = targetClass.getDeclaredField(fieldName);
            field.setAccessible(true);
        } catch (NoSuchFieldException e) {
            Class<?> superclass = targetClass.getSuperclass();
            if (!superclass.equals(Object.class)) {
                field = getDeclaredField(fieldName, superclass);
            } else {
                throw new PfModelRuntimeException(Response.Status.INTERNAL_SERVER_ERROR,
                        targetClass.getName() + " does not contain field: " + fieldName);
            }
        } catch (final Exception e) {
            throw new IllegalStateException(e);
        }
        return field;
    }

    /**
     * Set the value of a field.
     *
     * @param target the field's class
     * @param field the field to set
     * @param fieldValue the value to set
     */
    public void setFieldValue(final Object target, final Field field, final Object fieldValue) {
        try {
            field.set(target, fieldValue);
        } catch (final IllegalArgumentException e) {
            throw e;
        } catch (final Exception e) {
            throw new IllegalStateException(e);
        }
    }

    /**
     * If an object is encapsulated into a type: {@link Optional} the contained object is returned.
     *
     * @param target the object to check
     * @return the encapsulated object (if any)
     */
    private Object getRealTarget(final Object target) {
        AtomicReference<Object> realTarget = new AtomicReference<>(target);
        if (target instanceof Optional) {
            ((Optional<?>) target).ifPresent(realTarget::set);
        }
        return realTarget.get();
    }
}
