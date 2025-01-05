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

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import jakarta.servlet.http.HttpServletResponse;

/**
 *   Author:
 *     Eduardo Iglesias
 *   Contributors:
 *   	Eduardo Iglesias - initial API and implementation
 */
public class ProcessDownload {

    // ..ms = 1 week.
    private static final long DEFAULT_EXPIRE_TIME = 604800000L;

    /**
     * Atributo MULTIPART_BOUNDARY
     */
    private static final String MULTIPART_BOUNDARY = "MULTIPART_BYTERANGES";

    /**
     * Atributo C_If_None_Match
     */
    private static final String C_If_None_Match = "If-None-Match";

    /**
     * Atributo C_ETag
     */
    private static final String C_ETag = "ETag";

    /**
     * Atributo C_Expires
     */
    private static final String C_Expires = "Expires";

    /**
     * Atributo C_If_Modified_Since
     */
    private static final String C_If_Modified_Since = "If-Modified-Since";

    /**
     * Atributo C_If_Match
     */
    private static final String C_If_Match = "If-Match";

    /**
     * Atributo C_Range
     */
    private static final String C_Range = "Range";

    /**
     * Atributo C_If_Range
     */
    private static final String C_If_Range = "If-Range";

    /**
     * Atributo C_If_Unmodifield_Sice
     */
    private static final String C_If_Unmodifield_Sice = "If-Unmodified-Since";

    /**
     * Constructor ProcessDownload
     */
    public ProcessDownload() {
    }

    /**
     * process
     * @param file file
     * @param header header
     * @return MultipartFile
     * @throws FileNotFoundException FileNotFoundException
     */
    public MultipartFile process(File file, Map<String, Object> header) throws FileNotFoundException {
        MultipartFile multipartFile = new MultipartFile();
        //multipartFile.setFile(file);
        multipartFile.setFileName(file.getName());
        String fileName = file.getName();
        long length = file.length();
        long lastModified = file.lastModified();
        String eTag = fileName + "_" + length + "_" + lastModified;
        long expires = System.currentTimeMillis() + DEFAULT_EXPIRE_TIME;
        Object ifNoneMatch = header.get(C_If_None_Match);
        if (ifNoneMatch != null && matches(ifNoneMatch.toString(), eTag)) {
            multipartFile.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
            // Required in 304.
            multipartFile.setHeader(C_ETag, eTag);
            // Postpone cache with 1 week.
            multipartFile.setHeader(C_Expires, lastModified);
            return multipartFile;
        }
        // If-Modified-Since header should be greater than LastModified. If so, then return 304.
        // This header is ignored if any If-None-Match header is specified.
        Long ifModifiedSince = (Long) header.get(C_If_Modified_Since);
        if (ifNoneMatch == null && ifModifiedSince != null && ifModifiedSince != -1 && ifModifiedSince + 1000 > lastModified) {
            multipartFile.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
            // Required in 304.
            multipartFile.setHeader(C_ETag, eTag);
            // Postpone cache with 1 week.
            multipartFile.setDateHeader(C_Expires, expires);
            return multipartFile;
        }
        // If-Match header should contain "*" or ETag. If not, then return 412.
        Object ifMatch = header.get(C_If_Match);
        if (ifMatch != null && !matches(ifMatch.toString(), eTag)) {
            multipartFile.setError(HttpServletResponse.SC_PRECONDITION_FAILED);
            return multipartFile;
        }
        // If-Unmodified-Since header should be greater than LastModified. If not, then return 412.
        Long ifUnmodifiedSince = (Long) header.get(C_If_Unmodifield_Sice);
        if (ifUnmodifiedSince != null && ifUnmodifiedSince != -1 && ifUnmodifiedSince + 1000 <= lastModified) {
            multipartFile.setError(HttpServletResponse.SC_PRECONDITION_FAILED);
            return multipartFile;
        }
        //validate range
        // Prepare some variables. The full Range represents the complete file.
        Range full = new Range(0, length - 1, length);
        List<Range> ranges = new ArrayList<Range>();
        // Validate and process Range and If-Range headers.
        String range = (String) header.get(C_Range);
        if (range != null) {
            // Range header should match format "bytes=n-n,n-n,n-n...". If not, then return 416.
            if (!range.matches("^bytes=\\d*-\\d*(,\\d*-\\d*)*$")) {
                // Required in 416.
                multipartFile.setHeader("Content-Range", "bytes */" + length);
                multipartFile.setError(HttpServletResponse.SC_REQUESTED_RANGE_NOT_SATISFIABLE);
                return multipartFile;
            }
            // If-Range header should either match ETag or be greater then LastModified. If not,
            // then return full file.
            String ifRange = (String) header.get(C_If_Range);
            if (ifRange != null && !ifRange.equals(eTag)) {
                try {
                    // Throws IAE if invalid.
                    long ifRangeTime = Long.valueOf(ifRange);
                    if (ifRangeTime != -1 && ifRangeTime + 1000 < lastModified) {
                        ranges.add(full);
                    }
                } catch (IllegalArgumentException ignore) {
                    ranges.add(full);
                }
            }
            // If any valid If-Range header, then process each part of byte range.
            if (ranges.isEmpty()) {
                for (String part : range.substring(6).split(",")) {
                    // Assuming a file with length of 100, the following examples returns bytes at:
                    // 50-80 (50 to 80), 40- (40 to length=100), -20 (length-20=80 to length=100).
                    long start = sublong(part, 0, part.indexOf("-"));
                    long end = sublong(part, part.indexOf("-") + 1, part.length());
                    if (start == -1) {
                        start = length - end;
                        end = length - 1;
                    } else if (end == -1 || end > length - 1) {
                        end = length - 1;
                    }
                    // Check if Range is syntactically valid. If not, then return 416.
                    if (start > end) {
                        // Required in 416.
                        multipartFile.setHeader("Content-Range", "bytes */" + length);
                        multipartFile.setError(HttpServletResponse.SC_REQUESTED_RANGE_NOT_SATISFIABLE);
                        return multipartFile;
                    }
                    // Add range.
                    ranges.add(new Range(start, end, length));
                }
            }
        }
        // Prepare and initialize response --------------------------------------------------------
        // Else, expect for images, determine content disposition. If content type is supported by
        // the browser, then set to inline, else attachment which will pop a 'save as' dialogue.
        // else if (!contentType.startsWith("image")) {
        //     String accept = request.getHeader("Accept");
        //      disposition = accept != null && accepts(accept, contentType) ? "inline" : "attachment";
        // }
        // Initialize response.
        // response.reset();
        //response.setBufferSize(DEFAULT_BUFFER_SIZE);
        /// response.setHeader("Content-Disposition", disposition + ";filename=\"" + fileName + "\"");
        multipartFile.setHeader("Accept-Ranges", "bytes");
        multipartFile.setHeader("ETag", eTag);
        multipartFile.setDateHeader("Last-Modified", lastModified);
        multipartFile.setDateHeader("Expires", expires);
        // Send requested file (part(s)) to client ------------------------------------------------
        if (ranges.isEmpty() || ranges.get(0) == full) {
            // Return full file.
            multipartFile.setRange(full);
            multipartFile.setHeader("Content-Length", String.valueOf(full.length));
        } else if (ranges.size() == 1) {
            // Return single part of file.
            Range r = ranges.get(0);
            multipartFile.setRange(r);
            //response.setContentType(contentType);
            multipartFile.setHeader("Content-Range", "bytes " + r.start + "-" + r.end + "/" + r.total);
            multipartFile.setHeader("Content-Length", String.valueOf(r.length));
            // 206.
            multipartFile.setStatus(HttpServletResponse.SC_PARTIAL_CONTENT);
        } else {
            multipartFile.setRanges(ranges);
            // Return multiple parts of file.
            multipartFile.setContentType("multipart/byteranges; boundary=" + MULTIPART_BOUNDARY);
            // 206.
            multipartFile.setStatus(HttpServletResponse.SC_PARTIAL_CONTENT);
            multipartFile.setMultipart(true);
        }
        return multipartFile;
    }

    /**
     * matches
     * @param matchHeader matchHeader
     * @param toMatch toMatch
     * @return boolean
     */
    private static boolean matches(String matchHeader, String toMatch) {
        String[] matchValues = matchHeader.split("\\s*,\\s*");
        Arrays.sort(matchValues);
        return Arrays.binarySearch(matchValues, toMatch) > -1 || Arrays.binarySearch(matchValues, "*") > -1;
    }

    /**
     * sublong
     * @param value value
     * @param beginIndex beginIndex
     * @param endIndex endIndex
     * @return long
     */
    private static long sublong(String value, int beginIndex, int endIndex) {
        String substring = value.substring(beginIndex, endIndex);
        return (substring.length() > 0) ? Long.parseLong(substring) : -1;
    }
}
