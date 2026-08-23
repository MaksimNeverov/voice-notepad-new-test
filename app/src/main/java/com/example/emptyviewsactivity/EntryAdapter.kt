package com.example.emptyviewsactivity

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView

class EntryAdapter(
    private var onItemClick: (Entry) -> Unit = {},
    private var onLongClick: (Entry) -> Boolean = { false },
    private var onSelectionToggle: (Int, Boolean) -> Unit = { _, _ -> }
) : ListAdapter<Entry, EntryAdapter.EntryViewHolder>(EntryDiffCallback()) {

    // УБРАЛИ: private val selectedIds = mutableSetOf<Int>()
    // Состояние теперь хранится только в Activity.
    // Здесь мы храним только временную копию для быстрой проверки в onBindViewHolder,
    // но она обновляется ТОЛЬКО извне через setSelectedIds.
    private var currentSelectedIds = emptySet<Int>()

    fun setSelectedIds(ids: Collection<Int>) {
        currentSelectedIds = ids.toSet()
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EntryViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_entry, parent, false)
        return EntryViewHolder(view)
    }

    override fun onBindViewHolder(holder: EntryViewHolder, position: Int) {
        val entry = getItem(position)
        // Передаем текущее состояние из Activity
        holder.bind(entry, isSelected = entry.id in currentSelectedIds)
    }

    inner class EntryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvText: TextView = itemView.findViewById(R.id.tvText)
        private val tvDate: TextView = itemView.findViewById(R.id.tvDate)
        private val cbSelect: CheckBox = itemView.findViewById(R.id.cbSelect)
        private val row: LinearLayout = itemView.findViewById(R.id.entryRow)

        fun bind(entry: Entry, isSelected: Boolean) {
            val dateStr = DateUtils.getDateStringFromTimestamp(entry.timestamp)
            tvText.text = entry.text
            tvDate.text = dateStr

            // 1. Визуальное обновление фона и чекбокса строго по флагу isSelected
            if (isSelected) {
                row.setBackgroundColor(Color.parseColor("#E0E0E0"))
                cbSelect.isChecked = true
                cbSelect.visibility = View.VISIBLE
            } else {
                row.setBackgroundColor(Color.WHITE)
                cbSelect.isChecked = false
                cbSelect.visibility = View.GONE
            }

            // 2. УБРАЛИ: setOnCheckedChangeListener внутри bind.
            // Чекбокс теперь только отражает состояние, он не управляет им.
            // Управление происходит через клик по строке.

            // 3. Клик по всей строке
            itemView.setOnClickListener {
                // Если режим выделения активен (хотя бы один элемент выбран в Activity)
                if (currentSelectedIds.isNotEmpty()) {
                    val newState = !isSelected
                    // Сообщаем Activity об изменении состояния для этого ID
                    onSelectionToggle(entry.id, newState)
                } else {
                    // Обычный клик
                    onItemClick(entry)
                }
            }

            // 4. LongClick: включаем режим выделения
            itemView.setOnLongClickListener {
                // Если режим еще не включен (список пуст), включаем его и выбираем этот элемент
                if (currentSelectedIds.isEmpty()) {
                    onSelectionToggle(entry.id, true)
                    onLongClick(entry)
                    true
                } else {
                    // Если режим уже включен, считаем лонгклик как обычный клик для переключения
                    val newState = !isSelected
                    onSelectionToggle(entry.id, newState)
                    true
                }
            }
        }
    }

    class EntryDiffCallback : DiffUtil.ItemCallback<Entry>() {
        override fun areItemsTheSame(oldItem: Entry, newItem: Entry) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Entry, newItem: Entry) = oldItem == newItem
    }

    fun clearSelection() {
        currentSelectedIds = emptySet()
        notifyDataSetChanged()
    }
}
