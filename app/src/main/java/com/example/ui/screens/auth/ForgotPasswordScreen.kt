package com.example.ui.screens.auth

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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Email
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.mutableStateOf
import kotlinx.coroutines.launch
import com.example.ui.components.auth.AuthTextField
import com.example.ui.components.auth.AuthPrimaryButton
import com.example.ui.components.auth.AuthErrorText
import com.example.ui.components.auth.AuthFooterLink
import com.example.ui.theme.SakuCreamBackground
import com.example.ui.theme.SakuDarkGreen
import com.example.ui.theme.SakuIncomeGreen
import com.example.ui.theme.SakuTextMuted
import com.example.ui.theme.SakuTextSecondary

@Composable
fun ForgotPasswordScreen(
    viewModel: AuthViewModel,
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val scrollState = rememberScrollState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val localError = remember { mutableStateOf<String?>(null) }

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
                .padding(horizontal = 24.dp, vertical = 16.dp)
        ) {
            IconButton(
                onClick = onNavigateBack,
                modifier = Modifier.testTag("forgot_password_back_button")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Kembali ke Masuk",
                    tint = SakuDarkGreen
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Lupa Kata Sandi?",
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = SakuDarkGreen
                )
            )

            Text(
                text = "Jangan khawatir. Masukkan email yang terdaftar dan kami akan mengirimkan kode verifikasi untuk mengatur ulang kata sandimu.",
                style = MaterialTheme.typography.bodyMedium.copy(color = SakuTextSecondary),
                modifier = Modifier.padding(top = 6.dp, bottom = 24.dp)
            )

            if (uiState.forgotPasswordSuccess) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth().padding(vertical = 20.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = SakuIncomeGreen,
                        modifier = Modifier.size(54.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Instruksi Terkirim!",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = SakuDarkGreen
                        )
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Silakan periksa kotak masuk atau folder spam email Anda untuk petunjuk pengaturan ulang kata sandi.",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = SakuTextSecondary,
                            textAlign = TextAlign.Center
                        )
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                    AuthPrimaryButton(
                        text = "Kembali ke Halaman Masuk",
                        onClick = onNavigateBack,
                        height = 48.dp
                    )
                }
            } else {
                // Label "Email" above field
                Text(
                    text = "Email",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = SakuDarkGreen
                    ),
                    modifier = Modifier.padding(bottom = 6.dp)
                )

                AuthTextField(
                    value = uiState.email,
                    onValueChange = { 
                        viewModel.onEmailChange(it)
                        localError.value = null
                    },
                    label = "",
                    placeholder = "nama@email.com",
                    leadingIcon = Icons.Default.Email,
                    keyboardType = KeyboardType.Email,
                    imeAction = ImeAction.Done,
                    testTag = "forgot_password_email_input"
                )

                val errorToShow = localError.value ?: uiState.errorMessage
                if (errorToShow != null) {
                    Spacer(modifier = Modifier.height(10.dp))
                    AuthErrorText(
                        errorMessage = errorToShow,
                        testTag = "forgot_password_error_text"
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                AuthPrimaryButton(
                    text = "Kirim Kode Verifikasi",
                    onClick = {
                        // ponytail: no backend in fase ini — local validation mirrors AuthViewModel.sendResetPassword, no state swap
                        if (uiState.email.isNotBlank() && uiState.email.contains("@")) {
                            localError.value = null
                            scope.launch {
                                snackbarHostState.showSnackbar("Kode verifikasi dikirim ke email (demo)")
                            }
                        } else {
                            localError.value = "Masukkan email yang valid"
                        }
                    },
                    isLoading = uiState.isLoading,
                    testTag = "forgot_password_submit_button"
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Help text (center, gray)
                Text(
                    text = "Pastikan email yang dimasukkan sesuai dengan akun Saku yang terdaftar.",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = SakuTextMuted,
                        textAlign = TextAlign.Center
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Resend link (center, bold, SakuDarkGreen)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    TextButton(
                        onClick = {
                            scope.launch {
                                snackbarHostState.showSnackbar("Kode verifikasi dikirim ke email (demo)")
                            }
                        },
                        modifier = Modifier.testTag("forgot_password_resend_button")
                    ) {
                        Text(
                            text = "Kirim Ulang Kode Verifikasi",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = SakuDarkGreen,
                                fontWeight = FontWeight.SemiBold
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Footer: "Ingat kata sandimu? Masuk"
            AuthFooterLink(
                prefixText = "Ingat kata sandimu?",
                linkText = "Masuk",
                onClick = onNavigateBack,
                testTag = "forgot_password_back_to_login"
            )
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}
