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
package org.platkmframework.core.request.servlet;

import org.platkmframework.core.request.manager.HttpRequestManager;
import org.platkmframework.security.content.XSSRequestWrapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 *   Author:
 *     Eduardo Iglesias
 *   Contributors:
 *   	Eduardo Iglesias - initial API and implementation
 */
public class RequestManagerServlet extends HttpServlet {

    /**
     */
    private static final long serialVersionUID = 1L;

    
    /**
     * RequestManagerServlet
     */
    public RequestManagerServlet() {
		super();
	}

	/**
     * doGet
     * @param req req
     * @param resp resp
     * @throws ServletException ServletException
     */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException {
        process(req, resp);
    }

    /**
     * doPost
     * @param req req
     * @param resp resp
     * @throws ServletException ServletException
     */
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException {
        process(req, resp);
    }

    /**
     * doPut
     * @param req req
     * @param resp resp
     * @throws ServletException ServletException
     */
    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException {
        process(req, resp);
    }

    /**
     * doDelete
     * @param req req
     * @param resp resp
     * @throws ServletException ServletException
     */
    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException {
        process(req, resp);
    }

    /**
     * process
     * @param request request
     * @param response response
     * @throws ServletException ServletException
     */
    private void process(HttpServletRequest request, HttpServletResponse response) throws ServletException {
        XSSRequestWrapper xssReq = new XSSRequestWrapper(request);
        HttpRequestManager.instance().process(xssReq, response);
    }
}
