package co.afrivest.ui.advisors

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import co.afrivest.R
import co.afrivest.databinding.ActivityAdvisorsBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AdvisorsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAdvisorsBinding
    private val viewModel: AdvisorViewModel by viewModels()
    private lateinit var adapter: AdvisorAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdvisorsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        binding.toolbar.setNavigationOnClickListener { finish() }

        adapter = AdvisorAdapter { advisor ->
            startActivity(Intent(this, AdvisorDetailActivity::class.java).putExtra("advisor_id", advisor.id))
        }
        binding.rvAdvisors.layoutManager = LinearLayoutManager(this)
        binding.rvAdvisors.adapter = adapter

        observe()
        viewModel.loadAdvisors()
    }

    override fun onCreateOptionsMenu(menu: android.view.Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_advisors, menu)
        return true
    }

    override fun onOptionsItemSelected(item: android.view.MenuItem): Boolean {
        if (item.itemId == R.id.action_my_bookings) {
            startActivity(Intent(this, MyBookingsActivity::class.java))
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    private fun observe() {
        viewModel.loading.observe(this) { binding.progress.visibility = if (it) View.VISIBLE else View.GONE }
        viewModel.error.observe(this) { it?.let { m -> Toast.makeText(this, m, Toast.LENGTH_LONG).show(); viewModel.clearError() } }
        viewModel.advisors.observe(this) { list ->
            adapter.submit(list)
            binding.emptyState.visibility = if (list.isEmpty()) View.VISIBLE else View.GONE
            binding.rvAdvisors.visibility = if (list.isEmpty()) View.GONE else View.VISIBLE
        }
    }
}