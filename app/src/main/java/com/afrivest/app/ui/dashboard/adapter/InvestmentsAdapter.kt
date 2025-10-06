package com.afrivest.app.ui.dashboard.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.afrivest.app.R

data class Investment(
    val companyName: String,
    val investmentType: String,
    val rate: String,
    val maturity: String,
    val minInvestment: String,
    val logoIcon: Int = R.drawable.ic_chart
)

class InvestmentsAdapter(
    private val investments: List<Investment>
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
        holder.ivLogo.setImageResource(investment.logoIcon)
        holder.tvCompanyName.text = investment.companyName
        holder.tvInvestmentType.text = investment.investmentType
        holder.tvRate.text = investment.rate
        holder.tvMaturity.text = investment.maturity
        holder.tvMinInvestment.text = investment.minInvestment
    }

    override fun getItemCount() = investments.size
}