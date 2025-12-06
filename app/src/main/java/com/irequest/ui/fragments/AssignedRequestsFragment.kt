package com.irequest.ui.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.irequest.data.models.Request
import com.example.irequest.data.repository.FirebaseRequestRepository
import com.irequest.ui.adapters.RequestAdapter
import com.project.irequest.R
import com.project.irequest.databinding.FragmentAssignedRequestsBinding
import kotlinx.coroutines.launch

/**
 * AssignedRequestsFragment - Hiển thị các request đã được gán cho Agent
 * Features: 1, 2, 6, 7, 9, 11
 */
class AssignedRequestsFragment : Fragment() {

    private var _binding: FragmentAssignedRequestsBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var repository: FirebaseRequestRepository
    private lateinit var adapter: RequestAdapter
    private val requests = mutableListOf<Request>()

    companion object {
        private const val TAG = "AssignedRequestsFragment"
    }

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

        repository = FirebaseRequestRepository()
        
        setupRecyclerView()
        setupListeners()
        loadAssignedRequests()
    }

    private fun setupRecyclerView() {
        binding.rvAssignedRequests.layoutManager = LinearLayoutManager(requireContext())
        
        adapter = RequestAdapter(
            requests = emptyList(),
            onItemClick = { requestId ->
                navigateToDetail(requestId)
            }
        )
        binding.rvAssignedRequests.adapter = adapter
    }

    private fun loadAssignedRequests() {
        binding.swipeRefresh.isRefreshing = true
        
        lifecycleScope.launch {
            try {
                val result = repository.getMyTasks()
                
                if (result.isSuccess) {
                    requests.clear()
                    requests.addAll(result.getOrNull() ?: emptyList())
                    
                    if (requests.isEmpty()) {
                        showEmptyState()
                    } else {
                        showRequests()
                    }
                } else {
                    showErrorState(result.exceptionOrNull()?.message)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error loading assigned requests", e)
                showErrorState(e.message)
            } finally {
                binding.swipeRefresh.isRefreshing = false
            }
        }
    }
    
    private fun showRequests() {
        val requestItems = requests.map { request ->
            RequestAdapter.RequestItem(
                id = request.requestId,
                title = request.title,
                date = request.createdAt?.toString() ?: "",
                category = request.workflowName ?: "Không xác định",
                priority = request.priorityName ?: "Bình thường",
                description = request.description ?: "",
                status = request.statusName ?: "Mới",
                assignee = request.assignedUserName ?: "Chưa gán",
                deadline = ""
            )
        }
        
        adapter = RequestAdapter(
            requests = requestItems,
            onItemClick = { requestId ->
                navigateToDetail(requestId)
            }
        )
        binding.rvAssignedRequests.adapter = adapter
        
        binding.rvAssignedRequests.visibility = View.VISIBLE
        binding.llEmptyState.visibility = View.GONE
        binding.llErrorState.visibility = View.GONE
    }
    
    private fun showEmptyState() {
        binding.rvAssignedRequests.visibility = View.GONE
        binding.llEmptyState.visibility = View.VISIBLE
        binding.llErrorState.visibility = View.GONE
    }
    
    private fun showErrorState(message: String?) {
        binding.rvAssignedRequests.visibility = View.GONE
        binding.llEmptyState.visibility = View.GONE
        binding.llErrorState.visibility = View.VISIBLE
        
        Toast.makeText(
            requireContext(),
            "Lỗi: ${message ?: "Không thể tải danh sách yêu cầu"}",
            Toast.LENGTH_LONG
        ).show()
    }

    private fun setupListeners() {
        binding.swipeRefresh.setOnRefreshListener {
            loadAssignedRequests()
        }

        binding.btnRetry.setOnClickListener {
            loadAssignedRequests()
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
        loadAssignedRequests()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
