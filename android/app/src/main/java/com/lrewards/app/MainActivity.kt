package com.lrewards.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.items as gridItems
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import kotlin.random.Random

private val Ink = Color(0xFF07130E)
private val Panel = Color(0xFF10251B)
private val Panel2 = Color(0xFF183B2A)
private val Mint = Color(0xFFB8F7CF)
private val Green = Color(0xFF42D778)
private val Lime = Color(0xFFB7F34A)
private val Muted = Color(0xFF9BB5A5)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) { super.onCreate(savedInstanceState); setContent { LRewardsApp() } }
}

@Composable
private fun LRewardsApp(auth: AuthViewModel = androidx.lifecycle.viewmodel.compose.viewModel()) {
    val state by auth.state.collectAsState()
    MaterialTheme { if (state.signedIn) RewardsHome(state.email, auth) else AuthForm(state, auth) }
}

@Composable
private fun AuthForm(state: AuthState, auth: AuthViewModel) {
    var signup by remember { mutableStateOf(false) }; var name by remember { mutableStateOf("") }; var email by remember { mutableStateOf("") }; var password by remember { mutableStateOf("") }
    val colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Ink, unfocusedTextColor = Ink, focusedBorderColor = Green, unfocusedBorderColor = Color(0xFF78B68B), focusedLabelColor = Ink, unfocusedLabelColor = Color(0xFF275B3A), cursorColor = Ink)
    Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Ink, Color(0xFF155235)))).padding(22.dp), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(Modifier.size(70.dp).background(Brush.linearGradient(listOf(Lime, Green)), CircleShape), contentAlignment = Alignment.Center) { Text("L", color = Ink, fontSize = 40.sp, fontWeight = FontWeight.Black) }
            Text("L REWARDS", color = Color.White, fontSize = 30.sp, fontWeight = FontWeight.Black); Text("Earn more from every moment", color = Mint)
            Spacer(Modifier.height(24.dp)); if (signup) { OutlinedTextField(name, { name = it }, label = { Text("Display name") }, colors = colors, modifier = Modifier.fillMaxWidth()); Spacer(Modifier.height(8.dp)) }
            OutlinedTextField(email, { email = it }, label = { Text("Email") }, colors = colors, modifier = Modifier.fillMaxWidth()); Spacer(Modifier.height(8.dp))
            OutlinedTextField(password, { password = it }, label = { Text("Password") }, colors = colors, visualTransformation = PasswordVisualTransformation(), modifier = Modifier.fillMaxWidth())
            state.error?.let { Text(it, color = Color(0xFFFFD0C7), modifier = Modifier.padding(8.dp)) }; Spacer(Modifier.height(12.dp))
            Button(enabled = !state.loading && email.isNotBlank() && password.length >= 6, onClick = { if (signup) auth.signUp(email.trim(), password, name.ifBlank { "Rewarder" }) else auth.signIn(email.trim(), password) }, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = Green, contentColor = Ink)) { if (state.loading) CircularProgressIndicator(Modifier.size(20.dp), color = Ink) else Text(if (signup) "Create account" else "Sign in") }
            TextButton(onClick = { signup = !signup }) { Text(if (signup) "Already have an account? Sign in" else "New here? Create an account", color = Mint) }
        }
    }
}

private data class Game(val name: String, val subtitle: String, val reward: String, val limit: String, val icon: ImageVector, val accent: Color)

@Composable
private fun RewardsHome(email: String, auth: AuthViewModel) {
    var tab by remember { mutableIntStateOf(0) }; var coins by remember { mutableIntStateOf(0) }; var game by remember { mutableStateOf<Game?>(null) }; var message by remember { mutableStateOf<String?>(null) }
    val games = remember { listOf(Game("Spin Wheel", "Spin for a surprise", "1–10 coins", "5 today", Icons.Rounded.Casino, Lime), Game("Scratch Card", "Reveal your prize", "0–5 coins", "3 today", Icons.Rounded.AutoAwesome, Color(0xFFFFB95C)), Game("Captcha", "Type the code", "2 coins", "3 today", Icons.Rounded.CheckCircle, Green), Game("Math Quiz", "5 correct answers", "5 coins", "2 today", Icons.Rounded.QuestionMark, Color(0xFF71C7FF))) }
    Surface(Modifier.fillMaxSize(), color = Ink) { Column(Modifier.fillMaxSize().padding(18.dp)) {
        Text("WELCOME BACK", color = Mint, fontSize = 11.sp, fontWeight = FontWeight.Bold); Text(email.substringBefore("@"), color = Color.White, fontSize = 26.sp, fontWeight = FontWeight.Black); Text("Your next reward is waiting", color = Muted); Spacer(Modifier.height(16.dp))
        Card(colors = CardDefaults.cardColors(containerColor = Panel), shape = RoundedCornerShape(26.dp), modifier = Modifier.fillMaxWidth()) { Column(Modifier.padding(20.dp)) { Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) { Text("TOTAL BALANCE", color = Mint, fontSize = 12.sp); Text("TODAY  •  0/4", color = Green, fontSize = 11.sp, fontWeight = FontWeight.Bold) }; Text("$coins", color = Color.White, fontSize = 42.sp, fontWeight = FontWeight.Black); Text("coins available", color = Mint) } }
        Spacer(Modifier.height(18.dp)); when (tab) { 0, 1 -> EarnPage(games) { game = it }; 2 -> WalletPage(coins) { message = "Withdrawal options coming next" }; else -> ProfilePage(email, auth::signOut) }; Spacer(Modifier.weight(1f)); BottomBar(tab) { tab = it }
    }; game?.let { selected -> GameExperience(selected, { game = null }) { amount -> auth.playGame(selected.name.toType(), amount) { balance, error -> if (balance != null) coins = balance; message = error ?: "+$amount coins added"; game = null } } }; message?.let { text -> AlertDialog(onDismissRequest = { message = null }, confirmButton = { TextButton({ message = null }) { Text("OK", color = Green) } }, title = { Text("L Rewards", color = Color.White) }, text = { Text(text, color = Mint) }, containerColor = Panel) } }
}

private fun String.toType() = when (this) { "Spin Wheel" -> "spin"; "Scratch Card" -> "scratch"; "Captcha" -> "captcha"; else -> "quiz" }

@Composable private fun EarnPage(games: List<Game>, onGame: (Game) -> Unit) { Column { Text("Earn coins", color = Color.White, fontSize = 25.sp, fontWeight = FontWeight.Black); Text("Complete challenges to grow your balance", color = Muted); Spacer(Modifier.height(14.dp)); LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) { items(games) { g -> Card(onClick = { onGame(g) }, colors = CardDefaults.cardColors(containerColor = Panel), shape = RoundedCornerShape(20.dp), modifier = Modifier.fillMaxWidth()) { Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) { Box(Modifier.size(50.dp).clip(CircleShape).background(g.accent.copy(alpha = .18f)), contentAlignment = Alignment.Center) { Icon(g.icon, null, tint = g.accent, modifier = Modifier.size(27.dp)) }; Spacer(Modifier.width(14.dp)); Column(Modifier.weight(1f)) { Text(g.name, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 17.sp); Text(g.subtitle, color = Muted, fontSize = 13.sp); Text("${g.reward}  •  ${g.limit}", color = g.accent, fontSize = 12.sp, fontWeight = FontWeight.Bold) }; Text("PLAY", color = g.accent, fontWeight = FontWeight.Black, fontSize = 12.sp) } } } } }

@Composable private fun GameExperience(game: Game, onDismiss: () -> Unit, onReward: (Int) -> Unit) {
    var spin by remember { mutableStateOf(false) }; var scratch by remember { mutableIntStateOf(-1) }; var code by remember { mutableStateOf("") }; var question by remember { mutableIntStateOf(0) }; var quizScore by remember { mutableIntStateOf(0) }; val rotation = remember { Animatable(0f) }; val scope = rememberCoroutineScope(); val captcha = remember { (1..6).map { "ABCDEFGHJKLMNPQRSTUVWXYZ23456789".random() }.joinToString("") }
    val quiz = listOf("12 + 8 = ?" to listOf("18", "20", "22"), "7 × 6 = ?" to listOf("36", "42", "48"), "45 ÷ 5 = ?" to listOf("7", "8", "9"), "19 - 7 = ?" to listOf("10", "12", "14"), "8 × 4 = ?" to listOf("24", "32", "36")); val answer = listOf("20", "42", "9", "12", "32")
    AlertDialog(onDismissRequest = onDismiss, containerColor = Panel, title = { Text(game.name, color = Color.White, fontWeight = FontWeight.Black) }, text = { Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(game.subtitle, color = Muted); Spacer(Modifier.height(12.dp)); when (game.name) {
            "Spin Wheel" -> { Box(Modifier.size(220.dp), contentAlignment = Alignment.Center) { Canvas(Modifier.fillMaxSize().rotate(rotation.value)) { val colors = listOf(Lime, Green, Color(0xFF65BFFF), Color(0xFFFFB95C), Color(0xFFEF709D), Green); colors.forEachIndexed { i, c -> drawArc(c, i * 60f, 58f, true) }; drawCircle(Ink, radius = 28f); drawLine(Color.White, center, center.copy(y = 24f), 8f, StrokeCap.Round) } }; Text("Tap SPIN to rotate the wheel", color = Mint); Button(enabled = !spin, onClick = { spin = true; scope.launch { rotation.animateTo(rotation.value + 1440f + Random.nextInt(0, 360), tween(2200)); spin = false } }, colors = ButtonDefaults.buttonColors(containerColor = Green, contentColor = Ink)) { Text(if (spin) "SPINNING…" else "SPIN") } }
            "Scratch Card" -> { Text("Scratch a tile to reveal coins", color = Mint); Spacer(Modifier.height(8.dp)); androidx.compose.foundation.lazy.grid.LazyVerticalGrid(columns = androidx.compose.foundation.lazy.grid.GridCells.Fixed(3), modifier = Modifier.height(180.dp)) { gridItems(9) { i -> Card(onClick = { scratch = i }, colors = CardDefaults.cardColors(containerColor = if (scratch == i) Green else Panel2)) { Box(Modifier.height(54.dp), contentAlignment = Alignment.Center) { Text(if (scratch == i) "${i % 6}" else "SCRATCH", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold) } } } } }
            "Captcha" -> { Text(captcha, color = Lime, fontSize = 30.sp, fontWeight = FontWeight.Black); OutlinedTextField(code, { code = it }, label = { Text("Type the code") }, singleLine = true); Button(onClick = { if (code.equals(captcha, true)) onReward(2) }, colors = ButtonDefaults.buttonColors(containerColor = Green, contentColor = Ink)) { Text("VERIFY") } }
            else -> { Text("Question ${question + 1} of 5", color = Mint, fontWeight = FontWeight.Bold); Text(quiz[question].first, color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Black); quiz[question].second.forEach { option -> Button(onClick = { if (option == answer[question]) quizScore++; if (question == 4) onReward(if (quizScore + if (option == answer[question]) 1 else 0 == 5) 5 else 0) else question++ }, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = Panel2)) { Text(option, color = Color.White) } } }
        }; if (game.name == "Spin Wheel" && !spin) TextButton({ onReward(Random.nextInt(1, 11)) }) { Text("Claim the landed prize", color = Lime) }; if (game.name == "Scratch Card" && scratch >= 0) Button(onClick = { onReward(scratch % 6) }, colors = ButtonDefaults.buttonColors(containerColor = Green, contentColor = Ink)) { Text("COLLECT ${scratch % 6} COINS") }
    } }, confirmButton = { TextButton(onClick = onDismiss) { Text("Close", color = Mint) } })
}

@Composable private fun WalletPage(coins: Int, onWithdraw: () -> Unit) { Column { Text("Wallet", color = Color.White, fontSize = 25.sp, fontWeight = FontWeight.Black); Text("Your earnings and reward history", color = Muted); Spacer(Modifier.height(18.dp)); Card(colors = CardDefaults.cardColors(containerColor = Panel), shape = RoundedCornerShape(20.dp), modifier = Modifier.fillMaxWidth()) { Column(Modifier.padding(18.dp)) { Text("AVAILABLE", color = Mint, fontSize = 11.sp); Text("$coins coins", color = Lime, fontSize = 30.sp, fontWeight = FontWeight.Black); Button(onClick = onWithdraw, colors = ButtonDefaults.buttonColors(containerColor = Green, contentColor = Ink)) { Text("WITHDRAW") } } }; Spacer(Modifier.height(18.dp)); Text("REWARD HISTORY", color = Mint, fontWeight = FontWeight.Bold); Text("Your completed games will appear here", color = Muted, modifier = Modifier.padding(top = 8.dp)) } }
@Composable private fun ProfilePage(email: String, signOut: () -> Unit) { Column { Text("Profile", color = Color.White, fontSize = 25.sp, fontWeight = FontWeight.Black); Text(email, color = Mint, modifier = Modifier.padding(top = 10.dp)); Spacer(Modifier.height(22.dp)); Text("Account settings", color = Muted); Spacer(Modifier.height(12.dp)); Button(onClick = signOut, colors = ButtonDefaults.buttonColors(containerColor = Green, contentColor = Ink)) { Text("SIGN OUT") } } }
@Composable private fun BottomBar(selected: Int, onSelected: (Int) -> Unit) { Row(Modifier.fillMaxWidth().background(Panel, RoundedCornerShape(24.dp)).padding(8.dp), horizontalArrangement = Arrangement.SpaceAround) { listOf(Icons.Rounded.Home, Icons.Rounded.SportsEsports, Icons.Rounded.AccountBalanceWallet, Icons.Rounded.Person).forEachIndexed { i, icon -> Icon(icon, null, tint = if (selected == i) Lime else Muted, modifier = Modifier.size(30.dp).clickable { onSelected(i) }.padding(5.dp)) } } }

