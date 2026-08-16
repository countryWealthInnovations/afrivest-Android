package co.afrivest.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class LoanTerm(
    val term: String,
    val term_days: Int,
    val interest_rate: String
) : Parcelable

@Parcelize
data class LoanParty(
    val uuid: String?,
    val name: String?
) : Parcelable

@Parcelize
data class LoanRepaymentDto(
    val amount: String?,
    val currency: String?,
    val source: String?,
    val paid_at: String?
) : Parcelable

@Parcelize
data class Loan(
    val uuid: String,
    val reference: String,
    val principal: String?,
    val currency: String,
    val term: String?,
    val interest_rate: String?,
    val interest_amount: String?,
    val handling_fee: String?,
    val total_repayment: String?,
    val purpose: String?,
    val status: String,
    val due_date: String?,
    val created_at: String?,
    val net_interest: Double? = null,
    val borrower: LoanParty? = null,
    val lender: LoanParty? = null,
    val outstanding: Double? = null,
    val amount_repaid: Double? = null,
    val repayments: List<LoanRepaymentDto>? = null
) : Parcelable {
    val principalValue: Double get() = principal?.toDoubleOrNull() ?: 0.0
    val totalValue: Double get() = total_repayment?.toDoubleOrNull() ?: 0.0
}

data class MyLoans(
    val borrowed: List<Loan>,
    val lent: List<Loan>
)

data class LoanWrapper(
    val loan: Loan
)

data class LoanRequestBody(
    val amount: Double,
    val term: String,
    val currency: String,
    val purpose: String?
)

data class LoanRepayBody(
    val amount: Double
)