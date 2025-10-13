package com.nilson.appsportmate.di;

import com.nilson.appsportmate.domain.repository.AuthRepository;
import com.nilson.appsportmate.domain.repository.UserRepository;
import com.nilson.appsportmate.domain.usecase.LoginUserUseCase;
import com.nilson.appsportmate.domain.usecase.SignUpUserUseCase;

import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.components.SingletonComponent;

@Module
@InstallIn(SingletonComponent.class)
public class UseCaseModule {

    @Provides
    public LoginUserUseCase provideLoginUserUseCase(AuthRepository authRepo, UserRepository userRepo) {
        return new LoginUserUseCase(authRepo, userRepo);
    }

    @Provides
    public SignUpUserUseCase provideSignUpUserUseCase(AuthRepository authRepo, UserRepository userRepo) {
        return new SignUpUserUseCase(authRepo, userRepo);
    }
}
