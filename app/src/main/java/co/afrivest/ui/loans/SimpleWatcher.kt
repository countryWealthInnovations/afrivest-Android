package co.afrivest.ui.loans

import android.text.Editable
import android.text.TextWatcher

class SimpleWatcher(private val onChange: () -> Unit) : TextWatcher {
    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
    override fun afterTextChanged(s: Editable?) { onChange() }
}