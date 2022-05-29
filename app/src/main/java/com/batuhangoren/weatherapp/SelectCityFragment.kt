package com.batuhangoren.weatherapp

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.text.InputType
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.batuhangoren.weatherapp.databinding.FragmentSelectCityBinding
import com.batuhangoren.weatherapp.viewModels.HomeViewModel

class SelectCityFragment : Fragment() {

    private var _binding: FragmentSelectCityBinding? = null
    private val binding get() = _binding!!
    private val viewModel: HomeViewModel by activityViewModels()
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentSelectCityBinding.inflate(inflater, container, false)

        sharedPreferences = requireActivity()
            .getSharedPreferences("currentCity", Context.MODE_PRIVATE)

        val currentCity = sharedPreferences.getString("city", "")

        if (!currentCity.isNullOrEmpty()) {
            viewModel.setCurrentCity(currentCity)
        }

        val edit = sharedPreferences.edit()

        binding.selectButton.setOnClickListener {
            viewModel.setCurrentCity(
                binding.cityText.text.toString()
            )

            edit.apply {
                putString("city", binding.cityText.text.toString())
                apply()
            }

            val action = SelectCityFragmentDirections
                .actionSelectCityFragmentToHomeFragment()
            findNavController().navigate(action)
        }

        return binding.root
    }

    override fun onResume() {
        super.onResume()
        val typeList = viewModel.cities
        val arrayAdapter =
            this.context?.let { ArrayAdapter(it, R.layout.dropdown_item, typeList) }
        binding.cityText.inputType = InputType.TYPE_NULL
        binding.cityText.setAdapter(arrayAdapter)
    }
}