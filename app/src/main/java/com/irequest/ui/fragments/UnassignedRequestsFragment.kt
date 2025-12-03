package com.irequest.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.irequest.ui.adapters.RequestAdapter
import com.irequest.ui.data.RequestStore
import com.project.irequest.R
import com.project.irequest.databinding.FragmentUnassignedRequestsBinding

/**
 * UnassignedRequestsFragment - Hiển thị các request chưa được gán (NEW status, no assignee)
 * Features: 9 - Assignment Management
 */
class UnassignedRequestsFragment : Fragment() {

    private var _binding: FragmentUnassignedRequestsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentUnassignedRequestsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupListeners()
    }

    private fun setupRecyclerView() {
        binding.rvUnassignedRequests.layoutManager = LinearLayoutManager(requireContext())

        // Load từ RequestStore - filter những request NEW, chưa có assignee
        val requests = RequestStore.getAllRequests()
            .filter { it.assignee.isEmpty() && (it.status == "NEW" || it.status == "PENDING_APPROVAL") }
            .map { item ->
                RequestAdapter.RequestItem(
                    id = item.id.toIntOrNull() ?: 0,
                    title = item.title,
                    date = item.date,
                    category = item.category,
                    priority = item.priority,
                    description = item.description,
                    status = item.status,
                    assignee = item.assignee,
                    deadline = item.deadline
                )
            }

        if (requests.isEmpty()) {
            binding.rvUnassignedRequests.visibility = View.GONE
            binding.llEmptyState.visibility = View.VISIBLE
            binding.llErrorState.visibility = View.GONE
        } else {
            val adapter = RequestAdapter(
                requests = requests,
                onItemClick = { requestId ->
                    navigateToDetail(requestId)
                }
            )
            binding.rvUnassignedRequests.adapter = adapter
            binding.rvUnassignedRequests.visibility = View.VISIBLE
            binding.llEmptyState.visibility = View.GONE
            binding.llErrorState.visibility = View.GONE
        }
    }

    private fun setupListeners() {
        binding.swipeRefresh.setOnRefreshListener {
            setupRecyclerView()
            binding.swipeRefresh.isRefreshing = false
        }

        binding.btnRetry.setOnClickListener {
            setupRecyclerView()
        }
    }

    private fun navigateToDetail(requestId: Int) {
        val bundle = Bundle().apply {
            putString("requestId", requestId.toString())
        }
        findNavController().navigate(R.id.nav_request_detail_work, bundle)
    }

    override fun onResume() {
        super.onResume()
        setupRecyclerView()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
