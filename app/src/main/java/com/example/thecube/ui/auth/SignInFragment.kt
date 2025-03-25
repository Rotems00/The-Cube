package com.example.thecube.ui.auth

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.thecube.R
import com.example.thecube.databinding.FragmentSignInBinding
import com.example.thecube.local.AppDatabase
import com.example.thecube.remote.RetrofitInstance
import com.example.thecube.repository.CommentRepository
import com.example.thecube.viewModel.DishViewModel
import com.example.thecube.viewModel.SharedUserViewModel
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

class SignInFragment : Fragment() {

    private var _binding: FragmentSignInBinding? = null
    private val binding get() = _binding!!
    private val TAG = "SignInFragment"

    private lateinit var dishViewModel: DishViewModel
    // Obtain the shared view model from the activity scope.
    private val sharedUserViewModel: SharedUserViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSignInBinding.inflate(inflater, container, false)
        binding.signInRoot.apply {
            isClickable = true
            isFocusableInTouchMode = true
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dishViewModel = ViewModelProvider(requireActivity()).get(DishViewModel::class.java)

        binding.signInRoot.setOnTouchListener { v, event ->
            if (event.action == MotionEvent.ACTION_DOWN) {
                hideKeyboard(v)
                v.clearFocus()
            }
            false
        }

        binding.textViewSignUp.setOnClickListener {
            findNavController().navigate(R.id.action_signInFragment_to_signUpFragment)
        }

        binding.buttonSignIn.setOnClickListener {
            val email = binding.editTextEmail.text.toString().trim()
            val password = binding.editTextPassword.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(requireContext(), "Please enter email and password", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        // Load countries data and update the shared view model.
                        lifecycleScope.launch {
                            try {
                                val commentDao = AppDatabase.getDatabase(requireContext()).commentDao()
                                val commentRepo = CommentRepository(commentDao)
                                commentRepo.syncAllCommentsFromFirestore()
                                val countries = RetrofitInstance.api.getCountries()
                                sharedUserViewModel.countriesData.value = countries
                                Log.d(TAG, "Loaded ${countries.size} countries")
                            } catch (e: Exception) {
                                Log.e(TAG, "Error loading countries: ${e.message}")
                            }
                        }
                        Log.d(TAG, "Sign in successful")
                        Toast.makeText(requireContext(), "Sign in successful", Toast.LENGTH_SHORT).show()
                        findNavController().navigate(R.id.action_signInFragment_to_homeFragment)
                    } else {
                        Log.e(TAG, "Sign in failed: ${task.exception?.message}")
                        Toast.makeText(requireContext(), "Sign in failed: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                    }
                }
        }
    }

    private fun hideKeyboard(view: View) {
        val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
