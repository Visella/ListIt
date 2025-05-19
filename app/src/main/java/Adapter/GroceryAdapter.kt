package Adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageButton
import androidx.recyclerview.widget.RecyclerView
import model.GroceryItem
import com.example.listit.R

class GroceryAdapter(
    private val items: MutableList<GroceryItem>,
    private val onDeleteClick: (Int) -> Unit
) : RecyclerView.Adapter<GroceryAdapter.GroceryViewHolder>() {

    inner class GroceryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val checkBox: CheckBox = itemView.findViewById(R.id.checkbox_item)
        val deleteButton: ImageButton = itemView.findViewById(R.id.button_delete)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GroceryViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_grocery, parent, false)
        return GroceryViewHolder(view)
    }

    override fun onBindViewHolder(holder: GroceryViewHolder, position: Int) {
        val item = items[position]
        holder.checkBox.text = item.name
        holder.checkBox.isChecked = item.checked

        holder.checkBox.setOnCheckedChangeListener { _, isChecked ->
            item.checked = isChecked
        }

        holder.deleteButton.setOnClickListener {
            onDeleteClick(position)
        }
    }

    override fun getItemCount(): Int = items.size
}