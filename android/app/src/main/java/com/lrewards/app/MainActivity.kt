package com.lrewards.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import kotlinx.coroutines.launch
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlin.random.Random

private val Ink = Color(0xFF09090B)
private val Surface = Color(0xFF151519)
private val Surface2 = Color(0xFF222229)
private val Red = Color(0xFFE51C35)
private val RedBright = Color(0xFFFF4055)
private val RedSoft = Color(0xFFFFDDE1)
private val White = Color(0xFFF7F7F8)
private val Muted = Color(0xFF9B9BA4)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) { super.onCreate(savedInstanceState); setContent { LRewardsApp() } }
}

@Composable
private fun LRewardsApp(auth: AuthViewModel = androidx.lifecycle.viewmodel.compose.viewModel()) {
    val state by auth.state.collectAsState()
    LaunchedEffect(Unit) { auth.restoreSession() }
    MaterialTheme { if (state.signedIn) AppShell(state.email, auth) else AuthScreen(state, auth) }
}

@Composable
private fun AuthScreen(state: AuthState, auth: AuthViewModel) {
    var signup by remember { mutableStateOf(false) }; var name by remember { mutableStateOf("") }; var email by remember { mutableStateOf("") }; var password by remember { mutableStateOf("") }
    val fields = OutlinedTextFieldDefaults.colors(focusedTextColor = White, unfocusedTextColor = White, focusedBorderColor = RedBright, unfocusedBorderColor = Surface2, focusedLabelColor = RedSoft, unfocusedLabelColor = Muted, cursorColor = RedBright)
    Surface(Modifier.fillMaxSize(), color = Ink) { Column(Modifier.fillMaxSize().padding(24.dp), verticalArrangement = Arrangement.Center) {
        Text("L/", color = Red, fontSize = 64.sp, fontWeight = FontWeight.Black); Text("L REWARDS", color = White, fontSize = 30.sp, fontWeight = FontWeight.Black, letterSpacing = 3.sp); Text("PLAY · EARN · REDEEM", color = RedBright, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
        Spacer(Modifier.height(38.dp)); Text(if (signup) "Create account" else "Welcome back", color = White, fontSize = 28.sp, fontWeight = FontWeight.Black); Text(if (signup) "Join the rewards club" else "Sign in to continue", color = Muted)
        Spacer(Modifier.height(20.dp)); if (signup) { Field(name, { name = it }, "Name", fields); Spacer(Modifier.height(10.dp)) }; Field(email, { email = it }, "Email", fields); Spacer(Modifier.height(10.dp)); Field(password, { password = it }, "Password", fields, true)
        state.error?.let { Text(it, color = RedBright, modifier = Modifier.padding(top = 10.dp)) }; Spacer(Modifier.height(16.dp)); Button(enabled = !state.loading && email.isNotBlank() && password.length >= 6, onClick = { if (signup) auth.signUp(email.trim(), password, name.ifBlank { "Rewarder" }) else auth.signIn(email.trim(), password) }, modifier = Modifier.fillMaxWidth().height(52.dp), shape = RoundedCornerShape(8.dp), colors = ButtonDefaults.buttonColors(containerColor = Red, contentColor = White)) { Text(if (signup) "CREATE ACCOUNT" else "SIGN IN", fontWeight = FontWeight.Black) }
        TextButton(onClick = { signup = !signup }) { Text(if (signup) "Already registered? Sign in" else "New here? Create account", color = RedSoft) }
    } }
}

@Composable private fun Field(value: String, onChange: (String) -> Unit, label: String, colors: TextFieldColors, password: Boolean = false) { OutlinedTextField(value, onChange, label = { Text(label) }, singleLine = true, colors = colors, visualTransformation = if (password) PasswordVisualTransformation() else androidx.compose.ui.text.input.VisualTransformation.None, modifier = Modifier.fillMaxWidth()) }

private data class Game(val title: String, val caption: String, val reward: String, val icon: androidx.compose.ui.graphics.vector.ImageVector, val accent: Color)

@Composable
private fun AppShell(email: String, auth: AuthViewModel) {
    var tab by remember { mutableIntStateOf(0) }; var game by remember { mutableStateOf<Game?>(null) }; var notice by remember { mutableStateOf<String?>(null) }; val wallet by auth.wallet.collectAsState(); val coins = wallet.balance
    BackHandler(enabled = game != null || tab != 0) { if (game != null) game = null else tab = 0 }
    val games = remember { listOf(Game("Daily Spin", "Spin the wheel", "1–10 coins", Icons.Rounded.Casino, RedBright), Game("Scratch & Win", "Reveal your prize", "0–5 coins", Icons.Rounded.AutoAwesome, Color(0xFFFFA63D)), Game("Quick Quiz", "Test your knowledge", "5 coins", Icons.Rounded.QuestionMark, Color(0xFF8B7CFF)), Game("Captcha", "Complete a challenge", "2 coins", Icons.Rounded.CheckCircle, Color(0xFF45C6B7))) }
    Surface(Modifier.fillMaxSize(), color = Ink) { Column(Modifier.fillMaxSize().padding(horizontal = 18.dp)) {
        Row(Modifier.fillMaxWidth().padding(top = 18.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) { Column { Text("L/", color = Red, fontSize = 30.sp, fontWeight = FontWeight.Black); Text("WELCOME BACK", color = Muted, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp); Text(email.substringBefore("@"), color = White, fontSize = 21.sp, fontWeight = FontWeight.Black) }; Box(Modifier.size(44.dp).background(Surface2, CircleShape), contentAlignment = Alignment.Center) { Icon(Icons.Rounded.Person, null, tint = RedSoft) } }
        Spacer(Modifier.height(18.dp)); BalanceCard(coins)
        Spacer(Modifier.height(18.dp)); Box(Modifier.weight(1f).fillMaxWidth()) { when (tab) { 0 -> HomePage(games) { game = it }; 1 -> WalletPage(coins, wallet.transactions, wallet.withdrawals); 2 -> RedeemPage(coins, wallet.rewards) { id, cost, destination -> if (id == "__back__") tab = 0 else auth.requestRedemption(id, cost, destination) { _, error -> notice = error ?: "Request submitted"; auth.loadWallet() } }; else -> ProfilePage(email, auth::signOut) } }
        BottomNav(tab) { tab = it }
    } }
    game?.let { activeGame -> GameScreen(activeGame, { game = null }) { auth.playGame(activeGame.title.toType()) { _, error -> notice = error ?: "Coins added to your wallet"; auth.loadWallet(); game = null } } }
    notice?.let { AlertDialog(onDismissRequest = { notice = null }, confirmButton = { TextButton(onClick = { notice = null }) { Text("OK", color = RedBright) } }, title = { Text("L REWARDS", color = White) }, text = { Text(it, color = RedSoft) }, containerColor = Surface) }
}

@Composable private fun BalanceCard(coins: Int) { Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(10.dp), colors = CardDefaults.cardColors(containerColor = Red)) { Column(Modifier.padding(20.dp)) { Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { Text("YOUR BALANCE", color = Ink, fontSize = 11.sp, fontWeight = FontWeight.Black, letterSpacing = 1.sp); Text("100 COINS = ₹1", color = Ink, fontSize = 10.sp, fontWeight = FontWeight.Black) }; Text(coins.toString(), color = Ink, fontSize = 50.sp, fontWeight = FontWeight.Black); Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { Text("COINS AVAILABLE", color = Ink, fontWeight = FontWeight.Bold); Text("TODAY · 0/4", color = Ink, fontSize = 11.sp, fontWeight = FontWeight.Black) } } } }

@Composable private fun HomePage(games: List<Game>, onGame: (Game) -> Unit) { Column { Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Bottom) { Column { Text("PLAY & EARN", color = White, fontSize = 25.sp, fontWeight = FontWeight.Black); Text("Choose your challenge", color = Muted) }; Text("DAILY", color = RedBright, fontSize = 11.sp, fontWeight = FontWeight.Black) }; Spacer(Modifier.height(14.dp)); LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp), contentPadding = PaddingValues(bottom = 18.dp)) { items(games) { item -> GameCard(item, onGame) }; item { SectionLabel("TRENDING OFFERS"); OfferCard("Complete today’s bonus", "Earn extra coins", Red) } } } }

@Composable private fun GameCard(game: Game, onGame: (Game) -> Unit) { Card(onClick = { onGame(game) }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(10.dp), colors = CardDefaults.cardColors(containerColor = Surface)) { Row(Modifier.padding(15.dp), verticalAlignment = Alignment.CenterVertically) { Box(Modifier.size(52.dp).clip(RoundedCornerShape(10.dp)).background(game.accent.copy(alpha = .18f)), contentAlignment = Alignment.Center) { Icon(game.icon, null, tint = game.accent, modifier = Modifier.size(28.dp)) }; Spacer(Modifier.width(14.dp)); Column(Modifier.weight(1f)) { Text(game.title.uppercase(), color = White, fontSize = 16.sp, fontWeight = FontWeight.Black, letterSpacing = .5.sp); Text(game.caption, color = Muted, fontSize = 13.sp); Text(game.reward, color = game.accent, fontSize = 12.sp, fontWeight = FontWeight.Bold) }; Text("PLAY", color = game.accent, fontWeight = FontWeight.Black, fontSize = 11.sp) } } }

@Composable private fun SectionLabel(text: String) { Text(text, color = RedSoft, fontSize = 12.sp, fontWeight = FontWeight.Black, letterSpacing = 1.sp, modifier = Modifier.padding(vertical = 8.dp)) }
@Composable private fun OfferCard(title: String, caption: String, color: Color) { Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(10.dp), colors = CardDefaults.cardColors(containerColor = Surface2)) { Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) { Box(Modifier.size(42.dp).background(color, RoundedCornerShape(8.dp))); Spacer(Modifier.width(12.dp)); Column { Text(title, color = White, fontWeight = FontWeight.Bold); Text(caption, color = Muted, fontSize = 12.sp) } } } }

@Composable private fun GameScreen(game: Game, onBack: () -> Unit, onReward: (Int) -> Unit) { var spinning by remember { mutableStateOf(false) }; var scratched by remember { mutableStateOf(false) }; var input by remember { mutableStateOf("") }; val rotation = remember { Animatable(0f) }; val scope = rememberCoroutineScope(); val code = remember { (1..6).map { "ABCDEFGHJKLMNPQRSTUVWXYZ23456789".random() }.joinToString("") }; Dialog(onDismissRequest = onBack, properties = DialogProperties(usePlatformDefaultWidth = false)) { Surface(Modifier.fillMaxSize(), color = Ink) { Column(Modifier.fillMaxSize().verticalScroll(androidx.compose.foundation.rememberScrollState()).padding(20.dp)) { Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) { Column { Text("L/", color = Red, fontSize = 25.sp, fontWeight = FontWeight.Black); Text(game.title.uppercase(), color = White, fontSize = 24.sp, fontWeight = FontWeight.Black, letterSpacing = 1.sp) }; TextButton(onClick = onBack) { Text("‹ BACK", color = RedBright, fontWeight = FontWeight.Black) } }; Text(game.caption, color = Muted); Spacer(Modifier.height(26.dp)); when (game.title) {
        "Daily Spin" -> { Text("SPIN & WIN", color = RedSoft, fontSize = 14.sp, fontWeight = FontWeight.Black, letterSpacing = 2.sp, modifier = Modifier.align(Alignment.CenterHorizontally)); Spacer(Modifier.height(18.dp)); Box(Modifier.size(300.dp).align(Alignment.CenterHorizontally), contentAlignment = Alignment.Center) { Canvas(Modifier.fillMaxSize().graphicsLayer { rotationZ = rotation.value }) { val colors = listOf(Red, Color(0xFF6B0D1B), RedBright, Color(0xFF37070E), Color(0xFFB30F27), Color(0xFF801021), RedBright, Color(0xFF4E0913), Red, Color(0xFFB30F27)); colors.forEachIndexed { i, c -> drawArc(c, i * 36f - 90f, 36f, true) }; drawCircle(Surface, 40f, center); drawCircle(RedSoft, 7f, center) }; Canvas(Modifier.align(Alignment.TopCenter).size(36.dp, 44.dp)) { drawPath(Path().apply { moveTo(size.width / 2, size.height); lineTo(0f, 0f); lineTo(size.width, 0f); close() }, color = RedSoft) } }; Spacer(Modifier.height(20.dp)); PrimaryButton("SPIN NOW", !spinning) { spinning = true; scope.launch { val reward = Random.nextInt(1, 11); rotation.animateTo(rotation.value + 1440f + (10 - reward) * 36f, tween(1900)); spinning = false; onReward(reward) } } }
        "Scratch & Win" -> { Text("SCRATCH & WIN", color = RedSoft, fontSize = 14.sp, fontWeight = FontWeight.Black, letterSpacing = 2.sp, modifier = Modifier.align(Alignment.CenterHorizontally)); Spacer(Modifier.height(18.dp)); Box(Modifier.fillMaxWidth().height(230.dp).clip(RoundedCornerShape(18.dp)).background(if (scratched) Red else Surface2).pointerInput(Unit) { detectDragGestures { _, _ -> scratched = true } }, contentAlignment = Alignment.Center) { Text(if (scratched) "YOU WON 100 COINS" else "SCRATCH HERE", color = White, fontSize = 22.sp, fontWeight = FontWeight.Black) }; Spacer(Modifier.height(20.dp)); PrimaryButton("CLAIM REWARD", scratched) { onReward(5) } }
        "Captcha" -> { CodePanel(code); OutlinedTextField(input, { input = it.uppercase() }, label = { Text("Enter code") }, singleLine = true, colors = OutlinedTextFieldDefaults.colors(focusedTextColor = White, unfocusedTextColor = White, focusedBorderColor = RedBright, unfocusedBorderColor = Surface2, focusedLabelColor = RedSoft, unfocusedLabelColor = Muted), modifier = Modifier.fillMaxWidth()); Spacer(Modifier.height(18.dp)); PrimaryButton("VERIFY & EARN", input.equals(code, true)) { onReward(2) } }
        else -> { Text("QUESTION 01 / 05", color = RedBright, fontWeight = FontWeight.Black); Spacer(Modifier.height(12.dp)); Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Surface), shape = RoundedCornerShape(14.dp)) { Column(Modifier.padding(20.dp)) { Text("Which is the largest planet?", color = White, fontSize = 22.sp, fontWeight = FontWeight.Black); Spacer(Modifier.height(20.dp)); listOf("Earth", "Mars", "Jupiter", "Venus").forEach { answer -> Row(Modifier.fillMaxWidth().padding(vertical = 7.dp).clip(RoundedCornerShape(8.dp)).background(Surface2).clickable { }.padding(14.dp)) { Text(answer, color = White) } } } }; Spacer(Modifier.height(18.dp)); PrimaryButton("SUBMIT ANSWER", true) { onReward(5) } }
    } } } } }

@Composable private fun CodePanel(code: String) { Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Surface), shape = RoundedCornerShape(14.dp)) { Column(Modifier.padding(22.dp), horizontalAlignment = Alignment.CenterHorizontally) { Text("CAPTCHA CHALLENGE", color = Muted, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp); Text(code, color = RedBright, fontSize = 32.sp, fontWeight = FontWeight.Black, letterSpacing = 6.sp, modifier = Modifier.padding(15.dp)) } } }
@Composable private fun PrimaryButton(text: String, enabled: Boolean, onClick: () -> Unit) { Button(enabled = enabled, onClick = onClick, modifier = Modifier.fillMaxWidth().height(52.dp), shape = RoundedCornerShape(8.dp), colors = ButtonDefaults.buttonColors(containerColor = Red, contentColor = White, disabledContainerColor = Surface2, disabledContentColor = Muted)) { Text(text, fontWeight = FontWeight.Black, letterSpacing = 1.sp) } }

@Composable private fun WalletPage(coins: Int, transactions: List<kotlinx.serialization.json.JsonObject>, withdrawals: List<kotlinx.serialization.json.JsonObject>) { Column(Modifier.fillMaxSize().verticalScroll(androidx.compose.foundation.rememberScrollState()).padding(bottom = 20.dp)) { SectionLabel("WALLET"); Text("EARNINGS HISTORY", color = White, fontSize = 25.sp, fontWeight = FontWeight.Black); Text("Your coins and activity", color = Muted); Spacer(Modifier.height(16.dp)); BalanceCard(coins); Spacer(Modifier.height(20.dp)); SectionLabel("RECENT ACTIVITY"); if (transactions.isEmpty()) Text("No activity yet", color = Muted) else transactions.take(20).forEach { row -> HistoryRow(row["game_type"]?.toString()?.trim('"') ?: "Reward", "+${row["amount"] ?: row["coins"] ?: 0} coins", row["created_at"]?.toString()?.trim('"') ?: "Completed") }; SectionLabel("WITHDRAWALS"); withdrawals.take(10).forEach { row -> HistoryRow(row["reward_type"]?.toString()?.trim('"') ?: "Redemption", row["status"]?.toString()?.trim('"') ?: "pending", row["created_at"]?.toString()?.trim('"') ?: "Submitted") } } }
@Composable private fun HistoryRow(title: String, amount: String, date: String) { Row(Modifier.fillMaxWidth().padding(vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) { Box(Modifier.size(38.dp).background(Surface2, RoundedCornerShape(8.dp))); Spacer(Modifier.width(12.dp)); Column(Modifier.weight(1f)) { Text(title.replaceFirstChar { it.uppercase() }, color = White, fontWeight = FontWeight.Bold); Text(date, color = Muted, fontSize = 11.sp) }; Text(amount, color = RedBright, fontWeight = FontWeight.Black) } }

@Composable private fun RedeemPage(coins: Int, rewards: List<kotlinx.serialization.json.JsonObject>, onRedeem: (String, Int, String) -> Unit) { var destination by remember { mutableStateOf("") }; Column(Modifier.fillMaxSize().verticalScroll(androidx.compose.foundation.rememberScrollState()).padding(bottom = 30.dp)) { Row(verticalAlignment = Alignment.CenterVertically) { TextButton(onClick = { onRedeem("__back__", 0, "") }) { Text("‹ BACK", color = RedBright, fontWeight = FontWeight.Black) }; Text("WITHDRAW", color = White, fontSize = 25.sp, fontWeight = FontWeight.Black) }; Text("100 coins = ₹1 · Balance $coins", color = Muted); Spacer(Modifier.height(16.dp)); OutlinedTextField(destination, { destination = it }, label = { Text("UPI ID or email for gift card") }, colors = OutlinedTextFieldDefaults.colors(focusedTextColor = White, unfocusedTextColor = White, focusedBorderColor = RedBright, unfocusedBorderColor = Surface2, focusedLabelColor = RedSoft, unfocusedLabelColor = Muted), modifier = Modifier.fillMaxWidth()); Spacer(Modifier.height(14.dp)); (if (rewards.isEmpty()) listOf(buildJsonObject { put("type", JsonPrimitive("upi")); put("label", JsonPrimitive("UPI ₹10")); put("cost", JsonPrimitive("1000")) }, buildJsonObject { put("type", JsonPrimitive("amazon")); put("label", JsonPrimitive("Amazon ₹10")); put("cost", JsonPrimitive("1000")) }) else rewards).forEach { raw -> val type = raw["type"]?.toString()?.trim('"') ?: "upi"; val label = raw["label"]?.toString()?.trim('"') ?: "Reward"; val cost = raw["cost"]?.toString()?.trim('"')?.toIntOrNull() ?: 1000; Card(Modifier.fillMaxWidth().padding(vertical = 5.dp), colors = CardDefaults.cardColors(containerColor = Surface), shape = RoundedCornerShape(10.dp)) { Row(Modifier.padding(15.dp), verticalAlignment = Alignment.CenterVertically) { Box(Modifier.size(44.dp).background(Red.copy(alpha = .2f), RoundedCornerShape(9.dp)), contentAlignment = Alignment.Center) { Text(type.take(2).uppercase(), color = RedBright, fontWeight = FontWeight.Black) }; Spacer(Modifier.width(12.dp)); Column(Modifier.weight(1f)) { Text(label, color = White, fontWeight = FontWeight.Bold); Text("$cost coins · ₹${cost / 100}", color = Muted, fontSize = 12.sp) }; TextButton(enabled = coins >= cost && destination.isNotBlank(), onClick = { onRedeem(type, cost, destination) }) { Text("REDEEM", color = RedBright, fontWeight = FontWeight.Black) } } } } } }

@Composable private fun ProfilePage(email: String, onSignOut: () -> Unit) { Column(Modifier.fillMaxSize().verticalScroll(androidx.compose.foundation.rememberScrollState())) { SectionLabel("PROFILE"); Text("YOUR ACCOUNT", color = White, fontSize = 25.sp, fontWeight = FontWeight.Black); Spacer(Modifier.height(18.dp)); Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Surface), shape = RoundedCornerShape(12.dp)) { Column(Modifier.padding(20.dp)) { Text(email.substringBefore("@"), color = White, fontSize = 20.sp, fontWeight = FontWeight.Black); Text(email, color = Muted); Spacer(Modifier.height(20.dp)); Text("Settings", color = RedSoft, fontWeight = FontWeight.Bold); Text("Notifications and account preferences", color = Muted, fontSize = 12.sp) } }; Spacer(Modifier.height(18.dp)); OutlinedButton(onClick = onSignOut, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.outlinedButtonColors(contentColor = RedBright)) { Text("LOG OUT", fontWeight = FontWeight.Black) } } }

@Composable private fun BottomNav(selected: Int, onSelect: (Int) -> Unit) { Row(Modifier.fillMaxWidth().padding(vertical = 12.dp), horizontalArrangement = Arrangement.SpaceAround) { listOf(Icons.Rounded.Home to "Home", Icons.Rounded.AccountBalanceWallet to "Wallet", Icons.Rounded.CardGiftcard to "Redeem", Icons.Rounded.Person to "Profile").forEachIndexed { index, pair -> Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.clickable { onSelect(index) }) { Icon(pair.first, pair.second, tint = if (selected == index) RedBright else Muted); Text(pair.second, color = if (selected == index) RedBright else Muted, fontSize = 10.sp) } } } }

private fun String.toType() = when (this) { "Daily Spin" -> "spin"; "Scratch & Win" -> "scratch"; "Captcha" -> "captcha"; else -> "quiz" }

@Composable private fun AppPreviewTheme(content: @Composable () -> Unit) { content() }

private fun Map<String, String>.toJsonObject(): kotlinx.serialization.json.JsonObject = kotlinx.serialization.json.buildJsonObject { forEach { (key, value) -> put(key, kotlinx.serialization.json.JsonPrimitive(value)) } }
