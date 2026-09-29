package com.example.ui.components.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.SakuCreamBackground
import com.example.ui.theme.SakuCreamBorder
import com.example.ui.theme.SakuDarkGreen
import com.example.ui.theme.SakuExpenseRed
import com.example.ui.theme.SakuTextPrimary
import com.example.ui.theme.SakuTextSecondary

@Composable
fun AuthTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String,
    leadingIcon: androidx.compose.ui.graphics.vector.ImageVector,
    keyboardType: KeyboardType = KeyboardType.Text,
    imeAction: ImeAction = ImeAction.Next,
    keyboardActions: androidx.compose.foundation.text.KeyboardActions = androidx.compose.foundation.text.KeyboardActions(),
    isPasswordVisible: Boolean = false,
    onToggleVisibility: (() -> Unit)? = null,
    testTag: String? = null,
    modifier: Modifier = Modifier,
    singleLine: Boolean = true,
    visualTransformation: VisualTransformation = VisualTransformation.None,
) {
    val textStyle = MaterialTheme.typography.bodyMedium.copy(color = SakuTextPrimary)
    val labelStyle = MaterialTheme.typography.bodyMedium.copy(color = SakuTextSecondary)
    val placeholderStyle = MaterialTheme.typography.bodyMedium.copy(color = SakuTextSecondary)

    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(text = label, style = labelStyle) },
        placeholder = { Text(text = placeholder, style = placeholderStyle) },
        leadingIcon = {
            Icon(
                imageVector = leadingIcon,
                contentDescription = null,
                tint = SakuDarkGreen
            )
        },
        trailingIcon = if (onToggleVisibility != null) {
            {
                IconButton(onClick = onToggleVisibility) {
                    Icon(
                        imageVector = if (isPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                        contentDescription = if (isPasswordVisible) "Sembunyikan" else "Tampilkan",
                        tint = SakuTextSecondary
                    )
                }
            }
        } else null,
        singleLine = singleLine,
        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = keyboardType, imeAction = imeAction),
        keyboardActions = keyboardActions,
        visualTransformation = if (isPasswordVisible) VisualTransformation.None else visualTransformation,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = SakuDarkGreen,
            unfocusedBorderColor = SakuCreamBorder,
            focusedLabelColor = SakuDarkGreen,
            focusedContainerColor = Color.White,
            unfocusedContainerColor = SakuCreamBackground
        ),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(14.dp),
        modifier = modifier
            .fillMaxWidth()
            .testTag(testTag ?: "")
    )
}

@Composable
fun AuthPrimaryButton(
    text: String,
    onClick: () -> Unit,
    isLoading: Boolean = false,
    enabled: Boolean = true,
    testTag: String? = null,
    modifier: Modifier = Modifier,
    height: androidx.compose.ui.unit.Dp = 52.dp
) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = SakuDarkGreen,
            contentColor = Color.White,
            disabledContainerColor = SakuCreamBorder,
            disabledContentColor = SakuTextSecondary
        ),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(14.dp),
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .testTag(testTag ?: ""),
        enabled = enabled && !isLoading
    ) {
        if (isLoading) {
            androidx.compose.material3.CircularProgressIndicator(
                color = Color.White,
                modifier = Modifier.size(24.dp),
                strokeWidth = 2.5.dp
            )
        } else {
            Text(
                text = text,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
        }
    }
}

@Composable
fun AuthFooterLink(
    prefixText: String,
    linkText: String,
    onClick: () -> Unit,
    testTag: String? = null
) {
    if (prefixText.isNotBlank()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = androidx.compose.foundation.layout.Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = prefixText,
                style = MaterialTheme.typography.bodyMedium.copy(color = SakuTextSecondary)
            )
            TextButton(onClick = onClick, modifier = Modifier.testTag(testTag ?: "")) {
                Text(
                    text = linkText,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = SakuDarkGreen,
                        fontWeight = FontWeight.Bold
                    )
                )
            }
        }
    } else {
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            TextButton(onClick = onClick, modifier = Modifier.testTag(testTag ?: "")) {
                Text(
                    text = linkText,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = SakuDarkGreen,
                        fontWeight = FontWeight.SemiBold
                    )
                )
            }
        }
    }
}

@Composable
fun AuthQuoteCard(
    quoteText: String = "Keuangan yang baik, hidup yang lebih baik.",
    modifier: Modifier = Modifier
) {
    androidx.compose.material3.Card(
        modifier = modifier.fillMaxWidth(),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
        colors = androidx.compose.material3.CardDefaults.cardColors(containerColor = Color.White),
        elevation = androidx.compose.material3.CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(
                        color = com.example.ui.theme.SakuLightGreen,
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = androidx.compose.ui.res.painterResource(id = com.example.R.drawable.ic_sprout),
                    contentDescription = null,
                    tint = SakuDarkGreen,
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(modifier = Modifier.padding(horizontal = 6.dp))
            Text(
                text = quoteText,
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = SakuDarkGreen,
                    fontWeight = FontWeight.Medium
                )
            )
        }
    }
}

@Composable
fun AuthErrorText(
    errorMessage: String?,
    testTag: String? = null
) {
    errorMessage?.let {
        Text(
            text = it,
            style = MaterialTheme.typography.bodySmall.copy(
                color = SakuExpenseRed,
                fontWeight = FontWeight.Medium
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 10.dp)
                .testTag(testTag ?: "")
        )
    }
}