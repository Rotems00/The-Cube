package com.example.thecube.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.thecube.databinding.FragmentAddDishBinding

class AddDishFragment : Fragment() {

    private var _binding: FragmentAddDishBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddDishBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.buttonSubmitDish.setOnClickListener {
            val dishName = binding.editTextDishName.text.toString().trim()
            val dishDescription = binding.editTextDishDescription.text.toString().trim()

            // Basic input validation
            if (dishName.isEmpty() || dishDescription.isEmpty()) {
                Toast.makeText(requireContext(), "Please fill in all fields", Toast.LENGTH_SHORT).show()
            } else {
                // Here you would typically add the dish to your repository or database.
                // For now, we'll just show a success message.
                Toast.makeText(requireContext(), "Dish added successfully", Toast.LENGTH_SHORT).show()

                // Optionally, clear the input fields:
                binding.editTextDishName.text?.clear()
                binding.editTextDishDescription.text?.clear()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
