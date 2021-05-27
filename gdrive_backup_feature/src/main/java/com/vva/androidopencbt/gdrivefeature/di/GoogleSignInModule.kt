package com.vva.androidopencbt.gdrivefeature.di

import android.app.Activity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.Scope
import com.google.api.services.drive.DriveScopes
import com.vva.androidopencbt.di.AuthGoogleSignInClient
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn

@InstallIn(DriveComponent::class)
@Module
object GoogleSignInModule {
    @AuthGoogleSignInClient
    @Provides
    fun provideClient(activity: Activity): GoogleSignInClient {
        val signInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .requestScopes(Scope(DriveScopes.DRIVE_FILE))
            .build()
        return GoogleSignIn.getClient(activity, signInOptions)
    }
}