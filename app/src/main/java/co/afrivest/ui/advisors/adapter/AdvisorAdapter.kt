package co.afrivest.ui.advisors

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import co.afrivest.data.model.Advisor
import co.afrivest.databinding.ItemAdvisorBinding

class AdvisorAdapter(
    private val onClick: (Advisor) -> Unit
) : RecyclerView.Adapter<AdvisorAdapter.VH>() {

    private val items = mutableListOf<Advisor>()

    fun submit(list: List<Advisor>) {
        items.clear(); items.addAll(list); notifyDataSetChanged()
    }

    inner class VH(val b: ItemAdvisorBinding) : RecyclerView.ViewHolder(b.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val b = ItemAdvisorBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return VH(b)
    }

    override fun getItemCount() = items.size

    override fun onBindViewHolder(holder: VH, position: Int) {
        val a = items[position]
        holder.b.tvName.text = a.display_name
        holder.b.tvTitle.text = a.title ?: ""
        holder.b.tvTitle.visibility = if (a.title.isNullOrBlank()) android.view.View.GONE else android.view.View.VISIBLE
        holder.b.tvExpertise.text = a.expertise ?: ""
        holder.b.tvExpertise.visibility = if (a.expertise.isNullOrBlank()) android.view.View.GONE else android.view.View.VISIBLE
        holder.b.tvFee.text = "Fee: ${a.booking_fee_currency ?: ""} ${a.booking_fee ?: "0"} • ${a.session_duration_minutes ?: 30} min"
        holder.b.root.setOnClickListener { onClick(a) }
    }
}