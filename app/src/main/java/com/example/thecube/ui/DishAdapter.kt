package com.example.thecube.ui

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.navigation.findNavController
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.thecube.R
import com.example.thecube.databinding.ItemDishBinding
import com.example.thecube.model.Country
import com.example.thecube.model.Dish
import com.example.thecube.ui.ProfileFragmentDirections
import com.google.firebase.auth.FirebaseAuth
import com.squareup.picasso.Picasso

class DishAdapter(
    private val onItemClick: ((Dish) -> Unit)? = null,
    private var countries: List<Country> = emptyList(),
    private val onLikeClicked: ((Dish) -> Unit)? = null
) : ListAdapter<Dish, DishAdapter.DishViewHolder>(DishDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DishViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_dish, parent, false)
        val binding = ItemDishBinding.bind(view)
        return DishViewHolder(binding)
    }

    override fun onBindViewHolder(holder: DishViewHolder, position: Int) {
        val dish = getItem(position)
        holder.bind(dish, countries, onLikeClicked)
        holder.itemView.setOnClickListener { view ->
            val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
            if (dish.userId == currentUserId) {
                // Navigate to edit screen using Safe Args.
                val action = ProfileFragmentDirections.actionProfileFragmentToEditDishFragment(dish)
                view.findNavController().navigate(action)
            } else {
                val bundle = Bundle().apply { putParcelable("dish", dish) }
                view.findNavController().navigate(R.id.dishDetailFragment, bundle)
            }
        }
    }

    fun setCountries(countries: List<Country>) {
        this.countries = countries
        notifyDataSetChanged()
    }

    class DishViewHolder(private val binding: ItemDishBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(dish: Dish, countries: List<Country>, onLikeClicked: ((Dish) -> Unit)?) {
            binding.textViewDishName.text = dish.dishName

            // Load dish image.
            Picasso.get()
                .load(dish.imageUrl)
                .resize(720, 720)
                .noFade()
                .centerCrop()
                .into(binding.imageViewDish)

            // Set like icon and count.
            val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
            val isLikedByCurrentUser = dish.likedBy.contains(currentUserId)
            val likeIconRes = if (isLikedByCurrentUser) R.drawable.heart_icon else R.drawable.heart_icon_no
            binding.imageViewLike.setImageResource(likeIconRes)
            binding.textViewLikeCount.text = dish.countLikes.toString()

            // Toggle like state on click.
            binding.imageViewLike.setOnClickListener {
                val updatedLikedBy = dish.likedBy.toMutableList().apply {
                    if (contains(currentUserId)) remove(currentUserId) else add(currentUserId)
                }
                val computedCount = updatedLikedBy.size
                val updatedDish = dish.copy(likedBy = updatedLikedBy, countLikes = computedCount)
                binding.imageViewLike.setImageResource(
                    if (updatedLikedBy.contains(currentUserId)) R.drawable.heart_icon else R.drawable.heart_icon_no
                )
                binding.textViewLikeCount.text = computedCount.toString()
                Log.d("DishAdapter", "After clicking like, updatedLikedBy = $updatedLikedBy, count = $computedCount")
                onLikeClicked?.invoke(updatedDish)
                Log.d("DishAdapter", "After callback, updatedDish.likedBy = ${updatedDish.likedBy}, countLikes = ${updatedDish.countLikes}")
            }

            // Flag logic.
            if (!dish.flagImageUrl.isNullOrEmpty()) {
                Log.d("DishAdapter", "Loading flag from dish.flagImageUrl: ${dish.flagImageUrl}")
                Picasso.get()
                    .load(dish.flagImageUrl)
                    .resize(720, 720)
                    .noFade()
                    .centerCrop()
                    .into(binding.imageViewFlag)
            } else {
                val dishCountry = dish.country.trim()
                val matchingCountry = countries.find { country ->
                    country.name.common.trim().equals(dishCountry, ignoreCase = true)
                }
                if (matchingCountry != null) {
                    Log.d("DishAdapter", "Matching country found: ${matchingCountry.name.common} for dish country: ${dish.country}")
                    val flagUrl = matchingCountry.flags.png
                    Log.d("DishAdapter", "Loading flag from matchingCountry.flags.png: $flagUrl")
                    Picasso.get()
                        .load(flagUrl)
                        .resize(720, 720)
                        .noFade()
                        .centerCrop()
                        .into(binding.imageViewFlag)
                } else {
                    Log.d("DishAdapter", "No matching country found for dish country: ${dish.country}")
                    binding.imageViewFlag.setImageResource(R.drawable.placeholder_flag)
                }
            }


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

class DishDiffCallback : DiffUtil.ItemCallback<Dish>() {
    override fun areItemsTheSame(oldItem: Dish, newItem: Dish): Boolean = oldItem.id == newItem.id
    override fun areContentsTheSame(oldItem: Dish, newItem: Dish): Boolean = oldItem == newItem
}
