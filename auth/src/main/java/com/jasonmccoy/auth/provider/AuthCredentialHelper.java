package com.jasonmccoy.auth.provider;

import android.support.annotation.Nullable;

import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.GoogleAuthProvider;
import com.jasonmccoy.auth.IdpResponse;

public final class AuthCredentialHelper {
    @Nullable
    public static AuthCredential getAuthCredential(IdpResponse idpResponse) {
        switch (idpResponse.getProviderType()) {
            case GoogleAuthProvider.PROVIDER_ID:
                return GoogleProvider.createAuthCredential(idpResponse);
            case FacebookAuthProvider.PROVIDER_ID:
                return FacebookProvider.createAuthCredential(idpResponse);
            default:
                return null;
        }
    }
}
