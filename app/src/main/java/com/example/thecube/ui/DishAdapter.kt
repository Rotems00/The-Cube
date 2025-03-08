package com.example.thecube.ui

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.navigation.findNavController
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.thecube.R
import com.example.thecube.model.Dish
import com.example.thecube.model.Country
import com.example.thecube.ui.MyDishesFragmentDirections
import com.google.firebase.auth.FirebaseAuth

class DishAdapter(
    private val onItemClick: ((Dish) -> Unit)? = null,
    private var countries: List<Country> = emptyList() // list of countries from API
) : ListAdapter<Dish, DishAdapter.DishViewHolder>(DishDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DishViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_dish, parent, false)
        return DishViewHolder(view)
    }

    override fun onBindViewHolder(holder: DishViewHolder, position: Int) {
        val dish = getItem(position)
        holder.bind(dish, countries)
        holder.itemView.setOnClickListener { view ->
            // Check if the dish belongs to the current user
            val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
            Log.d("DishAdapter", "Current user: $currentUserId, Dish owner: ${dish.userId}")
            if (currentUserId != null && dish.userId == currentUserId) {
                // Navigate to EditDishFragment using Safe Args
                val action = MyDishesFragmentDirections.actionMyDishesFragmentToEditDishFragment(dish)
                view.findNavController().navigate(action)
            } else {
                // Navigate to DishDetailFragment using a bundle
                val bundle = Bundle().apply {
                    putParcelable("dish", dish)
                }
                view.findNavController().navigate(R.id.dishDetailFragment, bundle)
            }
        }
    }

    fun setCountries(countries: List<Country>) {
        this.countries = countries
        notifyDataSetChanged()
    }

    class DishViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val dishNameTextView: TextView = itemView.findViewById(R.id.textViewDishName)
        private val dishImageView: ImageView = itemView.findViewById(R.id.imageViewDish)
        private val flagImageView: ImageView = itemView.findViewById(R.id.imageViewFlag)

        fun bind(dish: Dish, countries: List<Country>) {
            dishNameTextView.text = dish.dishName
            Glide.with(itemView.context)
                .load(dish.imageUrl)
                .into(dishImageView)

            val dishCountry = dish.country.trim()
            Log.d("DishAdapter", "Dish country: '$dishCountry'")
            val matchingCountry = countries.find { country ->
                val apiCountry = country.name.common.trim()
                Log.d("DishAdapter", "Comparing with API country: '$apiCountry'")
                // Use exact match, or check if one string contains the other (ignoring case)
                apiCountry.equals(dishCountry, ignoreCase = true) ||
                        apiCountry.contains(dishCountry, ignoreCase = true) ||
                        dishCountry.contains(apiCountry, ignoreCase = true)
            }
            if (matchingCountry != null) {
                Log.d("DishAdapter", "Matched API country: '${matchingCountry.name.common}'")
                Glide.with(itemView.context)
                    .load(matchingCountry.flags.png)
                    .into(flagImageView)
            } else {
                Log.w("DishAdapter", "No matching country found for: '$dishCountry'")
                flagImageView.setImageResource(R.drawable.placeholder_flag)
            }
        }
    }
}

class DishDiffCallback : DiffUtil.ItemCallback<Dish>() {
    override fun areItemsTheSame(oldItem: Dish, newItem: Dish): Boolean = oldItem.id == newItem.id
    override fun areContentsTheSame(oldItem: Dish, newItem: Dish): Boolean = oldItem == newItem
}
