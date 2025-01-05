/**
 * ****************************************************************************
 *  Copyright(c) 2023 the original author Eduardo Iglesias Taylor.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  	 https://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *  Contributors:
 *  	Eduardo Iglesias Taylor - initial API and implementation
 * *****************************************************************************
 */
package org.platkmframework.core.request.util;

import java.lang.reflect.Field;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.jsoup.Jsoup;
import org.jsoup.safety.Safelist;
import org.platkmframework.annotation.BeanAttribute;
import org.platkmframework.core.request.exception.RequestProcessException;
import org.platkmframework.core.request.exception.ResourceNotFoundException;
import org.platkmframework.util.reflection.ReflectionUtil;

/**
 *   Author:
 *     Eduardo Iglesias
 *   Contributors:
 *   	Eduardo Iglesias - initial API and implementation
 */
public class ValidateRequiredAttributeUtil {

	
	
	/**
	 * ValidateRequiredAttributeUtil
	 */
    public ValidateRequiredAttributeUtil() {
		super();
	}

	/**
     * validate
     * @param obj obj
     * @throws RequestProcessException RequestProcessException
     */
    public static void validate(Object obj) throws RequestProcessException {
        if (obj == null)
            return;
        if (obj instanceof List)
            validateList((List) obj);
        if (obj instanceof Map)
            validateList((Map) obj);
        else
            try {
                validateBean(obj);
            } catch (ResourceNotFoundException e) {
                throw new RequestProcessException(e);
            }
    }

    /**
     * validateBean
     * @param obj obj
     * @throws ResourceNotFoundException ResourceNotFoundException
     */
    private static void validateBean(Object obj) throws ResourceNotFoundException {
        List<Field> fields = ReflectionUtil.getAllFieldHeritage(obj.getClass());
        if (fields != null) {
            Field field;
            Object value;
            for (int i = 0; i < fields.size(); i++) {
                field = fields.get(i);
                try {
                    if (field.getClass().isAnnotationPresent(BeanAttribute.class)) {
                        BeanAttribute beanAttribute = field.getAnnotation(BeanAttribute.class);
                        if (beanAttribute.required() && field.get(obj) == null) {
                            throw new ResourceNotFoundException("The attribute value is required: " + field.getName());
                        }
                    }
                    if (String.class.getSimpleName().equals(field.getType().getSimpleName())) {
                        field.setAccessible(true);
                        value = field.get(obj);
                        if (value != null) {
                            field.set(obj, Jsoup.clean(value.toString(), Safelist.basic()));
                        }
                    }
                } catch (IllegalAccessException e) {
                    throw new ResourceNotFoundException("The attribute validation could not be completed: " + field.getName());
                }
            }
        }
    }

    /**
     * validateList
     * @param map map
     * @throws RequestProcessException RequestProcessException
     */
    private static void validateList(Map map) throws RequestProcessException {
        if (map == null)
            return;
        Iterator iterator = map.entrySet().iterator();
        while (iterator.hasNext()) {
            validate(iterator.next());
        }
    }

    /**
     * validateList
     * @param list list
     * @throws RequestProcessException RequestProcessException
     */
    private static void validateList(List list) throws RequestProcessException {
        if (list == null)
            return;
        for (Object object : list) validate(object);
    }
}
