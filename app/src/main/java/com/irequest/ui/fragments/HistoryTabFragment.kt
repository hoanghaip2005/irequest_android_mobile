package com.irequest.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.project.irequest.R
import com.project.irequest.databinding.TabHistoryBinding

class HistoryTabFragment : Fragment() {

    private var _binding: TabHistoryBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = TabHistoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
    }

    private fun setupRecyclerView() {
        binding.rvActivityLogs.layoutManager = LinearLayoutManager(requireContext())
        val mockHistory = listOf(
            HistoryItem("Yêu cầu được tạo", "2025-12-03 09:00", "Người dùng"),
            HistoryItem("Trạng thái: Chưa bắt đầu → Đang xử lý", "2025-12-03 10:00", "Nhân viên hỗ trợ"),
            HistoryItem("Thêm bình luận", "2025-12-03 14:30", "Kỹ thuật viên")
        )
        binding.rvActivityLogs.adapter = HistoryAdapter(mockHistory)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    data class HistoryItem(val action: String, val time: String, val actor: String)
}

class HistoryAdapter(private val history: List<HistoryTabFragment.HistoryItem>) :
    RecyclerView.Adapter<HistoryAdapter.HistoryViewHolder>() {

    inner class HistoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvAction = itemView.findViewById<TextView>(R.id.tvAction)
        private val tvTime = itemView.findViewById<TextView>(R.id.tvTime)
        private val tvActor = itemView.findViewById<TextView>(R.id.tvActor)

        fun bind(item: HistoryTabFragment.HistoryItem) {
            tvAction.text = item.action
            tvTime.text = item.time
            tvActor.text = item.actor
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_history, parent, false)
        return HistoryViewHolder(view)
    }

    override fun onBindViewHolder(holder: HistoryViewHolder, position: Int) {
        holder.bind(history[position])
    }

    override fun getItemCount() = history.size
}
