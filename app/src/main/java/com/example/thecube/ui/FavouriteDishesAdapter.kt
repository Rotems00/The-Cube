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
import com.example.thecube.model.Dish

class FavouriteDishesAdapter(
    private val onItemClick: ((Dish) -> Unit)? = null
) : ListAdapter<Dish, FavouriteDishesAdapter.FavouriteDishViewHolder>(DishDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FavouriteDishViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_dish, parent, false)
        return FavouriteDishViewHolder(view)
    }

    override fun onBindViewHolder(holder: FavouriteDishViewHolder, position: Int) {
        val dish = getItem(position)
        holder.bind(dish)
        // Set click listener if needed
        holder.itemView.setOnClickListener {
            onItemClick?.invoke(dish)
        }
    }

    class FavouriteDishViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val dishNameTextView: TextView = itemView.findViewById(R.id.textViewDishName)
        private val dishImageView: ImageView = itemView.findViewById(R.id.imageViewDish)

        fun bind(dish: Dish) {
            dishNameTextView.text = dish.dishName
            Glide.with(itemView.context)
                .load(dish.imageUrl)
                .into(dishImageView)
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