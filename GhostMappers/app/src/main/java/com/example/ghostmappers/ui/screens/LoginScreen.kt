package com.example.ghostmappers.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Login
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.example.ghostmappers.data.UserRepository
import com.example.ghostmappers.ui.theme.Beige
import com.example.ghostmappers.ui.theme.Maron
import com.example.ghostmappers.ui.theme.Orange
import com.google.firebase.auth.FirebaseAuth

@Composable
fun LoginScreen(auth: FirebaseAuth, onLoginSuccess: () -> Unit) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val userRepository = UserRepository()

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }

    var isLoading by remember { mutableStateOf(false) }
    // Toggle between Login and Sign Up mode
    var isLoginMode by remember { mutableStateOf(true) }

    val accentColor = if (isLoginMode) Maron else Orange

    val modeIcon =
        if (isLoginMode) Icons.AutoMirrored.Filled.Login
        else Icons.Default.PersonAdd

    val subtitleText =
        if (isLoginMode) "Welcome back! Please log in."
        else "Create your account to get started."

    fun performAuth() {
        if (email.isEmpty() || password.isEmpty() || (!isLoginMode && username.isEmpty())) {
            Toast.makeText(context, "Please fill all fields.", Toast.LENGTH_SHORT).show()
            return
        }

        isLoading = true
        focusManager.clearFocus()

        if (isLoginMode) {
            // --- LOGIN LOGIC ---
            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    isLoading = false
                    if (task.isSuccessful) {
                        onLoginSuccess()
                    } else {
                        Toast.makeText(
                            context,
                            "Email or password is incorrect",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
        } else {
            // --- REGISTRATION LOGIC ---
            auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener { task ->
                isLoading = false
                if (task.isSuccessful) {
                    val uid = auth.currentUser!!.uid

                    val result = userRepository.createNewAccount(uid, username, email)
                    if (result.isSuccess) {
                        onLoginSuccess()
                    } else {
                        Toast.makeText(
                            context,
                            "Account created but username not saved.",
                            Toast.LENGTH_LONG
                        ).show()
                    }


                } else {
                    Toast.makeText(context, "Error: ${task.exception?.message}", Toast.LENGTH_SHORT)
                        .show()
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Beige)
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = modeIcon,
            contentDescription = "Mode Icon",
            tint = accentColor,
            modifier = Modifier.size(70.dp)
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = if (isLoginMode) "Login" else "Create Account",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Medium,
            color = Color.Black
        )

        Text(
            text = subtitleText,
            color = Color.Black,
            style = MaterialTheme.typography.bodyMedium
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (!isLoginMode) {
            TextField(
                value = username,
                onValueChange = { username = it },
                label = { Text("Username") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                keyboardActions = KeyboardActions(
                    onNext = { focusManager.moveFocus(FocusDirection.Down) }
                ),
                colors = OutlinedTextFieldDefaults.colors(
                    cursorColor = Color.Black,
                    focusedLabelColor = Color.Black,
                    unfocusedLabelColor = Color.Black.copy(0.7f),
                    unfocusedTextColor = Color.Black.copy(0.7f),
                    focusedTextColor = Color.Black,
                    unfocusedContainerColor = Color.White.copy(alpha = 0.7f),
                    focusedContainerColor = Color.White.copy(alpha = 0.7f),
                    unfocusedBorderColor = Maron.copy(0.7f),
                    focusedBorderColor = Maron
                )
            )

            Spacer(modifier = Modifier.height(8.dp))
        }

        TextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Email,
                imeAction = ImeAction.Next
            ),
            keyboardActions = KeyboardActions(
                onNext = { focusManager.moveFocus(FocusDirection.Down) }
            ),
            colors = OutlinedTextFieldDefaults.colors(
                cursorColor = Color.Black,
                focusedLabelColor = Color.Black,
                unfocusedLabelColor = Color.Black.copy(0.7f),
                unfocusedTextColor = Color.Black.copy(0.7f),
                focusedTextColor = Color.Black,
                unfocusedContainerColor = Color.White.copy(alpha = 0.7f),
                focusedContainerColor = Color.White.copy(alpha = 0.7f),
                unfocusedBorderColor = Maron.copy(0.7f),
                focusedBorderColor = Maron
            )
        )

        Spacer(modifier = Modifier.height(8.dp))

        TextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true, // Prevents newline characters
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Done // Shows "Checkmark" or "Go"
            ),
            keyboardActions = KeyboardActions(
                onDone = {
                    performAuth() // Submits the form
                }
            ),
            colors = OutlinedTextFieldDefaults.colors(
                cursorColor = Color.Black,
                focusedLabelColor = Color.Black,
                unfocusedLabelColor = Color.Black.copy(0.7f),
                unfocusedTextColor = Color.Black.copy(0.7f),
                focusedTextColor = Color.Black,
                unfocusedContainerColor = Color.White.copy(alpha = 0.7f),
                focusedContainerColor = Color.White.copy(alpha = 0.7f),
                unfocusedBorderColor = Maron.copy(0.7f),
                focusedBorderColor = Maron
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (isLoading) {
            CircularProgressIndicator()
        } else {
            Button(
                onClick = { performAuth() },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = accentColor)
            ) {
                Text(
                    if (isLoginMode) "Login" else "Sign Up",
                    color = Color.White
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Toggle Button
        TextButton(onClick = { isLoginMode = !isLoginMode }) {
            Text(
                if (isLoginMode) "Don't have an account? Sign Up"
                else "Already have an account? Login",
                color = Color.Black.copy(alpha = 0.7f),
                fontWeight = FontWeight.Medium,
            )
        }
    }

}