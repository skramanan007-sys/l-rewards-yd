package com.lrewards.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccountBalanceWallet
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Casino
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.QuestionMark
import androidx.compose.material.icons.rounded.SportsEsports
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel

private val Ink = Color(0xFF07130E)
private val SurfaceGreen = Color(0xFF10251B)
private val SoftGreen = Color(0xFF183B2A)
private val Mint = Color(0xFFB8F7CF)
private val Green = Color(0xFF42D778)
private val Lime = Color(0xFFB7F34A)
private val Muted = Color(0xFF9BB5A5)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) { super.onCreate(savedInstanceState); setContent { LRewardsApp() } }
}

@Composable
private fun LRewardsApp(auth: AuthViewModel = viewModel()) {
    val state by auth.state.collectAsState()
    MaterialTheme { if (state.signedIn) RewardsHome(state.email, auth) else AuthForm(state, auth) }
}

@Composable
private fun authFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedTextColor = Color(0xFF143522),
    unfocusedTextColor = Color(0xFF143522),
    focusedBorderColor = Green,
    unfocusedBorderColor = Color(0xFF4D8561),
    focusedLabelColor = Color(0xFF1B4D32),
    unfocusedLabelColor = Color(0xFF275B3A),
    cursorColor = Color(0xFF1B4D32),
)

@Composable
private fun AuthForm(state: AuthState, auth: AuthViewModel) {
    var signUp by remember { mutableStateOf(false) }
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Ink, Color(0xFF123F2A)))).padding(24.dp), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("L", color = Ink, fontSize = 36.sp, fontWeight = FontWeight.Black, modifier = Modifier.size(62.dp).background(Brush.linearGradient(listOf(Lime, Green)), CircleShape).padding(10.dp))
            Text("L REWARDS", color = Color.White, fontSize = 30.sp, fontWeight = FontWeight.Black)
            Text("Earn more from every moment", color = Mint)
            Spacer(Modifier.height(24.dp))
            if (signUp) { OutlinedTextField(name, { name = it }, label = { Text("Display name") }, modifier = Modifier.fillMaxWidth()); Spacer(Modifier.height(8.dp)) }
            OutlinedTextField(email, { email = it }, label = { Text("Email", color = Color(0xFF1B4D32)) }, modifier = Modifier.fillMaxWidth(), colors = authFieldColors())
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(password, { password = it }, label = { Text("Password", color = Color(0xFF1B4D32)) }, visualTransformation = PasswordVisualTransformation(), modifier = Modifier.fillMaxWidth(), colors = authFieldColors())
            state.error?.let { Text(it, color = Color(0xFFFFB4AB), modifier = Modifier.padding(8.dp)) }
            Spacer(Modifier.height(12.dp))
            Button(enabled = !state.loading && email.isNotBlank() && password.length >= 6, onClick = { if (signUp) auth.signUp(email.trim(), password, name.ifBlank { "Rewarder" }) else auth.signIn(email.trim(), password) }, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = Green, contentColor = Ink)) { if (state.loading) CircularProgressIndicator(Modifier.size(20.dp), color = Ink) else Text(if (signUp) "Create account" else "Sign in") }
            TextButton(onClick = { signUp = !signUp }) { Text(if (signUp) "Already have an account? Sign in" else "New here? Create an account", color = Mint) }
        }
    }
}

private data class Game(val title: String, val subtitle: String, val reward: String, val limit: String, val icon: ImageVector, val color: Color)

@Composable
private fun RewardsHome(email: String, auth: AuthViewModel) {
    var tab by remember { mutableIntStateOf(0) }; var coins by remember { mutableIntStateOf(0) }; var selected by remember { mutableStateOf<Game?>(null) }
    val games = remember { listOf(Game("Spin Wheel", "Try your luck", "1–10 coins", "5 plays", Icons.Rounded.Casino, Lime), Game("Scratch Card", "Reveal a prize", "0–5 coins", "3 plays", Icons.Rounded.AutoAwesome, Color(0xFFFFB95C)), Game("Captcha", "Quick challenge", "2 coins", "3 plays", Icons.Rounded.CheckCircle, Green), Game("Math Quiz", "Five questions", "5 coins", "2 plays", Icons.Rounded.QuestionMark, Color(0xFF71C7FF))) }
    Surface(Modifier.fillMaxSize(), color = Ink) {
        Column(Modifier.fillMaxSize().padding(18.dp)) {
            Text("WELCOME BACK", color = Mint, fontSize = 11.sp, fontWeight = FontWeight.Bold); Text(email.substringBefore("@"), color = Color.White, fontSize = 25.sp, fontWeight = FontWeight.Black); Text("Ready to earn today?", color = Muted)
            Spacer(Modifier.height(16.dp)); Card(colors = CardDefaults.cardColors(containerColor = SurfaceGreen), shape = RoundedCornerShape(24.dp), modifier = Modifier.fillMaxWidth()) { Column(Modifier.padding(20.dp)) { Text("TOTAL BALANCE", color = Mint, fontSize = 12.sp); Text("$coins", color = Color.White, fontSize = 40.sp, fontWeight = FontWeight.Black); Text("coins available", color = Mint) } }
            Spacer(Modifier.height(18.dp)); when (tab) { 0, 1 -> EarnPage(games) { selected = it }; 2 -> WalletPage(coins); else -> ProfilePage(email, auth::signOut) }
            Spacer(Modifier.weight(1f)); BottomBar(tab) { tab = it }
        }
        selected?.let { game -> GameDialog(game, { selected = null }) { reward -> auth.playGame(game.title.toGameType(), reward) { balance, _ -> if (balance != null) coins = balance; selected = null } } }
    }
}

private fun String.toGameType() = when (this) { "Spin Wheel" -> "spin"; "Scratch Card" -> "scratch"; "Captcha" -> "captcha"; else -> "quiz" }

@Composable private fun EarnPage(games: List<Game>, onGame: (Game) -> Unit) { Column { Text("Earn coins", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Black); Text("Pick a challenge and keep your streak", color = Muted); Spacer(Modifier.height(14.dp)); LazyVerticalGrid(columns = GridCells.Fixed(2), modifier = Modifier.height(330.dp), horizontalArrangement = Arrangement.spacedBy(10.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) { items(games) { game -> Card(onClick = { onGame(game) }, colors = CardDefaults.cardColors(containerColor = SurfaceGreen), shape = RoundedCornerShape(20.dp)) { Column(Modifier.padding(14.dp)) { Icon(game.icon, null, tint = game.color, modifier = Modifier.size(30.dp)); Spacer(Modifier.height(10.dp)); Text(game.title, color = Color.White, fontWeight = FontWeight.Bold); Text(game.subtitle, color = Muted, fontSize = 12.sp); Spacer(Modifier.height(8.dp)); Text(game.reward, color = game.color, fontWeight = FontWeight.Bold, fontSize = 12.sp) } } } } } }

@Composable private fun GameDialog(game: Game, onDismiss: () -> Unit, onReward: (Int) -> Unit) { var answer by remember { mutableStateOf("") }; var card by remember { mutableIntStateOf(-1) }; val captcha = "69Y67"; val rewards = listOf(0, 1, 2, 3, 4, 5, 1, 2, 3); val questions = listOf("10 + 5 = ?" to listOf("12", "15", "17"), "9 + 9 = ?" to listOf("16", "18", "20"), "13 + 3 = ?" to listOf("14", "16", "23"), "9 + 2 = ?" to listOf("11", "13", "18"), "7 + 5 = ?" to listOf("10", "12", "15")); var step by remember { mutableIntStateOf(0) }; AlertDialog(onDismissRequest = onDismiss, containerColor = SurfaceGreen, title = { Text(game.title, color = Color.White, fontWeight = FontWeight.Black) }, text = { Column(horizontalAlignment = Alignment.CenterHorizontally) { Text("Complete the challenge to earn coins", color = Muted); Spacer(Modifier.height(10.dp)); when (game.title) { "Spin Wheel" -> Text("SPIN WHEEL\n\nTap claim to spin for 1–10 coins", color = Lime, fontSize = 20.sp, fontWeight = FontWeight.Black); "Scratch Card" -> { Text("Choose a card to reveal", color = Mint); LazyVerticalGrid(columns = GridCells.Fixed(3), modifier = Modifier.height(170.dp)) { items(9) { index -> Card(onClick = { card = index }, colors = CardDefaults.cardColors(containerColor = if (card == index) Green else Color(0xFF68756E))) { Box(Modifier.height(48.dp), contentAlignment = Alignment.Center) { Text(if (card == index) "${rewards[index]}" else "?", color = Color.White) } } } } }; "Captcha" -> { Text(captcha, color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.Black); OutlinedTextField(answer, { answer = it }, label = { Text("Type exactly") }) }; else -> { Text("Question ${step + 1} of 5\n${questions[step].first}", color = Mint, fontSize = 20.sp); questions[step].second.forEach { option -> Button(onClick = { if (step < 4) step++ }, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = SoftGreen)) { Text(option) } } } } } }, confirmButton = { Button(onClick = { val reward = when (game.title) { "Spin Wheel" -> (1..10).random(); "Scratch Card" -> rewards[card]; "Captcha" -> 2; else -> 5 }; if (game.title != "Captcha" || answer.equals(captcha, true)) onReward(reward) }, enabled = game.title != "Scratch Card" || card >= 0, colors = ButtonDefaults.buttonColors(containerColor = Green, contentColor = Ink)) { Text("CLAIM REWARD") } }, dismissButton = { TextButton(onClick = onDismiss) { Text("Close", color = Mint) } }) }

@Composable private fun WalletPage(coins: Int) { Column { Text("Wallet", color = Color.White, fontSize = 25.sp, fontWeight = FontWeight.Black); Text("Your available balance", color = Muted); Spacer(Modifier.height(20.dp)); Text("$coins coins", color = Lime, fontSize = 32.sp, fontWeight = FontWeight.Black) } }
@Composable private fun ProfilePage(email: String, signOut: () -> Unit) { Column { Text("Profile", color = Color.White, fontSize = 25.sp, fontWeight = FontWeight.Black); Spacer(Modifier.height(12.dp)); Text(email, color = Mint); Spacer(Modifier.height(20.dp)); Button(onClick = signOut, colors = ButtonDefaults.buttonColors(containerColor = Green, contentColor = Ink)) { Text("Sign out") } } }
@Composable private fun BottomBar(selected: Int, onSelected: (Int) -> Unit) { Row(Modifier.fillMaxWidth().background(SurfaceGreen, RoundedCornerShape(24.dp)).padding(8.dp), horizontalArrangement = Arrangement.SpaceAround) { listOf(Icons.Rounded.Home, Icons.Rounded.SportsEsports, Icons.Rounded.AccountBalanceWallet, Icons.Rounded.Person).forEachIndexed { index, icon -> Icon(icon, null, tint = if (selected == index) Lime else Muted, modifier = Modifier.size(28.dp).clickable { onSelected(index) }.padding(4.dp)) } } }
