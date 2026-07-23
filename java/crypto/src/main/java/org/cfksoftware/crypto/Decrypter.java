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

import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.spec.InvalidKeySpecException;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;

import org.cfksoftware.common.StreamUtils;
import org.cfksoftware.common.zip.ZipReader;
import org.cfksoftware.crypto.common.CryptoException;
import org.cfksoftware.crypto.common.CryptoUtils;

public class Decrypter implements Closeable {
  public static Decrypter getInstance(byte[] cipherText) throws IOException {
    ZipReader reader = ZipReader.getInstance(cipherText);
    return new Decrypter(reader);
  }

  public static Decrypter getInstance(File cipherText) throws IOException {
    ZipReader reader = ZipReader.getInstance(cipherText);
    return new Decrypter(reader);
  }

  private ZipReader reader;

  private Decrypter(ZipReader reader) {
    this.reader = reader;
  }

  public byte[] decryptToBytes(char[] password) throws CryptoException {
    try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
      this.decrypt(baos, password);
      return baos.toByteArray();
    } catch (IOException e) {
      throw new CryptoException(CryptoException.ERROR_UNKNOWN, "Error decrypting data.", e);
    }
  }

  public void decryptToFile(File plainText, char[] password) throws CryptoException {
    try (FileOutputStream fos = new FileOutputStream(plainText)) {
      this.decrypt(fos, password);
    } catch (IOException e) {
      throw new CryptoException(CryptoException.ERROR_UNKNOWN, "Error decrypting data.", e);
    }
  }

  private void decrypt(OutputStream out, char[] password) throws CryptoException {
    try {
      String version = this.reader.readString(Encrypter.PARAM_VERSION);
      String method = this.reader.readString(Encrypter.PARAM_METHOD);

      byte[] salt = this.reader.readBytes(Encrypter.PARAM_PBE_SALT);
      int iterations = this.reader.readInt(Encrypter.PARAM_PBE_ITERATIONS);
      String algorithm = this.reader.readString(Encrypter.PARAM_PBE_ALGORITHM);

      String cipherAlgorithm = this.reader.readString(Encrypter.PARAM_CIPHER_ALGORITHM);
      int keySize = this.reader.readInt(Encrypter.PARAM_KEY_SIZE);

      // TODO: do not hard code pbe algorithm ...
      SecretKey key = CryptoUtils.pbeSecretKey(password, salt, iterations, keySize, cipherAlgorithm);
      this.decrypt(out, key);
    } catch (IOException | InvalidKeySpecException | NoSuchAlgorithmException e) {
      throw new CryptoException(CryptoException.ERROR_UNKNOWN, "Error decrypting data.", e);
    }
  }

  public byte[] decryptToBytes(PrivateKey privateKey) throws CryptoException {
    try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
      this.decrypt(baos, privateKey);
      return baos.toByteArray();
    } catch (IOException e) {
      throw new CryptoException(CryptoException.ERROR_UNKNOWN, "Error decrypting data.", e);
    }
  }

  public void decryptToFile(File plainText, PrivateKey privateKey) throws CryptoException {
    try (FileOutputStream fos = new FileOutputStream(plainText)) {
      this.decrypt(fos, privateKey);
    } catch (IOException e) {
      throw new CryptoException(CryptoException.ERROR_UNKNOWN, "Error decrypting data.", e);
    }
  }

  private void decrypt(OutputStream out, PrivateKey privateKey) throws CryptoException {
    try {
      String version = this.reader.readString(Encrypter.PARAM_VERSION);
      String method = this.reader.readString(Encrypter.PARAM_METHOD);

      byte[] wrappedKey = this.reader.readBytes(Encrypter.PARAM_PKI_WRAPPED_KEY);
      String cipherAlgorithm = this.reader.readString(Encrypter.PARAM_CIPHER_ALGORITHM);

      // TODO: do not hard code pbe algorithm ...
      SecretKey key = CryptoUtils.unwrapKey(wrappedKey, cipherAlgorithm, privateKey);
      this.decrypt(out, key);
    } catch (IOException | InvalidKeyException | NoSuchAlgorithmException | NoSuchPaddingException e) {
      throw new CryptoException(CryptoException.ERROR_UNKNOWN, "Error decrypting data.", e);
    }
  }

  private String getCipherTransform() throws IOException {
    String alg = this.reader.readString(Encrypter.PARAM_CIPHER_ALGORITHM);
    String mode = this.reader.readString(Encrypter.PARAM_CIPHER_MODE);
    String pad = this.reader.readString(Encrypter.PARAM_CIPHER_PADDING);

    return String.format("%s/%s/%s", alg, mode, pad);
  }

  private void decrypt(OutputStream out, SecretKey key) throws CryptoException {
    try {
      byte[] iv = this.reader.readBytes(Encrypter.PARAM_IV);
      GCMParameterSpec params = new GCMParameterSpec(128, iv);

      Cipher cipher = Cipher.getInstance(this.getCipherTransform(), CryptoUtils.getBouncyCastleProvider());
      cipher.init(Cipher.DECRYPT_MODE, key, params);

      try (CipherInputStream cin = new CipherInputStream(this.reader.getInputStream(Encrypter.PARAM_CIPHER_TEXT), cipher)) {
        StreamUtils.copy(cin, out);
        out.flush();
      }
    } catch (IOException | InvalidKeyException | InvalidAlgorithmParameterException | NoSuchAlgorithmException | NoSuchPaddingException e) {
      throw new CryptoException(CryptoException.ERROR_UNKNOWN, "Error decrypting data.", e);
    }
  }

  @Override
  public void close() throws IOException {
    this.reader.close();
  }
}
