package com.nilson.appsportmate.di;

import com.nilson.appsportmate.data.local.AuthLocalDataSource;
import com.nilson.appsportmate.data.remote.AuthRemoteDataSource;
import com.nilson.appsportmate.data.repository.AuthRepositoryImpl;
import com.nilson.appsportmate.domain.repository.AuthRepository;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.components.SingletonComponent;

@Module
@InstallIn(SingletonComponent.class)
public class RepositoryModule {

    @Provides
    @Singleton
    public AuthRepository provideAuthRepository(
            AuthRemoteDataSource remoteDataSource,
            AuthLocalDataSource localDataSource
    ) {
        return new AuthRepositoryImpl(remoteDataSource, localDataSource);
    }
}
