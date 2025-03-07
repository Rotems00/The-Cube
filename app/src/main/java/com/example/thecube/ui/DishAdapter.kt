package com.example.thecube.ui

import android.util.Log
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
import com.example.thecube.model.Dish
import com.example.thecube.model.Country

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
        holder.itemView.setOnClickListener {
            onItemClick?.invoke(dish)
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

            // Trim and compare country names to avoid whitespace issues
            val dishCountry = dish.country.trim()
            val matchingCountry = countries.find {
                it.name.common.trim().equals(dishCountry, ignoreCase = true)
            }
            if (matchingCountry != null) {
                Glide.with(itemView.context)
                    .load(matchingCountry.flags.png)
                    .into(flagImageView)
            } else {
                // Log a warning for debugging
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
