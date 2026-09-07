package com.lrewards.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.runtime.*
import com.lrewards.app.data.RewardsRepository
import kotlinx.coroutines.launch
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonObject
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.random.Random

private val Ink = Color(0xFF0B1020)
private val Panel = Color(0xFF151C32)
private val Purple = Color(0xFF8B5CF6)
private val Blue = Color(0xFF38BDF8)
private val Mint = Color(0xFF34D399)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) { super.onCreate(savedInstanceState); setContent { LRewardsApp() } }
}

data class Game(val title: String, val subtitle: String, val reward: String, val limit: String, val icon: androidx.compose.ui.graphics.vector.ImageVector, val color: Color)
data class Transaction(val title: String, val date: String, val amount: String)

@Composable fun LRewardsApp() {
    var signedIn by remember { mutableStateOf(false) }
    var displayName by remember { mutableStateOf("Alex") }
    if (!signedIn) AuthScreen(onSuccess = { name -> displayName = name; signedIn = true })
    else RewardsShell(displayName = displayName, onSignOut = { signedIn = false })
}

@Composable fun AuthScreen(onSuccess: (String) -> Unit) {
    var signup by remember { mutableStateOf(false) }; var email by remember { mutableStateOf("") }; var password by remember { mutableStateOf("") }; var name by remember { mutableStateOf("") }; var error by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope(); val repository = remember { RewardsRepository() }
    Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Ink, Color(0xFF201543)))), contentAlignment = Alignment.Center) {
        Column(Modifier.padding(28.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Text("L", color = Blue, fontSize = 58.sp, fontWeight = FontWeight.Black)
            Text("L REWARDS", color = Color.White, fontSize = 26.sp, fontWeight = FontWeight.Bold, letterSpacing = 4.sp)
            Text(if (signup) "Create your rewards account" else "Earn more. Redeem smarter.", color = Color(0xFFAAB5D0))
            if (signup) OutlinedTextField(name, { name = it }, label = { Text("Display name") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(email, { email = it }, label = { Text("Email") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(password, { password = it }, label = { Text("Password") }, modifier = Modifier.fillMaxWidth())
            error?.let { Text(it, color = Color(0xFFFCA5A5), textAlign = TextAlign.Center) }
            Button(onClick = { scope.launch { runCatching { if (signup) repository.signUp(email.trim(), password, name.ifBlank { "Rewarder" }) else repository.signIn(email.trim(), password) }.onSuccess { if (signup) error = "Check your email to confirm your account." else onSuccess(name.ifBlank { "Rewarder" }) }.onFailure { error = "Unable to complete authentication. Please check your details and try again." } } }, modifier = Modifier.fillMaxWidth().height(54.dp), shape = RoundedCornerShape(16.dp), colors = ButtonDefaults.buttonColors(containerColor = Purple)) { Text(if (signup) "Create account" else "Sign in", fontWeight = FontWeight.Bold) }
            TextButton({ signup = !signup }) { Text(if (signup) "Already have an account? Sign in" else "New here? Create an account") }
        }
    }
}

@Composable fun RewardsShell(displayName: String, onSignOut: () -> Unit) {
    var tab by remember { mutableIntStateOf(0) }; var balance by remember { mutableIntStateOf(2480) }; var toast by remember { mutableStateOf<String?>(null) }; var transactions by remember { mutableStateOf(emptyList<com.lrewards.app.data.Transaction>()) }; val scope = rememberCoroutineScope(); val repository = remember { RewardsRepository() }
    LaunchedEffect(Unit) { repository.currentUserId()?.let { userId -> transactions = runCatching { repository.transactions(userId) }.getOrDefault(emptyList()) } }
    val games = listOf(Game("Spin Wheel", "Lucky spin", "1–10 coins", "5 left", Icons.Default.Refresh, Purple), Game("Scratch Card", "Reveal a prize", "0–5 coins", "3 left", Icons.Default.Star, Color(0xFFF59E0B)), Game("Captcha", "Quick challenge", "+2 coins", "3 left", Icons.Default.Lock, Blue), Game("Math Quiz", "Five questions", "+5 coins", "2 left", Icons.Default.School, Mint))
    Scaffold(containerColor = Ink, bottomBar = { NavigationBar(containerColor = Panel) { listOf("Home" to Icons.Default.Home, "Earn" to Icons.Default.Bolt, "Wallet" to Icons.Default.AccountBalanceWallet, "Profile" to Icons.Default.Person).forEachIndexed { i, p -> NavigationBarItem(selected = tab == i, onClick = { tab = i }, icon = { Icon(p.second, null) }, label = { Text(p.first) }) } } }) { pad ->
        Box(Modifier.padding(pad).fillMaxSize()) {
            when (tab) { 0 -> HomeScreen(displayName, balance, games, { game, reward -> scope.launch { runCatching { repository.claimReward(game.lowercase().replace(" ", "").replace("wheel", "").replace("card", ""), reward) }.onSuccess { result -> balance = result["balance"]?.toString()?.toIntOrNull() ?: (balance + reward); toast = "+$reward coins earned" }.onFailure { error -> toast = if (error.message?.contains("daily_limit_reached", ignoreCase = true) == true) "Daily limit reached" else "Reward unavailable right now" } } }); 1 -> EarnScreen(games, { game, reward -> scope.launch { runCatching { repository.claimReward(game.lowercase().replace(" ", "").replace("wheel", "").replace("card", ""), reward) }.onSuccess { result -> balance = result["balance"]?.toString()?.toIntOrNull() ?: (balance + reward); toast = "+$reward coins earned" }.onFailure { error -> toast = if (error.message?.contains("daily_limit_reached", ignoreCase = true) == true) "Daily limit reached" else "Reward unavailable right now" } } }); 2 -> WalletScreen(balance, transactions) { rewardType, amount, destination -> scope.launch { runCatching { repository.requestRedemption(rewardType, amount, destination) }.onSuccess { balance -= amount; toast = "Redemption requested" }.onFailure { toast = "Unable to request redemption" } } }; else -> ProfileScreen(displayName, onSignOut) }
            toast?.let { message -> Snackbar(Modifier.align(Alignment.BottomCenter).padding(16.dp), action = { toast = null }) { Text(message) } }
        }
    }
}

@Composable fun HomeScreen(name: String, balance: Int, games: List<Game>, onReward: (String, Int) -> Unit) {
    LazyColumn(Modifier.fillMaxSize().padding(20.dp), verticalArrangement = Arrangement.spacedBy(18.dp)) {
        item { Text("Good morning, $name", color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.Bold); Text("Your daily rewards are waiting", color = Color(0xFFAAB5D0)) }
        item { BalanceCard(balance) }
        item { Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) { StatCard("Today", "+120", Mint, Modifier.weight(1f)); StatCard("Streak", "D4", Blue, Modifier.weight(1f)) } }
        item { Text("Daily games", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold) }
        items(games.take(2)) { game -> GameCard(game, onReward) }
        item { Text("Keep earning", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold) }
        items(games.drop(2)) { game -> GameCard(game, onReward) }
    }
}

@Composable fun EarnScreen(games: List<Game>, onReward: (String, Int) -> Unit) { LazyColumn(Modifier.fillMaxSize().padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) { item { Text("Earn coins", color = Color.White, fontSize = 30.sp, fontWeight = FontWeight.Bold); Text("Complete a challenge and grow your balance", color = Color(0xFFAAB5D0)) }; items(games) { GameCard(it, onReward) } } }

@Composable fun GameCard(game: Game, onReward: (String, Int) -> Unit) { Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Panel), shape = RoundedCornerShape(22.dp)) { Row(Modifier.padding(18.dp), verticalAlignment = Alignment.CenterVertically) { Box(Modifier.size(50.dp).background(game.color.copy(alpha = .18f), CircleShape), contentAlignment = Alignment.Center) { Icon(game.icon, null, tint = game.color) }; Spacer(Modifier.width(14.dp)); Column(Modifier.weight(1f)) { Text(game.title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 17.sp); Text(game.subtitle, color = Color(0xFFAAB5D0), fontSize = 13.sp); Text("${game.reward}  •  ${game.limit}", color = game.color, fontSize = 13.sp) }; Button({ onReward(game.title, if (game.title == "Spin Wheel") Random.nextInt(1, 11) else if (game.title == "Scratch Card") Random.nextInt(0, 6) else if (game.title == "Math Quiz") 5 else 2) }, colors = ButtonDefaults.buttonColors(containerColor = game.color.copy(alpha = .85f)), shape = RoundedCornerShape(12.dp)) { Text("PLAY") } } } }

@Composable fun BalanceCard(balance: Int) { Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(26.dp), colors = CardDefaults.cardColors(containerColor = Color.Transparent)) { Box(Modifier.background(Brush.linearGradient(listOf(Color(0xFF5B21B6), Color(0xFF0EA5E9)))).padding(22.dp)) { Column { Text("TOTAL BALANCE", color = Color.White.copy(.75f), fontSize = 12.sp, letterSpacing = 2.sp); Text("$balance", color = Color.White, fontSize = 42.sp, fontWeight = FontWeight.Black); Text("coins", color = Color.White.copy(.8f)); Spacer(Modifier.height(16.dp)); Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { Text("+120 today", color = Color.White, fontWeight = FontWeight.Bold); Text("View wallet  ›", color = Color.White.copy(.9f)) } } } } }
@Composable fun StatCard(label: String, value: String, tint: Color, modifier: Modifier) { Card(modifier, colors = CardDefaults.cardColors(containerColor = Panel), shape = RoundedCornerShape(18.dp)) { Column(Modifier.padding(16.dp)) { Text(label, color = Color(0xFFAAB5D0)); Text(value, color = tint, fontSize = 24.sp, fontWeight = FontWeight.Bold) } } }

@Composable fun WalletScreen(balance: Int, transactions: List<com.lrewards.app.data.Transaction>, onRedeem: (String, Int, String) -> Unit) { LazyColumn(Modifier.fillMaxSize().padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) { item { Text("Wallet", color = Color.White, fontSize = 30.sp, fontWeight = FontWeight.Bold); BalanceCard(balance) }; item { Text("Redeem rewards", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold) }; item { Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) { listOf("UPI", "Amazon", "Google Play").forEach { reward -> Button(onClick = { onRedeem(reward, 500, "Add destination in your account") }, colors = ButtonDefaults.buttonColors(containerColor = Purple), shape = RoundedCornerShape(14.dp)) { Text(reward) } } } }; item { Text("Recent activity", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold) }; items(transactions) { t -> ListItem(headlineContent = { Text(t.type.replaceFirstChar { it.uppercase() }, color = Color.White) }, supportingContent = { Text(t.created_at, color = Color(0xFFAAB5D0)) }, trailingContent = { Text(if (t.amount >= 0) "+${t.amount}" else t.amount.toString(), color = if (t.amount >= 0) Mint else Color(0xFFF87171), fontWeight = FontWeight.Bold) }, colors = ListItemDefaults.colors(containerColor = Panel)) } } }
@Composable fun Chip(text: String) { Surface(shape = RoundedCornerShape(14.dp), color = Panel, modifier = Modifier.clickable { }) { Text(text, Modifier.padding(horizontal = 16.dp, vertical = 14.dp), color = Color.White) } }
@Composable fun ProfileScreen(name: String, onSignOut: () -> Unit) { Column(Modifier.fillMaxSize().padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) { Text("Profile", color = Color.White, fontSize = 30.sp, fontWeight = FontWeight.Bold); Card(colors = CardDefaults.cardColors(containerColor = Panel), shape = RoundedCornerShape(22.dp)) { Row(Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) { Box(Modifier.size(58.dp).background(Purple, CircleShape), contentAlignment = Alignment.Center) { Text(name.take(1).uppercase(), color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold) }; Spacer(Modifier.width(16.dp)); Column { Text(name, color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold); Text("member@lrewards.app", color = Color(0xFFAAB5D0)) } } }; Text("Settings", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold); ListItem(headlineContent = { Text("Notifications", color = Color.White) }, trailingContent = { Switch(true, {}) }, colors = ListItemDefaults.colors(containerColor = Panel)); ListItem(headlineContent = { Text("Admin panel", color = Color.White) }, supportingContent = { Text("Restricted access", color = Color(0xFFAAB5D0)) }, colors = ListItemDefaults.colors(containerColor = Panel)); OutlinedButton(onSignOut, modifier = Modifier.fillMaxWidth()) { Text("Sign out") } } }

@Preview(showBackground = true) @Composable fun PreviewRewards() { MaterialTheme { RewardsShell("Alex", { }) } }
