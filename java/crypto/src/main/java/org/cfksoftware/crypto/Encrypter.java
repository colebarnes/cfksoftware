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
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import java.security.spec.InvalidKeySpecException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;

import org.cfksoftware.common.ByteUtils;
import org.cfksoftware.common.zip.ZipWriter;
import org.cfksoftware.crypto.common.CryptoException;
import org.cfksoftware.crypto.common.CryptoUtils;

public class Encrypter implements Closeable {
  public static final String CURRENT_VERSION = "0.0.1";

  public static final String PARAM_VERSION = "version";
  public static final String PARAM_METHOD = "method";

  private static final String PARAM_PBE_PREFIX = "pbe";
  public static final String PARAM_PBE_SALT = Encrypter.PARAM_PBE_PREFIX + "/salt";
  public static final String PARAM_PBE_ITERATIONS = Encrypter.PARAM_PBE_PREFIX + "/method";
  public static final String PARAM_PBE_ALGORITHM = Encrypter.PARAM_PBE_PREFIX + "/algorithm";

  private static final String PARAM_PKI_PREFIX = "pki";
  public static final String PARAM_PKI_RECIPIENT = Encrypter.PARAM_PKI_PREFIX + "/recipeint";
  public static final String PARAM_PKI_WRAPPED_KEY = Encrypter.PARAM_PKI_PREFIX + "/wrapped_key";

  public static final String PARAM_KEY_SIZE = "key_size";
  public static final String PARAM_IV = "iv";
  public static final String PARAM_CIPHER_TEXT = "cipher_text";

  public static final String PARAM_CIPHER_ALGORITHM = "cipher/algorithm";
  public static final String PARAM_CIPHER_MODE = "cipher/mode";
  public static final String PARAM_CIPHER_PADDING = "cipher/padding";

  public static final String METHOD_PBE = Encrypter.PARAM_PBE_PREFIX;
  public static final String METHOD_PKI = Encrypter.PARAM_PKI_PREFIX;

  public static Encrypter serpent256() {
    return new Encrypter("serpent", 256);
  }

  public static Encrypter aes256() {
    return new Encrypter("aes", 256);
  }

  public static Encrypter twofish256() {
    return new Encrypter("twofish", 256);
  }

  private String algorithm;
  private final String mode;
  private final String padding;
  private int keySize;

  private Encrypter(String algorithm, int keySize) {
    this.algorithm = algorithm;
    this.mode = "gcm";
    this.padding = "nopadding";
    this.keySize = keySize;
  }

  public String getCipherAlgorithm() {
    return this.algorithm;
  }

  public String getCipherMode() {
    return this.mode;
  }

  public String getCipherPadding() {
    return this.padding;
  }

  public int getKeySize() {
    return this.keySize;
  }

  public String getCipherTransform() {
    return String.format("%s/%s/%s", this.getCipherAlgorithm(), this.getCipherMode(), this.getCipherPadding());
  }

  public byte[] encrypt(byte[] plainText, char[] password) throws CryptoException {
    try (ByteArrayInputStream bais = new ByteArrayInputStream(plainText); ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
      this.encrypt(bais, baos, password);
      return baos.toByteArray();
    } catch (IOException e) {
      throw new CryptoException(CryptoException.ERROR_UNKNOWN, "Error encrypting data.", e);
    }
  }

  public File encrypt(File plainText, char[] password) throws CryptoException {
    File cipherText = new File(plainText.getAbsolutePath() + ".enc");
    try (FileInputStream fis = new FileInputStream(plainText); FileOutputStream fos = new FileOutputStream(cipherText)) {
      this.encrypt(fis, fos, password);
      return cipherText;
    } catch (IOException e) {
      throw new CryptoException(CryptoException.ERROR_UNKNOWN, "Error encrypting file.", e);
    }
  }

  private void encrypt(InputStream in, OutputStream out, char[] password) throws CryptoException {
    try (ZipWriter writer = ZipWriter.getInstance(out)) {
      writer.write(Encrypter.PARAM_METHOD, Encrypter.METHOD_PBE);

      // TODO: do not hard code pbe salt length
      byte[] salt = ByteUtils.random(64);
      writer.write(Encrypter.PARAM_PBE_SALT, salt);

      // TODO: do not hard code pbe iteration count
      int iterations = 10000;
      writer.write(Encrypter.PARAM_PBE_ITERATIONS, iterations);

      // TODO: do not hard code pbe algorithm ...
      writer.write(Encrypter.PARAM_PBE_ALGORITHM, "PBKDF2WithHmacSHA256");
      SecretKey key = CryptoUtils.pbeSecretKey(password, salt, iterations, this.getKeySize(), this.getCipherAlgorithm());
      encrypt(in, writer, key);
    } catch (NoSuchAlgorithmException | InvalidKeySpecException | IOException e) {
      throw new CryptoException(CryptoException.ERROR_UNKNOWN, "Error encrypting data.", e);
    }
  }

  public byte[] encrypt(byte[] plainText, X509Certificate recipient) throws CryptoException {
    try (ByteArrayInputStream bais = new ByteArrayInputStream(plainText); ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
      this.encrypt(bais, baos, recipient);
      return baos.toByteArray();
    } catch (IOException e) {
      throw new CryptoException(CryptoException.ERROR_UNKNOWN, "Error encrypting data.", e);
    }
  }

  public File encrypt(File plainText, X509Certificate recipient) throws CryptoException {
    File cipherText = new File(plainText.getAbsolutePath() + ".enc");
    try (FileInputStream fis = new FileInputStream(plainText); FileOutputStream fos = new FileOutputStream(cipherText)) {
      this.encrypt(fis, fos, recipient);
      return cipherText;
    } catch (IOException e) {
      throw new CryptoException(CryptoException.ERROR_UNKNOWN, "Error encrypting file.", e);
    }
  }

  private void encrypt(InputStream in, OutputStream out, X509Certificate recipient) throws CryptoException {
    try (ZipWriter writer = ZipWriter.getInstance(out)) {
      writer.write(Encrypter.PARAM_METHOD, Encrypter.METHOD_PKI);
      SecretKey key = CryptoUtils.randomSecretKey(this.getCipherAlgorithm(), this.getKeySize());
      encrypt(in, writer, key);

      byte[] wrappedKey = CryptoUtils.wrapKey(key, recipient.getPublicKey());
      writer.write(Encrypter.PARAM_PKI_WRAPPED_KEY, wrappedKey);
      writer.write(Encrypter.PARAM_PKI_RECIPIENT, recipient.getEncoded());
    } catch (IOException | CertificateEncodingException | InvalidKeyException | NoSuchAlgorithmException | NoSuchPaddingException | IllegalBlockSizeException | BadPaddingException e) {
      throw new CryptoException(CryptoException.ERROR_UNKNOWN, "Error encrypting data.", e);
    }
  }

  private void encrypt(InputStream in, ZipWriter writer, SecretKey key) throws CryptoException {
    try {
      writer.write(Encrypter.PARAM_VERSION, Encrypter.CURRENT_VERSION);
      writer.write(Encrypter.PARAM_KEY_SIZE, this.getKeySize());
      writer.write(Encrypter.PARAM_CIPHER_ALGORITHM, this.getCipherAlgorithm());
      writer.write(Encrypter.PARAM_CIPHER_MODE, this.getCipherMode());
      writer.write(Encrypter.PARAM_CIPHER_PADDING, this.getCipherPadding());

      // TODO: do not hard code iv size ...
      byte[] iv = CryptoUtils.secureRandom(12);
      writer.write(Encrypter.PARAM_IV, iv);
      GCMParameterSpec params = new GCMParameterSpec(128, iv);

      Cipher cipher = Cipher.getInstance(this.getCipherTransform(), CryptoUtils.getBouncyCastleProvider());
      cipher.init(Cipher.ENCRYPT_MODE, key, params);

      try (CipherInputStream cin = new CipherInputStream(in, cipher)) {
        writer.write(Encrypter.PARAM_CIPHER_TEXT, cin);
      }
    } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | InvalidAlgorithmParameterException | IOException e) {
      throw new CryptoException(CryptoException.ERROR_UNKNOWN, "Error encrypting file", e);
    }
  }

  @Override
  public void close() throws IOException {
    // nothing to do?
  }
}
