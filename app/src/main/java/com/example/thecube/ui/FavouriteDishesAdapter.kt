package com.example.thecube.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.thecube.R
import com.example.thecube.model.Country
import com.example.thecube.model.Dish

class FavouriteDishesAdapter(
    private val onItemClick: ((Dish) -> Unit)? = null
) : ListAdapter<Dish, FavouriteDishesAdapter.FavouriteDishViewHolder>(DishDiffCallback()) {

    // List of countries fetched from the API
    private var countries: List<Country> = emptyList()

    // Setter for the countries list
    fun setCountries(countries: List<Country>) {
        this.countries = countries
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FavouriteDishViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_dish, parent, false)
        return FavouriteDishViewHolder(view)
    }

    override fun onBindViewHolder(holder: FavouriteDishViewHolder, position: Int) {
        val dish = getItem(position)
        holder.bind(dish, countries)
        holder.itemView.setOnClickListener {
            onItemClick?.invoke(dish)
        }
    }

    class FavouriteDishViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val dishNameTextView: TextView = itemView.findViewById(R.id.textViewDishName)
        private val dishImageView: ImageView = itemView.findViewById(R.id.imageViewDish)
        private val flagImageView: ImageView = itemView.findViewById(R.id.imageViewFlag)

        fun bind(dish: Dish, countries: List<Country>) {
            dishNameTextView.text = dish.dishName

            // Load the dish image
            Glide.with(itemView.context)

                .load(dish.imageUrl)
                .into(dishImageView)

            // Normalize the dish country (trim and ignore case)
            val dishCountry = dish.country.trim()
            // Find matching API country for this dish's country
            val matchingCountry = countries.find {
                it.name.common.trim().equals(dishCountry, ignoreCase = true)
            }
            if (matchingCountry != null) {
                Glide.with(itemView.context)
                    .load(matchingCountry.flags.png)
                    .into(flagImageView)
            } else {
                // No matching country found; use a placeholder flag
                flagImageView.setImageResource(R.drawable.placeholder_flag)
            }
        }
    }

    class DishDiffCallback : DiffUtil.ItemCallback<Dish>() {
        override fun areItemsTheSame(oldItem: Dish, newItem: Dish): Boolean {
            return oldItem.id == newItem.id
        }
        override fun areContentsTheSame(oldItem: Dish, newItem: Dish): Boolean {
            return oldItem == newItem
        }
    }
}
