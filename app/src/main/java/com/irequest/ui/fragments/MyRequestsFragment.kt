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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.irequest.ui.adapters.RequestAdapter
import com.project.irequest.R
import com.project.irequest.databinding.FragmentMyRequestsBinding
import kotlinx.coroutines.launch

class MyRequestsFragment : Fragment() {

    private var _binding: FragmentMyRequestsBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var repository: FirebaseRequestRepository
    private lateinit var adapter: RequestAdapter
    private val requests = mutableListOf<Request>()

    companion object {
        private const val TAG = "MyRequestsFragment"
    }

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
        
        repository = FirebaseRequestRepository(
            FirebaseFirestore.getInstance(),
            FirebaseAuth.getInstance()
        )
        
        // Debug: Check current user
        val currentUser = FirebaseAuth.getInstance().currentUser
        Log.d(TAG, "Current user: ${currentUser?.uid}, email: ${currentUser?.email}")
        
        setupRecyclerView()
        setupListeners()
        loadMyRequests()
    }

    private fun setupRecyclerView() {
        binding.rvMyRequests.layoutManager = LinearLayoutManager(requireContext())
        
        adapter = RequestAdapter(
            requests = emptyList(),
            onItemClick = { requestId ->
                val bundle = Bundle().apply {
                    putString("requestId", requestId.toString())
                }
                val navigationId = if (requireActivity().javaClass.simpleName == "WorkActivity") {
                    R.id.nav_request_detail_work
                } else {
                    R.id.nav_request_detail
                }
                findNavController().navigate(navigationId, bundle)
            }
        )
        binding.rvMyRequests.adapter = adapter
    }

    private fun loadMyRequests() {
        showLoading()
        
        lifecycleScope.launch {
            try {
                val result = repository.getMyRequests()
                
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
                Log.e(TAG, "Error loading requests", e)
                showErrorState(e.message)
            }
        }
    }
    
    private fun showLoading() {
        binding.progressBar.visibility = View.VISIBLE
        binding.rvMyRequests.visibility = View.GONE
        binding.llEmptyState.visibility = View.GONE
        binding.llErrorState.visibility = View.GONE
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
                val bundle = Bundle().apply {
                    putString("requestId", requestId.toString())
                }
                val navigationId = if (requireActivity().javaClass.simpleName == "WorkActivity") {
                    R.id.nav_request_detail_work
                } else {
                    R.id.nav_request_detail
                }
                findNavController().navigate(navigationId, bundle)
            }
        )
        binding.rvMyRequests.adapter = adapter
        
        binding.progressBar.visibility = View.GONE
        binding.rvMyRequests.visibility = View.VISIBLE
        binding.llEmptyState.visibility = View.GONE
        binding.llErrorState.visibility = View.GONE
    }
    
    private fun showEmptyState() {
        binding.progressBar.visibility = View.GONE
        binding.rvMyRequests.visibility = View.GONE
        binding.llEmptyState.visibility = View.VISIBLE
        binding.llErrorState.visibility = View.GONE
    }
    
    private fun showErrorState(message: String?) {
        binding.progressBar.visibility = View.GONE
        binding.rvMyRequests.visibility = View.GONE
        binding.llEmptyState.visibility = View.GONE
        binding.llErrorState.visibility = View.VISIBLE
        
        Toast.makeText(
            requireContext(),
            "Lỗi: ${message ?: "Không thể tải danh sách yêu cầu"}",
            Toast.LENGTH_LONG
        ).show()
    }

    private fun setupListeners() {
        binding.fabCreateRequest.setOnClickListener {
            val navigationId = if (requireActivity().javaClass.simpleName == "WorkActivity") {
                R.id.nav_edit_request_work
            } else {
                R.id.nav_edit_request
            }
            findNavController().navigate(navigationId)
        }

        binding.swipeRefresh.setOnRefreshListener {
            loadMyRequests()
            binding.swipeRefresh.isRefreshing = false
        }

        binding.btnSearch.setOnClickListener {
            binding.searchLayout.visibility = 
                if (binding.searchLayout.visibility == View.VISIBLE) View.GONE else View.VISIBLE
        }
        
        binding.btnRetry?.setOnClickListener {
            loadMyRequests()
        }
    }

    override fun onResume() {
        super.onResume()
        loadMyRequests()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
