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
package org.platkmframework.core.response.util;

import java.io.IOException;
import java.io.PrintWriter;
import org.platkmframework.comon.service.exception.CustomServletException;
import org.platkmframework.util.JsonUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jakarta.servlet.http.HttpServletResponse;

/**
 *   Author:
 *     Eduardo Iglesias
 *   Contributors:
 *   	Eduardo Iglesias - initial API and implementation
 */
public class ResponseUtil {

	/**
	 * Base64Util
	 */
	  private ResponseUtil() {
	    throw new IllegalStateException("ResponseUtil class");
	  }
	  
    /**
     * Atributo logger
     */
    private static Logger logger = LoggerFactory.getLogger(ResponseUtil.class);

    /**
     * createResponse
     * @param resp resp
     * @param status status
     * @param message message
     * @throws CustomServletException CustomServletException
     */
    public static void createResponse(HttpServletResponse resp, int status, String message) throws CustomServletException {
        _createResponse(resp, null, status, message);
    }

    /**
     * createResponse
     * @param resp resp
     * @param data data
     * @param status status
     * @param message message
     * @throws CustomServletException CustomServletException
     */
    public static void createResponse(HttpServletResponse resp, Object data, int status, String message) throws CustomServletException {
        _createResponse(resp, data, status, message);
    }

    /**
     * createResponse
     * @param resp resp
     * @param data data
     * @param status status
     * @throws CustomServletException CustomServletException
     */
    public static void createResponse(HttpServletResponse resp, Object data, int status) throws CustomServletException {
        _createResponse(resp, data, status, null);
    }

    /**
     * _createResponse
     * @param resp resp
     * @param object object
     * @param status status
     * @param message message
     * @throws CustomServletException CustomServletException
     */
    private static void _createResponse(HttpServletResponse resp, Object object, int status, String message) throws CustomServletException {
        try {
            resp.setCharacterEncoding("UTF-8");
            resp.setContentType("application/json");
            String json;
            if (object != null) {
                json = JsonUtil.objectToJson(object);
                //resp.setContentLength(json.length());
                PrintWriter out = resp.getWriter();
                out.println(json);
                out.flush();
            }
            resp.setStatus(status);
        } catch (Exception e) {
            logger.error(e.getMessage());
            throwError("No se pudo realizar el proceso, inténtelo más tarde");
        }
    }

    /**
     * throwError
     * @param msg msg
     * @throws CustomServletException CustomServletException
     */
    public static void throwError(String msg) throws CustomServletException {
        throw new CustomServletException(msg);
    }

    /**
     * setResponseError
     * @param resp resp
     * @param status status
     * @param msg msg
     * @throws IOException IOException
     */
    public static void setResponseError(HttpServletResponse resp, int status, String msg) throws IOException {
        resp.setContentType("text/plain");
        resp.sendError(status, msg);
    }
}
