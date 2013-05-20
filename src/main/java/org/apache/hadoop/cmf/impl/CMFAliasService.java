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
package org.apache.hadoop.cmf.impl;

import java.security.Key;
import java.security.KeyStore;
import java.util.Map;
import java.util.Random;

import org.apache.hadoop.cmf.AliasService;
import org.apache.hadoop.cmf.KeystoreService;
import org.apache.hadoop.cmf.KeystoreServiceException;

public class CMFAliasService implements AliasService {

  protected char[] chars = { 'a', 'b', 'c', 'd', 'e', 'f', 'g',
  'h', 'j', 'k', 'm', 'n', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w',
  'x', 'y', 'z', 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'J', 'K',
  'M', 'N', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z',
  '2', '3', '4', '5', '6', '7', '8', '9',};

  private KeystoreService keystoreService;

  @Override
  public char[] getPasswordFromAlias(String alias) {
    return getPasswordFromAlias(alias, false);
  }

  @Override
  public char[] getPasswordFromAlias(String alias, boolean generate) {
    char[] credential = keystoreService.getCredential(alias);
    if (credential == null) {
      if (generate) {
        generateAlias(alias);
      }
    }
    return credential;
  }

  @Override
  public char[] getPasswordFromConfigValue(String configValue) {
	char[] credential = null;
	if (configValue.startsWith("${ALIAS=") && configValue.endsWith("}")){
	    String pswdAlias = configValue.substring(8, configValue.length()-1);
		credential = getPasswordFromAlias(pswdAlias);
	}
	else {
		// backward compatibile clear text configValue
		credential = configValue.toCharArray();
	}
    return credential;
  }

  private String generatePassword(int length) {
    StringBuffer sb = new StringBuffer();
    Random r = new Random();
    for (int i = 0; i < length; i++) {
      sb.append(chars[r.nextInt(chars.length)]);
    }
    return sb.toString();
  }
  
  public void setKeystoreService(KeystoreService ks) {
    this.keystoreService = ks;
  }

  @Override
  public void generateAlias(String alias) {
    String passwordString = generatePassword(16);
    addAlias(alias, passwordString);
  }

  @Override
  public void addAlias(String alias, String value) {
    keystoreService.addCredential(alias, value);
  }
}
