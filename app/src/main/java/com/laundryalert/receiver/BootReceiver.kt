package com.laundryalert.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.laundryalert.model.LaundryStatus
import com.laundryalert.repository.LaundryRepository
import com.laundryalert.service.AlarmScheduler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BootReceiver : BroadcastReceiver() {
    
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED ||
            intent.action == Intent.ACTION_MY_PACKAGE_REPLACED ||
            intent.action == Intent.ACTION_PACKAGE_REPLACED) {
            
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val repository = LaundryRepository(context)
                    val currentStatus = repository.getCurrentStatus()
                    
                    // 洗濯物が干されている場合は天気チェックを再開
                    if (currentStatus == LaundryStatus.HANGING) {
                        val alarmScheduler = AlarmScheduler(context)
                        alarmScheduler.scheduleWeatherCheck()
                    }
                    
                } catch (e: Exception) {
                    // エラーハンドリング
                }
            }
        }
    }
}

