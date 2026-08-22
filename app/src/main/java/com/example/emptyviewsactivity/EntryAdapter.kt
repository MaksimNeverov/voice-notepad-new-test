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
    private var onLongClick: (Entry) -> Boolean = { false }
) : ListAdapter<Entry, EntryAdapter.EntryViewHolder>(EntryDiffCallback()) {

    // Состояние выделения теперь управляется извне (из MainActivity)
    private val selectedIds = mutableSetOf<Int>()

    // Публичный метод для установки выделенных ID из Activity
    fun setSelectedIds(ids: Collection<Int>) {
        selectedIds.clear()
        selectedIds.addAll(ids)
        notifyDataSetChanged() // Перерисовываем все строки, чтобы покрасить фон
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EntryViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_entry, parent, false)
        return EntryViewHolder(view)
    }

    override fun onBindViewHolder(holder: EntryViewHolder, position: Int) {
        val entry = getItem(position)
        holder.bind(entry, selectedIds.contains(entry.id))
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

            // 1. Меняем цвет фона строки: серый если выделено, белый если нет
            if (isSelected) {
                row.setBackgroundColor(Color.parseColor("#E0E0E0")) // Плотный светло-серый
                cbSelect.isChecked = true
            } else {
                row.setBackgroundColor(Color.WHITE)
                cbSelect.isChecked = false
            }

            // 2. Показываем/скрываем чекбокс
            cbSelect.visibility = if (isSelected) View.VISIBLE else View.GONE

            // 3. Слушатель чекбокса (переключаем состояние)
            cbSelect.setOnCheckedChangeListener(null)
            cbSelect.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) selectedIds.add(entry.id)
                else selectedIds.remove(entry.id)
                // Важно: сообщаем Activity, что список выделенных изменился
                // Для этого можно добавить callback, но проще обновлять UI в Activity после клика
            }

            // 4. Клик по всей строке: если режим выделения — переключаем чекбокс, иначе onItemClick
            itemView.setOnClickListener {
                if (selectedIds.isNotEmpty()) {
                    // Переключаем состояние
                    val newState = !isSelected
                    if (newState) selectedIds.add(entry.id)
                    else selectedIds.remove(entry.id)
                    // Обновляем UI через Activity (там будет notifyDataSetChanged)
                    // Для простоты здесь просто обновляем локально и вызываем onItemClick как заглушку
                    // Но лучше передавать callback для обновления UI из Activity
                } else {
                    onItemClick(entry)
                }
            }

            // 5. LongClick: включаем режим выделения
            itemView.setOnLongClickListener {
                if (selectedIds.isEmpty()) {
                    selectedIds.add(entry.id)
                    cbSelect.isChecked = true
                    onLongClick(entry)
                    true
                } else {
                    false
                }
            }
        }
    }

    class EntryDiffCallback : DiffUtil.ItemCallback<Entry>() {
        override fun areItemsTheSame(oldItem: Entry, newItem: Entry) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Entry, newItem: Entry) = oldItem == newItem
    }

    fun getSelectedIds(): List<Int> = selectedIds.toList()

    fun clearSelection() {
        selectedIds.clear()
        notifyDataSetChanged()
    }
}
