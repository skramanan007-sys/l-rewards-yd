package com.lrewards.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lrewards.app.data.*
import kotlinx.coroutines.launch
import kotlin.random.Random

private val Ink = Color(0xFF080D1D)
private val Panel = Color(0xFF131B31)
private val Purple = Color(0xFF8B5CF6)
private val Blue = Color(0xFF38BDF8)
private val Mint = Color(0xFF34D399)
private val Muted = Color(0xFFAAB5D0)

private fun gameTypeFor(title: String): String = when (title) {
    "Spin Wheel" -> "spin"
    "Scratch Card" -> "scratch"
    "Captcha" -> "captcha"
    "Math Quiz" -> "quiz"
    else -> error("Unsupported game")
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) { super.onCreate(savedInstanceState); setContent { LRewardsApp() } }
}

data class Game(val title: String, val subtitle: String, val reward: String, val limit: Int, val icon: androidx.compose.ui.graphics.vector.ImageVector, val color: Color)
data class SessionUi(val profile: Profile? = null, val transactions: List<Transaction> = emptyList(), val loading: Boolean = false, val message: String? = null)

@Composable fun LRewardsApp() {
    val auth = remember { AuthViewModel() }
    val authState by auth.state.collectAsState()
    if (!authState.signedIn) AuthScreen(authState, auth) else RewardsShell(authState.displayName, auth)
}

@Composable fun AuthScreen(state: AuthState, auth: AuthViewModel) {
    var signup by remember { mutableStateOf(false) }; var email by remember { mutableStateOf("") }; var password by remember { mutableStateOf("") }; var name by remember { mutableStateOf("") }
    Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Ink, Color(0xFF221543)))), contentAlignment = Alignment.Center) {
        Column(Modifier.padding(28.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(15.dp)) {
            Text("L", color = Blue, fontSize = 62.sp, fontWeight = FontWeight.Black)
            Text("L REWARDS", color = Color.White, fontSize = 26.sp, fontWeight = FontWeight.Bold, letterSpacing = 4.sp)
            Text(if (signup) "Create your rewards account" else "Earn more. Redeem smarter.", color = Muted)
            if (signup) OutlinedTextField(name, { name = it }, label = { Text("Display name") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(email, { email = it }, label = { Text("Email") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(password, { password = it }, label = { Text("Password") }, visualTransformation = PasswordVisualTransformation(), modifier = Modifier.fillMaxWidth())
            state.error?.let { Text(it, color = if (it.startsWith("Check")) Mint else Color(0xFFFCA5A5), textAlign = TextAlign.Center) }
            Button(enabled = !state.loading && email.isNotBlank() && password.length >= 6, onClick = { if (signup) auth.signUp(email.trim(), password, name.ifBlank { "Rewarder" }) else auth.signIn(email.trim(), password) }, modifier = Modifier.fillMaxWidth().height(54.dp), shape = RoundedCornerShape(16.dp), colors = ButtonDefaults.buttonColors(containerColor = Purple)) { if (state.loading) CircularProgressIndicator(Modifier.size(20.dp), color = Color.White) else Text(if (signup) "Create account" else "Sign in", fontWeight = FontWeight.Bold) }
            TextButton({ signup = !signup }) { Text(if (signup) "Already have an account? Sign in" else "New here? Create an account") }
        }
    }
}

@Composable fun RewardsShell(name: String, auth: AuthViewModel) {
    val repository = remember { RewardsRepository() }; val scope = rememberCoroutineScope(); var tab by remember { mutableIntStateOf(0) }; var session by remember { mutableStateOf(SessionUi(loading = true)) }; var toast by remember { mutableStateOf<String?>(null) }
    fun reload() { scope.launch { session = session.copy(loading = true); session = runCatching { val p = repository.currentProfile(); val tx = repository.currentUserId()?.let { repository.transactions(it) }.orEmpty(); SessionUi(p, tx) }.getOrElse { SessionUi(message = "Unable to load your account") } } }
    LaunchedEffect(Unit) { reload() }
    val profile = session.profile; val balance = profile?.coins ?: 0
    val games = listOf(Game("Spin Wheel", "Lucky spin", "1–10 coins", 5, Icons.Default.Refresh, Purple), Game("Scratch Card", "Reveal a prize", "0–5 coins", 3, Icons.Default.Star, Color(0xFFF59E0B)), Game("Captcha", "Quick challenge", "+2 coins", 3, Icons.Default.Lock, Blue), Game("Math Quiz", "Five questions", "+5 coins", 2, Icons.Default.School, Mint))
    fun reward(game: Game) { scope.launch { val amount = when (game.title) { "Spin Wheel" -> Random.nextInt(1, 11); "Scratch Card" -> Random.nextInt(0, 6); "Math Quiz" -> 5; else -> 2 }; runCatching { repository.claimReward(gameTypeFor(game.title), amount) }.onSuccess { toast = "+$amount coins earned"; reload() }.onFailure { toast = if (it.message.orEmpty().contains("daily_limit")) "Daily limit reached" else "Reward unavailable" } } }
    Scaffold(containerColor = Ink, bottomBar = { NavigationBar(containerColor = Panel) { listOf("Home" to Icons.Default.Home, "Earn" to Icons.Default.Bolt, "Wallet" to Icons.Default.AccountBalanceWallet, "Profile" to Icons.Default.Person).forEachIndexed { i, item -> NavigationBarItem(tab == i, { tab = i }, icon = { Icon(item.second, null) }, label = { Text(item.first) }) } } }) { pad ->
        Box(Modifier.fillMaxSize().padding(pad)) {
            if (session.loading && profile == null) CircularProgressIndicator(Modifier.align(Alignment.Center), color = Purple)
            else when (tab) { 0 -> HomeScreen(name, balance, games, ::reward); 1 -> EarnScreen(games, ::reward); 2 -> WalletScreen(balance, session.transactions, { type, amount, destination -> scope.launch { runCatching { repository.requestRedemption(type, amount, destination) }.onSuccess { toast = "Redemption requested"; reload() }.onFailure { toast = "Insufficient balance or invalid request" } } }); else -> ProfileScreen(profile, name, { newName -> scope.launch { runCatching { repository.updateProfile(newName) }.onSuccess { toast = "Profile updated"; reload() }.onFailure { toast = "Could not update profile" } } }, { auth.signOut() }, { tab = 4 }) }
            session.message?.let { Text(it, Modifier.align(Alignment.TopCenter).padding(16.dp), color = Color(0xFFFCA5A5)) }
            if (tab == 4) AdminScreen(repository, { tab = 3 })
            toast?.let { Snackbar(Modifier.align(Alignment.BottomCenter).padding(16.dp), action = { toast = null }) { Text(it) } }
        }
    }
}

@Composable fun HomeScreen(name: String, balance: Int, games: List<Game>, onReward: (Game) -> Unit) { LazyColumn(Modifier.fillMaxSize().padding(20.dp), verticalArrangement = Arrangement.spacedBy(18.dp)) { item { Text("Good morning, $name", color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.Bold); Text("Your daily rewards are waiting", color = Muted) }; item { BalanceCard(balance) }; item { Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) { StatCard("Today", "+120", Mint, Modifier.weight(1f)); StatCard("Streak", "D4", Blue, Modifier.weight(1f)) } }; item { Text("Daily games", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold) }; items(games) { GameCard(it, onReward) } } }
@Composable fun EarnScreen(games: List<Game>, onReward: (Game) -> Unit) { LazyColumn(Modifier.fillMaxSize().padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) { item { Text("Earn coins", color = Color.White, fontSize = 30.sp, fontWeight = FontWeight.Bold); Text("Complete challenges and grow your balance", color = Muted) }; items(games) { GameCard(it, onReward) } } }
@Composable fun GameCard(game: Game, onReward: (Game) -> Unit) {
    var open by remember { mutableStateOf(false) }
    Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Panel), shape = RoundedCornerShape(22.dp)) {
        Row(Modifier.padding(18.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(50.dp).background(game.color.copy(alpha = .18f), CircleShape), contentAlignment = Alignment.Center) { Icon(game.icon, null, tint = game.color) }
            Spacer(Modifier.width(14.dp)); Column(Modifier.weight(1f)) { Text(game.title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 17.sp); Text(game.subtitle, color = Muted, fontSize = 13.sp); Text("${game.reward}  •  ${game.limit} per day", color = game.color, fontSize = 13.sp) }
            Button({ open = true }, colors = ButtonDefaults.buttonColors(containerColor = game.color), shape = RoundedCornerShape(12.dp)) { Text("PLAY") }
        }
    }
    if (open) GameChallengeDialog(game, { open = false; onReward(game) }, { open = false })
}

@Composable fun GameChallengeDialog(game: Game, onComplete: () -> Unit, onDismiss: () -> Unit) {
    var answer by remember { mutableStateOf("") }; var selected by remember { mutableStateOf<Int?>(null) }; var revealed by remember { mutableStateOf(false) }
    val question = remember { Random.nextInt(2, 9) }; val second = remember { Random.nextInt(1, 9) }
    AlertDialog(onDismissRequest = onDismiss, title = { Text(game.title) }, text = {
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            when (game.title) {
                "Spin Wheel" -> { var rotation by remember { mutableFloatStateOf(0f) }; val animated by animateFloatAsState(rotation, label = "wheel"); Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) { Text("WHEEL", Modifier.rotate(animated), color = Purple, fontSize = 28.sp, fontWeight = FontWeight.Black) }; Button({ rotation += 720f; onComplete() }, Modifier.fillMaxWidth()) { Text("SPIN NOW") } }
                "Scratch Card" -> { Text(if (revealed) "You revealed a prize" else "Scratch to reveal your prize"); Button({ revealed = true }, Modifier.fillMaxWidth()) { Text(if (revealed) "CLAIM PRIZE" else "REVEAL CARD") }; if (revealed) Text("Prize ready: 0–5 coins", color = Mint) }
                "Captcha" -> { Text("What is $question + $second?"); OutlinedTextField(answer, { answer = it }, label = { Text("Your answer") }, modifier = Modifier.fillMaxWidth()); Button({ if (answer.toIntOrNull() == question + second) onComplete() }, Modifier.fillMaxWidth()) { Text("SUBMIT") } }
                else -> { Text("Choose the correct answer: $question + $second"); listOf(question + second, question + second + 1, question + second - 1, question + 2).distinct().forEach { option -> OutlinedButton({ selected = option }, Modifier.fillMaxWidth(), colors = ButtonDefaults.outlinedButtonColors(containerColor = if (selected == option) Purple.copy(alpha = .35f) else Color.Transparent)) { Text(option.toString()) } }; Button({ if (selected == question + second) onComplete() }, Modifier.fillMaxWidth()) { Text("SUBMIT QUIZ") } }
            }
        }
    }, confirmButton = {}, dismissButton = { TextButton(onDismiss) { Text("CANCEL") } })
}
@Composable fun BalanceCard(balance: Int) { Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(26.dp), colors = CardDefaults.cardColors(containerColor = Color.Transparent)) { Box(Modifier.background(Brush.linearGradient(listOf(Color(0xFF5B21B6), Color(0xFF0EA5E9)))).padding(22.dp)) { Column { Text("TOTAL BALANCE", color = Color.White.copy(.75f), fontSize = 12.sp, letterSpacing = 2.sp); Text("$balance", color = Color.White, fontSize = 42.sp, fontWeight = FontWeight.Black); Text("coins", color = Color.White.copy(.8f)); Spacer(Modifier.height(16.dp)); Text("Earn consistently to unlock better rewards", color = Color.White.copy(.9f)) } } } }
@Composable fun StatCard(label: String, value: String, tint: Color, modifier: Modifier) { Card(modifier, colors = CardDefaults.cardColors(containerColor = Panel), shape = RoundedCornerShape(18.dp)) { Column(Modifier.padding(16.dp)) { Text(label, color = Muted); Text(value, color = tint, fontSize = 24.sp, fontWeight = FontWeight.Bold) } } }

@Composable fun WalletScreen(balance: Int, transactions: List<Transaction>, onRedeem: (String, Int, String) -> Unit) { var destination by remember { mutableStateOf("") }; LazyColumn(Modifier.fillMaxSize().padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) { item { Text("Wallet", color = Color.White, fontSize = 30.sp, fontWeight = FontWeight.Bold); BalanceCard(balance) }; item { Text("Redeem rewards", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold); OutlinedTextField(destination, { destination = it }, label = { Text("UPI ID or delivery email") }, modifier = Modifier.fillMaxWidth()) }; item { Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) { listOf("UPI", "Amazon", "Google Play").forEach { type -> Button({ if (destination.isNotBlank()) onRedeem(type, 500, destination) }, colors = ButtonDefaults.buttonColors(containerColor = Purple), shape = RoundedCornerShape(12.dp)) { Text(type) } } } }; item { Text("Transaction history", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold) }; items(transactions) { t -> ListItem(headlineContent = { Text(t.type.replaceFirstChar { it.uppercase() }, color = Color.White) }, supportingContent = { Text(t.created_at, color = Muted) }, trailingContent = { Text(if (t.amount >= 0) "+${t.amount}" else t.amount.toString(), color = if (t.amount >= 0) Mint else Color(0xFFF87171), fontWeight = FontWeight.Bold) }, colors = ListItemDefaults.colors(containerColor = Panel)) } } }

@Composable fun ProfileScreen(profile: Profile?, fallbackName: String, onSave: (String) -> Unit, onSignOut: () -> Unit, onAdmin: () -> Unit) { var editing by remember { mutableStateOf(false) }; var name by remember(profile?.display_name) { mutableStateOf(profile?.display_name ?: fallbackName) }; Column(Modifier.fillMaxSize().padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) { Text("Profile", color = Color.White, fontSize = 30.sp, fontWeight = FontWeight.Bold); Card(colors = CardDefaults.cardColors(containerColor = Panel), shape = RoundedCornerShape(22.dp)) { Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) { Text(name, color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold); Text(profile?.email ?: "", color = Muted); Text("Member since ${profile?.created_at?.take(10) ?: "today"}", color = Muted); if (editing) { OutlinedTextField(name, { name = it }, label = { Text("Display name") }, modifier = Modifier.fillMaxWidth()); Button({ onSave(name); editing = false }, colors = ButtonDefaults.buttonColors(containerColor = Purple)) { Text("Save changes") } } else TextButton({ editing = true }) { Text("Edit profile") } } }; Text("Settings", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold); ListItem(headlineContent = { Text("Notifications", color = Color.White) }, trailingContent = { Switch(true, {}) }, colors = ListItemDefaults.colors(containerColor = Panel)); if (profile?.is_admin == true) OutlinedButton(onClick = onAdmin, modifier = Modifier.fillMaxWidth()) { Text("Open admin panel") }; OutlinedButton(onSignOut, modifier = Modifier.fillMaxWidth()) { Text("Sign out") } } }

@Composable fun AdminScreen(repository: RewardsRepository, onBack: () -> Unit) { var metrics by remember { mutableStateOf<AdminMetrics?>(null) }; var error by remember { mutableStateOf<String?>(null) }; val scope = rememberCoroutineScope(); fun load() { scope.launch { runCatching { repository.adminMetrics() }.onSuccess { metrics = it; error = null }.onFailure { error = "Admin access required" } } }; LaunchedEffect(Unit) { load() }; LazyColumn(Modifier.fillMaxSize().padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) { item { Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) { Text("Admin control room", color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.Bold); TextButton(onBack) { Text("Back") } } }; error?.let { item { Text(it, color = Color(0xFFFCA5A5)) } }; metrics?.let { m -> item { Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) { StatCard("Users", m.total_users.toString(), Blue, Modifier.weight(1f)); StatCard("Coins", m.total_coins.toString(), Mint, Modifier.weight(1f)) } }; item { StatCard("Pending withdrawals", m.pending_withdrawals.toString(), Purple, Modifier.fillMaxWidth()) } }; item { Text("Moderation and payout actions are protected by Supabase admin RPCs.", color = Muted, lineHeight = 22.sp) }; item { Button({ load() }, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = Purple)) { Text("Refresh dashboard") } } } }

@Composable fun PreviewRewards() { MaterialTheme { RewardsShell("Alex", remember { AuthViewModel() }) } }
