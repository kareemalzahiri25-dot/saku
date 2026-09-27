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
import androidx.compose.foundation.BorderStroke
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.foundation.layout.width
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.OutlinedButton
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

            // App Brand Header (Gambar 1 — judul besar + ikon wallet kanan)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Saku",
                    style = MaterialTheme.typography.displayMedium.copy(
                        fontWeight = FontWeight.ExtraBold,
                        color = SakuDarkGreen,
                        letterSpacing = 0.5.sp
                    )
                )
                Spacer(modifier = Modifier.width(12.dp))
                Icon(
                    imageVector = Icons.Default.AccountBalanceWallet,
                    contentDescription = "Ikon Dompet",
                    tint = SakuGoldAccent,
                    modifier = Modifier.size(36.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Masuk untuk mencapai keuanganmu",
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = SakuTextMuted,
                    textAlign = TextAlign.Center
                ),
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 4.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Email/Phone Field — satu field, hint "Email / Nomor Telepon"
            OutlinedTextField(
                value = uiState.email,
                onValueChange = { viewModel.onEmailChange(it) },
                label = { Text("Email / Nomor Telepon") },
                placeholder = { Text("nama@email.com atau 0812...") },
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

            // Password Field — dengan toggle visibility
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

            // Forgot Password Link — di BAWAH field, rata kanan
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

            // Inline error message
            if (uiState.errorMessage != null) {
                Text(
                    text = uiState.errorMessage ?: "",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = SakuExpenseRed,
                        fontWeight = FontWeight.Medium
                    ),
                    modifier = Modifier.padding(top = 8.dp, bottom = 8.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Login Button (primary, full width)
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

            // Quick Demo Button — sebelum divider, outlined/text style
            Spacer(modifier = Modifier.height(10.dp))
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

            Spacer(modifier = Modifier.height(16.dp))

            // Divider "atau masuk dengan"
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                HorizontalDivider(
                    modifier = Modifier.weight(1f),
                    thickness = 1.dp,
                    color = SakuCreamBorder
                )
                Text(
                    text = "atau masuk dengan",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = SakuTextMuted,
                        fontWeight = FontWeight.Medium
                    ),
                    modifier = Modifier.padding(horizontal = 12.dp)
                )
                HorizontalDivider(
                    modifier = Modifier.weight(1f),
                    thickness = 1.dp,
                    color = SakuCreamBorder
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Social Buttons — Google & Facebook side by side
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = { /* TODO: Google Login */ },
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("google_login_button"),
                    border = BorderStroke(1.dp, SakuCreamBorder)
                ) {
                    // Placeholder Google icon (simple circle)
                    Icon(
                        imageVector = Icons.Default.AccountBalanceWallet,
                        contentDescription = "Google",
                        tint = SakuTextMuted,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Google",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = SakuTextPrimary,
                            fontWeight = FontWeight.Medium
                        )
                    )
                }

                OutlinedButton(
                    onClick = { /* TODO: Facebook Login */ },
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("facebook_login_button"),
                    border = BorderStroke(1.dp, SakuCreamBorder)
                ) {
                    // Placeholder Facebook icon (simple circle)
                    Icon(
                        imageVector = Icons.Default.AccountBalanceWallet,
                        contentDescription = "Facebook",
                        tint = SakuTextMuted,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Facebook",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = SakuTextPrimary,
                            fontWeight = FontWeight.Medium
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Register Footer
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Belum punya akun?",
                    style = MaterialTheme.typography.bodyMedium.copy(color = SakuTextSecondary)
                )
                TextButton(
                    onClick = onNavigateToRegister,
                    modifier = Modifier.testTag("register_navigation_button")
                ) {
                    Text(
                        text = "Buat Akun Baru",
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
