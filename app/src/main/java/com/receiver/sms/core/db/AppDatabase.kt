package com.receiver.sms.core.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.receiver.sms.features.apiconfig.data.local.ApiConfigDao
import com.receiver.sms.features.apiconfig.data.local.ApiConfigEntity
import com.receiver.sms.features.calllog.data.local.CallLogDao
import com.receiver.sms.features.calllog.data.local.CallLogEntity
import com.receiver.sms.features.dispatch.data.local.ReceivedSmsDao
import com.receiver.sms.features.dispatch.data.local.ReceivedSmsEntity

/** Composes the feature-owned tables. Bump [version] and add a migration for any schema change. */
@Database(
    entities = [ApiConfigEntity::class, CallLogEntity::class, ReceivedSmsEntity::class],
    version = 1,
    exportSchema = true,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun apiConfigDao(): ApiConfigDao

    abstract fun callLogDao(): CallLogDao

    abstract fun receivedSmsDao(): ReceivedSmsDao
}
