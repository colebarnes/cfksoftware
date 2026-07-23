/*
 * Copyright © 2026 cfksoftware@proton.me, https://cfksoftware.org
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the “Software”), to deal in
 * the Software without restriction, including without limitation the rights to use,
 * copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the
 * Software, and to permit persons to whom the Software is furnished to do so,
 * subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED “AS IS”, WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
 * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A
 * PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR  COPYRIGHT
 * HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
 * OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
 * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package org.cfksoftware.common.zip;

import java.io.ByteArrayInputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.cfksoftware.common.ByteUtils;
import org.cfksoftware.common.StreamUtils;
import org.cfksoftware.common.StringUtils;

public class ZipWriter implements Closeable {
  public static ZipWriter getInstance(File file) throws FileNotFoundException {
    // TODO: check input file
    return new ZipWriter(new FileOutputStream(file));
  }

  public static ZipWriter getInstance(OutputStream out) {
    return new ZipWriter(out);
  }

  private ZipOutputStream zipOut;

  private ZipWriter(final OutputStream out) {
    if (out == null) {
      throw new NullPointerException("'out' is null.");
    }

    this.zipOut = new ZipOutputStream(out);
  }

  public long write(final String entryName, final long number) throws IOException {
    return this.write(entryName, ByteUtils.fromLong(number));
  }

  public long write(final String entryName, final int number) throws IOException {
    return this.write(entryName, ByteUtils.fromInt(number));
  }

  public long write(final String entryName, final String str) throws IOException {
    if (str == null) {
      throw new NullPointerException("'str' is null.");
    }

    return this.write(entryName, StringUtils.toBytes(str));
  }

  public long write(final String entryName, final byte[] bytes) throws IOException {
    if (bytes == null) {
      throw new NullPointerException("'bytes' is null.");
    }

    try (InputStream in = new ByteArrayInputStream(bytes)) {
      return this.write(entryName, in);
    }
  }

  public long write(final String entryName, final InputStream in) throws IOException {
    if (StringUtils.isNullOrBlank(entryName)) {
      throw new NullPointerException("'entryName' is null or blank.");
    }

    if (in == null) {
      throw new NullPointerException("'in' is null.");
    }

    try {
      ZipEntry entry = new ZipEntry(entryName);
      this.zipOut.putNextEntry(entry);
      return StreamUtils.copy(in, this.zipOut);
    } finally {
      this.zipOut.closeEntry();
    }
  }

  @Override
  public void close() throws IOException {
    try {
      this.zipOut.flush();
    } finally {
      this.zipOut.close();
    }
  }
}
