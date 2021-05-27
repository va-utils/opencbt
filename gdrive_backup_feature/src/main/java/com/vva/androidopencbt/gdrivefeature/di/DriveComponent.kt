package com.vva.androidopencbt.gdrivefeature.di

import android.content.Context
import com.vva.androidopencbt.di.DriveModuleDependencies
import com.vva.androidopencbt.gdrivefeature.DriveListFragment
import com.vva.androidopencbt.gdrivefeature.DriveLoginFragment
import dagger.BindsInstance
import dagger.Component

@Component(dependencies = [DriveModuleDependencies::class])
interface DriveComponent {
    fun inject(fragment: DriveLoginFragment)

    @Component.Builder
    interface Builder {
        fun context(@BindsInstance context: Context): Builder
        fun appDependencies(driveModuleDependencies: DriveModuleDependencies): Builder
        fun build(): DriveComponent
    }
}