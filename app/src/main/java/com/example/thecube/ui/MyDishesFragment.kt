package com.example.thecube.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.thecube.databinding.FragmentMyDishesBinding
import com.example.thecube.ui.DishAdapter
import com.example.thecube.viewModel.DishViewModel
import com.example.thecube.model.Dish
import com.google.firebase.auth.FirebaseAuth


class MyDishesFragment : Fragment() {

    private var _binding: FragmentMyDishesBinding? = null
    private val binding get() = _binding!!

    private lateinit var dishViewModel: DishViewModel
    private lateinit var dishAdapter: DishAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentMyDishesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dishViewModel = ViewModelProvider(this).get(DishViewModel::class.java)


        dishAdapter = DishAdapter { selectedDish ->
            // Navigate to EditDishFragment, passing the selected dish.
            val action = MyDishesFragmentDirections.actionMyDishesFragmentToEditDishFragment(selectedDish)
            findNavController().navigate(action)
        }

        binding.viewPagerMyDishes.adapter = dishAdapter

        // Observe dishes from Room
        dishViewModel.allDishes.observe(viewLifecycleOwner) { dishList ->
            dishAdapter.submitList(dishList)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
