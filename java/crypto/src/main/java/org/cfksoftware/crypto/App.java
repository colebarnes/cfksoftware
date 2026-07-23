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

import java.security.KeyPair;
import java.util.Arrays;

import javax.crypto.SecretKey;

import org.cfksoftware.common.StringUtils;
import org.cfksoftware.common.logging.CfkLogger;
import org.cfksoftware.crypto.common.CryptoUtils;

public class App {
  private static void testKeyWrapping() throws Exception {
    KeyPair keyPair = CryptoUtils.randomRsaKeyPair(4096);
    SecretKey secretKey = CryptoUtils.randomAesKey(256);
    CfkLogger.info("key len = %d", secretKey.getEncoded().length);
    byte[] keyWrapped = CryptoUtils.wrapKey(secretKey, keyPair.getPublic());
    SecretKey secretKeyUnwrapped = CryptoUtils.unwrapKey(keyWrapped, "aes", keyPair.getPrivate());
    CfkLogger.info("Keys match: %d", Arrays.compare(secretKey.getEncoded(), secretKeyUnwrapped.getEncoded()));
  }

  private static void testHashing() throws Exception {
    String str = "Hello world!";
    CfkLogger.info("String: %s", str);
    CfkLogger.info("  sha256: %s", Hasher.sha256().hash(str));
    CfkLogger.info("  sha512: %s", Hasher.sha512().hash(str));
  }

  private static void testEncryption() throws Exception {
    String str = "This is a super secret message!!!";
    char[] password = "p@s5w0rd123".toCharArray();

    Encrypter enc = Encrypter.serpent256();
    byte[] cipherText = enc.encrypt(StringUtils.toBytes(str), password);

    Decrypter dec = Decrypter.getInstance(cipherText);
    byte[] plainText = dec.decryptToBytes(password);

    CfkLogger.info("Decrypted message: %s", StringUtils.fromBytes(plainText));
  }

  public static void main(String[] args) {
    try {
      CfkLogger.info(App.class.getCanonicalName());
      App.testKeyWrapping();
      App.testHashing();
      App.testEncryption();
    } catch (Exception e) {
      CfkLogger.warn(e);
    }
  }
}
