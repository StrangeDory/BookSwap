package com.example.bookswap

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import java.util.*


// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [SignInFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class SignInFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    private var auth: FirebaseAuth = Firebase.auth
    private val databaseReference = Firebase.database.reference
    private lateinit var client: GoogleSignInClient
    private val storageRef = Firebase.storage.reference
    private lateinit var callbackManager: CallbackManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_sign_in, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        callbackManager = CallbackManager.Factory.create()
        LoginManager.getInstance().registerCallback(callbackManager,
            object : FacebookCallback<LoginResult> {
                override fun onCancel() {
                    // App code
                }

                override fun onError(exception: FacebookException) {
                    // App code
                }

                override fun onSuccess(result: LoginResult) {
                    val intent = Intent(activity, ForgotPasswordActivity::class.java)
                    activity?.startActivity(intent)
                    activity?.finish()
                }
            })

        view.findViewById<TextView>(R.id.signUpText).setOnClickListener {
            val viewPager2 = activity?.findViewById<ViewPager2>(R.id.viewPager2)
            if (viewPager2 != null) {
                viewPager2.currentItem = 1
            }
        }

        view.findViewById<Button>(R.id.signInBtn).setOnClickListener{
            if(checkAllFields(view)) {
                val email = view.findViewById<EditText>(R.id.editEmailSignIN).text.toString()
                val password = view.findViewById<EditText>(R.id.editPassSignIn).text.toString()
                auth.signInWithEmailAndPassword(email, password).addOnCompleteListener{
                    if(it.isSuccessful) {
                        val intent = Intent(activity, UserProfileActivity::class.java)
                        activity?.startActivity(intent)
                        activity?.finish()
                    } else {
                        Log.e("error", it.exception.toString())
                    }
                }
            }
        }

        view.findViewById<TextView>(R.id.tvForgotPass).setOnClickListener{
            val intent = Intent(activity, ForgotPasswordActivity::class.java)
            activity?.startActivity(intent)
            activity?.finish()
        }

        view.findViewById<ImageView>(R.id.googleLoginBtn).setOnClickListener {
            val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken("1093245781995-ft2glisi8dfd2bghqklb87janf6cn06h.apps.googleusercontent.com")
                .requestEmail()
                .build()
            client = GoogleSignIn.getClient(requireActivity(), gso)

            val signInIntent = client.signInIntent
            startActivityForResult(signInIntent, 1001)
        }

        view.findViewById<ImageView>(R.id.fbLoginBtn).setOnClickListener{
            LoginManager.getInstance().logInWithReadPermissions(this, listOf("public_profile"));
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        callbackManager.onActivityResult(requestCode, resultCode, data);

        if(requestCode == 1001) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try{
                val account = task.getResult(ApiException::class.java)
                Log.d("auth", "Firebase auth with google " + account.id)
                firebaseAuthWithGoogle(account.idToken!!)
            } catch (e: Exception) {
                Log.e("error", "Google sign in failed! " + e.message.toString())
            }
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener {
                if(it.isSuccessful) {
                    Log.d("signIn", "Sign in with credential is success")
                    for (profile in auth.currentUser?.providerData!!) {
                        val name = profile.displayName
                        val email = profile.email
                        val photoUrl: Uri? = profile.photoUrl
                        databaseReference.child("users").child(auth.currentUser!!.uid).child("email").setValue(email).addOnCompleteListener {it1 ->
                            if(it1.isSuccessful){

                            }else{
                                Log.e("error", "Faild to write email!")
                            }
                        }
                        databaseReference.child("users").child(auth.currentUser!!.uid).child("fullname").setValue(name).addOnCompleteListener {it1 ->
                            if(it1.isSuccessful){

                            }else{
                                Log.e("error", "Faild to write fullname!")
                            }
                        }
                        if(photoUrl != null)
                            storageRef.child("users/" + auth.currentUser!!.uid).putFile(photoUrl).addOnSuccessListener { taskSnapshot ->
                                val downloadUrl = taskSnapshot.storage.downloadUrl
                                Log.d("photo upload successful", downloadUrl.toString())
                            }
                                .addOnFailureListener { exception ->
                                    Log.e("photo upload failed", exception.message.toString())
                                }
                    }
                    val intent = Intent(activity, UserProfileActivity::class.java)
                    activity?.startActivity(intent)
                    activity?.finish()
                } else {
                    Log.e("error", "Sign in with credential failed " + it.exception.toString())
                }
            }
    }

    private fun checkAllFields(view: View): Boolean {
        val email = view.findViewById<EditText>(R.id.editEmailSignIN)
        val password = view.findViewById<EditText>(R.id.editPassSignIn)
        if (email.text.toString().isEmpty()) {
            email.error = "Email is required!"
            return false
        }
        if(!Patterns.EMAIL_ADDRESS.matcher(email.text.toString()).matches()) {
            email.error = "Check email format!"
            return false
        }
        if (password.text.toString().isEmpty()) {
            password.error = "Password is required!"
            return false
        }
        if(password.text.toString().length < 7) {
            password.error = "Password should be at least 7 characters!"
            return false
        }
        return true
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment SignInFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            SignInFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}
