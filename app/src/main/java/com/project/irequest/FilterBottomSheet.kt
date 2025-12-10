package com.project.irequest

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Spinner
import android.widget.ArrayAdapter
import androidx.fragment.app.FragmentManager
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup

class FilterBottomSheet : BottomSheetDialogFragment() {

    private var listener: ((FilterOptions) -> Unit)? = null

    fun setOnApplyListener(cb: (FilterOptions) -> Unit) {
        listener = cb
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.bottom_sheet_filters, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val spinnerBranch = view.findViewById<Spinner>(R.id.spinnerBranch)
        val spinnerShift = view.findViewById<Spinner>(R.id.spinnerShift)
        val cgEmployeeType = view.findViewById<ChipGroup>(R.id.cgEmployeeType)
        val cgJoinOrder = view.findViewById<ChipGroup>(R.id.cgJoinOrder)
        val cgContractStatus = view.findViewById<ChipGroup>(R.id.cgContractStatus)
        val btnKpiLowToHigh = view.findViewById<Button>(R.id.btnKpiLowToHigh)
        val btnKpiHighToLow = view.findViewById<Button>(R.id.btnKpiHighToLow)
        val btnCancel = view.findViewById<Button>(R.id.btnCancelFilter)
        val btnApply = view.findViewById<Button>(R.id.btnApplyFilter)

        // Populate spinners with simple sample entries
        val branches = listOf("", "Chi nhánh A", "Chi nhánh B")
        val branchAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, branches)
        branchAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerBranch.adapter = branchAdapter

        val shifts = listOf("", "Ca sáng", "Ca chiều", "Ca đêm")
        val shiftAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, shifts)
        shiftAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerShift.adapter = shiftAdapter

        // Simple toggle behavior for KPI buttons
        btnKpiLowToHigh.setOnClickListener {
            btnKpiLowToHigh.isSelected = true
            btnKpiHighToLow.isSelected = false
            btnKpiLowToHigh.alpha = 1f
            btnKpiHighToLow.alpha = 0.6f
        }
        btnKpiHighToLow.setOnClickListener {
            btnKpiHighToLow.isSelected = true
            btnKpiLowToHigh.isSelected = false
            btnKpiHighToLow.alpha = 1f
            btnKpiLowToHigh.alpha = 0.6f
        }

        btnCancel.setOnClickListener {
            dismiss()
        }

        btnApply.setOnClickListener {
            val branch = spinnerBranch.selectedItem?.toString()?.takeIf { it.isNotBlank() }
            val shift = spinnerShift.selectedItem?.toString()?.takeIf { it.isNotBlank() }

            val types = mutableListOf<String>()
            for (i in 0 until cgEmployeeType.childCount) {
                val child = cgEmployeeType.getChildAt(i)
                if (child is Chip && child.isChecked) types.add(child.text.toString())
            }

            val joinOrder = when (cgJoinOrder.checkedChipId) {
                R.id.chipJoinNewest -> "newest"
                R.id.chipJoinOldest -> "oldest"
                else -> null
            }

            val contract = mutableListOf<String>()
            for (i in 0 until cgContractStatus.childCount) {
                val child = cgContractStatus.getChildAt(i)
                if (child is Chip && child.isChecked) contract.add(child.text.toString())
            }

            val kpiSort = when {
                btnKpiLowToHigh.isSelected -> "low_to_high"
                btnKpiHighToLow.isSelected -> "high_to_low"
                else -> null
            }

            val options = FilterOptions(
                branch = branch,
                shift = shift,
                employeeTypes = types,
                joinOrder = joinOrder,
                contractStatus = contract,
                kpiSort = kpiSort
            )

            listener?.invoke(options)
            dismiss()
        }
    }

    companion object {
        const val TAG = "FilterBottomSheet"
        fun show(manager: FragmentManager, cb: (FilterOptions) -> Unit) {
            val fragment = FilterBottomSheet()
            fragment.setOnApplyListener(cb)
            fragment.show(manager, TAG)
        }
    }
}
