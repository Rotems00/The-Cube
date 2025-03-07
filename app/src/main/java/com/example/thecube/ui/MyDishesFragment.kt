package com.example.thecube.ui

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.thecube.databinding.FragmentMyDishesBinding
import com.example.thecube.model.Country
import com.example.thecube.repository.UserRepository
import com.example.thecube.ui.DishAdapter
import com.example.thecube.viewModel.DishViewModel
import com.example.thecube.remote.RetrofitInstance
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

class MyDishesFragment : Fragment() {

    private var _binding: FragmentMyDishesBinding? = null
    private val binding get() = _binding!!

    private lateinit var dishViewModel: DishViewModel
    private lateinit var dishAdapter: DishAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMyDishesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dishViewModel = ViewModelProvider(this).get(DishViewModel::class.java)

        // 1) Load the user’s updated profile photo from Firestore
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
        if (currentUserId != null) {
            UserRepository().getUser(currentUserId) { user ->
                if (user != null && user.imageUrl.isNotEmpty()) {
                    // Suppose your FragmentMyDishesBinding has an ImageView named "imageViewProfile"
                    Glide.with(this)
                        .load(user.imageUrl)
                        .circleCrop()
                        .into(binding.imageViewProfile)
                }
            }
        }

        // 2) Set up your DishAdapter
        dishAdapter = DishAdapter(onItemClick = { selectedDish ->
            val action = MyDishesFragmentDirections
                .actionMyDishesFragmentToEditDishFragment(selectedDish)
            findNavController().navigate(action)
        })
        binding.viewPagerMyDishes.adapter = dishAdapter

        // 3) Observe dishes from Room
        dishViewModel.allDishes.observe(viewLifecycleOwner) { dishList ->
            // You can do something with the first dish if you want...
            if (dishList.isNotEmpty()) {
                val firstDish = dishList[0]
                Log.d("MyDishesFragment", "First dish imageUrl: ${firstDish.imageUrl}")
                // (Optional) If you ever want to load the first dish image somewhere, do so here.
            }
            dishAdapter.submitList(dishList)
        }

        // 4) (Optional) Fetch countries from API
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val countriesList: List<Country> = RetrofitInstance.api.getCountries()
                dishAdapter.setCountries(countriesList)
            } catch (e: Exception) {
                Log.e("MyDishesFragment", "Error fetching countries: ${e.message}")
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
