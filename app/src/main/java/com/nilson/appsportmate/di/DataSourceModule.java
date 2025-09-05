package com.nilson.appsportmate.di;

import android.content.Context;

import com.nilson.appsportmate.data.local.AuthLocalDataSource;
import com.nilson.appsportmate.data.remote.AuthRemoteDataSource;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.android.qualifiers.ApplicationContext;
import dagger.hilt.components.SingletonComponent;

@Module
@InstallIn(SingletonComponent.class)
public class DataSourceModule {

    @Provides
    @Singleton
    public AuthRemoteDataSource provideAuthRemoteDataSource() {
        return new AuthRemoteDataSource();
    }

    @Provides
    @Singleton
    public AuthLocalDataSource provideAuthLocalDataSource(@ApplicationContext Context context) {
        return new AuthLocalDataSource(context);
    }
}
