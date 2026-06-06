package co.afrivest.ui.base

import android.content.pm.ActivityInfo
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

open class BaseActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        super.onCreate(savedInstanceState)
        applyEdgeToEdge()
    }

    private fun applyEdgeToEdge() {
        androidx.core.view.WindowCompat.setDecorFitsSystemWindows(window, true)
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            window.statusBarColor = androidx.core.content.ContextCompat.getColor(
                this, co.afrivest.R.color.background_dark_1
            )
            window.navigationBarColor = androidx.core.content.ContextCompat.getColor(
                this, co.afrivest.R.color.background_dark_2
            )
        }
    }
}