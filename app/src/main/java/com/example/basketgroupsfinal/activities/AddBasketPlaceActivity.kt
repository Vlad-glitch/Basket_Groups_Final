package com.example.basketgroupsfinal.activities

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.webkit.MimeTypeMap
import android.widget.Toast
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.example.basketgroupsfinal.R
import com.example.basketgroupsfinal.databinding.ActivityAddBasketPlaceBinding
import com.example.basketgroupsfinal.databinding.ActivityMyProfileBinding
import com.example.basketgroupsfinal.firebase.FirestoreClass
import com.example.basketgroupsfinal.models.Place
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.io.IOException
import com.example.basketgroupsfinal.utils.Constants

class AddBasketPlaceActivity : BaseActivity() {

    private var binding: ActivityAddBasketPlaceBinding? = null

    private var mSelectedImageFileUri: Uri? = null

    private var mPlaceImgURL : String = ""

    private var mLatitude: Double = 0.0 // A variable which will hold the latitude value.
    private var mLongitude: Double = 0.0 // A variable which will hold the longitude value.

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddBasketPlaceBinding.inflate(layoutInflater)
        setContentView(binding?.root)
        setupActionBar()

        binding?.tvAddImage?.setOnClickListener {
            choosePhotoFromGal()
        }

        binding?.btnSave?.setOnClickListener {
            //TODO
            when {
                binding?.etTitle?.text.isNullOrEmpty() -> {
                    Toast.makeText(this, "Please enter title", Toast.LENGTH_SHORT).show()
                }
                binding?.etDescription?.text.isNullOrEmpty() -> {
                    Toast.makeText(this, "Please enter description", Toast.LENGTH_SHORT)
                        .show()
                }
                binding?.etLocation?.text.isNullOrEmpty() -> {
                    Toast.makeText(this, "Please select location", Toast.LENGTH_SHORT)
                        .show()
                }
                mSelectedImageFileUri == null -> {
                    Toast.makeText(this, "Please add image", Toast.LENGTH_SHORT).show()
                }
                else -> {
                    uploadPlaceImg()
                }
            }
        }
    }

    private fun createPlace() {

        var place = Place(title = binding?.etTitle?.text.toString(),
            description = binding?.etDescription?.text.toString(),
            image = mSelectedImageFileUri.toString(),
            latitude = mLatitude, longitude = mLongitude
        )

        FirestoreClass().addPlace(this, place)
    }

    private fun uploadPlaceImg(){
        showProgressDialog(resources.getString(R.string.please_wait))

        if (mSelectedImageFileUri != null) {

            //getting the storage reference
            val sRef: StorageReference = FirebaseStorage.getInstance().reference.child(
                "PLACE_IMAGE" + System.currentTimeMillis() + "." + getFileExtension(
                    mSelectedImageFileUri
                )
            )

            //adding the file to reference
            sRef.putFile(mSelectedImageFileUri!!)
                .addOnSuccessListener { taskSnapshot ->
                    // The image upload is success
                    Log.i(
                        "Place Image URL",
                        taskSnapshot.metadata!!.reference!!.downloadUrl.toString()
                    )

                    // Get the downloadable url from the task snapshot
                    taskSnapshot.metadata!!.reference!!.downloadUrl
                        .addOnSuccessListener { uri ->
                            Log.i("Downloadable Image URL", uri.toString())

                            // assign the image url to the variable.
                            mPlaceImgURL = uri.toString()

                            // Call a function to update user details in the database.
                            createPlace()
                        }
                }
                .addOnFailureListener { exception ->
                    Toast.makeText(
                        this,
                        exception.message,
                        Toast.LENGTH_LONG
                    ).show()

                    hideProgressDialog()
                }
        }

    }

    private fun getFileExtension(uri: Uri?): String? {
        /*
         * MimeTypeMap: Two-way map that maps MIME-types to file extensions and vice versa.
         *
         * getSingleton(): Get the singleton instance of MimeTypeMap.
         *
         * getExtensionFromMimeType: Return the registered extension for the given MIME type.
         *
         * contentResolver.getType: Return the MIME type of the given content URL.
         */
        return MimeTypeMap.getSingleton().getExtensionFromMimeType(contentResolver.getType(uri!!))
    }

    fun placeCreatedSuccessfully() {

        hideProgressDialog()

        //finish()
    }


    private fun choosePhotoFromGal() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
            == PackageManager.PERMISSION_GRANTED
        ) {
            // TODO (Step 8: Call the image chooser function.)
            // START
            showImageChooser()
            // END
        } else {
            /*Requests permissions to be granted to this application. These permissions
             must be requested in your manifest, they should not be granted to your app,
             and they should have protection level*/
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                MyProfileActivity.READ_STORAGE_PERMISSION_CODE
            )
        }
    }

    private val getImage = registerForActivityResult(
        ActivityResultContracts.GetContent(),
        ActivityResultCallback {

            mSelectedImageFileUri = it

            try{
                Glide
                    .with(this@AddBasketPlaceActivity)
                    .load(it)
                    .centerCrop()
                    .placeholder(R.drawable.ic_user_place_holder)
                    .into(binding?.ivPlaceImage!!)
            } catch (e: IOException) {
                e.printStackTrace()
            }

        }
    )

    private fun showImageChooser() {
        getImage.launch("image/*")
    }

    private fun setupActionBar() {
        setSupportActionBar(binding?.toolbarAddActivity)

        val actionBar = supportActionBar
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.ic_baseline_arrow_back_ios_24)
            actionBar.title = resources.getString(R.string.add_place)
        }
    }
}