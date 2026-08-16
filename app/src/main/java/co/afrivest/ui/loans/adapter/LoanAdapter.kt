package co.afrivest.ui.loans

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import co.afrivest.R
import co.afrivest.data.model.Loan

class LoanAdapter(
    private var items: List<Loan>,
    private val onClick: (Loan) -> Unit
) : RecyclerView.Adapter<LoanAdapter.VH>() {

    class VH(v: View) : RecyclerView.ViewHolder(v) {
        val tvReference: TextView = v.findViewById(R.id.tvLoanReference)
        val tvAmount: TextView = v.findViewById(R.id.tvLoanAmount)
        val tvStatus: TextView = v.findViewById(R.id.tvLoanStatus)
        val tvMeta: TextView = v.findViewById(R.id.tvLoanMeta)
    }

    fun submit(list: List<Loan>) {
        items = list
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_loan, parent, false)
        return VH(v)
    }

    override fun getItemCount() = items.size

    override fun onBindViewHolder(holder: VH, position: Int) {
        val loan = items[position]
        holder.tvReference.text = loan.reference
        holder.tvAmount.text = "${loan.currency} ${loan.principal ?: "0"}"
        holder.tvStatus.text = loan.status.replaceFirstChar { it.uppercase() }
        holder.tvStatus.setTextColor(statusColor(loan.status))
        val party = loan.borrower?.name ?: loan.lender?.name
        val term = loan.term?.replace("_", " ") ?: ""
        holder.tvMeta.text = listOfNotNull(
            term.ifBlank { null },
            party?.let { "with $it" },
            loan.due_date?.let { "due ${it.take(10)}" }
        ).joinToString("  •  ")
        holder.itemView.setOnClickListener { onClick(loan) }
    }

    private fun statusColor(status: String): Int = when (status) {
        "active", "funded" -> Color.parseColor("#10b981")
        "repaid" -> Color.parseColor("#EFBF04")
        "defaulted" -> Color.parseColor("#EF4444")
        "requested" -> Color.parseColor("#9CA3AF")
        else -> Color.parseColor("#9CA3AF")
    }
}