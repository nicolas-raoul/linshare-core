/*
 * LinShare is an open source filesharing software, part of the LinPKI software
 * suite, developed by Linagora.
 *
 * Copyright (C) 2014 LINAGORA
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any
 * later version, provided you comply with the Additional Terms applicable for
 * LinShare software by Linagora pursuant to Section 7 of the GNU Affero General
 * Public License, subsections (b), (c), and (e), pursuant to which you must
 * notably (i) retain the display of the “LinShare™” trademark/logo at the top
 * of the interface window, the display of the “You are using the Open Source
 * and free version of LinShare™, powered by Linagora © 2009–2014. Contribute to
 * Linshare R&D by subscribing to an Enterprise offer!” infobox and in the
 * e-mails sent with the Program, (ii) retain all hypertext links between
 * LinShare and linshare.org, between linagora.com and Linagora, and (iii)
 * refrain from infringing Linagora intellectual property rights over its
 * trademarks and commercial brands. Other Additional Terms apply, see
 * <http://www.linagora.com/licenses/> for more details.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Affero General Public License and
 * its applicable Additional Terms for LinShare along with this program. If not,
 * see <http://www.gnu.org/licenses/> for the GNU Affero General Public License
 * version 3 and <http://www.linagora.com/licenses/> for the Additional Terms
 * applicable to LinShare software.
 */
package org.linagora.linshare.core.utils;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.linagora.linshare.core.exception.TechnicalErrorCode;
import org.linagora.linshare.core.exception.TechnicalException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DocumentUtils {

    private static final Logger logger = LoggerFactory.getLogger(DocumentUtils.class);

    public File getTempFile(InputStream stream, String fileName) {
        // Copy the input stream to a temporary file for safe use
        File tempFile = null;
        BufferedOutputStream bof = null;

        //extract extension
        int splitIdx = fileName.lastIndexOf('.');
        String extension = "";
        if (splitIdx > -1) {
            extension = fileName.substring(splitIdx, fileName.length());
        }
        logger.debug("Found extension :" + extension);
        try {
            //we need to keep the extension for the thumbnail generator
            tempFile = File.createTempFile("linshare", extension);

            tempFile.deleteOnExit();
            if (logger.isDebugEnabled()) {
                logger.debug("createTempFile:" + tempFile);
            }
            bof = new BufferedOutputStream(new FileOutputStream(tempFile));
            // Transfer bytes from in to out
            byte[] buf = new byte[64 * 4096]; // 256Kio
            int len;
            long total_len = 0;

            while ((len = stream.read(buf)) > 0) {
                bof.write(buf, 0, len);
                total_len += len;
                logger.debug("data read : " + total_len);
            }
            bof.flush();

        } catch (IOException e) {
            if (tempFile != null && tempFile.exists())
                tempFile.delete();
            throw new TechnicalException(TechnicalErrorCode.GENERIC, "couldn't create a temporary file");
        }
        finally {
            if (stream != null) {
                try {
                    stream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (bof != null) {
                try {
                    bof.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return tempFile;
    }
}
