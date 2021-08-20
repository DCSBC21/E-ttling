package com.example.arguscustody;

import android.app.Application;
import android.util.Log;

import com.example.arguscustody.model.Stash;

import org.bouncycastle.jce.provider.BouncyCastleProvider;

import java.security.Provider;
import java.security.Security;

public class walletApp extends Application {
	@Override
	public void onCreate() {
		super.onCreate();
		Stash.init(this);
		Stash.put("TX_ID", "");
		Stash.put("VAULT_ID", "");
		Stash.put("PENDING_VAULT_LIST", ""); // INIT ONCE
		Stash.put("VAULT_LIST", ""); // INIT ONCE
		Stash.put("PENDING_TX_LIST", ""); // INIT ONCE
		Stash.put("TX_LIST", ""); // INIT ONCE
		Stash.put("USER_LIST", ""); // INIT ONCE
		Stash.put("COINTYPE_LIST", ""); // INIT ONCE
		Stash.put("KEYDISTRIBUTIONTYPE_LIST", ""); // INIT ONCE

//		System.loadLibrary("mpe");
//		Log.d("###Rust###", hello("World"));
		setupBouncyCastle();
	}

	private void setupBouncyCastle() {
		final Provider provider = Security.getProvider(BouncyCastleProvider.PROVIDER_NAME);
		if (provider == null) {
			// Web3j will set up the provider lazily when it's first used.
			return;
		}
		if (provider.getClass().equals(BouncyCastleProvider.class)) {
			// BC with same package name, shouldn't happen in real life.
			return;
		}
		// Android registers its own BC provider. As it might be outdated and might not include
		// all needed ciphers, we substitute it with a known BC bundled in the app.
		// Android's BC has its package rewritten to "com.android.org.bouncycastle" and because
		// of that it's possible to have another BC implementation loaded in VM.
		Security.removeProvider(BouncyCastleProvider.PROVIDER_NAME);
		Security.insertProviderAt(new BouncyCastleProvider(), 1);
	}
//	native public String hello(String to);
}
