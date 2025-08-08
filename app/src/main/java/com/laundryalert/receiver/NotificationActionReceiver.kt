package com.laundryalert.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.laundryalert.model.LaundryStatus
import com.laundryalert.repository.LaundryRepository
import com.laundryalert.worker.SnoozeWorker
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

class NotificationActionReceiver : BroadcastReceiver() {
    
    companion object {
        const val ACTION_BRING_IN = "com.laundryalert.ACTION_BRING_IN"
        const val ACTION_SNOOZE = "com.laundryalert.ACTION_SNOOZE"
    }
    
    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            ACTION_BRING_IN -> {
                handleBringInAction(context)
            }
            
            ACTION_SNOOZE -> {
                handleSnoozeAction(context)
            }
        }
    }
    
    private fun handleBringInAction(context: Context) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // 洗濯物の状態を「取り込み済み」に更新
                val repository = LaundryRepository(context)
                repository.updateStatus(LaundryStatus.BROUGHT_IN)
                
                // 通知を削除
                val notificationManager = NotificationManager(context)
                notificationManager.cancelRainAlert()
                
                // 完了通知を表示
                notificationManager.showGeneralNotification(
                    "取り込み完了",
                    "洗濯物を取り込みました"
                )
                
            } catch (e: Exception) {
                // エラーハンドリング
                val notificationManager = NotificationManager(context)
                notificationManager.showGeneralNotification(
                    "エラー",
                    "状態の更新に失敗しました"
                )
            }
        }
    }
    
    private fun handleSnoozeAction(context: Context) {
        // 現在の通知を削除
        val notificationManager = NotificationManager(context)
        notificationManager.cancelRainAlert()
        
        // 10分後に再度通知するワーカーをスケジュール
        val snoozeWorkRequest = OneTimeWorkRequestBuilder<SnoozeWorker>()
            .setInitialDelay(10, TimeUnit.MINUTES)
            .build()
        
        WorkManager.getInstance(context).enqueue(snoozeWorkRequest)
        
        // スヌーズ確認通知
        notificationManager.showGeneralNotification(
            "スヌーズ設定",
            "10分後に再度お知らせします"
        )
    }
}

