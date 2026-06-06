package co.afrivest.utils

import android.content.Context
import android.provider.ContactsContract
import co.afrivest.data.model.AppContact

object ContactsHelper {

    fun loadContacts(context: Context): List<AppContact> {
        val contacts = mutableListOf<AppContact>()
        val seen = mutableSetOf<String>()

        val cursor = context.contentResolver.query(
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
            arrayOf(
                ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                ContactsContract.CommonDataKinds.Phone.NUMBER,
                ContactsContract.CommonDataKinds.Phone.CONTACT_ID
            ),
            null, null,
            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ASC"
        ) ?: return contacts

        cursor.use {
            val nameIdx   = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
            val numberIdx = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
            val idIdx     = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.CONTACT_ID)

            while (it.moveToNext()) {
                val name   = it.getString(nameIdx) ?: continue
                val number = it.getString(numberIdx)?.replace("\\s".toRegex(), "") ?: continue
                val id     = it.getString(idIdx) ?: continue

                if (seen.add(number)) {
                    contacts.add(AppContact(
                        id           = java.util.UUID.randomUUID().toString(),
                        name         = name,
                        phoneNumber  = number,
                        email        = null,
                        userId       = null,
                        isRegistered = false
                    ))
                }
            }
        }

        return contacts
    }
}