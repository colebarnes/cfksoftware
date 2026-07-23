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
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

import org.cfksoftware.common.ByteUtils;
import org.cfksoftware.common.StreamUtils;
import org.cfksoftware.common.StringUtils;
import org.cfksoftware.common.logging.CfkLogger;

public abstract class ZipReader implements Closeable {
  public static ZipReader getInstance(final File file) throws IOException {
    return new ZipFileReader(file);
  }

  public static ZipReader getInstance(final byte[] bytes) {
    return new ZipMemoryReader(bytes);
  }

  public long readLong(final String entryName) throws IOException {
    byte[] bytes = this.readBytes(entryName);
    return ByteUtils.toLong(bytes);
  }

  public int readInt(final String entryName) throws IOException {
    byte[] bytes = this.readBytes(entryName);
    return ByteUtils.toInt(bytes);
  }

  public String readString(final String entryName) throws IOException {
    byte[] bytes = this.readBytes(entryName);
    return StringUtils.fromBytes(bytes);
  }

  public byte[] readBytes(final String entryName) throws IOException {
    try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
      this.readEntry(entryName, out);
      return out.toByteArray();
    }
  }

  public void readEntry(final String entryName, final OutputStream out) throws IOException {
    try (InputStream in = this.getInputStream(entryName)) {
      StreamUtils.copy(in, out);
    }
  }

  public abstract Set<String> entryNames();

  public abstract InputStream getInputStream(final String entryName) throws IOException;

  private static class ZipMemoryReader extends ZipReader {
    private byte[] bytes;

    public ZipMemoryReader(final byte[] bytes) {
      if (bytes == null) {
        throw new NullPointerException("'bytes' is null.");
      }

      this.bytes = Arrays.copyOf(bytes, bytes.length);
    }

    @Override
    public InputStream getInputStream(final String entryName) throws IOException {
      if (entryName == null) {
        throw new NullPointerException("'entryName' is null.");
      }

      ZipInputStream in = new ZipInputStream(new ByteArrayInputStream(this.bytes));
      ZipEntry zipEntry;

      while ((zipEntry = in.getNextEntry()) != null) {
        if (zipEntry.getName().equals(entryName)) {
          break;
        }
      }

      if (zipEntry == null) {
        throw new IOException("The specified entry does not exist.");
      }

      return in;
    }

    @Override
    public void close() throws IOException {
      this.bytes = null;
    }

    @Override
    public Set<String> entryNames() {
      Set<String> entries = new TreeSet<>();

      try (ZipInputStream in = new ZipInputStream(new ByteArrayInputStream(this.bytes))) {
        ZipEntry zipEntry;

        while ((zipEntry = in.getNextEntry()) != null) {
          entries.add(zipEntry.getName());
        }
      } catch (IOException e) {
        CfkLogger.warn(e);
      }

      return entries;
    }
  }

  private static class ZipFileReader extends ZipReader {
    private ZipFile zipFile;

    public ZipFileReader(final File file) throws IOException {
      if (file == null) {
        throw new NullPointerException("'file' is null.");
      }

      if (!file.exists()) {
        throw new IllegalArgumentException("'file' does not exist.");
      }

      this.zipFile = new ZipFile(file);
    }

    @Override
    public InputStream getInputStream(final String entryName) throws IOException {
      if (entryName == null) {
        throw new NullPointerException("'entryName' is null.");
      }

      ZipEntry zipEntry = this.zipFile.getEntry(entryName);

      if (zipEntry == null) {
        throw new IOException("The specified entry does not exist.");
      }

      return this.zipFile.getInputStream(zipEntry);
    }

    @Override
    public void close() throws IOException {
      this.zipFile.close();
    }

    @Override
    public Set<String> entryNames() {
      Set<String> entries = new TreeSet<>();

      @SuppressWarnings("unchecked")
      Iterator<ZipEntry> it = (Iterator<ZipEntry>) this.zipFile.entries().asIterator();
      while (it.hasNext()) {
        entries.add(it.next().getName());
      }

      return entries;
    }
  }
}
