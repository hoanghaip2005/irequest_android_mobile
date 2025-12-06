package com.irequest.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.irequest.data.repository.FirebaseRequestRepository
import com.google.firebase.firestore.FirebaseFirestore
import com.irequest.ui.adapters.RequestAdapter
import com.project.irequest.R
import com.project.irequest.databinding.FragmentApprovalQueueBinding
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale

/**
 * ApprovalQueueFragment - Hiển thị các request cần phê duyệt (PENDING_APPROVAL status)
 * Features: 12, 13 - Approval Management
 */
class ApprovalQueueFragment : Fragment() {

    private var _binding: FragmentApprovalQueueBinding? = null
    private val binding get() = _binding!!
    private lateinit var repository: FirebaseRequestRepository
    private val requests = mutableListOf<RequestAdapter.RequestItem>()
    private val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentApprovalQueueBinding.inflate(inflater, container, false)
        repository = FirebaseRequestRepository(FirebaseFirestore.getInstance())
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupListeners()
        loadRequests()
    }

    private fun setupRecyclerView() {
        binding.rvApprovalQueue.layoutManager = LinearLayoutManager(requireContext())
    }

    private fun loadRequests() {
        showLoadingState()
        
        lifecycleScope.launch {
            try {
                val result = repository.getApprovalQueue()
                
                if (result.isSuccess) {
                    val firebaseRequests = result.getOrNull() ?: emptyList()
                    requests.clear()
                    requests.addAll(firebaseRequests.map { request ->
                        RequestAdapter.RequestItem(
                            id = request.requestId,
                            title = request.title ?: "No Title",
                            date = request.createdAt?.let { dateFormat.format(it) } ?: "",
                            category = request.workflowName ?: "Unknown",
                            priority = request.priorityName ?: "Medium",
                            description = request.description ?: "",
                            status = request.statusName ?: "Unknown",
                            assignee = request.assignedUserId ?: "Unassigned",
                            deadline = request.createdAt?.let { dateFormat.format(it) } ?: ""
                        )
                    })
                    showRequests()
                } else {
                    showErrorState(result.exceptionOrNull()?.message)
                }
            } catch (e: Exception) {
                showErrorState(e.message)
            }
        }
    }

    private fun showLoadingState() {
        binding.rvApprovalQueue.visibility = View.GONE
        binding.llEmptyState.visibility = View.GONE
        binding.llErrorState.visibility = View.GONE
        binding.swipeRefresh.isRefreshing = true
    }

    private fun showRequests() {
        binding.swipeRefresh.isRefreshing = false
        
        if (requests.isEmpty()) {
            binding.rvApprovalQueue.visibility = View.GONE
            binding.llEmptyState.visibility = View.VISIBLE
            binding.llErrorState.visibility = View.GONE
        } else {
            val adapter = RequestAdapter(
                requests = requests,
                onItemClick = { requestId ->
                    navigateToDetail(requestId)
                }
            )
            binding.rvApprovalQueue.adapter = adapter
            binding.rvApprovalQueue.visibility = View.VISIBLE
            binding.llEmptyState.visibility = View.GONE
            binding.llErrorState.visibility = View.GONE
        }
    }

    private fun showErrorState(message: String?) {
        binding.swipeRefresh.isRefreshing = false
        binding.rvApprovalQueue.visibility = View.GONE
        binding.llEmptyState.visibility = View.GONE
        binding.llErrorState.visibility = View.VISIBLE
    }

    private fun setupListeners() {
        binding.swipeRefresh.setOnRefreshListener {
            loadRequests()
        }

        binding.btnRetry.setOnClickListener {
            loadRequests()
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
        loadRequests()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
