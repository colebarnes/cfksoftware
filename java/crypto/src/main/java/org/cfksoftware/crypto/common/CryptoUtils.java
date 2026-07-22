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

import java.security.Provider;
import java.security.Provider.Service;
import java.security.SecureRandom;
import java.security.Security;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

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
}
