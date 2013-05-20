package org.apache.hadoop.cmf;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import java.io.DataOutputStream;
import java.io.FileOutputStream;

import java.io.File;
import java.io.FileInputStream;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;

import org.apache.hadoop.cmf.*;
import org.apache.hadoop.cmf.impl.*;

/**
 * Unit test for simple App.
 */
public class CMFTest 
    extends TestCase
{
	MasterService ms = null;
	KeystoreService ks = null;
	AliasService as = null;
	
	
    /**
     * Create the test case
     *
     * @param testName name of the test case
     */
    public CMFTest( String testName )
    {
        super( testName );
    }

    /**
     * @return the suite of tests being tested
     */
    public static Test suite()
    {
        return new TestSuite( CMFTest.class );
    }

	public void setup() {
  		// init
		ms = new TestCMFMasterService("test");
		try {
			ks = new CMFKeystoreService(".", "test", ms);
		}
		catch(KeystoreServiceException kse) {
		}
		as = new CMFAliasService(ks);
		
		// start
		try {
			ms.setupMasterSecret(".", true);
			if (!ks.isCredentialStoreAvailable()) {
				ks.createCredentialStore();
			}
		}
		catch(KeystoreServiceException kse) {
		}
		catch(MasterServiceException kse) {
		}
	}

    /**
     * Rigourous Test :-)
     */
	 public void testCMF()
	 {
		setup();
		// test aliases
		as.generateAlias("test");
		System.out.println("test alias secret: " + new String(as.getPasswordFromAlias("test")));
		
		// test config elements
		assertTrue(new String(as.getPasswordFromConfigValue("${ALIAS=test}")).equals(new String(as.getPasswordFromAlias("test"))));
		assertTrue(new String(as.getPasswordFromConfigValue("cleartext")).equals("cleartext"));
    }
}
