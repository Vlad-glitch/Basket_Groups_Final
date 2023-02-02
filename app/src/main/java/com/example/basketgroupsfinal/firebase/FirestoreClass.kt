package com.example.basketgroupsfinal.firebase

import android.app.Activity
import android.util.Log
import android.widget.Toast
import com.example.basketgroupsfinal.activities.*
import com.example.basketgroupsfinal.models.Place
import com.example.basketgroupsfinal.models.User
import com.example.basketgroupsfinal.utils.Constants
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions

class FirestoreClass {

    private val mFireStore = FirebaseFirestore.getInstance()

    fun registerUser(activity: SignUpActivity, userInfo: User) {

        mFireStore.collection(Constants.USERS)
            // Document ID for users fields. Here the document it is the User ID.
            .document(getCurrentUserID())
            // Here the userInfo are Field and the SetOption is set to merge. It is for if we wants to merge
            .set(userInfo, SetOptions.merge())
            .addOnSuccessListener {

                // Here call a function of base activity for transferring the result to it.
                activity.userRegisteredSuccess()
            }
            .addOnFailureListener { e ->
                Log.e(
                    activity.javaClass.simpleName,
                    "Error writing document",
                    e
                )
            }
    }

    fun loadUserData(activity: Activity) {

        // Here we pass the collection name from which we wants the data.
        mFireStore.collection(Constants.USERS)
            // The document id to get the Fields of user.
            .document(getCurrentUserID())
            .get()
            .addOnSuccessListener { document ->
                Log.i(
                    activity.javaClass.simpleName, document.toString()
                )

                // TODO (STEP 3: Pass the result to base activity.)
                // START
                // Here we have received the document snapshot which is converted into the User Data model object.
                val loggedInUser = document.toObject(User::class.java)!!

                when (activity) {
                    is SignInActivity -> {
                        activity.signInSuccess(loggedInUser)
                    }
                    is MainActivity -> {
                        activity.updateNavigationUserDetails(loggedInUser)
                    }
                    is MyProfileActivity -> {
                        activity.setUserDataInUI(loggedInUser)
                    }
                    // END
                }
                // END
            }
            .addOnFailureListener { e ->
                when (activity) {
                    is SignInActivity -> {
                        activity.hideProgressDialog()
                    }
                    is MainActivity -> {
                        activity.hideProgressDialog()
                    }
                    is MyProfileActivity -> {
                        activity.hideProgressDialog()
                    }

                    // END
                }

                Log.e(
                    activity.javaClass.simpleName,
                    "Error while getting loggedIn user details",
                    e
                )
            }
    }


    fun getCurrentUserID(): String {

        val currentUser = FirebaseAuth.getInstance().currentUser
        var currentUserID = ""
        if (currentUser != null) {
            currentUserID = currentUser.uid
        }
        return currentUserID
    }

    fun updateUserProfileData(activity: MyProfileActivity, userHashMap: HashMap<String, Any>) {
        mFireStore.collection(Constants.USERS) // Collection Name
            .document(getCurrentUserID()) // Document ID
            .update(userHashMap) // A hashmap of fields which are to be updated.
            .addOnSuccessListener {
                // Profile data is updated successfully.
                Log.i(activity.javaClass.simpleName, "Profile Data updated successfully!")

                Toast.makeText(activity, "Profile updated successfully!", Toast.LENGTH_SHORT).show()

                // Notify the success result.
                activity.profileUpdateSuccess()
            }
            .addOnFailureListener { e ->
                activity.hideProgressDialog()
                Log.e(
                    activity.javaClass.simpleName,
                    "Error while updating user data.",
                    e
                )
            }
    }

    fun getPlacesList(activity: MainActivity){
        mFireStore.collection(Constants.PLACE)
            .get()
            .addOnSuccessListener {
                document ->
                Log.i(activity.javaClass.simpleName, document.documents.toString())
                val placesList: ArrayList<Place> = ArrayList()

                // A for loop as per the list of documents to convert them into Boards ArrayList.
                for (i in document.documents) {

                    val place = i.toObject(Place::class.java)!!
                    place.id = i.id
                    placesList.add(place)
                }
                activity.setupBasketPlaces(placesList)
                //activity.setupBasketPlaces(null)
            }.addOnFailureListener { e ->

                activity.hideProgressDialog()
                Log.e(activity.javaClass.simpleName, "Error while getting places.", e)
            }

    }

    fun getPlaceDetails(activity: PlaceDetailsActivity, placeDocumentId: String) {
        mFireStore.collection(Constants.PLACE)
            .document(placeDocumentId)
            .get()
            .addOnSuccessListener {
                    document ->
                Log.i(activity.javaClass.simpleName, document.toString())
                activity.placeDetails(document.toObject(Place::class.java)!!)
            }.addOnFailureListener { e ->

                activity.hideProgressDialog()
                Log.e(activity.javaClass.simpleName, "Error while getting places.", e)
            }

    }

    fun addPlace(activity: AddBasketPlaceActivity, place: Place){
        mFireStore.collection(Constants.PLACE)
            .document()
            .set(place, SetOptions.merge())
            .addOnSuccessListener {
                Toast.makeText(activity, "Place added", Toast.LENGTH_SHORT).show()
                activity.placeCreatedSuccessfully()

            }.addOnFailureListener {
                Toast.makeText(activity, "Error adding place", Toast.LENGTH_SHORT).show()
                activity.hideProgressDialog()
            }

    }

}