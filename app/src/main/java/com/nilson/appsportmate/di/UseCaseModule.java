package com.nilson.appsportmate.di;

import com.nilson.appsportmate.domain.repository.AuthRepository;
import com.nilson.appsportmate.domain.usecase.LoginUserUseCase;

import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.components.SingletonComponent;

@Module
@InstallIn(SingletonComponent.class)
public class UseCaseModule {

    @Provides
    public LoginUserUseCase provideLoginUserUseCase(AuthRepository repo) {
        return new LoginUserUseCase(repo);
    }
}
