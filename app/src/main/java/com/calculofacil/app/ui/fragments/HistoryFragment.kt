package com.calculofacil.app.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.calculofacil.app.data.CalcStore
import com.calculofacil.app.databinding.FragmentHistoryBinding
import com.calculofacil.app.ui.MainActivity
import com.calculofacil.app.ui.adapters.HistoryAdapter
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class HistoryFragment : Fragment() {

    private var _binding: FragmentHistoryBinding? = null
    private val binding get() = _binding!!
    private var unsubscribe: (() -> Unit)? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentHistoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.list.layoutManager = LinearLayoutManager(requireContext())
        binding.clear.setOnClickListener { confirmClear() }
        unsubscribe = CalcStore.subscribe { refresh() }
        refresh()
    }

    private fun refresh() {
        if (_binding == null) return
        val history = CalcStore.history()
        binding.count.text = if (history.isEmpty()) "0 cálculos salvos" else "${history.size} cálculo(s) salvos"
        binding.clear.visibility = if (history.isEmpty()) View.GONE else View.VISIBLE
        binding.list.visibility = if (history.isEmpty()) View.GONE else View.VISIBLE
        binding.emptyState.visibility = if (history.isEmpty()) View.VISIBLE else View.GONE
        binding.list.adapter = HistoryAdapter(history) { item ->
            (activity as? MainActivity)?.openCalculator(item.expression, item.category == "Científico")
        }
    }

    private fun confirmClear() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Limpar histórico?")
            .setMessage("Todos os cálculos salvos serão removidos.")
            .setNegativeButton("Cancelar", null)
            .setPositiveButton("Limpar") { _, _ -> CalcStore.clearHistory() }
            .show()
    }

    override fun onDestroyView() {
        unsubscribe?.invoke(); unsubscribe = null
        super.onDestroyView()
        _binding = null
    }
}
