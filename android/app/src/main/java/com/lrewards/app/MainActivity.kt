package com.lrewards.app

import android.graphics.Paint
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
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccountBalanceWallet
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Casino
import androidx.compose.material.icons.rounded.CardGiftcard
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
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.TextFieldDefaults
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
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
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { LRewardsApp() }
    }
}

@Composable
private fun LRewardsApp(auth: AuthViewModel = androidx.lifecycle.viewmodel.compose.viewModel()) {
    val state by auth.state.collectAsState()
    MaterialTheme {
        if (state.signedIn) RewardsHome(state.email, auth) else AuthForm(state, auth)
    }
}

@Composable
private fun AuthForm(state: AuthState, auth: AuthViewModel) {
    var signup by remember { mutableStateOf(false) }
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val fieldColors = OutlinedTextFieldDefaults.colors(
        focusedTextColor = Color.White,
        unfocusedTextColor = Color.White,
        focusedBorderColor = Green,
        unfocusedBorderColor = Color(0xFF78B68B),
        focusedLabelColor = Ink,
        unfocusedLabelColor = Color(0xFF275B3A),
        cursorColor = Ink,
    )
    Box(
        modifier = Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Ink, Color(0xFF155235)))).padding(22.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(Modifier.size(70.dp).background(Brush.linearGradient(listOf(Lime, Green)), CircleShape), contentAlignment = Alignment.Center) {
                Text("L", color = Ink, fontSize = 40.sp, fontWeight = FontWeight.Black)
            }
            Text("L REWARDS", color = Color.White, fontSize = 30.sp, fontWeight = FontWeight.Black)
            Text("Earn more from every moment", color = Mint)
            Spacer(Modifier.height(24.dp))
            if (signup) {
                OutlinedTextField(name, { name = it }, label = { Text("Display name") }, colors = fieldColors, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(8.dp))
            }
            OutlinedTextField(email, { email = it }, label = { Text("Email") }, colors = fieldColors, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(password, { password = it }, label = { Text("Password") }, colors = fieldColors, visualTransformation = PasswordVisualTransformation(), modifier = Modifier.fillMaxWidth())
            state.error?.let { Text(it, color = Color(0xFFFFD0C7), modifier = Modifier.padding(8.dp)) }
            Spacer(Modifier.height(12.dp))
            Button(
                enabled = !state.loading && email.isNotBlank() && password.length >= 6,
                onClick = { if (signup) auth.signUp(email.trim(), password, name.ifBlank { "Rewarder" }) else auth.signIn(email.trim(), password) },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Green, contentColor = Ink),
            ) {
                if (state.loading) CircularProgressIndicator(Modifier.size(20.dp), color = Ink) else Text(if (signup) "Create account" else "Sign in")
            }
            TextButton(onClick = { signup = !signup }) {
                Text(if (signup) "Already have an account? Sign in" else "New here? Create an account", color = Mint)
            }
        }
    }
}

private data class Game(val name: String, val subtitle: String, val reward: String, val limit: String, val icon: ImageVector, val accent: Color)

@Composable
private fun RewardsHome(email: String, auth: AuthViewModel) {
    var tab by remember { mutableIntStateOf(0) }
    var selectedGame by remember { mutableStateOf<Game?>(null) }
    var message by remember { mutableStateOf<String?>(null) }
    val wallet by auth.wallet.collectAsState()
    val coins = wallet.balance
    val games = remember {
        listOf(
            Game("Spin Wheel", "Spin for a surprise", "1–10 coins", "5 today", Icons.Rounded.Casino, Lime),
            Game("Scratch Card", "Reveal your prize", "0–5 coins", "3 today", Icons.Rounded.AutoAwesome, Color(0xFFFFB95C)),
            Game("Captcha", "Type the code", "2 coins", "3 today", Icons.Rounded.CheckCircle, Green),
            Game("Math Quiz", "5 correct answers", "5 coins", "2 today", Icons.Rounded.QuestionMark, Color(0xFF71C7FF)),
        )
    }
    Surface(Modifier.fillMaxSize(), color = Ink) {
        Column(Modifier.fillMaxSize().padding(18.dp)) {
            Text("WELCOME BACK", color = Mint, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            Text(email.substringBefore("@"), color = Color.White, fontSize = 26.sp, fontWeight = FontWeight.Black)
            Text("Your next reward is waiting", color = Muted)
            Spacer(Modifier.height(16.dp))
            Card(colors = CardDefaults.cardColors(containerColor = Panel), shape = RoundedCornerShape(26.dp), modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(20.dp)) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("TOTAL BALANCE", color = Mint, fontSize = 12.sp)
                        Text("TODAY • 0/4", color = Green, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                    Text("$coins", color = Color.White, fontSize = 42.sp, fontWeight = FontWeight.Black)
                    Text("coins available", color = Mint)
                }
            }
            Spacer(Modifier.height(18.dp))
            when (tab) {
                0, 1 -> EarnPage(games) { selectedGame = it }
                2 -> WalletPage(coins, wallet.transactions)
                3 -> RedeemPage(coins) { type, cost, destination ->
                    auth.requestRedemption(type, cost, destination) { balance, error ->
                        if (balance != null) coins = balance
                        message = error ?: "${type.replaceFirstChar { it.uppercase() }} redemption requested"
                        auth.loadWallet()
                    }
                }
                else -> ProfilePage(email, auth::signOut)
            }
            Spacer(Modifier.weight(1f))
            BottomBar(tab) { tab = it }
        }
        selectedGame?.let { game ->
            GameExperience(game, onDismiss = { selectedGame = null }) { amount ->
                auth.playGame(game.name.toType(), amount) { balance, error ->
                    if (balance != null) coins = balance
                    message = error ?: "+$amount coins added"
                    auth.loadWallet()
                    selectedGame = null
                }
            }
        }
        message?.let { text ->
            AlertDialog(
                onDismissRequest = { message = null },
                confirmButton = { TextButton(onClick = { message = null }) { Text("OK", color = Green) } },
                title = { Text("L Rewards", color = Color.White) },
                text = { Text(text, color = Mint) },
                containerColor = Panel,
            )
        }
    }
}

private fun String.toType(): String = when (this) {
    "Spin Wheel" -> "spin"
    "Scratch Card" -> "scratch"
    "Captcha" -> "captcha"
    else -> "quiz"
}

@Composable
private fun EarnPage(games: List<Game>, onGame: (Game) -> Unit) {
    Column {
        Text("Earn coins", color = Color.White, fontSize = 25.sp, fontWeight = FontWeight.Black)
        Text("Complete challenges to grow your balance", color = Muted)
        Spacer(Modifier.height(14.dp))
        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            items(games) { game ->
                Card(onClick = { onGame(game) }, colors = CardDefaults.cardColors(containerColor = Panel), shape = RoundedCornerShape(20.dp), modifier = Modifier.fillMaxWidth()) {
                    Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Box(Modifier.size(50.dp).clip(CircleShape).background(game.accent.copy(alpha = .18f)), contentAlignment = Alignment.Center) {
                            Icon(game.icon, contentDescription = null, tint = game.accent, modifier = Modifier.size(27.dp))
                        }
                        Spacer(Modifier.width(14.dp))
                        Column(Modifier.weight(1f)) {
                            Text(game.name, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 17.sp)
                            Text(game.subtitle, color = Muted, fontSize = 13.sp)
                            Text("${game.reward} • ${game.limit}", color = game.accent, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                        Text("PLAY", color = game.accent, fontWeight = FontWeight.Black, fontSize = 12.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun GameExperience(game: Game, onDismiss: () -> Unit, onReward: (Int) -> Unit) {
    var spinning by remember { mutableStateOf(false) }
    var scratchIndex by remember { mutableIntStateOf(-1) }
    var captchaInput by remember { mutableStateOf("") }
    var captchaError by remember { mutableStateOf<String?>(null) }
    var quizChoices by remember { mutableStateOf(List(5) { "" }) }
    var quizError by remember { mutableStateOf<String?>(null) }
    val rotation = remember { Animatable(0f) }
    val scope = rememberCoroutineScope()
    val captcha = remember { (1..6).map { "ABCDEFGHJKLMNPQRSTUVWXYZ23456789".random() }.joinToString("") }
    val quiz = remember { listOf("12 + 8 = ?" to listOf("18", "20", "22"), "7 × 6 = ?" to listOf("36", "42", "48"), "45 ÷ 5 = ?" to listOf("7", "8", "9"), "19 − 7 = ?" to listOf("10", "12", "14"), "8 × 4 = ?" to listOf("24", "32", "36")).map { it.first to it.second.shuffled() } }
    val answers = listOf("20", "42", "9", "12", "32")
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Panel,
        title = { Text(game.name, color = Color.White, fontWeight = FontWeight.Black) },
        text = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(game.subtitle, color = Muted)
                Spacer(Modifier.height(12.dp))
                when (game.name) {
                    "Spin Wheel" -> {
                        Text("Spin the wheel and land on a real prize", color = Mint)
                        Box(contentAlignment = Alignment.Center, modifier = Modifier.size(260.dp)) {
                            Canvas(modifier = Modifier.fillMaxSize().graphicsLayer { rotationZ = rotation.value }) {
                                val slice = 36f
                                val wheelColors = listOf(Lime, Green, Color(0xFF25A7A0), Color(0xFF4BA3FF), Color(0xFF8D6BFF), Color(0xFFFFB95C), Color(0xFFFF6B81), Color(0xFF42D778), Color(0xFF71C7FF), Color(0xFFD9F99D))
                                wheelColors.forEachIndexed { index, color ->
                                    drawArc(color, index * slice - 90f, slice, true)
                                    val angle = Math.toRadians(index * slice - 72.0)
                                    val labelCenter = center + Offset((size.minDimension * .34f * kotlin.math.cos(angle)).toFloat(), (size.minDimension * .34f * kotlin.math.sin(angle)).toFloat())
                                    drawCircle(Color.Black.copy(alpha = .22f), radius = 17f, center = labelCenter)
                                    drawIntoCanvas { canvas ->
                                        canvas.nativeCanvas.drawText(
                                            "${index + 1}",
                                            labelCenter.x,
                                            labelCenter.y + 7f,
                                            android.graphics.Paint().also { paint -> paint.color = android.graphics.Color.WHITE; paint.textSize = 22f; paint.textAlign = android.graphics.Paint.Align.CENTER; paint.isFakeBoldText = true },
                                        )
                                    }
                                }
                                drawCircle(Panel, radius = 38f, center = center)
                                drawCircle(Lime, radius = 7f, center = center)
                            }
                            Text("COINS", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Black)
                            Canvas(Modifier.align(Alignment.TopCenter).size(width = 34.dp, height = 42.dp)) { drawPath(androidx.compose.ui.graphics.Path().apply { moveTo(size.width / 2f, size.height); lineTo(0f, 0f); lineTo(size.width, 0f); close() }, color = Color(0xFFFFD166)) }
                        }
                        Text("1  ·  2  ·  3  ·  4  ·  5  ·  6  ·  7  ·  8  ·  9  ·  10", color = Lime, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Button(enabled = !spinning, onClick = {
                            spinning = true
                            scope.launch {
                                val reward = Random.nextInt(1, 11)
                                val landingOffset = (10 - reward) * 36 + 18
                                rotation.animateTo(rotation.value + 1440f + landingOffset, tween(2000))
                                spinning = false
                                onReward(reward)
                            }
                        }, colors = ButtonDefaults.buttonColors(containerColor = Green, contentColor = Ink)) { Text(if (spinning) "SPINNING…" else "SPIN WHEEL") }
                    }
                    "Scratch Card" -> {
                        val revealed = remember { mutableStateOf(false) }
                        var scratchProgress by remember { mutableIntStateOf(0) }
                        val scratchMarks = remember { androidx.compose.runtime.mutableStateListOf<Offset>() }
                        val reward = remember { Random.nextInt(0, 6) }
                        Text("Scratch the silver panel to reveal your prize", color = Mint)
                        Spacer(Modifier.height(10.dp))
                        Box(
                            modifier = Modifier
                                .size(230.dp)
                                .clip(RoundedCornerShape(24.dp))
                                .background(Brush.linearGradient(listOf(Color(0xFF173D2A), Color(0xFF2C8B54))))
                                .pointerInput(Unit) {
                                    detectDragGestures { _, position ->
                                        if (!revealed.value) {
                                            scratchMarks.add(position)
                                            scratchProgress = (scratchProgress + 1).coerceAtMost(40)
                                            if (scratchProgress >= 24) revealed.value = true
                                        }
                                    }
                                },
                            contentAlignment = Alignment.Center,
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("$reward", color = Lime, fontSize = 54.sp, fontWeight = FontWeight.Black)
                                Text("COINS", color = Color.White, fontWeight = FontWeight.Bold)
                            }
                            if (!revealed.value) {
                                Canvas(Modifier.fillMaxSize()) {
                                    drawRoundRect(Color(0xFFB7C0BA), cornerRadius = androidx.compose.ui.geometry.CornerRadius(24f, 24f))
                                    drawCircle(Color(0xFFE5ECE8), radius = 34f, center = center)
                                    drawCircle(Color(0xFF9AA8A0), radius = 30f, center = center)
                                    scratchMarks.forEach { mark -> drawCircle(Color.Transparent, 24f, mark, blendMode = BlendMode.Clear) }
                                }
                                Text("SCRATCH", color = Ink, fontWeight = FontWeight.Black)
                            }
                        }
                        Text(if (revealed.value) "Prize revealed" else "Keep rubbing to reveal", color = if (revealed.value) Lime else Muted, fontSize = 13.sp)
                        if (revealed.value) Button(onClick = { onReward(reward) }, colors = ButtonDefaults.buttonColors(containerColor = Green, contentColor = Ink)) { Text("COLLECT $reward COINS") }
                    }
                    "Captcha" -> {
                        Card(colors = CardDefaults.cardColors(containerColor = Panel2), shape = RoundedCornerShape(18.dp)) {
                            Column(Modifier.padding(horizontal = 24.dp, vertical = 16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("SECURITY CHECK · +2 COINS", color = Muted, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                Text(captcha.mapIndexed { index, char -> if (index % 2 == 0) " $char " else " ̸$char " }.joinToString(""), color = Lime, fontSize = 30.sp, fontWeight = FontWeight.Black)
                            }
                        }
                        OutlinedTextField(captchaInput, { captchaInput = it; captchaError = null }, label = { Text("Type the code", color = Mint) }, singleLine = true, colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White, cursorColor = Lime, focusedBorderColor = Green, unfocusedBorderColor = Muted, focusedLabelColor = Mint, unfocusedLabelColor = Mint))
                        captchaError?.let { Text(it, color = Color(0xFFFF9E91), fontSize = 12.sp) }
                        Button(onClick = { if (captchaInput.trim().equals(captcha, ignoreCase = true)) onReward(2) else { captchaError = "Code does not match. Try again."; captchaInput = "" } }, colors = ButtonDefaults.buttonColors(containerColor = Green, contentColor = Ink)) { Text("VERIFY CAPTCHA · +2") }
                    }
                    else -> {
                        Text("Answer all five questions", color = Mint, fontWeight = FontWeight.Bold)
                        quiz.forEachIndexed { index, question ->
                            Column(Modifier.fillMaxWidth().padding(vertical = 5.dp)) {
                                Text("${index + 1}. ${question.first}", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Black)
                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth().padding(top = 5.dp)) {
                                    question.second.forEach { option ->
                                        Button(onClick = { quizChoices = quizChoices.toMutableList().also { it[index] = option }; quizError = null }, modifier = Modifier.weight(1f), colors = ButtonDefaults.buttonColors(containerColor = if (quizChoices[index] == option) Green else Panel2, contentColor = Color.White)) { Text(option) }
                                    }
                                }
                            }
                        }
                        quizError?.let { Text(it, color = Color(0xFFFF9E91), fontSize = 12.sp) }
                        Button(onClick = { if (quizChoices.any(String::isBlank)) quizError = "Answer all five questions first." else if (quizChoices == answers) onReward(5) else { quizError = "Some answers are incorrect. Try again."; quizChoices = List(5) { "" } } }, colors = ButtonDefaults.buttonColors(containerColor = Green, contentColor = Ink)) { Text("CHECK QUIZ · +5") }
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Close", color = Mint) } },
    )
}

@Composable
private fun WalletPage(coins: Int, transactions: List<kotlinx.serialization.json.JsonObject>) {
    Column {
        Text("Wallet", color = Color.White, fontSize = 25.sp, fontWeight = FontWeight.Black)
        Text("Your earnings and reward history", color = Muted)
        Spacer(Modifier.height(18.dp))
        Card(colors = CardDefaults.cardColors(containerColor = Panel), shape = RoundedCornerShape(20.dp), modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(18.dp)) {
                Text("AVAILABLE BALANCE", color = Mint, fontSize = 11.sp)
                Text("$coins coins", color = Lime, fontSize = 30.sp, fontWeight = FontWeight.Black)
                Text("100 coins = ₹1", color = Muted, fontSize = 12.sp)
                Spacer(Modifier.height(12.dp))
                Text("Redeem options: UPI Cash • Amazon Gift Cards • Google Play", color = Mint, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }
        Spacer(Modifier.height(18.dp))
        Text("REWARD HISTORY", color = Mint, fontWeight = FontWeight.Bold)
        if (transactions.isEmpty()) Text("No earning transactions yet", color = Muted, modifier = Modifier.padding(top = 8.dp)) else transactions.take(5).forEach { transaction -> HistoryRow(transaction["game_type"]?.toString()?.trim('"')?.replaceFirstChar { it.uppercase() } ?: transaction["type"]?.toString()?.trim('"') ?: "Reward", "+${transaction["amount"] ?: transaction["coins"] ?: 0} coins", transaction["created_at"]?.toString()?.trim('"') ?: "Completed", Lime) }
        Spacer(Modifier.height(14.dp))
    }
}

@Composable
private fun RedeemPage(coins: Int, onRedeem: (String, Int, String) -> Unit) {
    var destination by remember { mutableStateOf("") }
    val options = listOf(
        Triple("upi", "UPI Cash", listOf(1000 to "₹10", 2000 to "₹20", 3000 to "₹30")),
        Triple("amazon", "Amazon Gift Cards", listOf(1000 to "₹10", 2500 to "₹25", 5000 to "₹50")),
        Triple("google_play", "Google Play Gift Cards", listOf(1000 to "₹10", 2500 to "₹25", 5000 to "₹50")),
    )
    Column {
        Text("Redeem", color = Color.White, fontSize = 25.sp, fontWeight = FontWeight.Black)
        Text("Choose a real payout reward", color = Muted)
        Text("100 coins = ₹1", color = Lime, fontSize = 22.sp, fontWeight = FontWeight.Black, modifier = Modifier.padding(top = 8.dp))
        Text("Your balance: $coins coins · ₹${"%.2f".format(coins / 100.0)} available value", color = Mint)
        OutlinedTextField(destination, { destination = it }, label = { Text("UPI ID or gift-card email") }, singleLine = true, colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White, focusedBorderColor = Green, unfocusedBorderColor = Muted), modifier = Modifier.fillMaxWidth().padding(top = 14.dp))
        Spacer(Modifier.height(18.dp))
        options.forEach { (type, label, payouts) ->
            Text(label, color = Mint, fontSize = 18.sp, fontWeight = FontWeight.Black, modifier = Modifier.padding(top = 10.dp, bottom = 8.dp))
            payouts.forEach { (cost, value) ->
                Card(colors = CardDefaults.cardColors(containerColor = Panel), shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)) {
                    Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) { Text("$label $value", color = Color.White, fontWeight = FontWeight.Bold); Text("$cost coins · $value", color = Muted, fontSize = 12.sp) }
                        Button(enabled = coins >= cost && destination.trim().length >= 3, onClick = { onRedeem(type, cost, destination.trim()) }, colors = ButtonDefaults.buttonColors(containerColor = Green, contentColor = Ink)) { Text(if (coins >= cost) "Redeem" else "Need more") }
                    }
                }
            }
        }
    }
}

@Composable
private fun HistoryRow(title: String, amount: String, detail: String, accent: Color) {
    Row(Modifier.fillMaxWidth().padding(top = 10.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(34.dp).background(accent.copy(alpha = .18f), CircleShape), contentAlignment = Alignment.Center) { Text("+", color = accent, fontWeight = FontWeight.Black) }
        Spacer(Modifier.width(10.dp))
        Column(Modifier.weight(1f)) { Text(title, color = Color.White, fontWeight = FontWeight.Bold); Text(detail, color = Muted, fontSize = 12.sp) }
        Text(amount, color = accent, fontWeight = FontWeight.Black)
    }
}

@Composable
private fun ProfilePage(email: String, signOut: () -> Unit) {
    Column {
        Text("Profile", color = Color.White, fontSize = 25.sp, fontWeight = FontWeight.Black)
        Text("Your L Rewards account", color = Muted)
        Spacer(Modifier.height(18.dp))
        Card(colors = CardDefaults.cardColors(containerColor = Panel), shape = RoundedCornerShape(20.dp), modifier = Modifier.fillMaxWidth()) {
            Row(Modifier.padding(18.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(58.dp).background(Brush.linearGradient(listOf(Lime, Green)), CircleShape), contentAlignment = Alignment.Center) { Text(email.take(1).uppercase(), color = Ink, fontSize = 26.sp, fontWeight = FontWeight.Black) }
                Spacer(Modifier.width(14.dp))
                Column { Text(email.substringBefore("@"), color = Color.White, fontWeight = FontWeight.Black, fontSize = 18.sp); Text(email, color = Mint, fontSize = 12.sp); Text("Verified reward member", color = Muted, fontSize = 12.sp) }
            }
        }
        Spacer(Modifier.height(18.dp))
        Text("PROFILE & SETTINGS", color = Mint, fontWeight = FontWeight.Bold)
        SettingRow("Referral program", "Invite friends and earn bonus coins", Icons.Rounded.AutoAwesome)
        SettingRow("Notifications", "Reward and withdrawal updates", Icons.Rounded.CheckCircle)
        SettingRow("Help center", "Get support for your account", Icons.Rounded.QuestionMark)
        Spacer(Modifier.height(16.dp))
        Button(onClick = signOut, colors = ButtonDefaults.buttonColors(containerColor = Green, contentColor = Ink)) { Text("SIGN OUT") }
    }
}

@Composable
private fun SettingRow(title: String, detail: String, icon: ImageVector) {
    Row(Modifier.fillMaxWidth().padding(top = 14.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(38.dp).background(Lime.copy(alpha = .14f), CircleShape), contentAlignment = Alignment.Center) { Icon(icon, contentDescription = null, tint = Lime, modifier = Modifier.size(19.dp)) }
        Spacer(Modifier.width(12.dp))
        Column { Text(title, color = Color.White, fontWeight = FontWeight.Bold); Text(detail, color = Muted, fontSize = 12.sp) }
    }
}

@Composable
private fun BottomBar(selected: Int, onSelected: (Int) -> Unit) {
    Row(Modifier.fillMaxWidth().background(Panel, RoundedCornerShape(24.dp)).padding(8.dp), horizontalArrangement = Arrangement.SpaceAround) {
        val icons = listOf(Icons.Rounded.Home, Icons.Rounded.SportsEsports, Icons.Rounded.AccountBalanceWallet, Icons.Rounded.CardGiftcard, Icons.Rounded.Person)
        icons.forEachIndexed { index, icon ->
            Icon(icon, contentDescription = null, tint = if (selected == index) Lime else Muted, modifier = Modifier.size(30.dp).clickable { onSelected(index) }.padding(5.dp))
        }
    }
}
