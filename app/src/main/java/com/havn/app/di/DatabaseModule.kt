package com.havn.app.di

import android.content.Context
import com.havn.app.data.db.DoseLogDao
import com.havn.app.data.db.HavnDatabase
import com.havn.app.data.db.MedicationDao
import com.havn.app.data.db.UserDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext ctx: Context): HavnDatabase =
        HavnDatabase.getInstance(ctx)

    @Provides fun provideUserDao(db: HavnDatabase): UserDao = db.userDao()
    @Provides fun provideMedicationDao(db: HavnDatabase): MedicationDao = db.medicationDao()
    @Provides fun provideDoseLogDao(db: HavnDatabase): DoseLogDao = db.doseLogDao()
}
