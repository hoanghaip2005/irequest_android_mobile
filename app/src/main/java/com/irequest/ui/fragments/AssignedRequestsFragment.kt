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
import com.project.irequest.databinding.FragmentAssignedRequestsBinding

/**
 * AssignedRequestsFragment - Hiển thị các request đã được gán cho Agent
 * Features: 1, 2, 6, 7, 9, 11
 */
class AssignedRequestsFragment : Fragment() {

    private var _binding: FragmentAssignedRequestsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAssignedRequestsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupListeners()
    }

    private fun setupRecyclerView() {
        binding.rvAssignedRequests.layoutManager = LinearLayoutManager(requireContext())

        // Load từ RequestStore - filter những request có assignee
        val requests = RequestStore.getAllRequests()
            .filter { it.assignee.isNotEmpty() && it.status != "COMPLETED" }
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
            binding.rvAssignedRequests.visibility = View.GONE
            binding.llEmptyState.visibility = View.VISIBLE
            binding.llErrorState.visibility = View.GONE
        } else {
            val adapter = RequestAdapter(
                requests = requests,
                onItemClick = { requestId ->
                    navigateToDetail(requestId)
                }
            )
            binding.rvAssignedRequests.adapter = adapter
            binding.rvAssignedRequests.visibility = View.VISIBLE
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
