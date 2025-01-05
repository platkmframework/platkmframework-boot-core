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
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import org.platkmframework.core.request.manager.ResponseBase;

/**
 *   Author:
 *     Eduardo Iglesias
 *   Contributors:
 *   	Eduardo Iglesias - initial API and implementation
 */
public class MultipartFile extends ResponseBase implements Serializable {

    /**
     */
    private static final long serialVersionUID = 1L;

    /**
     * Atributo fileName
     */
    private String fileName;

    /**
     * Atributo id
     */
    private Long id;

    /**
     * Atributo code
     */
    private String code;

    /**
     * Atributo filetype
     */
    private String filetype;

    /**
     * Atributo error
     */
    private int error;

    /**
     * Atributo contentType
     */
    private String contentType;

    /**
     * Atributo filesize
     */
    private Long filesize;

    /**
     * Atributo file
     */
    private InputStream file;

    /**
     * Atributo multipart
     */
    private boolean multipart;

    /**
     * Atributo ranges
     */
    List<Range> ranges;

    /**
     * MultipartFile
     */
    public MultipartFile() {
        super();
    }

    /**
     * getFileName
     * @return String
     */
    public String getFileName() {
        return fileName;
    }

    /**
     * setFileName
     * @param fileName fileName
     */
    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    /**
     * getId
     * @return Long
     */
    public Long getId() {
        return id;
    }

    /**
     * setId
     * @param id id
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * getCode
     * @return String
     */
    public String getCode() {
        return code;
    }

    /**
     * setCode
     * @param code code
     */
    public void setCode(String code) {
        this.code = code;
    }

    /**
     * getFiletype
     * @return String
     */
    public String getFiletype() {
        return filetype;
    }

    /**
     * setFiletype
     * @param filetype filetype
     */
    public void setFiletype(String filetype) {
        this.filetype = filetype;
    }

    /**
     * getError
     * @return int
     */
    public int getError() {
        return error;
    }

    /**
     * setError
     * @param error error
     */
    public void setError(int error) {
        this.error = error;
    }

    /**
     * setRange
     * @param r r
     */
    public void setRange(Range r) {
        getRanges().add(r);
    }

    /**
     * setContentType
     * @param contentType contentType
     */
    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    /**
     * getContentType
     * @return String
     */
    public String getContentType() {
        return contentType;
    }

    /**
     * setMultipart
     * @param multipart multipart
     */
    public void setMultipart(boolean multipart) {
        this.multipart = multipart;
    }

    /**
     * isMultipart
     * @return boolean
     */
    public boolean isMultipart() {
        return multipart;
    }

    /**
     * setRanges
     * @param ranges ranges
     */
    public void setRanges(List<Range> ranges) {
        getRanges().addAll(ranges);
    }

    /**
     * getRanges
     * @return List
     */
    public List<Range> getRanges() {
        if (ranges == null)
            ranges = new ArrayList<Range>();
        return ranges;
    }

    /**
     * getFile
     * @return InputStream
     */
    public InputStream getFile() {
        return file;
    }

    /**
     * setFile
     * @param file file
     */
    public void setFile(InputStream file) {
        this.file = file;
    }

    /**
     * getFilesize
     * @return Long
     */
    public Long getFilesize() {
        return filesize;
    }

    /**
     * setFilesize
     * @param filesize filesize
     */
    public void setFilesize(Long filesize) {
        this.filesize = filesize;
    }
}
