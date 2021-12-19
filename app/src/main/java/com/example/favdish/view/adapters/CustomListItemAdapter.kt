package com.example.favdish.view.adapters

import android.app.Activity
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.example.favdish.databinding.ItemCustomListBinding
import com.example.favdish.view.activities.AddUpdateDishActivity
import com.example.favdish.view.activities.MainActivity
import com.example.favdish.view.fragments.AllDishesFragment

class CustomListItemAdapter(
    private val activity: Activity,
    private val listItems: List<String>,
    private val selection: String,
    private val fragment: Fragment? = null
) : RecyclerView.Adapter<CustomListItemAdapter.CustomListViewHolder>() {

    inner class CustomListViewHolder(binding: ItemCustomListBinding) :
        RecyclerView.ViewHolder(binding.root) {
        val tvText = binding.rvTvItem
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CustomListViewHolder {
        val binding: ItemCustomListBinding =
            ItemCustomListBinding.inflate(LayoutInflater.from(activity), parent, false)
        return CustomListViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CustomListViewHolder, position: Int) {
        val item = listItems[position]
        holder.tvText.text = item

        holder.itemView.setOnClickListener {
            if (activity is AddUpdateDishActivity) {
                    activity.selectedListItem(item, selection)
            }
            if(fragment is AllDishesFragment){
                fragment.filterSelection(item)
            }
        }
    }

    override fun getItemCount(): Int {
        return listItems.size
    }
}