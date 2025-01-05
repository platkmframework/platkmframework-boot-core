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
package org.platkmframework.core.request.caller;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Parameter;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.text.ParseException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.platkmframework.annotation.UIFilterToSearchConverter;
import org.platkmframework.annotation.exception.UIFilterToSearchConverterException;
import org.platkmframework.comon.service.validator.ValidatorUtil;
import org.platkmframework.context.ObjectContainer; 
import org.platkmframework.core.request.exception.RequestProcessException;
import org.platkmframework.core.request.multipart.MultiPart;
import org.platkmframework.util.JsonException;
import org.platkmframework.util.JsonUtil;
import org.platkmframework.util.Util;
import org.platkmframework.util.error.InvocationException;
import org.platkmframework.util.reflection.ReflectionUtil;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;

/**
 *   Author:
 *     Eduardo Iglesias
 *   Contributors:
 *   	Eduardo Iglesias - initial API and implementation
 *  LIST
 *     .DESDE FORM
 *        .Cuando se usan tablas, En java el listado es de objetos complejos o personalizados
 *          por el cliente
 *        .Cuando se usa un select multiple, En java el listado es de objetos no complejos
 *     .DESDE API
 *        La informacion estara en el cuerpo de POST
 */
public class ObjetTypeConverter {

    /**
     *  prefix to html table row, to know if it is new
     */
    public static final String C_TABLE_NEW_ROW_CHECKER = "html.table.newrow_checker";

    /**
     * Atributo primitive
     */
    private static Map<String, Class<?>> primitive = new HashMap<>();

    
    
    static {
        primitive.put("byte", Byte.class);
        primitive.put("short", Short.class);
        primitive.put("int", Integer.class);
        primitive.put("long", Long.class);
        primitive.put("float", Float.class);
        primitive.put("double", Double.class);
        primitive.put("boolean", Boolean.class);
        primitive.put("char", Character.class);
    }
    
	/**
	 * Base64Util
	 */
	  private ObjetTypeConverter() {
	    throw new IllegalStateException("ObjetTypeConverter class");
	  }    

    /**
     * converParamValueToObjectType
     * @param customParamName customParamName
     * @param required required
     * @param parameter parameter
     * @param req req
     * @param isAttribute isAttribute
     * @param paramValue paramValue
     * @return Object
     * @throws RequestProcessException RequestProcessException
     */
    public static Object converParamValueToObjectType(String customParamName, boolean required, Parameter parameter, HttpServletRequest req, boolean isAttribute, Object paramValue) throws RequestProcessException {
        try {
            if (parameter.getType().getName().startsWith("java.lang"))
                return convertToJavaLang(customParamName, parameter.getType(), paramValue, required);
            else if (isPrimitive(parameter.getType().getName()))
                return convertToJavaLang(customParamName, primitive.get(parameter.getType().getName()), paramValue, required);
            else if (parameter.getType().getName().startsWith("java.time"))
                return covertToTimeObject(parameter.getType(), paramValue, required);
            else if (parameter.getType().equals(MultiPart.class))
                return MultiparReader.read(req);
            else {
                //api or macro call
                //red de request body as a JSON and conver to type required
                String strJson = Util.inputSteamToString(req.getInputStream());
                if (StringUtils.isBlank(strJson))
                    return null;
                else
                    return JsonUtil.jsonToObject(strJson, parameter.getType());
            }
        } catch (Exception e) {
            throw new RequestProcessException(e);
        }
    }

    /**
     * covertToTimeObject
     * @param type type
     * @param value value
     * @param required required
     * @return Object
     * @throws RequestProcessException RequestProcessException
     */
    private static Object covertToTimeObject(Class<?> type, Object value, boolean required) throws RequestProcessException {
        if (value == null || StringUtils.isEmpty(value.toString())) {
            if (required)
                throw new RequestProcessException("required  field value not found -> " + type.toString());
            else
                return null;
        }
        if (LocalDate.class.equals(type)) {
            return LocalDate.parse(value.toString(), DateTimeFormatter.ofPattern(ObjectContainer.instance().getPropertyValue("platform.format.date")));
        } else if (LocalDateTime.class.equals(type)) {
            return LocalDateTime.parse(value.toString(), DateTimeFormatter.ofPattern(ObjectContainer.instance().getPropertyValue("platform.format.datetime")));
        } else if (LocalTime.class.equals(type)) {
            return LocalTime.parse(value.toString(), DateTimeFormatter.ofPattern(ObjectContainer.instance().getPropertyValue("platform.format.time")));
        }
        throw new RequestProcessException(type.getName() + " not implemented");
    }

    /**
     * converBodyValueToObjectType
     * @param parameter parameter
     * @param required required
     * @param uiFilterToSearchConverter uiFilterToSearchConverter
     * @param req req
     * @return Object
     * @throws RequestProcessException RequestProcessException
     */
    public static Object converBodyValueToObjectType(Parameter parameter, boolean required, Class<? extends UIFilterToSearchConverter> uiFilterToSearchConverter, HttpServletRequest req) throws RequestProcessException {
        try {
            if (parameter.getType().equals(MultiPart.class))
                return MultiparReader.read(req);
            else {
                String strBody = Util.inputSteamToString(req.getInputStream());
                if (StringUtils.isBlank(strBody))
                    if (required)
                        throw new RequestProcessException("required value not found for field : " + parameter.getName());
                    else
                        return null;
                if (uiFilterToSearchConverter != null) {
                    UIFilterToSearchConverter uIFilterToSearchConverter = (UIFilterToSearchConverter) ObjectContainer.instance().geApptScopeObj(uiFilterToSearchConverter);
                    try {
                        return uIFilterToSearchConverter.convert(strBody, parameter);
                    } catch (UIFilterToSearchConverterException e) {
                        throw new RequestProcessException(e);
                    }
                }
                if (parameter.getType().equals(List.class) && parameter.getParameterizedType() instanceof ParameterizedType && ((ParameterizedType) parameter.getParameterizedType()).getActualTypeArguments().length > 0) {
                    return JsonUtil.jsonToListObject(strBody, (Class) ((ParameterizedType) parameter.getParameterizedType()).getActualTypeArguments()[0]);
                } else {
                    Object objBean = JsonUtil.jsonToObject(strBody, parameter.getType());
                    ValidatorUtil.checkValidation(objBean);
                    return objBean;
                }
            }
        } catch (ServletException | JsonException | IOException e) {
            throw new RequestProcessException(e);
        }
    }

    /**
     * converFieldValueToObjectType
     * @param htmlFieldName htmlFieldName
     * @param field field
     * @param obj obj
     * @param param param
     * @param req req
     * @throws RequestProcessException RequestProcessException
     */
    public static void converFieldValueToObjectType(String htmlFieldName, Field field, Object obj, Map<String, Object> param, HttpServletRequest req) throws RequestProcessException {
        if (field.getType().getName().startsWith("java.lang")) {
            Object value = convertToJavaLang(htmlFieldName, field.getType(), param.get(htmlFieldName), false);
            try {
                ReflectionUtil.setAttributeValue(obj, field, value, false);
            } catch (InvocationException e) {
                throw new RequestProcessException(e);
            }
        } else {
            if (!List.class.equals(field.getType()))
                throw new RequestProcessException("Field name: " + field.getName() + " Data type->" + field.getType() + " not supported from page request");
            _processAttributeList(htmlFieldName, obj, field, param, req);
        }
    }

    /**
     * convertToJavaLang
     * @param customParamName customParamName
     * @param type type
     * @param value value
     * @param required required
     * @return Object
     * @throws RequestProcessException RequestProcessException
     */
    private static Object convertToJavaLang(String customParamName, Class<?> type, Object value, boolean required) throws RequestProcessException {
        //if empty value
        if (value == null || StringUtils.isEmpty(value.toString()))
            if (required)
                throw new RequestProcessException("El valor para el parámetro " + customParamName + " es requerido");
            else
                return null;
        if (int.class.equals(type))
            return Integer.valueOf(value.toString());
        else if (Integer.class.equals(type))
            return Integer.valueOf(value.toString());
        else if (long.class.equals(type))
            return Long.valueOf(value.toString());
        else if (Long.class.equals(type))
            return Long.valueOf(value.toString());
        else if (float.class.equals(type))
            return Float.valueOf(value.toString());
        else if (Float.class.equals(type))
            return Float.valueOf(value.toString());
        else if (boolean.class.equals(type))
            return Boolean.valueOf(value.toString());
        else if (Boolean.class.equals(type))
            return Boolean.valueOf(value.toString());
        else if (Date.class.equals(type))
            try {
                return Util.stringToDate(value.toString());
            } catch (ParseException e) {
                throw new RequestProcessException(e);
            }
        else if (String.class.equals(type))
            return value.toString();
        else
            throw new RequestProcessException("Name " + customParamName + "Data type->" + type + " not found to convert");
    }

    /**
     * _processAttributeList
     * @param htmlFieldName htmlFieldName
     * @param obj obj
     * @param field field
     * @param param param
     * @param req req
     * @return List
     * @throws RequestProcessException RequestProcessException
     */
    private static List<Object> _processAttributeList(String htmlFieldName, Object obj, Field field, Map<String, Object> param, HttpServletRequest req) throws RequestProcessException {
        Type t = field.getType().getGenericSuperclass();
        ParameterizedType p = (ParameterizedType) t;
        Type genericType = p.getActualTypeArguments()[0];
        if (genericType.getClass().getName().startsWith("java.lang.")) {
            String[] parameterValues = req.getParameterValues(htmlFieldName);
            if (parameterValues == null || parameterValues.length == 0)
                return null;
            ArrayList<Object> result = new ArrayList<Object>();
            if (genericType.getClass().equals(Long.class))
                return _fillByClassType(result, parameterValues, Long.class);
            else if (genericType.getClass().equals(Integer.class))
                return _fillByClassType(result, parameterValues, Integer.class);
            else if (genericType.getClass().equals(Float.class))
                return _fillByClassType(result, parameterValues, Float.class);
            else if (genericType.getClass().equals(Date.class))
                return _fillByClassType(result, parameterValues, Date.class);
            else if (genericType.getClass().equals(Boolean.class))
                return _fillByClassType(result, parameterValues, Boolean.class);
            else
                throw new RequestProcessException("Name :" + htmlFieldName + " The class type-> " + genericType.getClass().getName() + " is not included to process");
        } else {
            return _fillByComplexTable(htmlFieldName, req, obj, field, genericType.getClass());
        }
    }

    /**
     * _fillByComplexTable
     * @param htmlFieldName htmlFieldName
     * @param req req
     * @param obj obj
     * @param field field
     * @param classBean classBean
     * @return List
     * @throws RequestProcessException RequestProcessException
     */
    private static List<Object> _fillByComplexTable(String htmlFieldName, HttpServletRequest req, Object obj, Field field, Class classBean) throws RequestProcessException {
        try {
            List<Object> list = (List) ReflectionUtil.getAttributeValue(obj, field);
            List<Field> fields = ReflectionUtil.getAllFieldHeritage(obj.getClass());
            if (fields != null) {
                int row = 1;
                String htmlFielNameId = _getTableCellName(htmlFieldName, C_TABLE_NEW_ROW_CHECKER, row);
                if (!req.getParameterMap().containsKey(htmlFielNameId))
                    return list;
                //there are rows then check list
                if (list == null)
                    list = (List) ReflectionUtil.createInstance(field.getClass());
                boolean exists = true;
                Object objRow;
                Object value;
                while (exists) {
                    if (StringUtils.isEmpty(htmlFielNameId)) {
                        //new record
                        objRow = ReflectionUtil.createInstance(classBean);
                        list.add(objRow);
                    } else
                        objRow = list.get(_getCurrentRow(htmlFielNameId));
                    for (int i = 0; i < fields.size(); i++) {
                        htmlFielNameId = _getTableCellName(htmlFieldName, fields.get(i + 1).getName(), row);
                        if (req.getParameterMap().containsKey(htmlFielNameId)) {
                            value = convertToJavaLang(htmlFielNameId, fields.get(i + 1).getType(), req.getParameter(htmlFielNameId), false);
                            ReflectionUtil.setAttributeValue(objRow, fields.get(i + 1), value, false);
                        }
                    }
                    //next row
                    row++;
                    htmlFielNameId = _getTableCellName(htmlFieldName, C_TABLE_NEW_ROW_CHECKER, row);
                    if (!req.getParameterMap().containsKey(htmlFielNameId))
                        return list;
                }
            }
            return list;
        } catch (InvocationException e) {
            throw new RequestProcessException(e);
        }
    }

    /**
     * _getCurrentRow
     * @param newFieldChecker newFieldChecker
     * @return int
     */
    private static int _getCurrentRow(String newFieldChecker) {
        return Integer.parseInt(newFieldChecker.split("_")[2]);
    }

    /**
     * _getTableCellName
     * @param classFieldName classFieldName
     * @param beanFieldName beanFieldName
     * @param row row
     * @return String
     */
    private static String _getTableCellName(String classFieldName, String beanFieldName, int row) {
        return classFieldName + "_" + C_TABLE_NEW_ROW_CHECKER + "_" + row;
    }

    /**
     * _fillByClassType
     * @param list list
     * @param parameterValues parameterValues
     * @param class1 class1
     * @return List
     * @throws RequestProcessException RequestProcessException
     */
    private static List<Object> _fillByClassType(ArrayList<Object> list, String[] parameterValues, Class<?> class1) throws RequestProcessException {
        for (int i = 0; i < parameterValues.length; i++) list.add(_getValue(parameterValues[i], class1));
        return list;
    }

    /**
     * _getValue
     * @param value value
     * @param class1 class1
     * @return Object
     * @throws RequestProcessException RequestProcessException
     */
    private static Object _getValue(String value, Class<?> class1) throws RequestProcessException {
        if (class1.equals(Long.class))
            return Long.parseLong(value);
        else if (class1.equals(Integer.class))
            return Integer.parseInt(value);
        else if (class1.equals(Float.class))
            return Float.parseFloat(value);
        else if (class1.equals(Date.class))
            try {
                return Util.stringToDate(value);
            } catch (ParseException e) {
                throw new RequestProcessException(e);
            }
        else
            throw new RequestProcessException("The class type-> " + class1.getClass().getName() + " is not included to process");
    }

    /**
     * isPrimitive
     * @param type type
     * @return boolean
     */
    private static boolean isPrimitive(String type) {
        return primitive.containsKey(type);
    }
}
