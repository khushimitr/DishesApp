package com.example.favdish.view.adapters

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.favdish.R
import com.example.favdish.databinding.ItemDishCardBinding
import com.example.favdish.model.entities.FavDish
import com.example.favdish.util.Constants
import com.example.favdish.view.activities.AddUpdateDishActivity
import com.example.favdish.view.fragments.AllDishesFragment
import com.example.favdish.view.fragments.FavoriteDishesFragment

class FavDishAdapter(private val fragment : Fragment) : RecyclerView.Adapter<FavDishAdapter.FavDishViewHolder>() {

    private var dishes : List<FavDish> = listOf()

    inner class FavDishViewHolder(binding : ItemDishCardBinding) : RecyclerView.ViewHolder(binding.root) {
        val ivDishImage  =  binding.ivDishImage
        val tvDishTitle = binding.tvDishTitle
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FavDishViewHolder {
        val binding : ItemDishCardBinding = ItemDishCardBinding.inflate(LayoutInflater.from(fragment.context),parent,false)

        return FavDishViewHolder(binding)
    }

    override fun onBindViewHolder(holder: FavDishViewHolder, position: Int) {
        val dish = dishes[position]
        Glide.with(fragment)
            .load(dish.image)
            .into(holder.ivDishImage)
        holder.tvDishTitle.text = dish.title
        holder.itemView.setOnClickListener{
            when(fragment){
                is AllDishesFragment -> fragment.showDishDetails(dish)
                is FavoriteDishesFragment -> fragment.showDishDetails(dish)
            }
        }

        holder.itemView.setOnLongClickListener(){
            val popup = PopupMenu(fragment.context, holder.ivDishImage)
            popup.menuInflater.inflate(R.menu.menu_adapter, popup.menu)


            popup.setOnMenuItemClickListener {
                if(it.itemId == R.id.miEditDish)
                {
                    val intent = Intent(fragment.requireActivity(), AddUpdateDishActivity::class.java)
                    intent.putExtra(Constants.EXTRA_DISH_DETAILS, dish)
                    fragment.requireActivity().startActivity(intent)
                }
                if(it.itemId == R.id.miDeleteDish)
                {
                    when(fragment){
                        is AllDishesFragment -> fragment.deleteDish(dish)
                        is FavoriteDishesFragment -> fragment.deleteDish(dish)
                    }
                }
                true
            }

            popup.show()
            true
        }
    }

    override fun getItemCount(): Int {
        return dishes.size
    }

    fun dishesList(list : List<FavDish>)
    {
        dishes = list
        notifyDataSetChanged()
    }
}