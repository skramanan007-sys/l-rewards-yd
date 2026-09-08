package com.lrewards.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.graphicsLayer
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
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { LRewardsApp() }
    }
}

@Composable
private fun LRewardsApp(auth: AuthViewModel = viewModel()) {
    val state by auth.state.collectAsState()
    MaterialTheme {
        if (state.signedIn) RewardsHome(state.email, auth) else AuthForm(state, auth)
    }
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
            if (signUp) {
                OutlinedTextField(name, { name = it }, label = { Text("Display name") }, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(10.dp))
            }
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

@Composable
private fun BrandMark() {
    Box(Modifier.size(62.dp).background(Brush.linearGradient(listOf(Lime, Green)), CircleShape), contentAlignment = Alignment.Center) { Text("L", color = Ink, fontSize = 36.sp, fontWeight = FontWeight.Black) }
}

private data class Game(val title: String, val subtitle: String, val reward: String, val limit: String, val icon: ImageVector, val color: Color)

@Composable
private fun RewardsHome(email: String, auth: AuthViewModel) {
    var tab by remember { mutableIntStateOf(0) }
    var coins by remember { mutableIntStateOf(0) }
    var selected by remember { mutableStateOf<Game?>(null) }
    val games = remember {
        listOf(
            Game("Spin Wheel", "Try your luck", "1–10 coins", "5 plays", Icons.Rounded.Casino, Lime),
            Game("Scratch Card", "Reveal a prize", "0–5 coins", "3 plays", Icons.Rounded.AutoAwesome, Color(0xFFFFB95C)),
            Game("Captcha", "Quick challenge", "2 coins", "3 plays", Icons.Rounded.CheckCircle, Green),
            Game("Math Quiz", "Five questions", "5 coins", "2 plays", Icons.Rounded.QuestionMark, Color(0xFF71C7FF)),
        )
    }
    Surface(Modifier.fillMaxSize(), color = Ink) {
        Column(Modifier.fillMaxSize().padding(horizontal = 18.dp, vertical = 16.dp)) {
            Header(email)
            Spacer(Modifier.height(18.dp))
            BalanceCard(coins)
            Spacer(Modifier.height(22.dp))
            when (tab) {
                0, 1 -> EarnPage(games) { selected = it }
                2 -> WalletPage(coins)
                else -> ProfilePage(email, auth::signOut)
            }
            Spacer(Modifier.weight(1f))
            BottomBar(tab) { tab = it }
        }
        selected?.let { game -> GameDialog(game, { selected = null }) { reward -> auth.playGame(game.title.toGameType(), reward) { balance, _ -> if (balance != null) coins = balance; selected = null } } }
    }
}

private fun String.toGameType(): String = when (this) {
    "Spin Wheel" -> "spin"
    "Scratch Card" -> "scratch"
    "Captcha" -> "captcha"
    else -> "quiz"
}

@Composable
private fun Header(email: String) {
    Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
        Column { Text("WELCOME BACK", color = Mint, fontSize = 11.sp, fontWeight = FontWeight.Bold); Text(email.substringBefore("@"), color = Color.White, fontSize = 25.sp, fontWeight = FontWeight.Black); Text("Ready to earn today?", color = Muted, fontSize = 13.sp) }
        Box(Modifier.size(44.dp).background(SoftGreen, CircleShape), contentAlignment = Alignment.Center) { Icon(Icons.Rounded.AutoAwesome, null, tint = Lime) }
    }
}

@Composable
private fun BalanceCard(coins: Int) {
    Card(colors = CardDefaults.cardColors(containerColor = Color.Transparent), shape = RoundedCornerShape(28.dp), modifier = Modifier.fillMaxWidth()) {
        Box(Modifier.background(Brush.linearGradient(listOf(Color(0xFF2D963F), Color(0xFF0E4C31))).then(Modifier.padding(22.dp)))) {
            Column { Text("TOTAL BALANCE", color = Mint, fontSize = 12.sp, fontWeight = FontWeight.Bold); Text("$coins", color = Color.White, fontSize = 40.sp, fontWeight = FontWeight.Black); Text("coins available", color = Mint, fontSize = 13.sp); Spacer(Modifier.height(16.dp)); Text("Keep earning to unlock rewards", color = Color.White, fontSize = 12.sp) }
        }
    }
}

@Composable
private fun EarnPage(games: List<Game>, onGame: (Game) -> Unit) {
    Column {
        Text("Earn coins", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Black)
        Text("Pick a challenge and keep your streak", color = Muted, fontSize = 13.sp)
        Spacer(Modifier.height(16.dp))
        LazyVerticalGrid(columns = GridCells.Fixed(2), verticalArrangement = Arrangement.spacedBy(12.dp), horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.height(340.dp)) { items(games) { game -> GameCard(game) { onGame(game) } } }
        Spacer(Modifier.height(18.dp))
        Card(colors = CardDefaults.cardColors(containerColor = SurfaceGreen), shape = RoundedCornerShape(20.dp), modifier = Modifier.fillMaxWidth()) { Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) { Icon(Icons.Rounded.SportsEsports, null, tint = Lime, modifier = Modifier.size(28.dp)); Spacer(Modifier.width(12.dp)); Column { Text("Daily streak", color = Color.White, fontWeight = FontWeight.Bold); Text("Play one game today to keep it alive", color = Muted, fontSize = 12.sp) } } }
    }
}

@Composable
private fun GameCard(game: Game, onClick: () -> Unit) {
    Card(onClick = onClick, colors = CardDefaults.cardColors(containerColor = SurfaceGreen), shape = RoundedCornerShape(22.dp), modifier = Modifier.border(1.dp, game.color.copy(alpha = .22f), RoundedCornerShape(22.dp))) {
        Column(Modifier.padding(15.dp)) { Box(Modifier.size(44.dp).background(game.color.copy(alpha = .15f), CircleShape), Alignment.Center) { Icon(game.icon, null, tint = game.color) }; Spacer(Modifier.height(12.dp)); Text(game.title, color = Color.White, fontWeight = FontWeight.Bold); Text(game.subtitle, color = Muted, fontSize = 12.sp); Spacer(Modifier.height(12.dp)); Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) { Text(game.reward, color = game.color, fontWeight = FontWeight.Bold, fontSize = 12.sp); Text(game.limit, color = Muted, fontSize = 11.sp) } }
    }
}

@Composable
private fun GameDialog(game: Game, onDismiss: () -> Unit, onReward: (Int) -> Unit) {
    var answer by remember { mutableStateOf("") }
    var scratchIndex by remember { mutableIntStateOf(-1) }
    var spin by remember { mutableStateOf(false) }
    var rotation by remember { mutableStateOf(0f) }
    var quizStep by remember { mutableIntStateOf(0) }
    var quizScore by remember { mutableIntStateOf(0) }
    val captcha = remember { "69Y67" }
    val questions = listOf("10 + 5 = ?" to listOf("12", "15", "17"), "9 + 9 = ?" to listOf("16", "18", "20"), "13 + 3 = ?" to listOf("14", "16", "23"), "9 + 2 = ?" to listOf("11", "13", "18"), "7 + 5 = ?" to listOf("10", "12", "15"))
    val correct = listOf("15", "18", "16", "11", "12")
    val scratchRewards = listOf(0, 1, 2, 3, 4, 5, 1, 2, 3)
    val animatedRotation by animateFloatAsState(if (spin) rotation else 0f, animationSpec = tween(1100), label = "wheel")
    val quizComplete = game.title == "Math Quiz" && quizStep == 4
    AlertDialog(onDismissRequest = onDismiss, containerColor = SurfaceGreen, title = { Text(game.title, color = Color.White, fontWeight = FontWeight.Black) }, text = {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Complete the challenge to earn coins", color = Muted)
            Spacer(Modifier.height(12.dp))
            when (game.title) {
                "Spin Wheel" -> { Box(Modifier.size(190.dp).graphicsLayer { rotationZ = animatedRotation }.background(Brush.sweepGradient(listOf(Lime, Green, Color(0xFFFFC857), Green, Lime)), CircleShape), contentAlignment = Alignment.Center) { Box(Modifier.size(90.dp).background(SurfaceGreen, CircleShape), contentAlignment = Alignment.Center) { Text("SPIN", color = Lime, fontWeight = FontWeight.Black) } }; Text("1–10 coins", color = Mint, modifier = Modifier.padding(top = 10.dp)) }
                "Scratch Card" -> { Text("Choose one card to reveal", color = Mint); Spacer(Modifier.height(10.dp)); LazyVerticalGrid(columns = GridCells.Fixed(3), modifier = Modifier.height(180.dp), verticalArrangement = Arrangement.spacedBy(8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) { items(9) { index -> Card(onClick = { scratchIndex = index }, colors = CardDefaults.cardColors(containerColor = if (scratchIndex == index) Green.copy(alpha = .35f) else Color(0xFF68756E)), shape = RoundedCornerShape(14.dp)) { Box(Modifier.height(52.dp), contentAlignment = Alignment.Center) { Text(if (scratchIndex == index) "${scratchRewards[index]} COINS" else "?", color = Color.White, fontWeight = FontWeight.Black, fontSize = 11.sp) } } } } }
                "Captcha" -> { Text("Type the code exactly", color = Mint); Text(captcha, color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.Black, modifier = Modifier.padding(10.dp)); OutlinedTextField(answer, { answer = it }, label = { Text("Captcha") }) }
                else -> { Text("Question ${quizStep + 1} of 5", color = Muted); Text(questions[quizStep].first, color = Mint, fontSize = 21.sp, fontWeight = FontWeight.Black, modifier = Modifier.padding(8.dp)); questions[quizStep].second.forEach { option -> Button(onClick = { if (option == correct[quizStep]) quizScore++; if (quizStep < 4) quizStep++ }, modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp), colors = ButtonDefaults.buttonColors(containerColor = SoftGreen, contentColor = Color.White)) { Text(option) } }; Text("Score: $quizScore", color = Muted, fontSize = 12.sp) }
            }
        }
    }, confirmButton = { Button(onClick = { when (game.title) { "Spin Wheel" -> { spin = true; rotation += 1440f; onReward((1..10).random()) }; "Scratch Card" -> onReward(scratchRewards[scratchIndex]); "Captcha" -> if (answer.equals(captcha, ignoreCase = true)) onReward(2); "Math Quiz" -> if (quizComplete) onReward(if (quizScore == 5) 5 else 0) } }, enabled = !spin && (game.title != "Scratch Card" || scratchIndex >= 0) && (game.title != "Captcha" || answer.isNotBlank()) && (game.title != "Math Quiz" || quizComplete), colors = ButtonDefaults.buttonColors(containerColor = Green, contentColor = Ink)) { Text("CLAIM REWARD") }, dismissButton = { TextButton(onClick = onDismiss) { Text("Close", color = Mint) } })
}

@Composable
private fun WalletPage(coins: Int) { Column { Text("Wallet", color = Color.White, fontSize = 25.sp, fontWeight = FontWeight.Black); Text("Your available balance", color = Muted); Spacer(Modifier.height(20.dp)); Text("$coins coins", color = Lime, fontSize = 32.sp, fontWeight = FontWeight.Black) } }

@Composable
private fun ProfilePage(email: String, signOut: () -> Unit) { Column { Text("Profile", color = Color.White, fontSize = 25.sp, fontWeight = FontWeight.Black); Spacer(Modifier.height(12.dp)); Text(email, color = Mint); Spacer(Modifier.height(20.dp)); Button(onClick = signOut, colors = ButtonDefaults.buttonColors(containerColor = Green, contentColor = Ink)) { Text("Sign out") } } }

@Composable
private fun BottomBar(selected: Int, onSelected: (Int) -> Unit) { Row(Modifier.fillMaxWidth().background(SurfaceGreen, RoundedCornerShape(24.dp)).padding(8.dp), horizontalArrangement = Arrangement.SpaceAround) { listOf(Icons.Rounded.Home, Icons.Rounded.SportsEsports, Icons.Rounded.AccountBalanceWallet, Icons.Rounded.Person).forEachIndexed { index, icon -> Icon(icon, "", tint = if (selected == index) Lime else Muted, modifier = Modifier.size(28.dp).clickable { onSelected(index) }.padding(4.dp)) } } }
``} alcuff＿日本 ажәлар 彩神争霸如何ai? }彩网大发快三 } иаа] } 海南天天中彩票 
