package co.afrivest.ui.qr

import android.graphics.Bitmap
import android.graphics.Color
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import co.afrivest.data.local.SecurePreferences
import co.afrivest.data.model.Resource
import co.afrivest.data.repository.QrRepository
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MyQrActivity : AppCompatActivity() {

    @Inject lateinit var qrRepository: QrRepository
    @Inject lateinit var securePreferences: SecurePreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(co.afrivest.R.layout.activity_my_qr)

        findViewById<android.widget.ImageButton>(co.afrivest.R.id.btnBack).setOnClickListener { finish() }
        val ivQr = findViewById<ImageView>(co.afrivest.R.id.ivQr)
        val tvName = findViewById<TextView>(co.afrivest.R.id.tvQrName)
        val tvHint = findViewById<TextView>(co.afrivest.R.id.tvQrHint)

        tvName.text = securePreferences.getUserName() ?: ""
        tvHint.text = "Show this code to receive money"

        lifecycleScope.launch {
            when (val r = qrRepository.getMyQr()) {
                is Resource.Success -> {
                    r.data?.uuid?.let { ivQr.setImageBitmap(generateQr(it)) }
                    r.data?.name?.let { if (it.isNotBlank()) tvName.text = it }
                }
                is Resource.Error ->
                    android.widget.Toast.makeText(this@MyQrActivity, r.message, android.widget.Toast.LENGTH_LONG).show()
                else -> {}
            }
        }
    }

    private fun generateQr(content: String): Bitmap {
        val size = 720
        val matrix = QRCodeWriter().encode(content, BarcodeFormat.QR_CODE, size, size)
        val bmp = Bitmap.createBitmap(size, size, Bitmap.Config.RGB_565)
        for (x in 0 until size) {
            for (y in 0 until size) {
                bmp.setPixel(x, y, if (matrix[x, y]) Color.parseColor("#1A1A1A") else Color.WHITE)
            }
        }
        return bmp
    }
}