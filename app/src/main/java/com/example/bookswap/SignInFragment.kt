package com.example.bookswap

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
import com.google.firebase.ktx.Firebase

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
                        TODO() //enter to the account
                    } else {
                        Log.e("error", it.exception.toString())
                    }
                }
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