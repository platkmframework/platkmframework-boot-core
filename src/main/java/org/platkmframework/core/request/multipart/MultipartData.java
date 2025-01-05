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
package org.platkmframework.core.request.multipart;

import java.io.InputStream;

/**
 *   Author:
 *     Eduardo Iglesias
 *   Contributors:
 *   	Eduardo Iglesias - initial API and implementation
 */
public class MultipartData {

    /**
     * Atributo content
     */
    private InputStream content;

    /**
     * Atributo contentType
     */
    private String contentType;

    /**
     * Atributo name
     */
    private String name;

    /**
     * Atributo filename
     */
    private String filename;

    /**
     * Atributo size
     */
    private long size;
    
    
	/**
	 * MultipartData
	 */
    public MultipartData() {
		super();
	}

	/**
     * getContent
     * @return InputStream
     */
    public InputStream getContent() {
        return content;
    }

    /**
     * setContent
     * @param content content
     */
    public void setContent(InputStream content) {
        this.content = content;
    }

    /**
     * getContentType
     * @return String
     */
    public String getContentType() {
        return contentType;
    }

    /**
     * setContentType
     * @param contentType contentType
     */
    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    /**
     * getSize
     * @return long
     */
    public long getSize() {
        return size;
    }

    /**
     * setSize
     * @param size size
     */
    public void setSize(long size) {
        this.size = size;
    }

    /**
     * getName
     * @return String
     */
    public String getName() {
        return name;
    }

    /**
     * setName
     * @param name name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * getFilename
     * @return String
     */
    public String getFilename() {
        return filename;
    }

    /**
     * setFilename
     * @param filename filename
     */
    public void setFilename(String filename) {
        this.filename = filename;
    }
}
