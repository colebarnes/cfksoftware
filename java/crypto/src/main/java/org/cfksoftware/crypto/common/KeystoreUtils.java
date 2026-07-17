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

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.security.Key;
import java.security.KeyStore;
import java.security.KeyStore.Entry.Attribute;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Date;
import java.util.Enumeration;
import java.util.Set;

import org.cfksoftware.common.DateUtils;
import org.cfksoftware.common.FileUtils;
import org.cfksoftware.common.logging.CfkLogger;

public class KeystoreUtils {
  public static final int JKS = 0;
  public static final int PKCS12 = 1;
  public static final int BCFKS = 2;

  static {
    CryptoUtils.loadBouncyCastleProviders();
  }

  public static KeyStore openPkcs12(File file, char[] password) throws FileNotFoundException, KeyStoreException, NoSuchProviderException, NoSuchAlgorithmException, CertificateException, IOException {
    return KeystoreUtils.openKeystore(file, password, KeystoreUtils.PKCS12);
  }

  public static KeyStore openJks(File file, char[] password) throws FileNotFoundException, KeyStoreException, NoSuchProviderException, NoSuchAlgorithmException, CertificateException, IOException {
    return KeystoreUtils.openKeystore(file, password, KeystoreUtils.JKS);
  }

  public static KeyStore openBcfks(File file, char[] password) throws FileNotFoundException, KeyStoreException, NoSuchProviderException, NoSuchAlgorithmException, CertificateException, IOException {
    return KeystoreUtils.openKeystore(file, password, KeystoreUtils.BCFKS);
  }

  public static KeyStore openKeystore(File file, char[] password, int keystoreType) throws FileNotFoundException, IOException, KeyStoreException, NoSuchProviderException, NoSuchAlgorithmException, CertificateException {
    byte[] bytes = FileUtils.readFile(file);
    return openKeystore(bytes, password, keystoreType);
  }

  public static KeyStore openKeystore(byte[] bytes, char[] password, int keystoreType) throws KeyStoreException, NoSuchProviderException, NoSuchAlgorithmException, CertificateException, IOException {
    try (ByteArrayInputStream bin = new ByteArrayInputStream(bytes)) {
      return KeystoreUtils.openKeystore(bin, password, keystoreType);
    }
  }

  public static KeyStore openKeystore(InputStream in, char[] password, int keystoreType) throws KeyStoreException, NoSuchProviderException, NoSuchAlgorithmException, CertificateException, IOException {
    String keyStoreType = KeystoreUtils.getKeystoreTypeName(keystoreType);
    String keyStoreProvider = KeystoreUtils.getProviderNameForType(keystoreType);
    KeyStore keystore;

    if (keyStoreProvider == null) {
      keystore = KeyStore.getInstance(keyStoreType);
    } else {
      keystore = KeyStore.getInstance(keyStoreType, keyStoreProvider);
    }

    keystore.load(in, password);
    return keystore;
  }

  private static String getProviderNameForType(int keystoreType) {
    switch (keystoreType) {
    case KeystoreUtils.JKS:
      return "SUN";
    case KeystoreUtils.PKCS12:
      return "SUN";
    case KeystoreUtils.BCFKS:
      return "BC";
    default:
      return null;
    }
  }

  private static String getKeystoreTypeName(int keystoreType) {
    switch (keystoreType) {
    case KeystoreUtils.JKS:
      return "JKS";
    case KeystoreUtils.PKCS12:
      return "PKCS12";
    case KeystoreUtils.BCFKS:
      return "BCFKS";
    default:
      return "UNKNOWN_KEYSTORE_TYPE";
    }
  }

  public static KeyStore emptyJks() throws CryptoException, NoSuchAlgorithmException, CertificateException, IOException {
    try {
      return KeystoreUtils.emptyKeystore(KeystoreUtils.JKS);
    } catch (KeyStoreException | NoSuchProviderException e) {
      throw new CryptoException(e);
    }
  }

  public static KeyStore emptyPkcs12() throws CryptoException, NoSuchAlgorithmException, CertificateException, IOException {
    try {
      return KeystoreUtils.emptyKeystore(KeystoreUtils.PKCS12);
    } catch (KeyStoreException | NoSuchProviderException e) {
      throw new CryptoException(e);
    }
  }

  public static KeyStore emptyBcfks() throws CryptoException, NoSuchAlgorithmException, CertificateException, IOException {
    try {
      return KeystoreUtils.emptyKeystore(KeystoreUtils.BCFKS);
    } catch (KeyStoreException | NoSuchProviderException e) {
      throw new CryptoException(e);
    }
  }

  private static KeyStore emptyKeystore(int keystoreType) throws KeyStoreException, NoSuchProviderException, NoSuchAlgorithmException, CertificateException, IOException {
    String type = KeystoreUtils.getKeystoreTypeName(keystoreType);
    String providerName = KeystoreUtils.getProviderNameForType(keystoreType);
    KeyStore ks;

    if (providerName == null) {
      ks = KeyStore.getInstance(type);
    } else {
      ks = KeyStore.getInstance(type, providerName);
    }

    ks.load(null);
    return ks;
  }

  public static void copy(KeyStore src, KeyStore dest, char[] password, boolean overwriteAliases) throws KeyStoreException, UnrecoverableKeyException, NoSuchAlgorithmException {
    // TODO: check input
    Enumeration<String> aliases = src.aliases();
    while (aliases.hasMoreElements()) {
      String alias = aliases.nextElement();

      if (dest.containsAlias(alias)) {
        if (!overwriteAliases) {
          // alias exist and not overwriting
          continue;
        } else {
          // overwriting existing alias
          dest.deleteEntry(alias);
        }
      }

      if (src.isCertificateEntry(alias)) {
        Certificate cert = src.getCertificate(alias);
        if (cert != null) {
          dest.setCertificateEntry(alias, cert);
        }
      } else /* is key entry */ {
        Certificate cert = src.getCertificate(alias);
        Key key = src.getKey(alias, password);
        Certificate[] chain = src.getCertificateChain(alias);

        if ((chain == null || chain.length < 1) && cert != null) {
          chain = new Certificate[] { cert };
        }

        dest.setKeyEntry(alias, key, password, chain);
      }
    }
  }

  public static void printInfo(KeyStore ks, char[] password) throws KeyStoreException, UnrecoverableKeyException, NoSuchAlgorithmException {
    if (ks != null) {
      CfkLogger.info("************************************************************");
      CfkLogger.info("* KeyStore Info:");
      CfkLogger.info("*   %s [s v%s]", ks.getType(), ks.getProvider().getName(), ks.getProvider().getVersionStr());

      Enumeration<String> aliases = ks.aliases();
      while (aliases.hasMoreElements()) {
        String alias = aliases.nextElement();
        CfkLogger.info("*");
        CfkLogger.info("*   Alias: %s [%s entry]", alias, ks.isKeyEntry(alias) ? "key" : "cert");

        Date date = ks.getCreationDate(alias);
        CfkLogger.info("*     Creation Date: %s", DateUtils.iso8601(date));

        Set<Attribute> attributes = ks.getAttributes(alias);
        if (attributes != null && attributes.size() > 0) {
          CfkLogger.info("*     Attributes:");
          for (Attribute attr : attributes) {
            CfkLogger.info("*       %s = %s", attr.getName(), attr.getValue());
          }
        }

        if (ks.isCertificateEntry(alias)) {
          Certificate cert = ks.getCertificate(alias);
          CfkLogger.info("*     Cert: %s", cert.toString());
        } else {
          Key key = ks.getKey(alias, password);
          CfkLogger.info("*     %s Key", key.getAlgorithm());

          Certificate[] chain = ks.getCertificateChain(alias);
          Certificate cert = ks.getCertificate(alias);

          if (chain != null && chain.length > 0) {
            CfkLogger.info("*     Cert chain:");
            for (Certificate crt : chain) {
              CfkLogger.info("*       -> %s:", X509Utils.toStringSimple((X509Certificate) crt));
            }
          } else if (cert != null) {
            // TODO: print cert
          } else {
            CfkLogger.info("*     [no cert info found]");
          }
        }
      }

      CfkLogger.info("************************************************************");
    }
  }
}
