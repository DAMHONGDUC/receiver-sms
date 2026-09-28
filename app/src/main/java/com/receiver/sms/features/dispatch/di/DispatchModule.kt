package com.receiver.sms.features.dispatch.di

import com.receiver.sms.core.db.AppDatabase
import com.receiver.sms.features.dispatch.data.local.ReceivedSmsDao
import com.receiver.sms.features.dispatch.data.remote.OkHttpExecutor
import com.receiver.sms.features.dispatch.data.repository.ReceivedSmsRepositoryImpl
import com.receiver.sms.features.dispatch.data.work.WorkManagerCallScheduler
import com.receiver.sms.features.dispatch.domain.repository.ReceivedSmsRepository
import com.receiver.sms.features.dispatch.domain.service.CallScheduler
import com.receiver.sms.features.dispatch.domain.service.FailureNotifier
import com.receiver.sms.features.dispatch.domain.service.HttpExecutor
import com.receiver.sms.features.dispatch.domain.service.KeepAliveController
import com.receiver.sms.features.dispatch.platform.AndroidKeepAliveController
import com.receiver.sms.features.dispatch.platform.NotificationHelper
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class DispatchModule {
    @Binds
    @Singleton
    abstract fun bindSmsRepository(impl: ReceivedSmsRepositoryImpl): ReceivedSmsRepository

    @Binds
    abstract fun bindHttpExecutor(impl: OkHttpExecutor): HttpExecutor

    @Binds
    abstract fun bindScheduler(impl: WorkManagerCallScheduler): CallScheduler

    @Binds
    abstract fun bindFailureNotifier(impl: NotificationHelper): FailureNotifier

    @Binds
    abstract fun bindKeepAlive(impl: AndroidKeepAliveController): KeepAliveController

    companion object {
        @Provides
        fun provideDao(db: AppDatabase): ReceivedSmsDao = db.receivedSmsDao()
    }
}
