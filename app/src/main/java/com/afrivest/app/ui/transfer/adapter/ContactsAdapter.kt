package com.afrivest.app.ui.transfer.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.afrivest.app.R
import com.afrivest.app.data.model.AppContact
import com.afrivest.app.utils.gone
import com.afrivest.app.utils.visible

class ContactsAdapter(
    private val onItemClick: (AppContact) -> Unit
) : ListAdapter<AppContact, ContactsAdapter.ViewHolder>(ContactDiffCallback()) {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvInitial: TextView = view.findViewById(R.id.tvContactInitial)
        val tvName: TextView = view.findViewById(R.id.tvContactName)
        val tvIdentifier: TextView = view.findViewById(R.id.tvContactIdentifier)
        val tvRegistered: TextView = view.findViewById(R.id.tvRegistered)
        val tvNotRegistered: TextView = view.findViewById(R.id.tvNotRegistered)
        val root: View = view
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_contact_transfer, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val contact = getItem(position)

        holder.tvInitial.text = contact.name.firstOrNull()?.uppercase() ?: "?"
        holder.tvName.text = contact.name
        holder.tvIdentifier.text = contact.getDisplayIdentifier()

        if (contact.isRegistered) {
            holder.tvRegistered.visible()
            holder.tvNotRegistered.gone()
            holder.root.alpha = 1.0f
        } else {
            holder.tvRegistered.gone()
            holder.tvNotRegistered.visible()
            holder.root.alpha = 0.5f
        }

        holder.root.setOnClickListener {
            if (contact.isRegistered) {
                onItemClick(contact)
            }
        }
    }

    class ContactDiffCallback : DiffUtil.ItemCallback<AppContact>() {
        override fun areItemsTheSame(oldItem: AppContact, newItem: AppContact): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: AppContact, newItem: AppContact): Boolean {
            return oldItem == newItem
        }
    }
}