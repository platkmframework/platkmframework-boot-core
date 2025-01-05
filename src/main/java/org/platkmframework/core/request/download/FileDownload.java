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
package org.platkmframework.core.request.download;

import java.io.IOException;
import java.util.Base64;
import org.apache.commons.io.IOUtils;
import org.platkmframework.core.request.multipart.MultipartFile;

/**
 *   Author:
 *     Eduardo Iglesias
 *   Contributors:
 *   	Eduardo Iglesias - initial API and implementation
 */
public class FileDownload {

    /**
     * 	private static final String MULTIPART_BOUNDARY = "MULTIPART_BYTERANGES";
     * 	private final int ARBITARY_SIZE = 1048;
     * 	private static final int DEFAULT_BUFFER_SIZE = 10240; // ..bytes = 10KB.
     */
    public FileDownload() {
    }

    /**
     * process
     * @param multipartFile multipartFile
     * @return DownFileInfo
     * @throws IOException IOException
     */
    public DownFileInfo process(MultipartFile multipartFile) throws IOException {
        return new DownFileInfo(multipartFile.getFileName(), multipartFile.getContentType(), Base64.getEncoder().encodeToString(IOUtils.toByteArray(multipartFile.getFile())), multipartFile.getFilesize());
    }
    /**
     *  the olderst  of 2
     * 	public void process(MultipartFile multipartFile, HttpServletRequest req, HttpServletResponse resp)
     * 	{
     * 		try{
     *
     * 			resp.setContentType(multipartFile.getFiletype());
     * 			resp.setHeader("Content-disposition", "attachment; filename=\"" + multipartFile.getFileName() +  "\"");
     *
     * 			multipartFile.getHeader().forEach((k, v) ->{
     * 				resp.setHeader(k,v.toString());
     * 			});
     *
     * 			multipartFile.getDateHeader().forEach((k, v) ->{
     * 				resp.setDateHeader(k,v);
     * 			});
     *
     * 			RandomAccessFile input = new RandomAccessFile(multipartFile.getFile(), "r");
     * 			OutputStream out = resp.getOutputStream();
     * 			if(multipartFile.getRanges().size() ==1) {
     *
     * 				copy(input, out, multipartFile.getRanges().get(0).start, multipartFile.getRanges().get(0).length);
     *
     * 			}else if(multipartFile.getRanges().size() > 1) {
     * 				ServletOutputStream sos = (ServletOutputStream) out;
     * 				for (Range r : multipartFile.getRanges()) {
     *                     // Add multipart boundary and header fields for every range.
     * 					sos.println();
     *                     sos.println("--" + MULTIPART_BOUNDARY);
     *                     sos.println("Content-Type: " + multipartFile.getFiletype());
     *                     sos.println("Content-Range: bytes " + r.start + "-" + r.end + "/" + r.total);
     *
     *                     // Copy single part range of multi part range.
     *                     copy(input, out, r.start, r.length);
     *                 }
     *
     * 			}
     *
     * 		}catch (Exception e){
     * 			e.printStackTrace();
     * 			resp.setStatus(HttpStatus.INTERNAL_SERVER_ERROR_500);
     * 		}
     *
     * 	}
     */
    /**
     *  description: last used
     *  @param multipartFile
     *  @param req
     *  @param resp
     *
     * 	public void process(MultipartFile multipartFile, HttpServletRequest req, HttpServletResponse resp)
     * 	{
     * 		try{
     * 	       resp.setContentType(multipartFile.getFiletype());
     * 	       resp.setHeader("Content-disposition", "attachment; filename=\"" + multipartFile.getFileName() +  "\"");
     * 	       OutputStream out = resp.getOutputStream();
     * 	       InputStream inputS = multipartFile.getFile(); //FileUtils.openInputStream(multipartFile.getFile());
     * 	       try(InputStream in =  inputS)
     * 	        {
     * 	            byte[] buffer = new byte[ARBITARY_SIZE];
     * 	            int numBytesRead;
     * 	            while ((numBytesRead = in.read(buffer)) > 0)
     * 	                out.write(buffer, 0, numBytesRead);
     * 	        }
     *
     * 	        out.flush();
     * 	        out.close();
     * 	       // resp.setCharacterEncoding("UTF-8");
     * 			resp.setStatus(HttpStatus.OK_200);
     * 		}catch (Exception e){
     * 			e.printStackTrace();
     * 			resp.setStatus(HttpStatus.INTERNAL_SERVER_ERROR_500);
     * 		}
     *
     * 	}
     */
    /**
     * 	private static void copy(RandomAccessFile input, OutputStream output, long start, long length)
     * 	        throws IOException
     * 	    {
     * 	        byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];
     * 	        int read;
     *
     * 	        if (input.length() == length) {
     * 	            // Write full range.
     * 	            while ((read = input.read(buffer)) > 0) {
     * 	                output.write(buffer, 0, read);
     * 	            }
     * 	        } else {
     * 	            // Write partial range.
     * 	            input.seek(start);
     * 	            long toRead = length;
     *
     * 	            while ((read = input.read(buffer)) > 0) {
     * 	                if ((toRead -= read) > 0) {
     * 	                    output.write(buffer, 0, read);
     * 	                } else {
     * 	                    output.write(buffer, 0, (int) toRead + read);
     * 	                    break;
     * 	                }
     * 	            }
     * 	        }
     * 	    }
     */
}
