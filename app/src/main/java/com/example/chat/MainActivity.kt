package com.example.chat

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.text.Editable
import android.text.InputFilter
import android.text.TextWatcher
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import com.firebase.ui.auth.AuthUI
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.UploadTask
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*






class MainActivity : AppCompatActivity() {



    private var userName: String? = null
    private lateinit var fbDatabase: DatabaseReference
    private lateinit var fbAuth: FirebaseAuth
    private lateinit var fbListener : FirebaseAuth.AuthStateListener
    private lateinit var fbConfig : FirebaseRemoteConfig
    private lateinit var recyclerView: RecyclerView
    private lateinit var viewManager: RecyclerView.LayoutManager
    private lateinit var messageData:MutableList<Message>
    private lateinit var messageAdapter: MessagesAdapter
    private lateinit var fbStorage: StorageReference
    private  var postListener : ChildEventListener? = null
    private  var imageUri: Uri? = null




    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        userName = ANONYMOUS
        // remote configuration changes
        fbConfig = FirebaseRemoteConfig.getInstance()
        // initialize a database
        fbDatabase = FirebaseDatabase.getInstance().reference.child("messages")
        //initialize fb storage
        fbStorage= FirebaseStorage.getInstance().reference.child("chat_photos")
        // initializing firebase auth
        fbAuth = FirebaseAuth.getInstance()
        // setting up adapter
        setAdapter()
        // ImagePickerButton shows an image picker to upload a image for a message
        photoPickerButton!!.setOnClickListener {
            Log.i("image", "on image click")
            // TODO: Fire an intent to show an image picker
        }

        messageEditText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) {
            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                if (p0.toString().trim().length > 0) {
                    sendButton.setEnabled(true);
                } else {
                    sendButton.setEnabled(false);
                }

            }
        })
        messageEditText!!.filters = arrayOf<InputFilter>(InputFilter.LengthFilter(MSG_LENGTH_LIMIT))

        // Send button sends a message and clears the EditText
        sendButton!!.setOnClickListener {

            val message = Message(messageEditText.text.toString(), userName, imageUri.toString())
            Log.i("pushing data", message.toString() )
            fbDatabase.push().setValue(message)
            // Clear input box
            messageEditText!!.setText("")
            // change attach image to nothing selected
            photoPickerButton.setImageResource(R.drawable.ic_attach_image);


            //  move to last position
            recyclerView.scrollToPosition(messageData.size-1)
        }

        // ImagePickerButton shows an image picker to upload a image for a message
        photoPickerButton.setOnClickListener( {

                val intent = Intent(Intent.ACTION_GET_CONTENT)
                intent.type = "image/jpeg"
                intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true)
                startActivityForResult(Intent.createChooser(intent, "Complete action using"), RC_PHOTO_PICKER)
            }
        )

        fbListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
           var currentUser = firebaseAuth.currentUser
            if(currentUser!=null){
                // user signed in
                Log.i("used logged in ",currentUser.displayName)
                onSignedIn(currentUser.displayName)

            } else {
                // user signed out
                onSignedOut()
                // send to log in menu
                startActivityForResult(
                    AuthUI.getInstance()
                        .createSignInIntentBuilder().setIsSmartLockEnabled(false)
                        .setAvailableProviders(
                            Arrays.asList(
                                AuthUI.IdpConfig.GoogleBuilder().build(),
                                AuthUI.IdpConfig.EmailBuilder().build()

                            )
                        )
                        .build(),
                    RC_SIGN_IN
                )

            }
        }
    }

    private fun onSignedIn(displayName: String?) {
        // set user name that came from authorization
        userName = displayName
        // attach read listener only after user initialize
        attachDatabaseReadListener();

    }

    private fun attachDatabaseReadListener() {
        // set listener on new data in te database, only if it was not attached before
        if(postListener==null) {
            postListener = object : ChildEventListener {
                override fun onCancelled(p0: DatabaseError) {
                }

                override fun onChildMoved(p0: DataSnapshot, p1: String?) {
                }

                override fun onChildChanged(p0: DataSnapshot, p1: String?) {
                }

                override fun onChildRemoved(p0: DataSnapshot) {
                    Log.i("message removed", p0.toString())
                }

                override fun onChildAdded(dataSnapshot: DataSnapshot, p1: String?) {

                    //Get massage object and use the values to update the UI
                    val message = dataSnapshot.getValue(Message::class.java)
                   messageAdapter.messageData.add(message!!)
                    recyclerView.adapter!!.notifyDataSetChanged()
                    Log.i("message from DB", message.name)
                }
            }
            fbDatabase.addChildEventListener(postListener!!)
        }

    }

    private fun onSignedOut(){
        // unset user name
        userName = ANONYMOUS
        // user after log out should not be able to see messages or may get message duplicate
        messageAdapter.messageData.clear()
        // remove listener
        detachDatabaseReadListener()


    }

    private fun detachDatabaseReadListener() {
        // make shore that we attaching and ditaching listener only once
        if (postListener != null) {
            fbDatabase.removeEventListener(postListener!!)}
        postListener = null

    }

    private fun setAdapter() {
        messageData = mutableListOf()
        messageAdapter = MessagesAdapter(messageData)
        viewManager = LinearLayoutManager(this)


       recyclerView =  messageRecyclerView.apply {
            // use this setting to improve performance if you know that changes
            // in content do not change the layout size of the RecyclerView
            setHasFixedSize(true)

            // use a linear layout manager
            layoutManager = viewManager

            // specify an viewAdapter
            adapter = messageAdapter

        }
        // scroll till the end of list
        if(messageData!=null) {
            recyclerView.scrollToPosition(messageData.size - 1)
        }
    }


    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        // check if we press back button on log in screen

        if (requestCode==RC_SIGN_IN) {
            if (resultCode == Activity.RESULT_CANCELED) {
                // if yes finish the app
                finish()
            }
        }
            if(requestCode==RC_PHOTO_PICKER){
                if(resultCode== Activity.RESULT_OK){

                    // get image from image provider and send it to
                   val imageUri: Uri = data!!.data
                    // create reference (path) to upload image in storage
                    val photoReference:StorageReference = fbStorage.child(imageUri.lastPathSegment)
                    // upload image to storage
                    // Upload file to Firebase Storage
                    photoReference.putFile(imageUri)
                        .addOnSuccessListener(this,
                            OnSuccessListener<UploadTask.TaskSnapshot> { taskSnapshot ->
                                // When the image has successfully uploaded, we get its download URL
                                photoReference.downloadUrl.addOnCompleteListener () {taskSnapshot ->
                                     this.imageUri = taskSnapshot.result
                                    Log.i("image URI", taskSnapshot.result.toString())
                                    photoPickerButton.setImageResource(R.drawable.ic_image_attached);

                                }


                            }).addOnFailureListener {

                            Log.i("image failed to upload", it.localizedMessage)
                        }

                }
            }
        }



    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.sign_out -> {
                AuthUI.getInstance().signOut(this)
                return true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }

    override fun onResume() {
        super.onResume()
        fbAuth.addAuthStateListener(fbListener)
    }

    override fun onPause() {
        super.onPause()
        if (fbListener != null) {
            fbAuth.removeAuthStateListener(fbListener)
        	        }
        // if activity destroyed listener detached
         messageAdapter.messageData.clear()
         detachDatabaseReadListener()
	    }


    companion object {

        val ANONYMOUS = "anonymous"
        val RC_SIGN_IN = 1
        val RC_PHOTO_PICKER = 2
        val MSG_LENGTH_LIMIT = 1000
    }
}