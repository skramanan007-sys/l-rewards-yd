package com.lrewards.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel

private val Navy = Color(0xFF080D1D)
private val Panel = Color(0xFF121A32)
private val Purple = Color(0xFF8B5CF6)
private val Cyan = Color(0xFF38BDF8)
private val Muted = Color(0xFF9AA8C7)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { LRewardsApp() }
    }
}

@Composable
private fun LRewardsApp(auth: AuthViewModel = viewModel()) {
    val state by auth.state.collectAsState()
    MaterialTheme { if (state.signedIn) HomeScreen(state.email, auth::signOut) else AuthForm(state, auth) }
}

@Composable
private fun AuthForm(state: AuthState, auth: AuthViewModel) {
    var signUp by remember { mutableStateOf(false) }
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Navy, Color(0xFF25134B)))).padding(28.dp)) {
        Column(Modifier.align(Alignment.Center), verticalArrangement = Arrangement.Center) {
            Text("L REWARDS", color = Color.White, fontSize = 30.sp, fontWeight = FontWeight.Black)
            Text("Small actions. Real rewards.", color = Muted, modifier = Modifier.padding(top = 6.dp))
            Spacer(Modifier.height(28.dp))
            if (signUp) OutlinedTextField(name, { name = it }, label = { Text("Display name") }, modifier = Modifier.fillMaxWidth())
            if (signUp) Spacer(Modifier.height(10.dp))
            OutlinedTextField(email, { email = it }, label = { Text("Email") }, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(10.dp))
            OutlinedTextField(password, { password = it }, label = { Text("Password") }, visualTransformation = PasswordVisualTransformation(), modifier = Modifier.fillMaxWidth())
            state.error?.let { Text(it, color = Color(0xFFFCA5A5), modifier = Modifier.padding(top = 12.dp)) }
            Spacer(Modifier.height(18.dp))
            Button(enabled = !state.loading && email.isNotBlank() && password.length >= 6, onClick = { if (signUp) auth.signUp(email.trim(), password, name.ifBlank { "Rewarder" }) else auth.signIn(email.trim(), password) }, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = Purple)) {
                if (state.loading) CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White) else Text(if (signUp) "Create account" else "Sign in")
            }
            TextButton(onClick = { signUp = !signUp }, modifier = Modifier.fillMaxWidth()) { Text(if (signUp) "Already have an account? Sign in" else "New here? Create an account", color = Color(0xFFC4B5FD)) }
        }
    }
}

private data class Game(val title: String, val subtitle: String, val reward: String, val accent: Color, val limit: String)

@Composable
private fun HomeScreen(email: String, signOut: () -> Unit) {
    var tab by remember { mutableIntStateOf(0) }
    var coins by remember { mutableIntStateOf(0) }
    var message by remember { mutableStateOf<String?>(null) }
    val games = listOf(
        Game("Spin Wheel", "Lucky spin", "+1–10", Color(0xFFF59E0B), "0/5 today"),
        Game("Scratch Card", "Reveal a prize", "0–5", Color(0xFFEC4899), "0/3 today"),
        Game("Captcha", "Quick challenge", "+2", Color(0xFF22C55E), "0/3 today"),
        Game("Math Quiz", "5 questions", "+5", Cyan, "0/2 today")
    )
    Surface(Modifier.fillMaxSize(), color = Navy) {
        Column(Modifier.fillMaxSize().padding(horizontal = 20.dp, vertical = 18.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column { Text("Good to see you", color = Muted, fontSize = 13.sp); Text(email.substringBefore("@"), color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold) }
                Box(Modifier.size(42.dp).background(Purple, CircleShape), contentAlignment = Alignment.Center) { Text("LR", color = Color.White, fontWeight = FontWeight.Bold) }
            }
            Spacer(Modifier.height(20.dp))
            BalanceCard(coins)
            Spacer(Modifier.height(22.dp))
            when (tab) {
                0 -> EarnHome(games) { game -> coins += game.reward.filter { it.isDigit() }.toIntOrNull() ?: 1; message = "You earned ${game.reward} coins" }
                1 -> EarnHome(games) { game -> coins += 1; message = "Challenge started: ${game.title}" }
                2 -> Wallet(coins)
                else -> Profile(email, signOut)
            }
            Spacer(Modifier.weight(1f))
            Row(Modifier.fillMaxWidth().background(Panel, RoundedCornerShape(22.dp)).padding(8.dp), horizontalArrangement = Arrangement.SpaceAround) {
                listOf("Home", "Earn", "Wallet", "Profile").forEachIndexed { index, label -> Text(label, color = if (tab == index) Color.White else Muted, fontWeight = if (tab == index) FontWeight.Bold else FontWeight.Normal, modifier = Modifier.clickable { tab = index }.padding(12.dp)) }
            }
        }
        message?.let { text -> Box(Modifier.fillMaxSize().padding(20.dp), contentAlignment = Alignment.Center) { Card(colors = CardDefaults.cardColors(containerColor = Color(0xFF202C50)), shape = RoundedCornerShape(22.dp)) { Column(Modifier.padding(26.dp), horizontalAlignment = Alignment.CenterHorizontally) { Text(text, color = Color.White, fontSize = 18.sp); Spacer(Modifier.height(14.dp)); Button(onClick = { message = null }, colors = ButtonDefaults.buttonColors(containerColor = Purple)) { Text("Continue") } } } } }
    }
}

@Composable
private fun BalanceCard(coins: Int) {
    Card(colors = CardDefaults.cardColors(containerColor = Color.Transparent), modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(26.dp)) {
        Box(Modifier.background(Brush.linearGradient(listOf(Color(0xFF5633A4), Color(0xFF1F6CA5)))).padding(22.dp)) {
            Column { Text("TOTAL BALANCE", color = Color(0xFFD8D5FF), fontSize = 12.sp, fontWeight = FontWeight.Bold); Spacer(Modifier.height(6.dp)); Text("$coins coins", color = Color.White, fontSize = 34.sp, fontWeight = FontWeight.Black); Spacer(Modifier.height(8.dp)); Text("Keep earning to unlock rewards", color = Color(0xFFE0E7FF), fontSize = 13.sp) }
        }
    }
}

@Composable
private fun EarnHome(games: List<Game>, onGame: (Game) -> Unit) {
    Column { Text("Earn coins", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold); Text("Choose a challenge and start earning", color = Muted, modifier = Modifier.padding(top = 4.dp, bottom = 14.dp)); LazyVerticalGrid(columns = GridCells.Fixed(2), verticalArrangement = Arrangement.spacedBy(12.dp), horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.height(330.dp)) { items(games) { game -> GameCard(game) { onGame(game) } } } }
}

@Composable
private fun GameCard(game: Game, onClick: () -> Unit) {
    Card(onClick = onClick, colors = CardDefaults.cardColors(containerColor = Panel), shape = RoundedCornerShape(22.dp), modifier = Modifier.border(1.dp, game.accent.copy(alpha = .3f), RoundedCornerShape(22.dp))) {
        Column(Modifier.padding(16.dp)) { Box(Modifier.size(42.dp).background(game.accent.copy(alpha = .18f), CircleShape), contentAlignment = Alignment.Center) { Text(game.title.take(1), color = game.accent, fontWeight = FontWeight.Black, fontSize = 20.sp) }; Spacer(Modifier.height(12.dp)); Text(game.title, color = Color.White, fontWeight = FontWeight.Bold); Text(game.subtitle, color = Muted, fontSize = 12.sp); Spacer(Modifier.height(12.dp)); Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { Text(game.reward, color = game.accent, fontWeight = FontWeight.Bold); Text(game.limit, color = Muted, fontSize = 11.sp) } }
    }
}

@Composable
private fun Wallet(coins: Int) { Column { Text("Wallet", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold); Spacer(Modifier.height(14.dp)); Card(colors = CardDefaults.cardColors(containerColor = Panel), shape = RoundedCornerShape(20.dp), modifier = Modifier.fillMaxWidth()) { Column(Modifier.padding(20.dp)) { Text("Available coins", color = Muted); Text("$coins", color = Color.White, fontSize = 30.sp, fontWeight = FontWeight.Black); Spacer(Modifier.height(12.dp)); Text("Keep playing to reach the redemption minimum.", color = Muted) } } } }

@Composable
private fun Profile(email: String, signOut: () -> Unit) { Column { Text("Profile", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold); Spacer(Modifier.height(14.dp)); Card(colors = CardDefaults.cardColors(containerColor = Panel), shape = RoundedCornerShape(20.dp), modifier = Modifier.fillMaxWidth()) { Column(Modifier.padding(20.dp)) { Text("Account", color = Muted); Text(email, color = Color.White, modifier = Modifier.padding(top = 6.dp)); Spacer(Modifier.height(18.dp)); Button(onClick = signOut, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF33415F))) { Text("Sign out") } } } } }

private fun String.take(n: Int): String = if (length <= n) this else substring(0, n)
