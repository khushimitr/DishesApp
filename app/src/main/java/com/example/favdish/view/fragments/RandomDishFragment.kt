package com.example.favdish.view.fragments

import android.os.Build
import android.os.Bundle
import android.text.Html
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.example.favdish.R
import com.example.favdish.application.FavDishApplication
import com.example.favdish.databinding.FragmentRandomDishBinding
import com.example.favdish.model.entities.FavDish
import com.example.favdish.model.entities.RandomDish
import com.example.favdish.util.Constants
import com.example.favdish.view.adapters.FavDishAdapter
import com.example.favdish.viewmodel.FavDishViewModel
import com.example.favdish.viewmodel.FavDishViewModelFactory
import com.example.favdish.viewmodel.NotificationsViewModel
import com.example.favdish.viewmodel.RandomDishViewModel

class RandomDishFragment : Fragment() {

    private var binding: FragmentRandomDishBinding? = null
    private lateinit var randomDishViewModel: RandomDishViewModel


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = FragmentRandomDishBinding.inflate(inflater, container, false)

        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        randomDishViewModel = ViewModelProvider(this)[RandomDishViewModel::class.java]

        randomDishViewModel.getRandomDishRecipeFromAPI()

        binding!!.srlRandomDish.setOnRefreshListener {
            randomDishViewModel.getRandomDishRecipeFromAPI()
        }

        randomDishViewModelObserver()
    }

    private fun randomDishViewModelObserver() {
        randomDishViewModel.randomDishResponse.observe(viewLifecycleOwner) { randomDishResponse ->
            randomDishResponse?.let {
//                Log.i("RESPONSE", "${randomDishResponse.recipes[0]}")
                if(binding!!.srlRandomDish.isRefreshing){
                    binding!!.srlRandomDish.isRefreshing = false
                }
                setRandomDish(it.recipes[0])
            }
        }

        randomDishViewModel.isLoadingRandomDish.observe(viewLifecycleOwner) { loadingData ->
            loadingData?.let {
                Log.i("RESPONSE_LD", "$it")
            }
        }

        randomDishViewModel.isLoadingError.observe(viewLifecycleOwner) { dataError ->
            dataError?.let {
                Log.e("RESPONSE_ER", "$it")

                if(binding!!.srlRandomDish.isRefreshing){
                    binding!!.srlRandomDish.isRefreshing = false
                }
            }

        }
    }

    private fun setRandomDish(recipe: RandomDish.Recipe) {
        Glide.with(requireActivity())
            .load(recipe.image)
            .centerCrop()
            .into(binding!!.ivDishImage)


        binding!!.apply {
            tvTitle.text = recipe.title

            var dishType: String = "other"
            if (recipe.dishTypes.isNotEmpty()) {
                dishType = recipe.dishTypes[0]
                tvType.text = dishType
            }

            tvCategory.text = "Other"

            var ingredients = ""

            for (x in recipe.extendedIngredients) {
                if (ingredients.isEmpty()) {
                    ingredients = x.original
                } else {
                    ingredients = ingredients + ",\n" + x.original
                }
            }
            tvIngredients.text = ingredients

            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.N) {
                tvCookingDirection.text = Html.fromHtml(
                    recipe.instructions,
                    Html.FROM_HTML_MODE_COMPACT
                )
            }else{
                @Suppress("DEPRECATION")
                tvCookingDirection.text = Html.fromHtml(recipe.sourceName)
            }

            tvCookingTime.text = resources.getString(
                R.string.lbl_estimate_cooking_time,
                recipe.readyInMinutes.toString()
            )

            ivFavoriteDish.setImageDrawable(
                ContextCompat.getDrawable(requireActivity(),R.drawable.ic_favorite_unselected)
            )

            var addedToFav : Boolean = false

            ivFavoriteDish.setOnClickListener{
                if(addedToFav)
                {
                    Toast.makeText(requireActivity(), resources.getString(R.string.msg_already_added_to_favorite), Toast.LENGTH_SHORT).show()
                }
                else {
                    addedToFav = true
                    val randomDishList = FavDish(
                        recipe.image,
                        recipe.title,
                        dishType,
                        "Other",
                        ingredients,
                        Constants.DISH_IMAGE_SOURCE_ONLINE,
                        recipe.readyInMinutes.toString(),
                        recipe.instructions,
                        true
                    )

                    val favDishViewModel: FavDishViewModel by viewModels {
                        FavDishViewModelFactory((requireActivity().application as FavDishApplication).repository)
                    }

                    favDishViewModel.insert(randomDishList)

                    ivFavoriteDish.setImageDrawable(
                        ContextCompat.getDrawable(
                            requireActivity(),
                            R.drawable.ic_favorite_selected
                        )
                    )

                    Toast.makeText(
                        requireActivity(),
                        "Dish Added to Favorites.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }
}