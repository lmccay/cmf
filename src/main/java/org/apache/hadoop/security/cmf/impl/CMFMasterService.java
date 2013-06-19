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

import java.io.Console;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.FileUtils;
import org.apache.commons.net.ntp.TimeStamp;
import org.apache.hadoop.cmf.EncryptionResult;
import org.apache.hadoop.cmf.MasterServiceException;
import org.apache.hadoop.cmf.KeystoreServiceException;
import org.apache.hadoop.cmf.MasterService;

public class CMFMasterService implements MasterService {

  private static final String MASTER_PASSPHRASE = "masterpassphrase";
  private static final String MASTER_PERSISTENCE_TAG = "#1.0# " + TimeStamp.getCurrentTime().toDateString();
  protected char[] master = null;
  protected String serviceName = null;
  private AESEncryptor aes = new AESEncryptor(MASTER_PASSPHRASE);

  public CMFMasterService(String serviceName) {
    super();
    this.serviceName = serviceName;
  }

  public char[] getMasterSecret() {
    return this.master;
  }

  public void setupMasterSecret(String securityDir, boolean persisting) throws MasterServiceException {
    getMasterSecret(securityDir, serviceName + "-master", persisting);
  }

  protected void getMasterSecret(String securityDir, String filename, boolean persisting) throws MasterServiceException {
    File masterFile = new File(securityDir, filename);
    if (masterFile.exists()) {
      try {
        initializeFromMaster(masterFile);
      } catch (Exception e) {
        throw new MasterServiceException("Unable to load the persisted master secret.", e);
      }
    }
    else {
      displayWarning(persisting);
      promptUser();
      if(persisting) {
        persistMaster(master, masterFile);
      }
    }
  }

  protected void promptUser() {
    Console c = System.console();
    if (c == null) {
        System.err.println("No console.");
        System.exit(1);
    }
  
    boolean noMatch;
    do {
        char [] newPassword1 = c.readPassword("Enter master secret: ");
        char [] newPassword2 = c.readPassword("Enter master secret again: ");
        noMatch = ! Arrays.equals(newPassword1, newPassword2);
        if (noMatch) {
            c.format("Passwords don't match. Try again.%n");
        } else {
            this.master = Arrays.copyOf(newPassword1, newPassword1.length);
        }
        Arrays.fill(newPassword1, ' ');
        Arrays.fill(newPassword2, ' ');
    } while (noMatch);
  }

  protected void displayWarning(boolean persisting) {
    Console c = System.console();
    if (c == null) {
        System.err.println("No console.");
        System.exit(1);
    }
    if (persisting) {
      c.printf("***************************************************************************************************\n");
      c.printf("You have indicated that you would like to persist the master secret for this service instance.\n");
      c.printf("Be aware that this is less secure than manually entering the secret on startup.\n");
      c.printf("The persisted file will be encrypted and primarily protected through OS permissions.\n");
      c.printf("***************************************************************************************************\n");
    }
    else {
      c.printf("***************************************************************************************************\n");
      c.printf("Be aware that you will need to enter your master secret for future starts exactly as you do here.\n");
      c.printf("This secret is needed to access protected resources for the service process.\n");
      c.printf("The master secret must be protected, kept secret and not stored in clear text anywhere.\n");
      c.printf("***************************************************************************************************\n");
    }
  }

  private void persistMaster(char[] master, File masterFile) {
    EncryptionResult atom = encryptMaster(master);
    try {
      ArrayList<String> lines = new ArrayList<String>();
      lines.add(MASTER_PERSISTENCE_TAG);
      
      String line = Base64.encodeBase64String((
          Base64.encodeBase64String(atom.salt) + "::" + 
          Base64.encodeBase64String(atom.iv) + "::" + 
          Base64.encodeBase64String(atom.cipher)).getBytes("UTF8"));
      lines.add(line);
      FileUtils.writeLines(masterFile, "UTF8", lines);
      
      // restrict os permissions to only the user running this process
      chmod("600", masterFile);
    } catch (IOException e) {
      // TODO log appropriate message that the master secret has not been persisted
      e.printStackTrace();
    }
  }

  private EncryptionResult encryptMaster(char[] master) {
    // TODO Auto-generated method stub
    try {
      return aes.encrypt(new String(master));
    } catch (Exception e) {
		e.printStackTrace();
    }
    return null;
  }

  private void initializeFromMaster(File masterFile) throws Exception {
    try {
      List<String> lines = FileUtils.readLines(masterFile, "UTF8");
      String tag = lines.get(0);
      // TODO: log - if appropriate - at least at finest level
      System.out.println("Loading from persistent master: " + tag);
      String line = new String(Base64.decodeBase64(lines.get(1)));
      String[] parts = line.split("::");
      this.master = new String(aes.decrypt(Base64.decodeBase64(parts[0]), Base64.decodeBase64(parts[1]), Base64.decodeBase64(parts[2])), "UTF8").toCharArray();
    } catch (IOException e) {
      throw e;
    } catch (KeystoreServiceException e) {
      throw e;
    } catch (MasterServiceException e) {
      throw e;
    } catch (Exception e) {
      throw e;
    }
  }
  
  private void chmod(String args, File file) throws IOException, IllegalArgumentException {
    // TODO: move to Java 7 NIO support to add windows as well
    // TODO: look into the following for Windows: Runtime.getRuntime().exec("attrib -r myFile");
    if (isUnixEnv()) {
	  if (file == null || args == null)
		throw new IllegalArgumentException("Arguments and File parameters must not be null.");
      if (!file.exists()) 
        throw new IOException("fileNotFound");
      
      // " +" regular expression for 1 or more spaces
      final String[] argsString = args.split(" +");
      List<String> cmdList = new ArrayList<String>();
      cmdList.add("/bin/chmod");
      cmdList.addAll(Arrays.asList(argsString));
      cmdList.add(file.getAbsolutePath());
      new ProcessBuilder(cmdList).start();
    }
  }

  private boolean isUnixEnv() {
    return (File.separatorChar == '/');
  }

}