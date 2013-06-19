/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.hadoop.security.cmf.impl;

import java.io.File;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.Key;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import org.apache.hadoop.security.cmf.KeystoreServiceException;
import org.apache.hadoop.security.cmf.KeystoreService;
import org.apache.hadoop.security.cmf.MasterService;


public class CMFKeystoreService extends BaseKeystoreService implements KeystoreService {

  private static final String SS_CERT_DN = "CN=hadoop,OU=Test,O=Hadoop,L=Test,ST=Test,C=US";
  private static final String CREDENTIALS_SUFFIX = "-credentials.jceks";

  private String serviceName = null;
  
  public CMFKeystoreService(String keystoreDir, String serviceName, MasterService ms)
      throws KeystoreServiceException {
	super(ms);
    this.serviceName = serviceName;
    this.keyStoreDir = keystoreDir + File.separator;
    File ksd = new File(this.keyStoreDir);
    if (!ksd.exists()) {
      ksd.mkdirs();
    }
  }

  public void createKeystore() throws KeystoreServiceException {
    String filename = keyStoreDir + serviceName + ".jks";
    createKeystore(filename, "JKS");
  }

  public KeyStore getKeystore() {
    final File  keyStoreFile = new File( keyStoreDir + serviceName  );
	try {
	  return getKeystore(keyStoreFile, "JKS");
	}
	catch (Exception e) {
	  e.printStackTrace();
	}
    return null;
  }
  
  public void addSelfSignedCert(String alias, char[] passphrase) throws KeystoreServiceException {
    KeyPairGenerator keyPairGenerator;
    try {
      keyPairGenerator = KeyPairGenerator.getInstance("RSA");
      keyPairGenerator.initialize(1024);  
      KeyPair KPair = keyPairGenerator.generateKeyPair();
      X509Certificate cert = generateCertificate(SS_CERT_DN, KPair, 365, "SHA1withRSA");

      KeyStore privateKS = getKeystore();
      privateKS.setKeyEntry(alias, KPair.getPrivate(),  
          passphrase,  
          new java.security.cert.Certificate[]{cert});  
      
      writeKeystoreToFile(privateKS, new File( keyStoreDir + serviceName  ));
    } catch (NoSuchAlgorithmException e) {
      throw new KeystoreServiceException("Unable to add self signed cert.",e);
    } catch (GeneralSecurityException e) {
      throw new KeystoreServiceException("Unable to add self signed cert.",e);
    } catch (IOException e) {
      throw new KeystoreServiceException("Unable to add self signed cert.",e);
    }  
  }
  
  public void createCredentialStore() throws KeystoreServiceException {
    String filename = keyStoreDir + serviceName + CREDENTIALS_SUFFIX;
    createKeystore(filename, "JCEKS");
  }

  public boolean isCredentialStoreAvailable() throws KeystoreServiceException {
    final File  keyStoreFile = new File( keyStoreDir + serviceName + CREDENTIALS_SUFFIX  );
    try {
      return isKeystoreAvailable(keyStoreFile, "JCEKS");
    } catch (KeyStoreException e) {
      throw new KeystoreServiceException(e);
    } catch (IOException e) {
      throw new KeystoreServiceException(e);
    }
  }

  public boolean isKeystoreAvailable() throws KeystoreServiceException {
    final File  keyStoreFile = new File( keyStoreDir + serviceName + ".jks" );
    try {
      return isKeystoreAvailable(keyStoreFile, "JKS");
    } catch (KeyStoreException e) {
      throw new KeystoreServiceException(e);
    } catch (IOException e) {
      throw new KeystoreServiceException(e);
    }
  }

  public Key getKey(String alias, char[] passphrase) throws KeystoreServiceException {
    Key key = null;
    KeyStore ks = getKeystore();
    if (ks != null) {
      try {
        key = ks.getKey(alias, passphrase);
      } catch (UnrecoverableKeyException e) {
        e.printStackTrace();
      } catch (KeyStoreException e) {
        e.printStackTrace();
      } catch (NoSuchAlgorithmException e) {
        e.printStackTrace();
      }
    }
    return key;
  }  
  
  public KeyStore getCredentialStore() {
    final File  keyStoreFile = new File( keyStoreDir + serviceName + CREDENTIALS_SUFFIX  );
    try {
      return getKeystore(keyStoreFile, "JCEKS");
    }
    catch (Exception e) {
      e.printStackTrace();
    }
    return null;
  }

  public void addCredential(String alias, String value) throws KeystoreServiceException {
    try {
	  KeyStore ks = getCredentialStore();
	  addCredential(alias, value, ks);
	  final File  keyStoreFile = new File( keyStoreDir + serviceName + CREDENTIALS_SUFFIX  );
      writeKeystoreToFile(ks, keyStoreFile);
    } catch (KeyStoreException e) {
      throw new KeystoreServiceException("Unable to add credential.",e);
    } catch (NoSuchAlgorithmException e) {
      throw new KeystoreServiceException("Unable to add credential.",e);
    } catch (CertificateException e) {
      throw new KeystoreServiceException("Unable to add credential.",e);
    } catch (IOException e) {
      throw new KeystoreServiceException("Unable to add credential.",e);
    }
  }

  public char[] getCredential(String alias) {
    char[] credential = null;
    KeyStore ks = getCredentialStore();
    credential = getCredential(alias, credential, ks);
    return credential;
  }

}
