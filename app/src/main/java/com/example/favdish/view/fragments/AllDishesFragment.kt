package com.example.favdish.view.fragments

import android.app.AlertDialog
import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.favdish.R
import com.example.favdish.application.FavDishApplication
import com.example.favdish.databinding.DialogCustomListBinding
import com.example.favdish.databinding.FragmentAllDishesBinding
import com.example.favdish.model.entities.FavDish
import com.example.favdish.util.Constants
import com.example.favdish.view.activities.AddUpdateDishActivity
import com.example.favdish.view.activities.MainActivity
import com.example.favdish.view.adapters.CustomListItemAdapter
import com.example.favdish.view.adapters.FavDishAdapter
import com.example.favdish.viewmodel.FavDishViewModel
import com.example.favdish.viewmodel.FavDishViewModelFactory

class AllDishesFragment : Fragment() {

    private lateinit var favDishAdapter: FavDishAdapter
    private lateinit var customListDialog: Dialog

    private val mFavDishViewModel: FavDishViewModel by viewModels {
        FavDishViewModelFactory((requireActivity().application as FavDishApplication).repository)
    }

    private lateinit var binding: FragmentAllDishesBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.rvDishCard.layoutManager = GridLayoutManager(requireActivity(), 2)
        favDishAdapter = FavDishAdapter(this@AllDishesFragment)
        binding.rvDishCard.adapter = favDishAdapter

        mFavDishViewModel.allDishesList.observe(viewLifecycleOwner) { dishes ->
            dishes.let {
                if (it.isNotEmpty()) {
                    binding.rvDishCard.visibility = View.VISIBLE
                    binding.tvDishCardPlaceholder.visibility = View.GONE

                    favDishAdapter.dishesList(it)
                } else {
                    binding.rvDishCard.visibility = View.GONE
                    binding.tvDishCardPlaceholder.visibility = View.VISIBLE
                }
            }
        }
    }

    fun showDishDetails(favDish: FavDish) {
        findNavController().navigate(
            AllDishesFragmentDirections.navigateFromAllDishesToDishDetails(
                favDish
            )
        )

        if (requireActivity() is MainActivity) {
            (activity as MainActivity?)?.hideBottomNavigationView()
        }
    }

    fun deleteDish(favDish: FavDish) {
        val alertDialog: AlertDialog = AlertDialog.Builder(requireActivity())
            .setTitle(resources.getString(R.string.title_delete_dish))
            .setMessage(resources.getString(R.string.msg_delete_dish_dialog, favDish.title))
            .setIcon(R.drawable.ic_warning)
            .setPositiveButton(resources.getString(R.string.lbl_yes)) { _, _ ->
                mFavDishViewModel.delete(favDish)
            }
            .setNegativeButton(resources.getString(R.string.lbl_no)) { _, _ ->
                //
            }
            .setCancelable(false)
            .create()

        alertDialog.show()
    }

    private fun filterDishesDialog() {
        customListDialog = Dialog(requireActivity())
        val dBinding: DialogCustomListBinding = DialogCustomListBinding.inflate(layoutInflater)

        customListDialog.setContentView(dBinding.root)
        dBinding.tvTitle.text = resources.getString(R.string.title_select_item_to_filter)
        val dishTypes = Constants.dishTypes()
        dishTypes.add(0, Constants.ALL_ITEMS)
        dBinding.rvList.layoutManager = LinearLayoutManager(requireActivity())
        val adapter =
            CustomListItemAdapter(requireActivity(), dishTypes, Constants.FILTER_SELECTION, this)
        dBinding.rvList.adapter = adapter
        customListDialog.show()
    }

    fun filterSelection(filteredString: String) {
        customListDialog.dismiss()

        if (filteredString == Constants.ALL_ITEMS) {
            mFavDishViewModel.allDishesList.observe(viewLifecycleOwner) { dishes ->
                dishes.let {
                    if (it.isNotEmpty()) {
                        binding.rvDishCard.visibility = View.VISIBLE
                        binding.tvDishCardPlaceholder.visibility = View.GONE

                        favDishAdapter.dishesList(it)
                    } else {
                        binding.rvDishCard.visibility = View.GONE
                        binding.tvDishCardPlaceholder.visibility = View.VISIBLE
                    }
                }
            }
        }else{
            mFavDishViewModel.filter(filteredString).observe(viewLifecycleOwner){dishes->
                dishes.let{
                    if (it.isNotEmpty()) {
                        binding.rvDishCard.visibility = View.VISIBLE
                        binding.tvDishCardPlaceholder.visibility = View.GONE

                        favDishAdapter.dishesList(it)
                    } else {
                        binding.rvDishCard.visibility = View.GONE
                        binding.tvDishCardPlaceholder.visibility = View.VISIBLE
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()

        if (requireActivity() is MainActivity) {
            (activity as MainActivity?)?.showBottomNavigationView()
        }
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAllDishesBinding.inflate(inflater, container, false)
        return binding.root
    }


    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_add_dishes, menu)

        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.miAddDish -> {
                startActivity(Intent(requireActivity(), AddUpdateDishActivity::class.java))
            }
            R.id.miFilterDish -> {
                filterDishesDialog()
            }
        }
        return true
    }
}