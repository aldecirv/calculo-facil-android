package com.calculofacil.app.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.calculofacil.app.data.CalcHistory
import com.calculofacil.app.databinding.ItemHistoryBinding

class HistoryAdapter(
    private val items: List<CalcHistory>,
    private val onReuse: (CalcHistory) -> Unit
) : RecyclerView.Adapter<HistoryAdapter.Holder>() {

    inner class Holder(val binding: ItemHistoryBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: CalcHistory) {
            binding.category.text = item.category.uppercase()
            binding.date.text = item.dateLabel
            binding.expression.text = item.expression
            binding.result.text = item.result
            binding.root.setOnClickListener { onReuse(item) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        Holder(ItemHistoryBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: Holder, position: Int) = holder.bind(items[position])
    override fun getItemCount() = items.size
}
