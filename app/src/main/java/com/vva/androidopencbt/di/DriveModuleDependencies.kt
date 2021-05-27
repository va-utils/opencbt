package com.vva.androidopencbt.di

import com.google.android.gms.auth.api.signin.GoogleSignInClient
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent
import javax.inject.Qualifier

@EntryPoint
@InstallIn(ActivityComponent::class)
interface DriveModuleDependencies {
//    @AuthGoogleSignInClient
//    fun signInClient(): GoogleSignInClient
}

@Qualifier
annotation class AuthGoogleSignInClient

