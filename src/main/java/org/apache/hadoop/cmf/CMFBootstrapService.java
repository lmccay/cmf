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
			ks = new CMFKeystoreService(keystoreDir, serviceName, ms);
		}
		catch(KeystoreServiceException kse) {
		}
		as = new CMFAliasService(ks);
	}
	
	public void start(boolean persist) {
		try {
			ms.setupMasterSecret(securityDir, persist);
			if (!ks.isCredentialStoreAvailable()) {
				ks.createCredentialStore();
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
	
	public AliasService getAliasService() {
		return as;
	}
}