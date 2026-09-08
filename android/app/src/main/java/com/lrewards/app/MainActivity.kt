package com.lrewards.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { LRewardsTestApp() }
    }
}

@Composable
private fun LRewardsTestApp(auth: AuthViewModel = viewModel()) {
    val state by auth.state.collectAsState()
    MaterialTheme {
        if (state.signedIn) {
            Column(Modifier.fillMaxSize().background(Color(0xFF080D1D)).padding(28.dp), verticalArrangement = Arrangement.Center) {
                Text("L Rewards", color = Color.White, style = MaterialTheme.typography.headlineLarge)
                Spacer(Modifier.height(12.dp))
                Text("Signed in successfully", color = Color(0xFF34D399))
                Spacer(Modifier.height(8.dp))
                Text(state.email, color = Color.White)
                Spacer(Modifier.height(24.dp))
                Button(onClick = auth::signOut, modifier = Modifier.fillMaxWidth()) { Text("Sign out") }
            }
        } else AuthForm(state, auth)
    }
}

@Composable
private fun AuthForm(state: AuthState, auth: AuthViewModel) {
    var signUp by remember { mutableStateOf(false) }
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    Column(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color(0xFF080D1D), Color(0xFF24134A)))).padding(28.dp), verticalArrangement = Arrangement.Center) {
        Text("L REWARDS", color = Color.White, style = MaterialTheme.typography.headlineLarge)
        Spacer(Modifier.height(8.dp))
        Text(if (signUp) "Create your account" else "Sign in to continue", color = Color(0xFFAAB5D0))
        Spacer(Modifier.height(24.dp))
        if (signUp) OutlinedTextField(name, { name = it }, label = { Text("Display name") }, modifier = Modifier.fillMaxWidth())
        if (signUp) Spacer(Modifier.height(10.dp))
        OutlinedTextField(email, { email = it }, label = { Text("Email") }, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(10.dp))
        OutlinedTextField(password, { password = it }, label = { Text("Password") }, visualTransformation = PasswordVisualTransformation(), modifier = Modifier.fillMaxWidth())
        state.error?.let { Spacer(Modifier.height(12.dp)); Text(it, color = Color(0xFFFCA5A5)) }
        Spacer(Modifier.height(18.dp))
        Button(enabled = !state.loading && email.isNotBlank() && password.length >= 6, onClick = { if (signUp) auth.signUp(email.trim(), password, name.ifBlank { "Rewarder" }) else auth.signIn(email.trim(), password) }, modifier = Modifier.fillMaxWidth()) {
            if (state.loading) CircularProgressIndicator() else Text(if (signUp) "Create account" else "Sign in")
        }
        TextButton(onClick = { signUp = !signUp }, modifier = Modifier.fillMaxWidth()) { Text(if (signUp) "Already have an account? Sign in" else "New here? Create an account") }
    }
}
