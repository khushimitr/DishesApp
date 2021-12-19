package com.example.favdish.view.activities

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.provider.Settings
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.example.favdish.R
import com.example.favdish.application.FavDishApplication
import com.example.favdish.databinding.ActivityAddUpdateDishBinding
import com.example.favdish.databinding.DialogCustomImageSelectionBinding
import com.example.favdish.databinding.DialogCustomListBinding
import com.example.favdish.model.entities.FavDish
import com.example.favdish.util.Constants
import com.example.favdish.util.Constants.DISH_CATEGORY
import com.example.favdish.util.Constants.DISH_COOKING_TIME
import com.example.favdish.util.Constants.DISH_TYPE
import com.example.favdish.util.Constants.IMAGE_DIR
import com.example.favdish.util.Constants.dishCategories
import com.example.favdish.util.Constants.dishCookTime
import com.example.favdish.util.Constants.dishTypes
import com.example.favdish.view.adapters.CustomListItemAdapter
import com.example.favdish.viewmodel.FavDishViewModel
import com.example.favdish.viewmodel.FavDishViewModelFactory
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream
import java.util.*

class AddUpdateDishActivity : AppCompatActivity(), View.OnClickListener {

    private lateinit var launchCameraActivity: ActivityResultLauncher<Intent>
    private lateinit var launchGalleryActivity: ActivityResultLauncher<Intent>
    private lateinit var binding: ActivityAddUpdateDishBinding
    private lateinit var customRVDialog: Dialog
    private var imagePath: String = ""

    private var favDishDetails : FavDish? = null

    private val mFavDishViewModel: FavDishViewModel by viewModels {
        FavDishViewModelFactory((application as FavDishApplication).repository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityAddUpdateDishBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if(intent.hasExtra(Constants.EXTRA_DISH_DETAILS))
        {
            favDishDetails = intent.getParcelableExtra(Constants.EXTRA_DISH_DETAILS)
        }

        setUpActionBar()

        favDishDetails?.let {
            if(it.id != 0)
            {
                imagePath = it.image
                Glide.with(this)
                    .load(imagePath)
                    .into(binding.ivDishImage)

                binding.etTitle.setText(it.title)
                binding.etType.setText(it.type)
                binding.etCategory.setText(it.category)
                binding.etDirectionToCook.setText(it.directionToCook)
                binding.etIngredients.setText(it.ingredients)
                binding.etCookingTime.setText(it.cookingTime)

                binding.btnAddDish.text = resources.getString(R.string.lbl_update_dish)
            }
        }

        binding.ivAddDishImage.setOnClickListener(this)
        binding.etType.setOnClickListener(this)
        binding.etCategory.setOnClickListener(this)
        binding.etCookingTime.setOnClickListener(this)
        binding.btnAddDish.setOnClickListener(this)

        launchCameraActivity =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == Activity.RESULT_OK) {
                    val intent = result.data
                    val bitmap = intent?.extras?.get("data") as Bitmap
                    Glide.with(this)
                        .load(bitmap)
                        .centerCrop()
                        .into(binding.ivDishImage)

                    imagePath = saveImageToInternalStorage(bitmap)
                    Log.i("IMAGEPATH", imagePath)

                    binding.ivAddDishImage.setImageDrawable(
                        ContextCompat.getDrawable(
                            this,
                            R.drawable.ic_edit
                        )
                    )
                }
            }

        launchGalleryActivity =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == Activity.RESULT_OK) {
                    val uri = result.data?.data
                    Glide.with(this)
                        .load(uri)
                        .centerCrop()
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .listener(object : RequestListener<Drawable> {
                            override fun onLoadFailed(
                                e: GlideException?,
                                model: Any?,
                                target: Target<Drawable>?,
                                isFirstResource: Boolean
                            ): Boolean {
                                Log.e("LOAD_FAILED", "Failed to Load Image")
                                return false
                            }

                            override fun onResourceReady(
                                resource: Drawable?,
                                model: Any?,
                                target: Target<Drawable>?,
                                dataSource: DataSource?,
                                isFirstResource: Boolean
                            ): Boolean {
                                resource?.let {
                                    val bitmap = resource.toBitmap()
                                    imagePath = saveImageToInternalStorage(bitmap)
                                    Log.i("IMAGEPATH", imagePath)
                                }

                                return false
                            }

                        })
                        .into(binding.ivDishImage)

                    binding.ivAddDishImage.setImageDrawable(
                        ContextCompat.getDrawable(
                            this,
                            R.drawable.ic_edit
                        )
                    )
                }

            }
    }

    private fun setUpActionBar() {
        setSupportActionBar(binding.toolbarAddDishActivity)
        if(favDishDetails != null && favDishDetails!!.id != 0)
        {
            supportActionBar?.let{
                it.title = resources.getString(R.string.title_edit_dish)
            }
        }else{
            supportActionBar?.let{
                it.title = resources.getString(R.string.title_add_dish)
            }
        }

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbarAddDishActivity.setNavigationOnClickListener {
            onBackPressed()
        }
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.ivAddDishImage -> {
                customImageSelectionDialog()
                return
            }
            R.id.etType -> {
                customItemsDialog(
                    resources.getString(R.string.title_select_dish_type),
                    dishTypes(),
                    DISH_TYPE
                )
                return
            }
            R.id.etCategory -> {
                customItemsDialog(
                    resources.getString(R.string.title_select_dish_category),
                    dishCategories(),
                    DISH_CATEGORY
                )
                return
            }
            R.id.etCookingTime -> {
                customItemsDialog(
                    resources.getString(R.string.title_select_dish_cooking_time),
                    dishCookTime(),
                    DISH_COOKING_TIME
                )
                return
            }
            R.id.btnAddDish -> {

                val title = binding.etTitle.text.toString().trim { it <= ' ' }
                val type = binding.etType.text.toString().trim { it <= ' ' }
                val category = binding.etCategory.text.toString().trim { it <= ' ' }
                val ingredients = binding.etIngredients.text.toString().trim { it <= ' ' }
                val cookingTimeInMinutes = binding.etCookingTime.text.toString().trim { it <= ' ' }
                val cookingDirection = binding.etDirectionToCook.text.toString().trim { it <= ' ' }

                when {
                    TextUtils.isEmpty(imagePath) -> {
                        Toast.makeText(
                            this,
                            resources.getString(R.string.err_msg_select_dish_image),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    TextUtils.isEmpty(title) -> {
                        Toast.makeText(
                            this,
                            resources.getString(R.string.err_msg_enter_dish_title),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    TextUtils.isEmpty(type) -> {
                        Toast.makeText(
                            this,
                            resources.getString(R.string.err_msg_select_dish_type),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    TextUtils.isEmpty(category) -> {
                        Toast.makeText(
                            this,
                            resources.getString(R.string.err_msg_select_dish_category),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    TextUtils.isEmpty(ingredients) -> {
                        Toast.makeText(
                            this,
                            resources.getString(R.string.err_msg_enter_dish_ingredients),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    TextUtils.isEmpty(cookingTimeInMinutes) -> {
                        Toast.makeText(
                            this,
                            resources.getString(R.string.err_msg_select_dish_cooking_time),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    TextUtils.isEmpty(cookingDirection) -> {
                        Toast.makeText(
                            this,
                            resources.getString(R.string.err_msg_enter_dish_cooking_instructions),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    else -> {
                        var dishId = 0
                        var imageSource = Constants.DISH_IMAGE_SOURCE_LOCAL
                        var isFavouriteDish = false

                        favDishDetails?.let {
                            if(it.id != 0)
                            {
                                dishId = it.id
                                imageSource = it.imageSource
                                isFavouriteDish = it.favoriteDish
                            }
                        }

                        val favDish : FavDish = FavDish(
                            imagePath,
                            title,
                            type,
                            category,
                            ingredients,
                            imageSource,
                            cookingTimeInMinutes,
                            cookingDirection,
                            isFavouriteDish,
                            dishId
                        )

                        if(dishId == 0)
                        {
                            mFavDishViewModel.insert(favDish)
                            Toast.makeText(
                                this,
                                "You successfully added new dish.",
                                Toast.LENGTH_SHORT
                            ).show()

                            Log.i("INSERTION", "Success!!")
                        }
                        else
                        {
                            mFavDishViewModel.update(favDish)
                            Toast.makeText(
                                this,
                                "You successfully updated this dish.",
                                Toast.LENGTH_SHORT
                            ).show()

                            Log.i("UPDATING", "Success!!")
                        }
                        finish()
                    }
                }
            }
        }
    }

    private fun customImageSelectionDialog() {

        val dialog = Dialog(this)
        val dbinding: DialogCustomImageSelectionBinding =
            DialogCustomImageSelectionBinding.inflate(layoutInflater)
        dialog.setContentView(dbinding.root)

        dbinding.tvDgCamera.setOnClickListener {
            if (hasCameraPermission()) {
                //TODO INTENT
                val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                launchCameraActivity.launch(intent)
            } else {
                requestPermission()
            }
            dialog.dismiss()
        }

        dbinding.tvDgGallery.setOnClickListener {
            if (hasReadStoragePermission()) {
                val intent = Intent(Intent.ACTION_PICK).apply {
                    type = "image/*"
                }
                launchGalleryActivity.launch(intent)
            } else {
                requestPermission()
            }
            dialog.dismiss()
        }
        dialog.show()
    }


        private fun showRationaleAlertDialog() {
            AlertDialog.Builder(this)
                .setMessage("It looks like you have not provided permissions, You can enable it under settings")
                .setPositiveButton("GO TO SETTINGS") { _, _ ->
                    try {
                        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                        val uri = Uri.fromParts("package", packageName, null)
                        intent.data = uri
                        startActivity(intent)
                    } catch (error: ActivityNotFoundException) {
                        error.printStackTrace()
                    }
                }
                .setNegativeButton("CANCEL") { dialogInterface, _ ->
                    dialogInterface.dismiss()
                }.show()
        }


    private fun hasReadStoragePermission() = ActivityCompat.checkSelfPermission(
        this,
        Manifest.permission.READ_EXTERNAL_STORAGE
    ) == PackageManager.PERMISSION_GRANTED

    private fun hasWriteStoragePermission() = ActivityCompat.checkSelfPermission(
        this,
        Manifest.permission.WRITE_EXTERNAL_STORAGE
    ) == PackageManager.PERMISSION_GRANTED

    private fun hasCameraPermission() = ActivityCompat.checkSelfPermission(
        this,
        Manifest.permission.CAMERA
    ) == PackageManager.PERMISSION_GRANTED

    private fun requestPermission() {
        var permissions = mutableListOf<String>()
        if (!hasReadStoragePermission())
            permissions.add(Manifest.permission.READ_EXTERNAL_STORAGE)
        if (!hasCameraPermission())
            permissions.add(Manifest.permission.CAMERA)
        if (!hasWriteStoragePermission())
            permissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)

        if (permissions.isNotEmpty()) {
            ActivityCompat.requestPermissions(this, permissions.toTypedArray(), 0)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        var flag = false
        if (requestCode == 0 && grantResults.isNotEmpty()) {
            for (i in grantResults.indices) {
                if (grantResults[i] == PackageManager.PERMISSION_DENIED) {
                    flag = true
                    break
                }
            }

            if (flag)
                showRationaleAlertDialog()
        }
    }

    private fun customItemsDialog(
        title: String,
        itemList: List<String>,
        selection: String
    ) {
        customRVDialog = Dialog(this)
        val rvBinding: DialogCustomListBinding = DialogCustomListBinding.inflate(layoutInflater)
        customRVDialog.setContentView(rvBinding.root)

        rvBinding.tvTitle.text = title

        rvBinding.rvList.layoutManager = LinearLayoutManager(this)
        val adapter = CustomListItemAdapter(this, itemList, selection)
        rvBinding.rvList.adapter = adapter

        customRVDialog.show()
    }

    fun selectedListItem(item: String, selection: String) {
        when (selection) {
            Constants.DISH_TYPE -> {
                customRVDialog.dismiss()
                binding.etType.setText(item)
            }
            Constants.DISH_CATEGORY -> {
                customRVDialog.dismiss()
                binding.etCategory.setText(item)
            }
            Constants.DISH_COOKING_TIME -> {
                customRVDialog.dismiss()
                binding.etCookingTime.setText(item)
            }
        }
    }

    private fun saveImageToInternalStorage(bitmap: Bitmap): String {
        val wrapper = ContextWrapper(applicationContext)
        var file = wrapper.getDir(IMAGE_DIR, Context.MODE_PRIVATE)
        file = File(file, "${UUID.randomUUID()}.jpg")

        try {
            val stream: OutputStream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream)
            stream.flush()
            stream.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }

        return file.absolutePath
    }
}