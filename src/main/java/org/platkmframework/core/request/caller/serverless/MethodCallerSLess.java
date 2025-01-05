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
package org.platkmframework.core.request.caller.serverless;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.platkmframework.annotation.Service;
import org.platkmframework.context.ObjectContainer;
import org.platkmframework.core.request.caller.ObjetTypeConverter;
import org.platkmframework.core.request.exception.RequestProcessException;
import org.platkmframework.core.request.util.ValidateRequiredAttributeUtil;
import org.platkmframework.util.error.InvocationException;
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
@Service
public class MethodCallerSLess {

    /**
     * Atributo logger
     */
    private static Logger logger = LoggerFactory.getLogger(MethodCallerSLess.class);

    /**
     * Constructor MethodCallerSLess
     */
    public MethodCallerSLess() {
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
        logger.info("searching object from -> " + path + " - " + req.getMethod());
        //String key = "{" + req.getMethod() + ":"  + path + "}";
        // PathProccesor.getSessionObjectByPath(path, req.getMethod());
        Object fObject = ObjectContainer.instance().getFunctionals();
        if (fObject == null)
            throw new RequestProcessException("recurso no encontrado - " + req.getRequestURL().toString());
        List<Method> methods = ReflectionUtil.getAllMethoddHeritage(fObject.getClass(), false);
        Method method = methods.stream().filter((m) -> m.getName().equals(path) && m.canAccess(fObject)).findFirst().orElse(null);
        if (method == null)
            throw new RequestProcessException("recurso no encontrado - " + req.getRequestURL().toString());
        Map<String, Object> pathParameter = new HashMap<>();
        return execute(fObject, method, pathParameter, req, resp);
    }

    /**
     * execute
     * @param obj obj
     * @param method method
     * @param pathParameter pathParameter
     * @param req req
     * @param resp resp
     * @return Object
     * @throws RequestProcessException RequestProcessException
     */
    protected Object execute(Object obj, Method method, Map<String, Object> pathParameter, HttpServletRequest req, HttpServletResponse resp) throws RequestProcessException {
        try {
            //checkAuthorizationPermission(apiMethodInfo, method);
            Parameter[] parameters = method.getParameters();
            Object[] paramValue = null;
            if (parameters != null) {
                Object value;
                paramValue = new Object[parameters.length];
                for (int i = 0; i < parameters.length; i++) {
                    Parameter parameter = parameters[i];
                    value = null;
                    logger.info("object parameter -> " + obj.toString() + " - " + parameter.toString());
                    boolean required = false;
                    if (ReflectionUtil.isPrimitiveType(parameter.getType())) {
                        value = ObjetTypeConverter.converParamValueToObjectType(parameter.getName(), false, parameter, req, false, pathParameter.containsKey(parameter.getName()) ? pathParameter.get(parameter.getName()) : req.getParameter(parameter.getName()));
                    } else {
                        value = ObjetTypeConverter.converBodyValueToObjectType(parameter, required, null, req);
                        ValidateRequiredAttributeUtil.validate(value);
                    }
                    paramValue[i] = value;
                }
                Map<String, Object> header = new HashMap<>();
                Enumeration<String> headerEnumerator = req.getHeaderNames();
                while (headerEnumerator.hasMoreElements()) {
                    String headerName = headerEnumerator.nextElement();
                    header.put(headerName, req.getHeader(headerName));
                }
            }
            return ReflectionUtil.invokeMethod(obj, method, paramValue);
        } catch (InvocationException e) {
            throw new RequestProcessException(e);
        }
    }
}
