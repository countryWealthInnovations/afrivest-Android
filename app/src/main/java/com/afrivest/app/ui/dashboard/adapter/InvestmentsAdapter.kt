package com.afrivest.app.ui.dashboard.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.afrivest.app.R
import com.afrivest.app.data.api.InvestmentProduct

class InvestmentsAdapter(
    private val investments: List<InvestmentProduct>,
    private val onItemClick: (InvestmentProduct) -> Unit
) : RecyclerView.Adapter<InvestmentsAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val ivLogo: ImageView = view.findViewById(R.id.ivLogo)
        val tvCompanyName: TextView = view.findViewById(R.id.tvCompanyName)
        val tvInvestmentType: TextView = view.findViewById(R.id.tvInvestmentType)
        val tvRate: TextView = view.findViewById(R.id.tvRate)
        val tvMaturity: TextView = view.findViewById(R.id.tvMaturity)
        val tvMinInvestment: TextView = view.findViewById(R.id.tvMinInvestment)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_investment, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val investment = investments[position]

        holder.tvCompanyName.text = investment.partner?.name ?: investment.category?.name
        holder.tvInvestmentType.text = investment.category?.name
        holder.tvRate.text = if (investment.expected_returns == "0.00") "No Returns" else "${investment.expected_returns}% p.a"
        holder.tvMaturity.text = investment.duration_label
        holder.tvMinInvestment.text = investment.min_investment_formatted

        holder.itemView.setOnClickListener {
            onItemClick(investment)
        }
    }

    override fun getItemCount() = investments.size
}