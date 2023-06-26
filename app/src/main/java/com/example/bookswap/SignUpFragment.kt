package com.example.bookswap

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.viewpager2.widget.ViewPager2
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [SignUpFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class SignUpFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    private var auth: FirebaseAuth = Firebase.auth
    private val databaseReference = Firebase.database.reference

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
        return inflater.inflate(R.layout.fragment_sign_up, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.findViewById<TextView>(R.id.signInText).setOnClickListener {
            val viewPager2 = activity?.findViewById<ViewPager2>(R.id.viewPager2)
            if (viewPager2 != null) {
                viewPager2.currentItem = 0
            }
        }

        view.findViewById<Button>(R.id.signUpBtn).setOnClickListener{
            if(checkAllFields(view)) {
                val email = view.findViewById<EditText>(R.id.editEmailSignUp).text.toString()
                val password = view.findViewById<EditText>(R.id.editPassSignUp).text.toString()
                auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener{
                    if(it.isSuccessful) {
                        val username = view.findViewById<EditText>(R.id.editNameSignUp).text.toString()
                        // store to Realtime Database
                        databaseReference.child("users").child(auth.currentUser!!.uid).child("email").setValue(email).addOnCompleteListener {it1 ->
                            if(it1.isSuccessful){

                            }else{
                                Log.e("error", "Faild to write email!")
                            }
                        }
                        databaseReference.child("users").child(auth.currentUser!!.uid).child("username").setValue(username).addOnCompleteListener {it1 ->
                            if(it1.isSuccessful){

                            }else{
                                Log.e("error", "Faild to write username!")
                            }
                        }

                        val intent = Intent(activity, UserProfileActivity::class.java)
                        activity?.startActivity(intent)
                        activity?.finish()
                    } else {
                        Log.e("error", it.exception.toString())
                    }
                }
            }
        }
    }

    private fun checkAllFields(view: View): Boolean {
        val name = view.findViewById<EditText>(R.id.editNameSignUp)
        val email = view.findViewById<EditText>(R.id.editEmailSignUp)
        val password = view.findViewById<EditText>(R.id.editPassSignUp)
        if (name.text.toString().isEmpty()) {
            name.error = "User name is required!"
            return false
        }
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
         * @return A new instance of fragment SignUpFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            SignUpFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}