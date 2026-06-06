package co.afrivest.data.model

data class ContactLookupData(val contacts: List<MatchedContact>)

data class MatchedContact(
    val user_id: Int,
    val name: String,
    val phone: String?,
    val avatar_url: String?
)