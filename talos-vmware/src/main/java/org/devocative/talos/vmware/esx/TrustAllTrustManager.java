/*
 * ******************************************************
 * Copyright VMware, Inc. 2016.  All Rights Reserved.
 * SPDX-License-Identifier: MIT
 * ******************************************************
 *
 * DISCLAIMER. THIS PROGRAM IS PROVIDED TO YOU "AS IS"
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, WHETHER
 * ORAL OR WRITTEN, EXPRESS OR IMPLIED. THE AUTHOR
 * SPECIFICALLY DISCLAIMS ANY IMPLIED WARRANTIES OR
 * CONDITIONS OF MERCHANTABILITY, SATISFACTORY QUALITY,
 * NON-INFRINGEMENT AND FITNESS FOR A PARTICULAR PURPOSE.
 */

package org.devocative.talos.vmware.esx;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;

/**
 * This class breaks the Java SSL Trust Management services so that Java will
 * trust any and all SSL certificates. You probably don't ever want to use this
 * in a production environment but we are using it here to help you to test your
 * code against systems that may not have a real SSL certificate.
 * <p>
 * <strong>It is not advised to ever use this in production.</strong>
 */
public final class TrustAllTrustManager implements TrustManager, X509TrustManager {

	public void register() throws NoSuchAlgorithmException, KeyManagementException {
		register(this);
	}

	public static void register(TrustManager tm) throws NoSuchAlgorithmException, KeyManagementException {
		TrustManager[] trustAllCerts = new TrustManager[1];
		trustAllCerts[0] = tm;
		javax.net.ssl.SSLContext sc = javax.net.ssl.SSLContext.getInstance("SSL");
		javax.net.ssl.SSLSessionContext sslsc = sc.getServerSessionContext();
		sslsc.setSessionTimeout(0);
		sc.init(null, trustAllCerts, null);
		HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
	}

	@Override
	public X509Certificate[] getAcceptedIssuers() {
		return null;
	}

	@Override
	public void checkServerTrusted(X509Certificate[] certs, String authType) {
	}

	@Override
	public void checkClientTrusted(X509Certificate[] certs, String authType) {
	}

}
