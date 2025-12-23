package com.afrivest.app.ui.dashboard.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.afrivest.app.R

data class QuickAction(
    val icon: Int,
    val title: String,
    val key: String
)

class QuickActionsAdapter(
    private val actions: List<QuickAction>,
    private val onItemClick: (QuickAction) -> Unit
) : RecyclerView.Adapter<QuickActionsAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val ivIcon: ImageView = view.findViewById(R.id.ivIcon)
        val tvTitle: TextView = view.findViewById(R.id.tvTitle)
        val container: View = view.findViewById(R.id.llQuickAction)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_quick_action, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val action = actions[position]
        holder.ivIcon.setImageResource(action.icon)
        holder.tvTitle.text = action.title
        holder.container.setOnClickListener {
            onItemClick(action)
        }
    }

    override fun getItemCount() = actions.size
}