package com.example.basketgroupsfinal.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.example.basketgroupsfinal.R
import com.example.basketgroupsfinal.databinding.ActivityForgotPasswordBinding
import com.example.basketgroupsfinal.databinding.ActivitySignUpBinding
import com.google.firebase.auth.FirebaseAuth

class ForgotPasswordActivity : AppCompatActivity() {

    private var binding:ActivityForgotPasswordBinding? = null
    private var auth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityForgotPasswordBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        val controller = WindowInsetsControllerCompat(window, window.decorView)
        controller.hide(WindowInsetsCompat.Type.systemBars())

        setupActionBar()

        binding?.btnRecover?.setOnClickListener{
            val userEmail = binding?.etEmail?.text.toString()
            auth.sendPasswordResetEmail(userEmail).addOnCompleteListener{ task ->
                if(task.isSuccessful){

                    Toast.makeText(applicationContext, "We send a password reset email to the address", Toast.LENGTH_SHORT).show()
                    finish()

                }else{

                    Toast.makeText(applicationContext, task.exception?.localizedMessage, Toast.LENGTH_SHORT).show()


                }
            }

        }
    }

    private fun setupActionBar() {

        setSupportActionBar(binding?.toolbarForgotPasswordActivity)

        val actionBar = supportActionBar
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.ic_baseline_arrow_back_ios_24)
        }

        //binding?.toolbarSignUpActivity?.setNavigationOnClickListener { onBackPressed() }
    }
}