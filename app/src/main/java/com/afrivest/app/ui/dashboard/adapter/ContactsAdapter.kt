package com.afrivest.app.ui.dashboard.adapters

import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.graphics.ColorUtils
import androidx.recyclerview.widget.RecyclerView
import com.afrivest.app.R

data class Contact(
    val name: String,
    val initials: String,
    val color: Int
)

class ContactsAdapter(
    private val contacts: List<Contact>,
    private val onItemClick: (Contact) -> Unit
) : RecyclerView.Adapter<ContactsAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvInitial: TextView = view.findViewById(R.id.tvInitial)
        val tvName: TextView = view.findViewById(R.id.tvName)
        val vBackground: View = view.findViewById(R.id.vBackground)
        val root: View = view
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_contact, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val contact = contacts[position]
        holder.tvInitial.text = contact.initials
        holder.tvName.text = contact.name
        holder.tvInitial.setTextColor(contact.color)

        val context = holder.itemView.context
        val density = context.resources.displayMetrics.density
        val strokePx = (2 * density).toInt()

        // Single drawable with background + stroke (like MetricsAdapter)
        val avatarDrawable = GradientDrawable().apply {
            shape = GradientDrawable.OVAL
            setColor(ColorUtils.setAlphaComponent(contact.color, (0.3f * 255).toInt()))
            setStroke(strokePx, contact.color)
        }
        holder.vBackground.background = avatarDrawable

        holder.root.setOnClickListener {
            onItemClick(contact)
        }
    }

    override fun getItemCount() = contacts.size

    private fun adjustAlpha(color: Int, factor: Float): Int {
        val alpha = Math.round(android.graphics.Color.alpha(color) * factor)
        val red = android.graphics.Color.red(color)
        val green = android.graphics.Color.green(color)
        val blue = android.graphics.Color.blue(color)
        return android.graphics.Color.argb(alpha, red, green, blue)
    }
}