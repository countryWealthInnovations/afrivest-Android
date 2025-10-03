package com.afrivest.app.ui.components

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.doAfterTextChanged
import androidx.recyclerview.widget.LinearLayoutManager
import com.afrivest.app.ui.auth.models.Country
import com.afrivest.app.databinding.DialogCountryPickerBinding
import com.afrivest.app.ui.auth.adapters.CountryAdapter
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class CountryPickerDialog : BottomSheetDialogFragment() {

    private var _binding: DialogCountryPickerBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: CountryAdapter
    private var onCountrySelected: ((Country) -> Unit)? = null

    fun setOnCountrySelectedListener(listener: (Country) -> Unit) {
        onCountrySelected = listener
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogCountryPickerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupSearch()
    }

    private fun setupRecyclerView() {
        adapter = CountryAdapter { country ->
            onCountrySelected?.invoke(country)
            dismiss()
        }

        binding.recyclerViewCountries.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerViewCountries.adapter = adapter

        adapter.submitList(Country.ALL_COUNTRIES)
    }

    private fun setupSearch() {
        binding.editTextSearch.doAfterTextChanged { text ->
            val query = text.toString()
            val filtered = if (query.isEmpty()) {
                Country.ALL_COUNTRIES
            } else {
                Country.ALL_COUNTRIES.filter {
                    it.name.contains(query, ignoreCase = true) ||
                            it.dialCode.contains(query) ||
                            it.code.contains(query, ignoreCase = true)
                }
            }
            adapter.submitList(filtered)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}