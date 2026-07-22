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

package org.cfksoftware.crypto;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.cfksoftware.common.ByteUtils;
import org.cfksoftware.common.StreamUtils;
import org.cfksoftware.common.StringUtils;
import org.cfksoftware.crypto.common.CryptoException;
import org.cfksoftware.crypto.common.CryptoUtils;

public class Hasher {
  public static Hasher sha256() throws CryptoException {
    return Hasher.getInstance("sha256");
  }

  public static Hasher sha512() throws CryptoException {
    return Hasher.getInstance("sha512");
  }

  public static Hasher getInstance(String algorithm) throws CryptoException {
    try {
      return new Hasher(algorithm);
    } catch (NoSuchAlgorithmException e) {
      throw new CryptoException(e);
    }
  }

  private final MessageDigest messageDigest;

  private Hasher(String algorithm) throws NoSuchAlgorithmException {
    this.messageDigest = MessageDigest.getInstance(algorithm, CryptoUtils.getBouncyCastleProvider());
  }

  public String hash(String str) throws CryptoException {
    return this.hash(StringUtils.toBytes(str));
  }

  public String hash(byte[] bytes) throws CryptoException {
    try (ByteArrayInputStream in = new ByteArrayInputStream(bytes)) {
      byte[] hash = this.hash(in);
      return ByteUtils.toHex(hash);
    } catch (IOException e) {
      throw new CryptoException(e);
    }
  }

  public String hash(File file) throws CryptoException {
    try (FileInputStream in = new FileInputStream(file)) {
      byte[] hash = this.hash(in);
      return ByteUtils.toHex(hash);
    } catch (IOException e) {
      throw new CryptoException(e);
    }
  }

  public synchronized byte[] hash(InputStream in) throws CryptoException {
    this.messageDigest.reset();

    try (DigestInputStream din = new DigestInputStream(in, this.messageDigest)) {
      StreamUtils.copy(din, null);
      return din.getMessageDigest().digest();
    } catch (IOException e) {
      throw new CryptoException(e);
    }

  }
}
