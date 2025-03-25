package com.example.thecube.ui

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.navigation.findNavController

import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.thecube.R
import com.example.thecube.databinding.ItemDishBinding
import com.example.thecube.model.Country
import com.example.thecube.model.Dish
import com.google.firebase.auth.FirebaseAuth
import com.squareup.picasso.Picasso

class FavouriteDishesAdapter(
    private val onItemClick: ((Dish) -> Unit)? = null,
    private val onLikeToggle: ((Dish) -> Unit)? = null // Callback parameter
) : ListAdapter<Dish, FavouriteDishesAdapter.FavouriteDishViewHolder>(DishDiffCallback()) {

    // List of countries fetched from API.
    private var countries: List<Country> = emptyList()

    fun setCountries(countries: List<Country>) {
        this.countries = countries
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FavouriteDishViewHolder {
        val binding = ItemDishBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return FavouriteDishViewHolder(binding)
    }

    override fun onBindViewHolder(holder: FavouriteDishViewHolder, position: Int) {
        val dish = getItem(position)
        holder.bind(dish, countries, onLikeToggle)
        holder.itemView.setOnClickListener { view ->
            onItemClick?.invoke(dish)
        }
    }

    class FavouriteDishViewHolder(private val binding: ItemDishBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(dish: Dish, countries: List<Country>, onLikeToggle: ((Dish) -> Unit)?) {
            binding.textViewDishName.text = dish.dishName

            // Load dish image.
            Picasso.get()
                .load(dish.imageUrl)
                .resize(720, 720)
                .noFade()
                .centerCrop()
                .into(binding.imageViewDish)

            // Display the like count.
            binding.textViewLikeCount.text = dish.countLikes.toString()

            // Set the heart icon based on whether the current user liked it.
            val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
            val isLikedByCurrentUser = dish.likedBy.contains(currentUserId)
            val likeIconRes = if (isLikedByCurrentUser) R.drawable.heart_icon else R.drawable.heart_icon_no
            binding.imageViewLike.setImageResource(likeIconRes)

            // Toggle like on heart icon click.
            binding.imageViewLike.setOnClickListener {
                val updatedLikedBy = dish.likedBy.toMutableList().apply {
                    if (contains(currentUserId)) remove(currentUserId) else add(currentUserId)
                }
                val updatedDish = dish.copy(likedBy = updatedLikedBy, countLikes = updatedLikedBy.size)
                binding.imageViewLike.setImageResource(
                    if (updatedLikedBy.contains(currentUserId)) R.drawable.heart_icon else R.drawable.heart_icon_no
                )
                binding.textViewLikeCount.text = updatedLikedBy.size.toString()
                Log.d("FavouriteDishAdapter", "Like toggled. Updated likedBy: $updatedLikedBy")
                onLikeToggle?.invoke(updatedDish)
            }

            // Load flag image.
            if (dish.flagImageUrl.isNotEmpty()) {
                Picasso.get()
                    .load(dish.flagImageUrl)
                    .resize(720, 720)
                    .noFade()
                    .centerCrop()
                    .into(binding.imageViewFlag)
            } else {
                val dishCountry = dish.country.trim()
                val matchingCountry = countries.find {
                    it.name.common.trim().equals(dishCountry, ignoreCase = true)
                }
                if (matchingCountry != null) {
                    Picasso.get()
                        .load(matchingCountry.flags.png)
                        .resize(720, 720)
                        .noFade()
                        .centerCrop()
                        .into(binding.imageViewFlag)
                } else {
                    binding.imageViewFlag.setImageResource(R.drawable.placeholder_flag)
                }
            }

            // --- Difficulty Indicator ---
            binding.textViewDifficulty.text = dish.difficulty
            val difficultyColor = when (dish.difficulty) {
                "Easy" -> ContextCompat.getColor(binding.root.context, R.color.green)
                "Medium" -> ContextCompat.getColor(binding.root.context, R.color.yellow)
                "Hard" -> ContextCompat.getColor(binding.root.context, R.color.red)
                else -> ContextCompat.getColor(binding.root.context, R.color.defaultDifficultyColor)
            }
            binding.cardDifficulty.setCardBackgroundColor(difficultyColor)
        }
    }
}


