package com.example.kino.Activities

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.kino.AccountClasses.LoginValidationData
import com.example.kino.AccountClasses.Token
import com.example.kino.ApiKey
import com.example.kino.R
import com.example.kino.RetrofitService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext

class SignInActivity : AppCompatActivity(), CoroutineScope {

    private val job = Job()
    private lateinit var receivedToken: String
    private lateinit var loginValidationData: LoginValidationData
    private lateinit var token: Token
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var wrongDataText: TextView
    private lateinit var signInButton: Button
    private lateinit var username: EditText
    private lateinit var password: EditText
    private lateinit var registrationLink: TextView
    private lateinit var progressBar: ProgressBar

    private var sessionId: String = ""


    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + job

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_in)

        sharedPreferences =
            getSharedPreferences(getString(R.string.preference_file), Context.MODE_PRIVATE)

        if (sharedPreferences.contains(getString(R.string.session_id))) {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }
        bindViews()
    }

    private fun bindViews() {
        username = findViewById(R.id.evUsername)
        password = findViewById(R.id.evPassword)
        signInButton = findViewById(R.id.btnSignIn)
        wrongDataText = findViewById(R.id.tvWrongData)
        registrationLink = findViewById(R.id.tvAccountLink)

        progressBar = findViewById(R.id.progressBar)
        progressBar.visibility = View.GONE

        registrationLink.setOnClickListener {
            val browserIntent =
                Intent(Intent.ACTION_VIEW, Uri.parse("https://www.themoviedb.org/account/signup"))
            startActivity(browserIntent)
        }

        signInButton.setOnClickListener {
            progressBar.visibility = View.VISIBLE
            createTokenRequest()
        }
    }


    private fun createTokenRequest() {
        launch {
            try {
                val response = RetrofitService.getPostApi().createRequestToken(ApiKey)
                if (response.isSuccessful) {
                    val requestedToken = response.body()
                    if (requestedToken != null) {
                        receivedToken = requestedToken.token
                        loginValidationData = LoginValidationData(
                            username.text.toString(),
                            password.text.toString(), receivedToken
                        )
                        validateWithLogin()
                    }

                } else {
                    Toast.makeText(this@SignInActivity, "Error occured", Toast.LENGTH_SHORT).show()
                    progressBar.visibility = View.GONE
                    receivedToken = ""
                }
            } catch (e: Exception) {
                Toast.makeText(this@SignInActivity, "Internet error", Toast.LENGTH_SHORT).show()
                progressBar.visibility = View.GONE
                receivedToken = ""
            }
        }
    }

    private fun validateWithLogin() {
        launch {
            try {
                val response =
                    RetrofitService.getPostApi().validateWithLogin(ApiKey, loginValidationData)

                    if (response.isSuccessful) {
                        token = Token(receivedToken)
                        createSession()
                    } else {
                        wrongDataText.text = "Wrong data"
                        progressBar.visibility = View.GONE
                    }
            } catch (e: Exception) {
                wrongDataText.text = "Wrong data"
                progressBar.visibility = View.GONE
            }
        }
    }

    private fun createSession() {
        launch {
            try {
                val response = RetrofitService.getPostApi().createSession(ApiKey, token)
                if (response.isSuccessful) {
                    sessionId = response.body()?.sessionId.toString()

                    saveToSharedPreferences()

                    val intent = Intent(this@SignInActivity, MainActivity::class.java)
                    startActivity(intent)

                } else {
                    progressBar.visibility = View.GONE
                    Toast.makeText(this@SignInActivity, "Error occurred", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                progressBar.visibility = View.GONE
                Toast.makeText(this@SignInActivity, "Error occurred", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun saveToSharedPreferences() {

        val editor = sharedPreferences.edit()
        editor.putString(getString(R.string.username), username.text.toString())
        editor.putString(getString(R.string.session_id), sessionId)
        editor.apply()
    }

}
