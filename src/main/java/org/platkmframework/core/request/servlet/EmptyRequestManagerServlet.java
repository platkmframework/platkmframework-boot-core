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

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.platkmframework.comon.service.exception.CustomServletException;
import org.platkmframework.context.project.CorePropertyConstant;
import org.platkmframework.context.project.ProjectContent;
import org.platkmframework.core.response.util.ResponseUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 *   Author:
 *     Eduardo Iglesias
 *   Contributors:
 *   	Eduardo Iglesias - initial API and implementation
 */
@WebServlet(urlPatterns = { "" })
public class EmptyRequestManagerServlet extends HttpServlet {

    /**
     */
    private static final long serialVersionUID = 1L;

    /**
     * Atributo logger
     */
    private static Logger logger = LoggerFactory.getLogger(EmptyRequestManagerServlet.class);

    /**
     * EmptyRequestManagerServlet
     */
    public EmptyRequestManagerServlet() {
        super();
    }

    /**
     * doGet
     * @param req req
     * @param resp resp
     * @throws ServletException ServletException
     * @throws IOException IOException
     */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        process(req, resp);
    }

    /**
     * doPost
     * @param req req
     * @param resp resp
     * @throws ServletException ServletException
     * @throws IOException IOException
     */
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        process(req, resp);
    }

    /**
     * doPut
     * @param req req
     * @param resp resp
     * @throws ServletException ServletException
     * @throws IOException IOException
     */
    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        process(req, resp);
    }

    /**
     * process
     * @param request request
     * @param resp resp
     * @throws CustomServletException CustomServletException
     */
    private void process(HttpServletRequest request, HttpServletResponse resp) throws CustomServletException {
        String indexPate = ProjectContent.instance().getProperty(CorePropertyConstant.ORG_PLATKMFRAMEWORK_CONFIGURATION_INDEX_PAGE);
        if (StringUtils.isBlank(indexPate))
            ResponseUtil.throwError("recurso no encontrado");
        try {
            InputStream inputStream = this.getClass().getResourceAsStream("/dbapi/index.html");
            String openApiPage = IOUtils.toString(inputStream, StandardCharsets.UTF_8.name());
            openApiPage = openApiPage.replace("${serverAPI}", ProjectContent.instance().getProperty(CorePropertyConstant.ORG_PLATKMFRAMEWORK_SERVER_PROTOCOL) + "://" + ProjectContent.instance().getProperty(CorePropertyConstant.ORG_PLATKMFRAMEWORK_SERVER_NAME) + ":" + ProjectContent.instance().getProperty(CorePropertyConstant.ORG_PLATKMFRAMEWORK_SERVER_PORT) + ProjectContent.instance().getProperty(CorePropertyConstant.ORG_PLATKMFRAMEWORK_CONTENT_PATH) + ProjectContent.instance().getProperty(CorePropertyConstant.ORG_PLATKMFRAMEWORK_SERVLET_PLATH));
            resp.setCharacterEncoding(StandardCharsets.ISO_8859_1.name());
            resp.setContentType("html");
            resp.setStatus(200);
            PrintWriter out = resp.getWriter();
            out.println(openApiPage);
            out.flush();
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
    }
}
