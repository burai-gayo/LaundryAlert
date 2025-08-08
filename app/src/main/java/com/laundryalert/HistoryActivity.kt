package com.laundryalert

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.laundryalert.databinding.ActivityHistoryBinding

class HistoryActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityHistoryBinding
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHistoryBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "履歴"
        
        setupRecyclerView()
        loadHistoryData()
    }
    
    private fun setupRecyclerView() {
        binding.historyRecyclerView.layoutManager = LinearLayoutManager(this)
        // HistoryAdapterの実装は簡略化
    }
    
    private fun loadHistoryData() {
        // 履歴データの読み込み（簡略化）
        // 実際の実装では、データベースから履歴を取得
    }
}

