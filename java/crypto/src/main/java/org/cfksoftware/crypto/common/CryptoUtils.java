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

package org.cfksoftware.crypto.common;

import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.Provider;
import java.security.Provider.Service;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Security;
import java.security.cert.X509Certificate;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.jsse.provider.BouncyCastleJsseProvider;
import org.bouncycastle.pqc.jcajce.provider.BouncyCastlePQCProvider;
import org.cfksoftware.common.logging.CfkLogger;

public class CryptoUtils {
  static {
    CryptoUtils.loadBouncyCastleProviders();
  }

  private CryptoUtils() {
    /* This utility class should not be instantiated */
  }

  private static final SecureRandom sRand = new SecureRandom();

  public static synchronized byte[] secureRandom(int numBytes) {
    byte[] bytes = new byte[numBytes];
    CryptoUtils.secureRandom(bytes);
    return bytes;
  }

  public static synchronized void secureRandom(byte[] bytes) {
    if (bytes != null) {
      CryptoUtils.sRand.nextBytes(bytes);
    }
  }

  public static void loadBouncyCastleProviders() {
    CryptoUtils.getBouncyCastleProvider();
    CryptoUtils.getBouncyCastlePqcProvider();
    CryptoUtils.getBouncyCastleJseeProvider();
  }

  public static Map<String, Set<String>> getProviderInfo(Provider provider) {
    Map<String, Set<String>> providerInfo = new TreeMap<>();

    if (provider != null) {
      for (Service service : provider.getServices()) {
        if (service != null) {
          String serviceType = service.getType();
          String serviceAlgorithm = service.getAlgorithm();
          Set<String> algorithms;

          if (providerInfo.containsKey(serviceType)) {
            algorithms = providerInfo.get(serviceType);
          } else {
            algorithms = new TreeSet<>();
            providerInfo.put(serviceType, algorithms);
          }

          algorithms.add(serviceAlgorithm);
        }
      }
    }

    return providerInfo;
  }

  public static void printProviderInfo(Provider provider) {
    if (provider == null) {
      CfkLogger.warn("No provider provided...");
    } else {
      CfkLogger.info("################################################################################");
      CfkLogger.info("Provider: %s %s", provider.getName(), provider.getVersionStr());

      Map<String, Set<String>> providerInfo = CryptoUtils.getProviderInfo(provider);
      for (Entry<String, Set<String>> entry : providerInfo.entrySet()) {
        String serviceType = entry.getKey();
        CfkLogger.info("    %s: ", serviceType);

        for (String serviceAlgorithm : entry.getValue()) {
          CfkLogger.info("        %s", serviceAlgorithm);
        }
      }

      CfkLogger.info("################################################################################");
    }
  }

  public static void printProviderInfos() {
    for (Provider provider : Security.getProviders()) {
      CryptoUtils.printProviderInfo(provider);
    }
  }

  public static Provider getBouncyCastleProvider() {
    Provider provider = Security.getProvider("BC");

    if (provider == null) {
      provider = new BouncyCastleProvider();
      Security.insertProviderAt(provider, 0);
    }

    return provider;
  }

  public static Provider getBouncyCastlePqcProvider() {
    Provider provider = Security.getProvider("BCPQC");

    if (provider == null) {
      provider = new BouncyCastlePQCProvider();
      Security.insertProviderAt(provider, 0);
    }

    return provider;
  }

  public static Provider getBouncyCastleJseeProvider() {
    Provider provider = Security.getProvider("BCJSSE");

    if (provider == null) {
      provider = new BouncyCastleJsseProvider();
      Security.insertProviderAt(provider, 0);
    }

    return provider;
  }

  public static void isolateBouncyCastleProviders() {
    Set<String> providersToRemove = new TreeSet<String>();
    for (Provider provider : Security.getProviders()) {
      if (!provider.getName().startsWith("BC")) {
        providersToRemove.add(provider.getName());
      }
    }

    for (String providerName : providersToRemove) {
      Security.removeProvider(providerName);
    }
  }

  public static byte[] wrapKey(SecretKey keyToWrap, X509Certificate certReceiver) throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException {
    return CryptoUtils.wrapKey(keyToWrap, certReceiver.getPublicKey());
  }

  public static byte[] wrapKey(SecretKey keyToWrap, PublicKey keyReceiver) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
    Cipher cipher = Cipher.getInstance(keyReceiver.getAlgorithm(), CryptoUtils.getBouncyCastleProvider());
    cipher.init(Cipher.WRAP_MODE, keyReceiver);
    return cipher.wrap(keyToWrap);
  }

  public static SecretKey unwrapKey(byte[] keyWrapped, String keyWrappedAlgorithm, PrivateKey keyReceiver) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException {
    Cipher cipher = Cipher.getInstance(keyReceiver.getAlgorithm(), CryptoUtils.getBouncyCastleProvider());
    cipher.init(Cipher.UNWRAP_MODE, keyReceiver);
    return (SecretKey) cipher.unwrap(keyWrapped, keyWrappedAlgorithm, Cipher.SECRET_KEY);
  }

  public static SecretKey pbeTwoFishKey(char[] password, byte[] salt, int iterations, int keyLength) throws NoSuchAlgorithmException, InvalidKeySpecException {
    return CryptoUtils.pbeSecretKey(password, salt, iterations, keyLength, "twofish");
  }

  public static SecretKey pbeSerpentKey(char[] password, byte[] salt, int iterations, int keyLength) throws NoSuchAlgorithmException, InvalidKeySpecException {
    return CryptoUtils.pbeSecretKey(password, salt, iterations, keyLength, "serpent");
  }

  public static SecretKey pbeAesKey(char[] password, byte[] salt, int iterations, int keyLength) throws NoSuchAlgorithmException, InvalidKeySpecException {
    return CryptoUtils.pbeSecretKey(password, salt, iterations, keyLength, "aes");
  }

  private static SecretKey pbeSecretKey(char[] password, byte[] salt, int iterations, int keyLength, String keyAlgorithm) throws NoSuchAlgorithmException, InvalidKeySpecException {
    SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA512", CryptoUtils.getBouncyCastleProvider());
    KeySpec spec = new PBEKeySpec(password, salt, iterations, keyLength);
    return new SecretKeySpec(factory.generateSecret(spec).getEncoded(), keyAlgorithm);
  }

  public static SecretKey randomTwoFishKey(int length) throws NoSuchAlgorithmException {
    return CryptoUtils.randomSecretKey("twofish", length);
  }

  public static SecretKey randomSerpentKey(int length) throws NoSuchAlgorithmException {
    return CryptoUtils.randomSecretKey("serpent", length);
  }

  public static SecretKey randomAesKey(int length) throws NoSuchAlgorithmException {
    return CryptoUtils.randomSecretKey("aes", length);
  }

  private static SecretKey randomSecretKey(String algorithm, int length) throws NoSuchAlgorithmException {
    KeyGenerator keyGen = KeyGenerator.getInstance(algorithm, CryptoUtils.getBouncyCastleProvider());
    keyGen.init(length);
    return keyGen.generateKey();
  }

  public static KeyPair randomEcKeyPair(int length) throws NoSuchAlgorithmException {
    return CryptoUtils.randomKeyPair("ec", length);
  }

  public static KeyPair randomRsaKeyPair(int length) throws NoSuchAlgorithmException {
    return CryptoUtils.randomKeyPair("rsa", length);
  }

  private static KeyPair randomKeyPair(String algorithm, int length) throws NoSuchAlgorithmException {
    KeyPairGenerator keyPairGen = KeyPairGenerator.getInstance(algorithm, CryptoUtils.getBouncyCastleProvider());
    keyPairGen.initialize(length);
    return keyPairGen.generateKeyPair();
  }
}
