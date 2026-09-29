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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.ui.components.auth.AuthTextField
import com.example.ui.components.auth.AuthPrimaryButton
import com.example.ui.components.auth.AuthErrorText
import com.example.ui.components.auth.AuthFooterLink
import com.example.ui.components.auth.AuthQuoteCard
import com.example.ui.theme.SakuCreamBackground
import com.example.ui.theme.SakuCreamBorder
import com.example.ui.theme.SakuDarkGreen
import com.example.ui.theme.SakuTextMuted
import com.example.ui.theme.SakuTextPrimary
import com.example.ui.theme.SakuTextSecondary

@Composable
fun RegisterScreen(
    viewModel: AuthViewModel,
    onNavigateBack: () -> Unit,
    onRegisterSuccess: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val scrollState = rememberScrollState()
    val termsAccepted = remember { mutableStateOf(false) }
    val phoneNumber = remember { mutableStateOf("") }
    val passwordVisible = remember { mutableStateOf(false) }
    val confirmPasswordVisible = remember { mutableStateOf(false) }
    val localErrorMessage = remember { mutableStateOf<String?>(null) }

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
                modifier = Modifier
                    .size(44.dp)
                    .border(2.dp, SakuDarkGreen, CircleShape)
                    .testTag("register_back_button"),
                content = {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "Kembali ke Masuk",
                        tint = SakuDarkGreen,
                        modifier = Modifier.size(24.dp)
                    )
                }
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Buat Akun",
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = SakuDarkGreen
                )
            )

            Text(
                text = "Saku",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = SakuDarkGreen
                )
            )

            Text(
                text = "Mulai kelola keuanganmu dengan lebih mudah.",
                style = MaterialTheme.typography.bodyMedium.copy(color = SakuTextSecondary),
                modifier = Modifier.padding(top = 4.dp, bottom = 24.dp)
            )

            // Full Name
            AuthTextField(
                value = uiState.fullName,
                onValueChange = { 
                    viewModel.onFullNameChange(it)
                    localErrorMessage.value = null
                },
                label = "Nama Lengkap",
                placeholder = "misal: Budi Santoso",
                leadingIcon = Icons.Default.Person,
                keyboardType = KeyboardType.Text,
                imeAction = ImeAction.Next,
                testTag = "register_name_input"
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Email
            AuthTextField(
                value = uiState.email,
                onValueChange = { 
                    viewModel.onEmailChange(it)
                    localErrorMessage.value = null
                },
                label = "Email",
                placeholder = "nama@email.com",
                leadingIcon = Icons.Default.Email,
                keyboardType = KeyboardType.Email,
                imeAction = ImeAction.Next,
                testTag = "register_email_input"
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Phone
            // ponytail: local state only — no phoneNumber field in AuthViewModel yet; add when backend needs it
            AuthTextField(
                value = phoneNumber.value,
                onValueChange = { 
                    phoneNumber.value = it
                    localErrorMessage.value = null
                },
                label = "Nomor Telepon",
                placeholder = "0812...",
                leadingIcon = Icons.Default.Phone,
                keyboardType = KeyboardType.Phone,
                imeAction = ImeAction.Next,
                testTag = "register_phone_input"
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Password with visibility toggle
            AuthTextField(
                value = uiState.password,
                onValueChange = { 
                    viewModel.onPasswordChange(it)
                    localErrorMessage.value = null
                },
                label = "Kata Sandi",
                placeholder = "Minimal 6 karakter",
                leadingIcon = Icons.Default.Lock,
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Next,
                isPasswordVisible = passwordVisible.value,
                onToggleVisibility = { passwordVisible.value = !passwordVisible.value },
                visualTransformation = PasswordVisualTransformation(),
                testTag = "register_password_input"
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Confirm Password with visibility toggle
            AuthTextField(
                value = uiState.confirmPassword,
                onValueChange = { 
                    viewModel.onConfirmPasswordChange(it)
                    localErrorMessage.value = null
                },
                label = "Konfirmasi Kata Sandi",
                placeholder = "Ulangi kata sandi",
                leadingIcon = Icons.Default.Lock,
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Done,
                isPasswordVisible = confirmPasswordVisible.value,
                onToggleVisibility = { confirmPasswordVisible.value = !confirmPasswordVisible.value },
                visualTransformation = PasswordVisualTransformation(),
                testTag = "register_confirm_password_input"
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Checkbox for terms
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = termsAccepted.value,
                    onCheckedChange = { termsAccepted.value = it },
                    colors = CheckboxDefaults.colors(
                        checkedColor = SakuDarkGreen,
                        uncheckedColor = SakuCreamBorder
                    ),
                    modifier = Modifier.testTag("register_terms_checkbox")
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Saya menyetujui Syarat & Ketentuan",
                    style = MaterialTheme.typography.bodyMedium.copy(color = SakuTextPrimary)
                )
            }

            // Error from checkbox validation or viewModel
            val errorToShow = localErrorMessage.value ?: uiState.errorMessage
            if (errorToShow != null) {
                Spacer(modifier = Modifier.height(10.dp))
                AuthErrorText(
                    errorMessage = errorToShow,
                    testTag = "register_error_text"
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            AuthPrimaryButton(
                text = "Buat Akun",
                onClick = { 
                    if (!termsAccepted.value) {
                        localErrorMessage.value = "Centang persetujuan Syarat & Ketentuan terlebih dahulu."
                    } else {
                        localErrorMessage.value = null
                        viewModel.register(onRegisterSuccess)
                    }
                },
                isLoading = uiState.isLoading,
                testTag = "register_submit_button"
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Divider
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

            // Social Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = { /* TODO: Google Login */ },
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("register_google_button"),
                    border = BorderStroke(1.dp, SakuCreamBorder)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_google),
                        contentDescription = "Google",
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
                        .testTag("register_facebook_button"),
                    border = BorderStroke(1.dp, SakuCreamBorder)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_facebook),
                        contentDescription = "Facebook",
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

            // Footer
            AuthFooterLink(
                prefixText = "Sudah punya akun?",
                linkText = "Masuk",
                onClick = onNavigateBack,
                testTag = "register_login_link"
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Quote Card
            AuthQuoteCard()

            Spacer(modifier = Modifier.height(4.dp))
        }
    }
}
