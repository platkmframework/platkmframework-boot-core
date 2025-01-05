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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;
import org.platkmframework.annotation.HeaderParam;
import org.platkmframework.annotation.RequestBody;
import org.platkmframework.annotation.RequestParam;
import org.platkmframework.annotation.UIFilterToSearchConverter;
import org.platkmframework.annotation.security.SecurityRole;
import org.platkmframework.context.ObjectContainer;
import org.platkmframework.core.request.exception.RequestProcessException;
import org.platkmframework.core.request.exception.ResourceNotFoundException;
import org.platkmframework.core.request.exception.ResourcePermissionException;
import org.platkmframework.core.request.util.ValidateRequiredAttributeUtil;
import org.platkmframework.security.content.SecurityContent;
import org.platkmframework.util.reflection.ReflectionUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 *   Author:
 *     Eduardo Iglesias
 *   Contributors:
 *  Call an object method
 */
//@Service
public class MethodCaller {

    /**
     * Atributo logger
     */
    private static Logger logger = LoggerFactory.getLogger(MethodCaller.class);

    /**
     * MethodCaller
     */
    public MethodCaller() {
        super();
    }

    /**
     * execute
     * @param path path
     * @param req req
     * @param resp resp
     * @return Object
     * @throws RequestProcessException RequestProcessException
     */
    public Object execute(String path, HttpServletRequest req, HttpServletResponse resp) throws RequestProcessException {
        try {
            logger.info("searching object from -> " + path + " - " + req.getMethod());
            //String key = "{" + req.getMethod() + ":"  + path + "}";
            Object controller;
            Map<String, Object> pathParameter = new HashMap<>();
            // PathProccesor.getSessionObjectByPath(path, req.getMethod());
            String apiControllerMethodInfo = ObjectContainer.instance().getApiControllerInfo(path + "-" + req.getMethod().toLowerCase());
            if (StringUtils.isNotBlank(apiControllerMethodInfo)) {
                String[] controlllerMethod = apiControllerMethodInfo.split("-");
                controller = ObjectContainer.instance().getController(controlllerMethod[0]);
                return execute(controller, ReflectionUtil.getMethodByNameAndHeritage(controller.getClass(), controlllerMethod[1], true), pathParameter, req, resp);
            } else {
                List<String> fullPathVarialbeApiNameList = ObjectContainer.instance().getApiPathVariable(req.getMethod().toLowerCase() + "-" + path.split("/").length);
                if (fullPathVarialbeApiNameList != null && !fullPathVarialbeApiNameList.isEmpty()) {
                    //path variable
                    String[] arrayPath = path.split("/");
                    String[] arrayMethodPath;
                    Parameter[] parameters;
                    String auxParam;
                    String auxPath;
                    Method methodApiVariable;
                    for (String fullPathVarialbeApiName : fullPathVarialbeApiNameList) {
                        apiControllerMethodInfo = ObjectContainer.instance().getApiControllerInfo(fullPathVarialbeApiName);
                        String[] controlllerMethod = apiControllerMethodInfo.split("-");
                        controller = ObjectContainer.instance().getController(controlllerMethod[0]);
                        methodApiVariable = ReflectionUtil.getMethodByNameAndHeritage(controller.getClass(), controlllerMethod[1], true);
                        auxPath = fullPathVarialbeApiName;
                        arrayMethodPath = auxPath.split("/");
                        if (arrayPath.length == arrayMethodPath.length && methodApiVariable.getParameters().length > 0) {
                            pathParameter.clear();
                            parameters = methodApiVariable.getParameters();
                            for (int i = 0; i < arrayMethodPath.length; i++) {
                                if (arrayMethodPath[i].contains("{")) {
                                    auxParam = arrayMethodPath[i].replace("{", "").replace("}", "");
                                    for (Parameter parameter : parameters) {
                                        if (parameter.isAnnotationPresent(RequestParam.class) && parameter.getAnnotation(RequestParam.class).name().equalsIgnoreCase(auxParam)) {
                                            auxPath = auxPath.replace("{" + auxParam + "}", arrayPath[i]);
                                            pathParameter.put(auxParam, arrayPath[i]);
                                        }
                                    }
                                }
                            }
                            if (auxPath.equalsIgnoreCase(path))
                                return execute(controller, methodApiVariable, pathParameter, req, resp);
                        }
                    }
                }
            }
            throw new RequestProcessException("recurso no encontrado - " + req.getRequestURL().toString());
        } catch (ResourceNotFoundException | ResourcePermissionException e) {
            throw new RequestProcessException(e);
        }
    }

    /**
     * execute
     * @param controller controller
     * @param method method
     * @param pathParameter pathParameter
     * @param req req
     * @param resp resp
     * @return Object
     * @throws ResourceNotFoundException ResourceNotFoundException
     * @throws ResourcePermissionException ResourcePermissionException
     * @throws RequestProcessException RequestProcessException
     */
    protected Object execute(Object controller, Method method, Map<String, Object> pathParameter, HttpServletRequest req, HttpServletResponse resp) throws ResourceNotFoundException, ResourcePermissionException, RequestProcessException {
        checkAuthorizationPermission(controller, method);
        Parameter[] parameters = method.getParameters();
        Object[] paramValue = null;
        if (parameters != null) {
            Object value;
            paramValue = new Object[parameters.length];
            for (int i = 0; i < parameters.length; i++) {
                Parameter parameter = parameters[i];
                value = null;
                logger.info("object parameter -> " + controller.toString() + " - " + parameter.toString());
                //is body json?
                RequestBody requestBody;
                RequestParam requestParam;
                boolean required = false;
                if (parameter.isAnnotationPresent(RequestBody.class)) {
                    requestBody = parameter.getAnnotation(RequestBody.class);
                    required = requestBody.required();
                    Class<? extends UIFilterToSearchConverter> uiFilterToSearchConverter = null;
                    if (requestBody.converter().length > 0)
                        uiFilterToSearchConverter = requestBody.converter()[0];
                    value = ObjetTypeConverter.converBodyValueToObjectType(parameter, required, uiFilterToSearchConverter, req);
                    if (value == null && requestBody.required())
                        throw new ResourceNotFoundException("The body parameter value is required : " + req.getPathInfo());
                    ValidateRequiredAttributeUtil.validate(value);
                } else if (parameter.isAnnotationPresent(RequestParam.class)) {
                    requestParam = parameter.getAnnotation(RequestParam.class);
                    required = requestParam.required();
                    value = ObjetTypeConverter.converParamValueToObjectType(requestParam.name(), requestParam.required(), parameter, req, false, pathParameter.containsKey(requestParam.name()) ? pathParameter.get(requestParam.name()) : req.getParameter(requestParam.name()));
                } else if (parameter.isAnnotationPresent(HeaderParam.class)) {
                    Map<String, Object> header = new HashMap<>();
                    HeaderParam headerParam = parameter.getAnnotation(HeaderParam.class);
                    String names = headerParam.names();
                    if (StringUtils.isNotBlank(names)) {
                        String[] headerNames = names.split(",");
                        String headerNameType;
                        String[] headerNameTypeArray;
                        if (headerNames != null && headerNames.length > 0) {
                            for (int j = 0; j < headerNames.length; j++) {
                                headerNameType = headerNames[j];
                                headerNameTypeArray = headerNameType.split(":");
                                if (headerNameTypeArray.length == 1) {
                                    header.put(headerNameTypeArray[0], req.getHeader(headerNameTypeArray[0]));
                                } else if (headerNameTypeArray.length > 1) {
                                    if (headerNameTypeArray[1] == "date") {
                                        header.put(headerNameTypeArray[0], req.getDateHeader(headerNameTypeArray[0]));
                                    } else {
                                        //TODO: No se reconoce otro tipo de datos
                                        header.put(headerNameTypeArray[0], req.getHeader(headerNameTypeArray[0]));
                                    }
                                }
                            }
                        }
                    }
                    value = header;
                }
                if ((value == null || StringUtils.isEmpty(value.toString())) && required)
                    throw new ResourceNotFoundException("The parameter value is required " + parameter.getName() + " : " + req.getPathInfo());
                paramValue[i] = value;
            }
        }
        return invokeMethod(controller, method, paramValue);
    }

    /**
     * invokeMethod
     * @param ob ob
     * @param method method
     * @param args args
     * @return Object
     * @throws RequestProcessException RequestProcessException
     */
    private Object invokeMethod(Object ob, Method method, Object[] args) throws RequestProcessException {
        try {
            return method.invoke(ob, args);
        } catch (IllegalAccessException | IllegalArgumentException e) {
            throw new RequestProcessException(e);
        } catch (InvocationTargetException inve) {
            if (inve.getTargetException() != null) {
                throw new RequestProcessException(inve.getTargetException());
            } else {
                throw new RequestProcessException(inve);
            }
        }
    }

    /**
     * checkAuthorizationPermission
     * @param controller controller
     * @param method method
     * @throws ResourcePermissionException ResourcePermissionException
     */
    private void checkAuthorizationPermission(Object controller, Method method) throws ResourcePermissionException {
        String[] controllRoles = getControllerRoles(controller.getClass());
        String[] methodRoles = getMethodRoles(method);
        if (methodRoles != null && methodRoles.length > 0) {
            if (!containsAtLeastOneRole(methodRoles)) {
                logger.error(controller.getClass() + " " + method.getName());
                throw new ResourcePermissionException("no tiene permisos");
            }
        } else if (controllRoles != null && controllRoles.length > 0) {
            if (!containsAtLeastOneRole(controllRoles)) {
                logger.error(controller.getClass() + " " + method.getName());
                throw new ResourcePermissionException("no tiene permisos");
            }
        }
    }

    /**
     * containsAtLeastOneRole
     * @param roles roles
     * @return boolean
     */
    private boolean containsAtLeastOneRole(String[] roles) {
        if (roles == null || SecurityContent.instance().getPrincipal() == null)
            return false;
        for (int i = 0; i < roles.length; i++) {
            if (SecurityContent.instance().getPrincipal().credentials().contains(roles[i]))
                return true;
        }
        return false;
    }

    /**
     * getControllerRoles
     * @param controllerClass controllerClass
     * @return String
     */
    public String[] getControllerRoles(Class<?> controllerClass) {
        if (controllerClass.isAnnotationPresent(SecurityRole.class) && controllerClass.getAnnotation(SecurityRole.class).name() != null) {
            return controllerClass.getAnnotation(SecurityRole.class).name();
        } else
            return new String[] {};
    }

    /**
     * getMethodRoles
     * @param method method
     * @return String[]
     */
    public String[] getMethodRoles(Method method) {
        if (method.isAnnotationPresent(SecurityRole.class) && method.getAnnotation(SecurityRole.class).name() != null) {
            return method.getAnnotation(SecurityRole.class).name();
        } else
            return new String[] {};
    }
}
