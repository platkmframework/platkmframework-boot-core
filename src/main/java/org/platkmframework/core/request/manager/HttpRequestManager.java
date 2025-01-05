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
package org.platkmframework.core.request.manager;

import java.io.IOException;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.jetty.http.HttpStatus;
import org.platkmframework.comon.service.exception.CustomServletException;
import org.platkmframework.core.request.caller.MethodCaller;
import org.platkmframework.core.request.caller.serverless.MethodCallerSLess;
import org.platkmframework.core.request.download.DownFileInfo;
import org.platkmframework.core.request.download.FileDownload;
import org.platkmframework.core.request.exception.RequestProcessException;
import org.platkmframework.core.request.exception.ResourceNotFoundException;
import org.platkmframework.core.request.exception.ResourcePermissionException;
import org.platkmframework.core.request.multipart.MultipartFile;
import org.platkmframework.core.response.util.ResponseUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 *   Author:
 *     Eduardo Iglesias
 *   Contributors:
 *   	Eduardo Iglesias - initial API and implementation
 */
public class HttpRequestManager {

    //private static Logger logger = LoggerFactory.getLogger(HttpRequestManager.class);
    /**
     * Atributo maxMemSize
     */
    int maxMemSize = 5000 * 1024;

    /**
     * Atributo maxFileSize
     */
    int maxFileSize = 5000 * 1024;

    /**
     * Atributo httpRequestManager
     */
    private static HttpRequestManager httpRequestManager;

    /**
     * Atributo methodCaller
     */
    private MethodCaller methodCaller;

    /**
     * Atributo methodCallerSLess
     */
    private MethodCallerSLess methodCallerSLess;

    /**
     * Constructor HttpRequestManager
     */
    private HttpRequestManager() {
        super();
        methodCaller = new MethodCaller();
        methodCallerSLess = new MethodCallerSLess();
    }

    /**
     * instance
     * @return HttpRequestManager
     */
    public static HttpRequestManager instance() {
        if (httpRequestManager == null)
            httpRequestManager = new HttpRequestManager();
        return httpRequestManager;
    }

    /**
     * process
     * @param req req
     * @param resp resp
     * @throws ServletException ServletException
     */
    public void process(HttpServletRequest req, HttpServletResponse resp) throws ServletException {
        String path = req.getPathInfo();
        if (StringUtils.isEmpty(path))
            ResponseUtil.throwError("recurso no encontrado");
        _processApiRequest(path, req, resp);
    }

    /**
     * @param path
     * @param req
     * @param resp
     * @throws ServletException
     */
    private void _processApiRequest(String path, HttpServletRequest req, HttpServletResponse resp) throws ServletException {
        try {
            Object object = null;
            if ("serverless".contains(path))
                object = methodCallerSLess.execute(path, req, resp);
            else
                object = methodCaller.execute(path, req, resp);
            _writeResponse(object, req, resp);
        } catch (RequestProcessException e) {
            if (e.getCause() == null)
                throw new ServletException(e.getMessage());
            ;
            throw new ServletException(e.getCause());
        }
    }

    /**
     * @param object
     * @param req
     * @param resp
     * @throws CustomServletException
     */
    private void _writeResponse(Object object, HttpServletRequest req, HttpServletResponse resp) throws CustomServletException {
        if (object != null && object instanceof MultipartFile) {
            try {
                DownFileInfo downFileList = new FileDownload().process((MultipartFile) object);
                writeInfo(resp, downFileList, HttpStatus.OK_200);
            } catch (IOException e) {
                ResponseUtil.throwError("No se pudo realizar el proceso, inténtelo más tarde");
            }
        } else {
            writeInfo(resp, object, HttpStatus.OK_200);
        }
    }

    /**
     * writeInfo
     * @param resp resp
     * @param object object
     * @param status status
     * @throws CustomServletException CustomServletException
     */
    private void writeInfo(HttpServletResponse resp, Object object, int status) throws CustomServletException {
        ResponseUtil.createResponse(resp, object, status);
    }
}
