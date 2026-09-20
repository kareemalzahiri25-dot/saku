package com.example.ui.screens.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.ui.components.SakuCard
import com.example.ui.theme.SakuCreamBackground
import com.example.ui.theme.SakuCreamBorder
import com.example.ui.theme.SakuCreamSurface
import com.example.ui.theme.SakuDarkGreen
import com.example.ui.theme.SakuExpenseRed
import com.example.ui.theme.SakuGoldAccent
import com.example.ui.theme.SakuTextMuted
import com.example.ui.theme.SakuTextPrimary
import com.example.ui.theme.SakuTextSecondary

@Composable
fun LoginScreen(
    viewModel: AuthViewModel,
    onNavigateToRegister: () -> Unit,
    onNavigateToForgotPassword: () -> Unit,
    onLoginSuccess: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val scrollState = rememberScrollState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(SakuCreamBackground)
            .windowInsetsPadding(WindowInsets.statusBars)
            .imePadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 24.dp, vertical = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // App Brand Logo & Header
            Box(
                modifier = Modifier
                    .size(84.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(SakuDarkGreen),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.img_saku_icon),
                    contentDescription = "Logo Saku",
                    modifier = Modifier.size(64.dp),
                    contentScale = ContentScale.Fit
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Saku",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.ExtraBold,
                    color = SakuDarkGreen,
                    letterSpacing = 1.sp
                )
            )

            Text(
                text = "Kelola finansial pribadimu dengan tenang & bijak",
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = SakuTextSecondary,
                    textAlign = TextAlign.Center
                ),
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
            )

            Spacer(modifier = Modifier.height(28.dp))

            // Login Card
            SakuCard(
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = SakuCreamSurface,
                cornerRadius = 24.dp,
                elevation = 3.dp
            ) {
                Column(
                    modifier = Modifier.padding(24.dp)
                ) {
                    Text(
                        text = "Masuk ke Akun",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = SakuDarkGreen
                        )
                    )
                    Text(
                        text = "Akses catatan keuangan offline-first Anda",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = SakuTextMuted
                        )
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // Email Field
                    OutlinedTextField(
                        value = uiState.email,
                        onValueChange = { viewModel.onEmailChange(it) },
                        label = { Text("Email") },
                        placeholder = { Text("nama@email.com") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Email,
                                contentDescription = null,
                                tint = SakuDarkGreen
                            )
                        },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Email,
                            imeAction = ImeAction.Next
                        ),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = SakuDarkGreen,
                            unfocusedBorderColor = SakuCreamBorder,
                            focusedLabelColor = SakuDarkGreen,
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = SakuCreamBackground
                        ),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("login_email_input")
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Password Field
                    OutlinedTextField(
                        value = uiState.password,
                        onValueChange = { viewModel.onPasswordChange(it) },
                        label = { Text("Kata Sandi") },
                        placeholder = { Text("••••••••") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = null,
                                tint = SakuDarkGreen
                            )
                        },
                        trailingIcon = {
                            IconButton(onClick = { viewModel.togglePasswordVisibility() }) {
                                Icon(
                                    imageVector = if (uiState.isPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = if (uiState.isPasswordVisible) "Sembunyikan" else "Tampilkan",
                                    tint = SakuTextMuted
                                )
                            }
                        },
                        visualTransformation = if (uiState.isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = { viewModel.login(onLoginSuccess) }
                        ),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = SakuDarkGreen,
                            unfocusedBorderColor = SakuCreamBorder,
                            focusedLabelColor = SakuDarkGreen,
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = SakuCreamBackground
                        ),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("login_password_input")
                    )

                    // Forgot Password Link
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(
                            onClick = onNavigateToForgotPassword,
                            modifier = Modifier.testTag("forgot_password_button")
                        ) {
                            Text(
                                text = "Lupa Kata Sandi?",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    color = SakuDarkGreen,
                                    fontWeight = FontWeight.SemiBold
                                )
                            )
                        }
                    }

                    if (uiState.errorMessage != null) {
                        Text(
                            text = uiState.errorMessage ?: "",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = SakuExpenseRed,
                                fontWeight = FontWeight.Medium
                            ),
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Login Button
                    Button(
                        onClick = { viewModel.login(onLoginSuccess) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = SakuDarkGreen,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("login_submit_button"),
                        enabled = !uiState.isLoading
                    ) {
                        if (uiState.isLoading) {
                            CircularProgressIndicator(
                                color = Color.White,
                                modifier = Modifier.size(24.dp),
                                strokeWidth = 2.5.dp
                            )
                        } else {
                            Text(
                                text = "Masuk",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Quick Demo Fill
                    TextButton(
                        onClick = {
                            viewModel.onEmailChange("budi.santoso@email.com")
                            viewModel.onPasswordChange("saku1234")
                            viewModel.login(onLoginSuccess)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("quick_demo_login_button")
                    ) {
                        Text(
                            text = "Masuk Cepat Sebagai Pengguna Demo",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = SakuGoldAccent,
                                fontWeight = FontWeight.SemiBold
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Register Footer
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Belum memiliki akun?",
                    style = MaterialTheme.typography.bodyMedium.copy(color = SakuTextSecondary)
                )
                TextButton(
                    onClick = onNavigateToRegister,
                    modifier = Modifier.testTag("register_navigation_button")
                ) {
                    Text(
                        text = "Daftar Sekarang",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = SakuDarkGreen,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
