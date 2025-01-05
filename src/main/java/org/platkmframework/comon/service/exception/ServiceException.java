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
package org.platkmframework.comon.service.exception;

import org.eclipse.jetty.http.HttpStatus;
import org.platkmframework.annotation.TruslyException;

/**
 *   Author:
 *     Eduardo Iglesias
 *   Contributors:
 *   	Eduardo Iglesias - initial API and implementation
 */
@TruslyException
public class ServiceException extends Exception implements StatusException {

    /**
     */
    private static final long serialVersionUID = 1L;

    /**
     * Atributo status
     */
    public int status = HttpStatus.BAD_REQUEST_400;

    /**
     * Constructor ServiceException
     * @param status status
     * @param arg0 arg0
     */
    public ServiceException(int status, String arg0) {
        super(arg0);
        this.status = status;
    }

    /**
     * Constructor ServiceException
     * @param arg0 arg0
     */
    public ServiceException(String arg0) {
        super(arg0);
    }

    /**
     * getStatus
     * @return int
     */
    public int getStatus() {
        return status;
    }

    /**
     * setStatus
     * @param status status
     */
    public void setStatus(int status) {
        this.status = status;
    }
}
