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
package org.platkmframework.core.request.filter;

import java.io.IOException;
import org.eclipse.jetty.http.HttpStatus;
import org.platkmframework.comon.service.exception.CustomServletException;
import org.platkmframework.context.ObjectContainer;
import org.platkmframework.core.response.util.ResponseUtil;
import org.platkmframework.proxy.ProxyProcesorException;
import org.platkmframework.security.content.SecurityContent;
import org.platkmframework.security.exception.AuthSecurityException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletResponse;

/**
 *   Author:
 *     Eduardo Iglesias
 *   Contributors:
 *   	Eduardo Iglesias - initial API and implementation
 */
public class ExceptionFilter implements Filter {

    /**
     * Atributo logger
     */
    private static Logger logger = LoggerFactory.getLogger(ExceptionFilter.class);

    /**
     * ExceptionFilter
     */
    public ExceptionFilter() {
        super();
    }

    /**
     * destroy
     */
    @Override
    public void destroy() {
    }

    /**
     * doFilter
     * @param request request
     * @param response response
     * @param chain chain
     * @throws IOException IOException
     * @throws ServletException ServletException
     */
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletResponse resp = (HttpServletResponse) response;
        boolean throwNoControlledError = true;
        try {
            chain.doFilter(request, resp);
            throwNoControlledError = false;
        } catch (CustomServletException e) {
            logger.error(e.getMessage());
            throwNoControlledError = false;
            processException(resp, e);
        } catch (ProxyProcesorException e) {
            logger.error(e.getMessage());
            throwNoControlledError = false;
            sendErrorInfo(resp, e.getStatus(), e.getMessage());
        } catch (Exception e) {
            throwNoControlledError = false;
            logger.error(e.getMessage());
            if (e.getCause() != null && e.getCause() instanceof AuthSecurityException) {
                sendErrorInfo(resp, ((AuthSecurityException) e.getCause()).getStatus(), ((AuthSecurityException) e.getCause()).getMessage());
            } else {
                if (e.getCause() != null && ObjectContainer.instance().containsException(e.getCause())) {
                    sendErrorInfo(resp, HttpStatus.BAD_REQUEST_400, e.getCause().getMessage());
                } else if (ObjectContainer.instance().containsException(e)) {
                    sendErrorInfo(resp, HttpStatus.BAD_REQUEST_400, e.getMessage());
                } else {
                    sendErrorInfo(resp, HttpStatus.INTERNAL_SERVER_ERROR_500, "No se pudo realizar el proceso, inténtelo más tarde");
                }
            }
        } finally {
            SecurityContent.instance().clear();
            if (throwNoControlledError)
                sendErrorInfo(resp, HttpStatus.INTERNAL_SERVER_ERROR_500, "No se pudo realizar el proceso, inténtelo más tarde");
        }
    }

    /**
     * processException
     * @param resp resp
     * @param e e
     * @throws ServletException ServletException
     */
    private void processException(HttpServletResponse resp, CustomServletException e) throws ServletException {
        if (ObjectContainer.instance().containsException(e)) {
            if (e.getStatus() > 0) {
                sendErrorInfo(resp, e.getStatus(), e.getMessage());
            } else {
                sendErrorInfo(resp, HttpStatus.BAD_REQUEST_400, e.getMessage());
            }
        } else if (e.getCause() != null && ObjectContainer.instance().containsException(e)) {
            sendErrorInfo(resp, HttpStatus.BAD_REQUEST_400, e.getCause().getMessage());
        } else {
            sendErrorInfo(resp, HttpStatus.INTERNAL_SERVER_ERROR_500, "No se pudo realizar el proceso, inténtelo más tarde");
        }
    }

    /**
     * sendErrorInfo
     * @param resp resp
     * @param status status
     * @param msg msg
     */
    private void sendErrorInfo(HttpServletResponse resp, int status, String msg) {
        try {
            logger.error("ERROR STATUS->" + status);
            logger.error("ERROR MESSAGE->" + msg);
            ResponseUtil.setResponseError(resp, status, msg);
        } catch (IOException e) {
            logger.error(e.getMessage());
            resp.setStatus(status);
        }
    }

    /**
     * init
     * @param arg0 arg0
     * @throws ServletException ServletException
     */
    @Override
    public void init(FilterConfig arg0) throws ServletException {
    }
}
