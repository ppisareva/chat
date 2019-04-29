package com.example.chat

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.text.Editable
import android.text.InputFilter
import android.text.TextWatcher
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.ProgressBar
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {



    private var userName: String? = null
    private lateinit var reference: DatabaseReference



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        userName = ANONYMOUS
        reference = FirebaseDatabase.getInstance().reference.child("messages")

        // Initialize progress bar
        progressBar!!.visibility = ProgressBar.INVISIBLE

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

            val massage = Message(messageEditText.text.toString(), userName, "///")
            Log.i("pushing data", massage.toString() )
            reference.push().setValue(massage)
            // Clear input box
            messageEditText!!.setText("")
        }
    }



    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return super.onOptionsItemSelected(item)
    }

    companion object {

        val ANONYMOUS = "anonymous"
        val MSG_LENGTH_LIMIT = 1000
    }
}