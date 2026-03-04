package ru.fefu.jikananime.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import ru.fefu.jikananime.presentation.FavouritesManager
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object FavouritesModule {

    @Provides
    @Singleton
    fun provideFavouritesManager(): FavouritesManager {
        return FavouritesManager()
    }
}