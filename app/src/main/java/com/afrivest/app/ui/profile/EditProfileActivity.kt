package com.afrivest.app.ui.profile

import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.afrivest.app.R
import com.afrivest.app.databinding.ActivityEditProfileBinding
import com.afrivest.app.utils.gone
import com.afrivest.app.utils.visible
import com.bumptech.glide.Glide
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import java.io.ByteArrayOutputStream

@AndroidEntryPoint
class EditProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEditProfileBinding
    private val viewModel: EditProfileViewModel by viewModels()

    private var selectedImageBitmap: Bitmap? = null

    private val pickImage = registerForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        uri?.let {
            selectedImageBitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                val source = ImageDecoder.createSource(contentResolver, it)
                ImageDecoder.decodeBitmap(source)
            } else {
                @Suppress("DEPRECATION")
                MediaStore.Images.Media.getBitmap(contentResolver, it)
            }
            binding.ivAvatar.setImageBitmap(selectedImageBitmap)
            binding.tvRemovePhoto.visible()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        setupObservers()
        setupClickListeners()

        viewModel.loadProfile()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            title = "Personal Information"
        }
    }

    private fun setupObservers() {
        viewModel.user.observe(this) { user ->
            user?.let {
                binding.etName.setText(it.name)
                binding.etPhone.setText(it.phone_number)

                if (!it.avatar_url.isNullOrEmpty()) {
                    Glide.with(this)
                        .load(it.avatar_url)
                        .placeholder(R.drawable.ic_user_placeholder)
                        .into(binding.ivAvatar)
                    binding.tvRemovePhoto.visible()
                } else {
                    binding.tvInitials.visible()
                    binding.tvInitials.text = getInitials(it.name)
                }
            }
        }

        viewModel.isLoading.observe(this) { isLoading ->
            if (isLoading) {
                binding.loadingOverlay.root.visibility = View.VISIBLE
            } else {
                binding.loadingOverlay.root.visibility = View.INVISIBLE
            }
        }

        viewModel.errorMessage.observe(this) { error ->
            error?.let {
                Snackbar.make(binding.root, it, Snackbar.LENGTH_LONG).show()
            }
        }

        viewModel.updateSuccess.observe(this) { success ->
            if (success) {
                finish()
            }
        }
    }

    private fun setupClickListeners() {
        binding.tvChangePhoto.setOnClickListener {
            pickImage.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }

        binding.tvRemovePhoto.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Remove Photo")
                .setMessage("Are you sure you want to remove your profile photo?")
                .setPositiveButton("Remove") { _, _ ->
                    selectedImageBitmap = null
                    binding.ivAvatar.setImageResource(R.drawable.ic_user_placeholder)
                    binding.tvRemovePhoto.gone()
                    viewModel.deleteAvatar()
                }
                .setNegativeButton("Cancel", null)
                .show()
        }

        binding.btnSave.setOnClickListener {
            val name = binding.etName.text.toString().trim()
            val phone = binding.etPhone.text.toString().trim()

            if (name.isEmpty()) {
                Snackbar.make(binding.root, "Name cannot be empty", Snackbar.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            viewModel.updateProfile(name, phone, selectedImageBitmap)
        }
    }

    private fun getInitials(name: String): String {
        val parts = name.split(" ")
        return if (parts.size >= 2) {
            "${parts[0].first()}${parts[1].first()}".uppercase()
        } else {
            name.take(1).uppercase()
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}