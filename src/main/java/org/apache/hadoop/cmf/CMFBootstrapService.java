package org.apache.hadoop.cmf;

import org.apache.hadoop.cmf.MasterService;
import org.apache.hadoop.cmf.KeystoreService;
import org.apache.hadoop.cmf.AliasService;
import org.apache.hadoop.cmf.impl.CMFAliasService;
import org.apache.hadoop.cmf.impl.CMFMasterService;
import org.apache.hadoop.cmf.impl.CMFKeystoreService;

public class CMFBootstrapService {
	private MasterService ms = null;
	private KeystoreService ks = null;
	private AliasService as = null;
	String securityDir = null;
	String keystoreDir = null;
	
	public void init(String securityDir, String keystoreDir, String serviceName) {
		this.securityDir = securityDir;
		this.keystoreDir = keystoreDir;
		ms = new CMFMasterService(serviceName);
		try {
			ks = new CMFKeystoreService(keystoreDir, serviceName);
			((CMFKeystoreService)ks).setMasterService(ms);
		}
		catch(KeystoreServiceException kse) {
		}
		as = new CMFAliasService();
		((CMFAliasService)as).setKeystoreService(ks);
	}
	
	public void start(boolean persist) {
		try {
			((CMFMasterService)ms).setupMasterSecret(securityDir, persist);
			if (!ks.isCredentialStoreAvailable()) {
				((CMFKeystoreService)ks).createCredentialStore();
			}
		}
		catch(KeystoreServiceException kse) {
		}
		catch(MasterServiceException kse) {
		}
	}
	
	public void stop() {
	}
	
	public void destroy() {
	}
	
	public static void main(String[] args) {
		CMFBootstrapService boot = new CMFBootstrapService();
		boot.init(".", ".", args[0]);
		boot.start(true);

		// test aliases
		boot.as.generateAlias("test");
		System.out.println("test alias secret: " + new String(boot.as.getPasswordFromAlias("test")));
		
		// test config elements
		System.out.println(boot.as.getPasswordFromConfigValue("${ALIAS=test}"));
		System.out.println(boot.as.getPasswordFromConfigValue("cleartext"));

		boot.stop();
		boot.destroy();
	}
	
	public AliasService getAliasService() {
		return as;
	}
}