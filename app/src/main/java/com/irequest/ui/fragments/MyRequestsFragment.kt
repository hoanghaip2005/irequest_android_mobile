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
import com.project.irequest.databinding.FragmentMyRequestsBinding

class MyRequestsFragment : Fragment() {

    private var _binding: FragmentMyRequestsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMyRequestsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupRecyclerView()
        setupListeners()
    }

    private fun setupRecyclerView() {
        binding.rvMyRequests.layoutManager = LinearLayoutManager(requireContext())
        
        // Load from RequestStore
        val requests = RequestStore.getAllRequests().map { item ->
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
        
        val adapter = RequestAdapter(
            requests = requests,
            onItemClick = { requestId ->
                // Navigate to RequestDetailFragment using NavController
                val bundle = Bundle().apply {
                    putString("requestId", requestId.toString())
                }
                // Determine which navigation ID to use based on parent activity
                val navigationId = if (requireActivity().javaClass.simpleName == "WorkActivity") {
                    R.id.nav_request_detail_work
                } else {
                    R.id.nav_request_detail
                }
                findNavController().navigate(navigationId, bundle)
            }
        )
        binding.rvMyRequests.adapter = adapter
        
        // Show RecyclerView
        binding.rvMyRequests.visibility = View.VISIBLE
        binding.llEmptyState.visibility = View.GONE
        binding.llErrorState.visibility = View.GONE
    }

    private fun setupListeners() {
        binding.fabCreateRequest.setOnClickListener {
            // Navigate to EditRequestFragment to create new request
            val navigationId = if (requireActivity().javaClass.simpleName == "WorkActivity") {
                R.id.nav_edit_request_work
            } else {
                R.id.nav_edit_request
            }
            findNavController().navigate(navigationId)
        }

        binding.swipeRefresh.setOnRefreshListener {
            setupRecyclerView()
            binding.swipeRefresh.isRefreshing = false
        }

        binding.btnSearch.setOnClickListener {
            binding.searchLayout.visibility = 
                if (binding.searchLayout.visibility == View.VISIBLE) View.GONE else View.VISIBLE
        }
    }

    override fun onResume() {
        super.onResume()
        // Refresh list when returning from create/edit
        setupRecyclerView()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
