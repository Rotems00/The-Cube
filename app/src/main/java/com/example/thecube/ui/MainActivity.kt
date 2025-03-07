package com.example.thecube.ui

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.example.thecube.R
import com.example.thecube.databinding.ActivityMainBinding
import com.example.thecube.util.ThreadPoolManager
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)



        // Get the NavHostFragment and its NavController
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController

        // Set up an OnDestinationChangedListener to hide bottom nav on Sign In/Sign Up screens
        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.signInFragment, R.id.signUpFragment -> {
                    binding.bottomNavigationBar.visibility = View.GONE
                }
                else -> {
                    binding.bottomNavigationBar.visibility = View.VISIBLE
                }
            }
        }

        // Setup the bottom nav with the NavController
        binding.bottomNavigationBar.setupWithNavController(navController)

        // Optional: Custom actions for bottom nav items
        binding.bottomNavigationBar.setOnItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.menu_profile -> {
                    navController.navigate(R.id.profileFragment)
                    true
                }
                R.id.menu_favourite -> {
                    navController.navigate(R.id.favouriteFragment)
                    true
                }
                R.id.menu_regenerate -> {
                    // Call regenerateCube() in HomeFragment if available
                    val homeFragment = supportFragmentManager
                        .findFragmentById(R.id.nav_host_fragment)
                        ?.childFragmentManager
                        ?.fragments
                        ?.firstOrNull { it is HomeFragment } as? HomeFragment
                    homeFragment?.regenerateCube()
                    true
                }
                R.id.menu_add_dish -> {
                    navController.navigate(R.id.addDishFragment)
                    true
                }
                else -> false
            }
        }

        // Example: Run a background task using our thread pool.
        ThreadPoolManager.submitTask {
            // Simulate a heavy background task.
            val result = performHeavyTask()

            // Update the UI on the main thread.

        }
    }

    /**
     * Simulates a heavy background task.
     * @return A result string.
     */
    private fun performHeavyTask(): String {
        // Simulate some heavy work (e.g., network request, computation)
        Thread.sleep(2000) // Simulate delay of 2 seconds
        return "Background Task Completed"
    }

    override fun onDestroy() {
        super.onDestroy()
        // Shutdown the thread pool when the activity is destroyed.
        ThreadPoolManager.shutdown()
    }
}
