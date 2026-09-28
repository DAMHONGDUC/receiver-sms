package com.receiver.sms.core.di

import android.content.Context
import androidx.room.Room
import androidx.work.WorkManager
import com.receiver.sms.BuildConfig
import com.receiver.sms.core.constants.DatabaseConstants
import com.receiver.sms.core.db.AppDatabase
import com.receiver.sms.core.logging.AppLogger
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import javax.inject.Qualifier
import javax.inject.Singleton

/** A scope that outlives any screen, for work started by receivers. */
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class ApplicationScope

private const val HTTP_TAG = "Http"

@Module
@InstallIn(SingletonComponent::class)
object CoreModule {
    @Provides
    @Singleton
    @ApplicationScope
    fun provideApplicationScope(): CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase =
        Room.databaseBuilder(context, AppDatabase::class.java, DatabaseConstants.DB_NAME).build()

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        val builder: OkHttpClient.Builder = OkHttpClient.Builder()

        if (BuildConfig.DEBUG) {
            val logging: HttpLoggingInterceptor = HttpLoggingInterceptor { AppLogger.d(HTTP_TAG, it) }
            logging.level = HttpLoggingInterceptor.Level.BODY
            builder.addInterceptor(logging)
        }
        return builder.build()
    }

    @Provides
    @Singleton
    fun provideWorkManager(@ApplicationContext context: Context): WorkManager = WorkManager.getInstance(context)
}
