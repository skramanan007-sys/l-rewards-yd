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
    MaterialTheme { if (state.signedIn) RewardsHome(state.email, auth::signOut, auth) else AuthForm(state, auth) }
}

@Composable
private fun AuthForm(state: AuthState, auth: AuthViewModel) {
    var signUp by remember { mutableStateOf(false) }
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Ink, Color(0xFF123F2A)))).padding(24.dp)) {
        Column(Modifier.align(Alignment.Center), horizontalAlignment = Alignment.CenterHorizontally) {
            BrandMark()
            Text("L REWARDS", color = Color.White, fontSize = 30.sp, fontWeight = FontWeight.Black)
            Text("Earn more from every moment", color = Mint, modifier = Modifier.padding(top = 6.dp))
            Spacer(Modifier.height(28.dp))
            if (signUp) OutlinedTextField(name, { name = it }, label = { Text("Display name") }, modifier = Modifier.fillMaxWidth())
            if (signUp) Spacer(Modifier.height(10.dp))
            OutlinedTextField(email, { email = it }, label = { Text("Email") }, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(10.dp))
            OutlinedTextField(password, { password = it }, label = { Text("Password") }, visualTransformation = PasswordVisualTransformation(), modifier = Modifier.fillMaxWidth())
            state.error?.let { Text(it, color = Color(0xFFFFB4AB), modifier = Modifier.padding(top = 12.dp)) }
            Spacer(Modifier.height(18.dp))
            Button(enabled = !state.loading && email.isNotBlank() && password.length >= 6, onClick = { if (signUp) auth.signUp(email.trim(), password, name.ifBlank { "Rewarder" }) else auth.signIn(email.trim(), password) }, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = Green, contentColor = Ink)) {
                if (state.loading) CircularProgressIndicator(Modifier.size(20.dp), color = Ink) else Text(if (signUp) "Create account" else "Sign in", fontWeight = FontWeight.Bold)
            }
            TextButton(onClick = { signUp = !signUp }, modifier = Modifier.fillMaxWidth()) { Text(if (signUp) "Already have an account? Sign in" else "New here? Create an account", color = Mint) }
        }
    }
}

@Composable private fun BrandMark() { Box(Modifier.size(62.dp).background(Brush.linearGradient(listOf(Lime, Green)), CircleShape), contentAlignment = Alignment.Center) { Text("L", color = Ink, fontSize = 36.sp, fontWeight = FontWeight.Black) } }

private data class Game(val title: String, val subtitle: String, val reward: String, val limit: String, val icon: ImageVector, val color: Color)

@Composable
private fun RewardsHome(email: String, signOut: () -> Unit, auth: AuthViewModel) {
    var tab by remember { mutableIntStateOf(0) }
    var coins by remember { mutableIntStateOf(0) }
    var selected by remember { mutableStateOf<Game?>(null) }
    var showRedemption by remember { mutableStateOf(false) }
    val games = listOf(
        Game("Spin Wheel", "Try your luck", "1–10 coins", "5 plays", Icons.Rounded.Casino, Lime),
        Game("Scratch Card", "Reveal a prize", "0–5 coins", "3 plays", Icons.Rounded.AutoAwesome, Color(0xFFFFB95C)),
        Game("Captcha", "Quick challenge", "2 coins", "3 plays", Icons.Rounded.CheckCircle, Green),
        Game("Math Quiz", "Five questions", "5 coins", "2 plays", Icons.Rounded.QuestionMark, Color(0xFF71C7FF))
    )
    Surface(Modifier.fillMaxSize(), color = Ink) {
        Column(Modifier.fillMaxSize().padding(horizontal = 18.dp, vertical = 16.dp)) {
            Header(email)
            Spacer(Modifier.height(18.dp))
            BalanceCard(coins)
            Spacer(Modifier.height(22.dp))
            when (tab) { 0, 1 -> EarnPage(games) { selected = it }; 2 -> WalletPage(coins) { showRedemption = true }; else -> ProfilePage(email, signOut) }
            Spacer(Modifier.weight(1f))
            BottomBar(tab) { tab = it }
        }
        selected?.let { game -> GameDialog(game, onDismiss = { selected = null }) { reward -> auth.playGame(game.title.toGameType(), reward) { balance, error -> if (balance != null) coins = balance; selected = null } } }
        if (showRedemption) RedemptionDialog(
            coins = coins,
            onDismiss = { showRedemption = false },
            onRequest = { type, cost -> auth.requestRedemption(type, cost) { balance, _ -> if (balance != null) coins = balance; showRedemption = false } },
        )
    }
}

@Composable private fun Header(email: String) { Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) { Column { Text("WELCOME BACK", color = Mint, fontSize = 11.sp, fontWeight = FontWeight.Bold); Text(email.substringBefore("@"), color = Color.White, fontSize = 25.sp, fontWeight = FontWeight.Black); Text("Ready to earn today?", color = Muted, fontSize = 13.sp) }; Box(Modifier.size(44.dp).background(SoftGreen, CircleShape), contentAlignment = Alignment.Center) { Icon(Icons.Rounded.AutoAwesome, null, tint = Lime) } } }

@Composable private fun BalanceCard(coins: Int) { Card(colors = CardDefaults.cardColors(containerColor = Color.Transparent), shape = RoundedCornerShape(28.dp), modifier = Modifier.fillMaxWidth()) { Box(Modifier.background(Brush.linearGradient(listOf(Color(0xFF2D963F), Color(0xFF0E4C31)))).padding(22.dp)) { Column { Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) { Text("TOTAL BALANCE", color = Mint, fontSize = 12.sp, fontWeight = FontWeight.Bold); Text("+12% this week", color = Lime, fontSize = 11.sp) }; Spacer(Modifier.height(5.dp)); Text("$coins", color = Color.White, fontSize = 40.sp, fontWeight = FontWeight.Black); Text("coins available", color = Mint, fontSize = 13.sp); Spacer(Modifier.height(16.dp)); Row(verticalAlignment = Alignment.CenterVertically) { Box(Modifier.height(6.dp).weight(1f).clip(CircleShape).background(Color.White.copy(.18f))) { Box(Modifier.fillMaxWidth(.36f).height(6.dp).background(Lime, CircleShape)) }; Spacer(Modifier.width(10.dp)); Text("36% to next reward", color = Color.White, fontSize = 11.sp) } } } } }

@Composable private fun EarnPage(games: List<Game>, onGame: (Game) -> Unit) { Column { Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.Bottom) { Column { Text("Earn coins", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Black); Text("Pick a challenge and keep your streak", color = Muted, fontSize = 13.sp) }; Text("TODAY", color = Lime, fontSize = 11.sp, fontWeight = FontWeight.Bold) }; Spacer(Modifier.height(16.dp)); LazyVerticalGrid(columns = GridCells.Fixed(2), verticalArrangement = Arrangement.spacedBy(12.dp), horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.height(340.dp)) { items(games) { game -> GameCard(game) { onGame(game) } } }; Spacer(Modifier.height(18.dp)); Card(colors = CardDefaults.cardColors(containerColor = SurfaceGreen), shape = RoundedCornerShape(20.dp), modifier = Modifier.fillMaxWidth()) { Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) { Icon(Icons.Rounded.SportsEsports, null, tint = Lime, modifier = Modifier.size(28.dp)); Spacer(Modifier.width(12.dp)); Column { Text("Daily streak", color = Color.White, fontWeight = FontWeight.Bold); Text("Play one game today to keep it alive", color = Muted, fontSize = 12.sp) } } } } }

@Composable private fun GameCard(game: Game, onClick: () -> Unit) { Card(onClick = onClick, colors = CardDefaults.cardColors(containerColor = SurfaceGreen), shape = RoundedCornerShape(22.dp), modifier = Modifier.border(1.dp, game.color.copy(.22f), RoundedCornerShape(22.dp))) { Column(Modifier.padding(15.dp)) { Box(Modifier.size(44.dp).background(game.color.copy(.15f), CircleShape), Alignment.Center) { Icon(game.icon, null, tint = game.color) }; Spacer(Modifier.height(12.dp)); Text(game.title, color = Color.White, fontWeight = FontWeight.Bold); Text(game.subtitle, color = Muted, fontSize = 12.sp); Spacer(Modifier.height(12.dp)); Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) { Text(game.reward, color = game.color, fontWeight = FontWeight.Bold, fontSize = 12.sp); Text(game.limit, color = Muted, fontSize = 11.sp) } } } }

@Composable
private fun GameDialog(game: Game, onDismiss: () -> Unit, onReward: (Int) -> Unit) {
    var answer by remember { mutableStateOf("") }
    var scratchIndex by remember { mutableIntStateOf(-1) }
    var quizStep by remember { mutableIntStateOf(0) }
    val quizQuestions = listOf("5 + 3 = ?", "10 - 4 = ?", "2 × 3 = ?", "12 ÷ 4 = ?", "7 + 2 = ?")
    val quizOptions = listOf(listOf("6", "8", "9"), listOf("4", "5", "6"), listOf("5", "6", "8"), listOf("2", "3", "4"), listOf("7", "8", "9"))
    val quizAnswers = listOf("8", "6", "6", "3", "9")
    val scratchRewards = listOf(0, 1, 2, 3, 4, 5, 1, 2, 3)
    AlertDialog(onDismissRequest = onDismiss, containerColor = SurfaceGreen, title = { Text(game.title, color = Color.White, fontWeight = FontWeight.Black) }, text = {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Complete the challenge to claim your server-validated reward.", color = Muted)
            Spacer(Modifier.height(16.dp))
            when (game.title) {
                "Spin Wheel" -> { Box(Modifier.size(170.dp).background(Brush.sweepGradient(listOf(Lime, Green, Color(0xFFFFC857), Green)), CircleShape), contentAlignment = Alignment.Center) { Box(Modifier.size(116.dp).background(SurfaceGreen, CircleShape), contentAlignment = Alignment.Center) { Text("SPIN", color = Lime, fontWeight = FontWeight.Black, fontSize = 20.sp) } }; Spacer(Modifier.height(12.dp)); Text("Tap claim to spin for 1–10 coins", color = Mint) }
                "Scratch Card" -> { Text("Scratch a card", color = Mint, fontWeight = FontWeight.Bold); Spacer(Modifier.height(10.dp)); LazyVerticalGrid(columns = GridCells.Fixed(3), modifier = Modifier.height(180.dp), verticalArrangement = Arrangement.spacedBy(8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) { items(9) { index -> Card(onClick = { if (scratchIndex < 0) scratchIndex = index }, colors = CardDefaults.cardColors(containerColor = if (scratchIndex == index) Green.copy(.28f) else SoftGreen), shape = RoundedCornerShape(14.dp)) { Box(Modifier.height(52.dp), contentAlignment = Alignment.Center) { Text(if (scratchIndex == index) "+${scratchRewards[index]}" else "?", color = if (scratchIndex == index) Lime else Color.White, fontWeight = FontWeight.Black) } } } } }
                "Captcha" -> { Text("What is 5 + 3?", color = Mint, fontSize = 20.sp, fontWeight = FontWeight.Bold); Spacer(Modifier.height(10.dp)); OutlinedTextField(answer, { answer = it }, label = { Text("Answer") }) }
                else -> { Text("Question ${quizStep + 1} of 5", color = Muted, fontSize = 12.sp); Text(quizQuestions[quizStep], color = Mint, fontSize = 20.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 6.dp)); Spacer(Modifier.height(12.dp)); quizOptions[quizStep].forEach { option -> Button(onClick = { if (quizStep < 4) quizStep++ }, modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp), colors = ButtonDefaults.buttonColors(containerColor = SoftGreen, contentColor = Color.White)) { Text(option) } } }
            }
        }
    }, confirmButton = { Button(onClick = { val reward = when (game.title) { "Spin Wheel" -> (1..10).random(); "Scratch Card" -> if (scratchIndex >= 0) scratchRewards[scratchIndex] else 0; "Captcha" -> 2; else -> 5 }; if (game.title != "Captcha" || answer == "8") onReward(reward) }, enabled = game.title != "Scratch Card" || scratchIndex >= 0, colors = ButtonDefaults.buttonColors(containerColor = Green, contentColor = Ink)) { Text("Claim reward") } }, dismissButton = { TextButton(onClick = onDismiss) { Text("Later", color = Mint) } })
}

@Composable
private fun WalletPage(coins: Int, onRedeem: () -> Unit) {
    Column {
        Text("Wallet", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Black)
        Text("Your reward balance", color = Muted)
        Spacer(Modifier.height(16.dp))
        Card(
            colors = CardDefaults.cardColors(containerColor = SurfaceGreen),
            shape = RoundedCornerShape(22.dp),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column(Modifier.padding(20.dp)) {
                Text("AVAILABLE", color = Mint, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                Text("$coins coins", color = Color.White, fontSize = 30.sp, fontWeight = FontWeight.Black)
                Spacer(Modifier.height(12.dp))
                Button(
                    onClick = onRedeem,
                    enabled = coins >= 100,
                    colors = ButtonDefaults.buttonColors(containerColor = Green, contentColor = Ink),
                ) {
                    Text("Redeem from 100 coins")
                }
                Spacer(Modifier.height(8.dp))
                Text("Requests are reviewed before payout", color = Muted, fontSize = 12.sp)
            }
        }
    }
}

@Composable private fun RedemptionDialog(coins: Int, onDismiss: () -> Unit, onRequest: (String, Int) -> Unit) { AlertDialog(onDismissRequest = onDismiss, containerColor = SurfaceGreen, title = { Text("Request a reward", color = Color.White, fontWeight = FontWeight.Black) }, text = { Column { Text("Your balance: $coins coins", color = Mint); Spacer(Modifier.height(10.dp)); Text("Choose a 100-coin digital reward. An admin will review it.", color = Muted) } }, confirmButton = { Button(onClick = { onRequest("Digital reward", 100) }, enabled = coins >= 100, colors = ButtonDefaults.buttonColors(containerColor = Green, contentColor = Ink)) { Text("Request") } }, dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel", color = Mint) } }) }

@Composable private fun ProfilePage(email: String, signOut: () -> Unit) { Column { Text("Profile", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Black); Text("Manage your L Rewards account", color = Muted); Spacer(Modifier.height(16.dp)); Card(colors = CardDefaults.cardColors(containerColor = SurfaceGreen), shape = RoundedCornerShape(22.dp), modifier = Modifier.fillMaxWidth()) { Column(Modifier.padding(20.dp)) { Text("ACCOUNT", color = Mint, fontSize = 11.sp, fontWeight = FontWeight.Bold); Text(email, color = Color.White, modifier = Modifier.padding(top = 8.dp)); Spacer(Modifier.height(18.dp)); Button(onClick = signOut, colors = ButtonDefaults.buttonColors(containerColor = SoftGreen)) { Text("Sign out", color = Color.White) } } } } }

@Composable private fun BottomBar(selected: Int, onSelect: (Int) -> Unit) { Card(colors = CardDefaults.cardColors(containerColor = SurfaceGreen), shape = RoundedCornerShape(24.dp), modifier = Modifier.fillMaxWidth()) { Row(Modifier.fillMaxWidth().padding(6.dp), Arrangement.SpaceAround) { listOf(Icons.Rounded.Home, Icons.Rounded.AutoAwesome, Icons.Rounded.AccountBalanceWallet, Icons.Rounded.Person).forEachIndexed { index, icon -> Box(Modifier.size(52.dp).clip(RoundedCornerShape(18.dp)).clickable { onSelect(index) }.background(if (selected == index) Green.copy(.2f) else Color.Transparent), Alignment.Center) { Icon(icon, null, tint = if (selected == index) Lime else Muted) } } } } }

private fun String.toGameType(): String = when (this) {
    "Spin Wheel" -> "spin"
    "Scratch Card" -> "scratch"
    "Captcha" -> "captcha"
    else -> "quiz"
}

private fun String.take(n: Int): String = if (length <= n) this else substring(0, n)
