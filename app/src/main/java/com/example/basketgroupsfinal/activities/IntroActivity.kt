package com.example.basketgroupsfinal.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.example.basketgroupsfinal.R
import com.example.basketgroupsfinal.databinding.ActivityIntroBinding
import com.example.basketgroupsfinal.firebase.FirestoreClass

class IntroActivity : BaseActivity() {

    private var binding: ActivityIntroBinding? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_intro)

        val currentUserID = FirestoreClass().getCurrentUserID()

        if (currentUserID.isNotEmpty()) {
            // Start the Main Activity
            startActivity(Intent(this, MainActivity::class.java))
        }


        binding = ActivityIntroBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        val controller = WindowInsetsControllerCompat(window, window.decorView)
        controller.hide(WindowInsetsCompat.Type.systemBars())

        binding?.btnSignUpIntro?.setOnClickListener {
            startActivity(Intent(this, SignUpActivity::class.java))
        }

        binding?.btnSignInIntro?.setOnClickListener {
            startActivity(Intent(this, SignInActivity::class.java))
        }

        binding?.btnForgotPassword?.setOnClickListener{
            startActivity(Intent(this, ForgotPasswordActivity::class.java))
        }

    }

}