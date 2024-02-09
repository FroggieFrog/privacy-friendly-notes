package org.secuso.privacyfriendlynotes.ui.helper

import android.content.Context
import android.graphics.PorterDuff
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.util.Consumer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialog
import org.secuso.privacyfriendlynotes.R
import org.secuso.privacyfriendlynotes.model.SortingOrder

class SortingOptionDialog(
    context: Context,
    sortingOptionTextResId: Int,
    sortingOptionIconResId: Int,
    current: SortingOrder,
    reversed: Boolean,
    onChosen: Consumer<SortingOrder>
) {

    private val dialog = BottomSheetDialog(context)
    private val recyclerView by lazy {
        dialog.findViewById<RecyclerView>(R.id.sorting_options)!!
    }

    init {
        dialog.setContentView(R.layout.dialog_sorting_options)
        recyclerView.layoutManager = LinearLayoutManager(context)

        val icons = context.resources.obtainTypedArray(sortingOptionIconResId)
        val options = context.resources.getStringArray(sortingOptionTextResId)
            .zip((0 until icons.length()).map { icons.getResourceId(it, 0) })
            .mapIndexed { i, (text, icon) -> SortingOptionData(
                text,
                icon,
                SortingOrder.values()[i]
            ) }
        icons.recycle()
        recyclerView.adapter = SortingOptionAdapter(options, current, reversed) { option ->
            onChosen.accept(option)
            dialog.dismiss()
        }
    }

    fun chooseSortingOption() {
        dialog.show()
    }

    data class SortingOptionData(
        val text: String,
        val icon: Int,
        val option: SortingOrder
    )

    inner class SortingOptionAdapter(
        private val options: List<SortingOptionData>,
        private val current: SortingOrder,
        private val reversed: Boolean,
        private val onChosen: Consumer<SortingOrder>,
    ): RecyclerView.Adapter<SortingOptionAdapter.SortingOptionHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SortingOptionHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.dialog_sorting_options_item, parent, false)
            return SortingOptionHolder(view)
        }

        override fun getItemCount(): Int {
            return options.size
        }

        override fun onBindViewHolder(holder: SortingOptionHolder, position: Int) {
            val tint = run {
                val data = TypedValue()
                holder.itemView.context.theme.resolveAttribute(R.attr.colorOnSurface, data, true)
                return@run data.data
            }
            holder.textView.text = options[position].text
            holder.imgView.setImageResource(options[position].icon)
            holder.imgView.setColorFilter(tint, PorterDuff.Mode.SRC_IN)
            holder.itemView.setOnClickListener { _ -> onChosen.accept(options[position].option) }
            if (options[position].option == current) {
                holder.reverseOrder.setImageResource(if (reversed) R.drawable.baseline_arrow_downward_24 else R.drawable.baseline_arrow_upward_24)
            }
        }

        inner class SortingOptionHolder(view: View): RecyclerView.ViewHolder(view) {
            val textView: TextView = view.findViewById(R.id.sorting_option_text)
            val imgView: ImageView = view.findViewById(R.id.sorting_option_icon)
            val reverseOrder: ImageView = view.findViewById(R.id.sorting_option_reversed)
        }

    }
}