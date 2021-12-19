package com.example.favdish.view.fragments

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.example.favdish.R
import com.example.favdish.application.FavDishApplication
import com.example.favdish.databinding.FragmentFavoriteDishesBinding
import com.example.favdish.model.entities.FavDish
import com.example.favdish.view.activities.MainActivity
import com.example.favdish.view.adapters.FavDishAdapter
import com.example.favdish.viewmodel.DashboardViewModel
import com.example.favdish.viewmodel.FavDishViewModel
import com.example.favdish.viewmodel.FavDishViewModelFactory

class FavoriteDishesFragment : Fragment() {

    private var binding: FragmentFavoriteDishesBinding? = null
    private val favDishViewModel : FavDishViewModel by viewModels {
        FavDishViewModelFactory((requireActivity().application as FavDishApplication).repository)
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = FragmentFavoriteDishesBinding.inflate(inflater, container, false)

        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding!!.rvFavDishCard.layoutManager = GridLayoutManager(requireActivity(),2)
        val adapter = FavDishAdapter(this)
        binding!!.rvFavDishCard.adapter = adapter

        favDishViewModel.favoriteDishesList.observe(viewLifecycleOwner){ dishes->
            dishes.let{
                if(it.isNotEmpty())
                {
                    binding!!.rvFavDishCard.visibility = View.VISIBLE
                    binding!!.tvFavDishCardPlaceholder.visibility = View.GONE

                    adapter.dishesList(it)
                }
                else
                {
                    binding!!.rvFavDishCard.visibility = View.GONE
                    binding!!.tvFavDishCardPlaceholder.visibility = View.VISIBLE
                }
            }
        }
    }

    fun showDishDetails(favDish : FavDish){
        findNavController().navigate(FavoriteDishesFragmentDirections.navigateFromFavoriteDishesToDishDetailsFragment(
            favDish
        ))

        if(requireActivity() is MainActivity)
        {
            (activity as MainActivity?)?.hideBottomNavigationView()
        }
    }

    fun deleteDish(favDish: FavDish) {
        val alertDialog: AlertDialog = AlertDialog.Builder(requireActivity())
            .setTitle(resources.getString(R.string.title_delete_dish))
            .setMessage(resources.getString(R.string.msg_delete_dish_dialog, favDish.title))
            .setIcon(R.drawable.ic_warning)
            .setPositiveButton(resources.getString(R.string.lbl_yes)){ _, _ ->
                favDishViewModel.delete(favDish)
            }
            .setNegativeButton(resources.getString(R.string.lbl_no)){ _, _ ->
                //
            }
            .setCancelable(false)
            .create()

        alertDialog.show()
    }

    override fun onResume() {
        super.onResume()

        if(requireActivity() is MainActivity)
        {
            (activity as MainActivity?)?.showBottomNavigationView()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }
}