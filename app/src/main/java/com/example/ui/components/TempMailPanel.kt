package com.example.ui.components

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AlternateEmail
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.tempmail.TempMailMessage
import com.example.tempmail.TempMailRepository
import com.example.tempmail.TempMailbox
import com.example.ui.theme.BluePurpleHorizontalGradient
import com.example.ui.theme.GlassBorder
import com.example.ui.theme.GlassCardFill
import com.example.ui.theme.GlassCardSoft
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.MintEmerald
import com.example.ui.theme.NeonSkyBlue
import com.example.ui.theme.ObsidianDeep
import com.example.ui.theme.ObsidianElevated
import com.example.ui.theme.SoftLavender
import com.example.ui.theme.SubtleCardBorderGradient
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.VibrantPurple
import com.example.ui.theme.WarmAmber
import kotlinx.coroutines.launch

@Composable
fun TempMailPanel(
    onOpenTempMailWeb: (String) -> Unit,
    onShowNotice: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val clipboardManager = LocalClipboardManager.current
    val scope = rememberCoroutineScope()

    var mailbox by remember { mutableStateOf<TempMailbox?>(null) }
    var messages by remember { mutableStateOf<List<TempMailMessage>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    var selectedMessage by remember { mutableStateOf<TempMailMessage?>(null) }
    var selectedMessageBody by remember { mutableStateOf<String?>(null) }
    var isBodyLoading by remember { mutableStateOf(false) }

    val generateNewAddress: () -> Unit = {
        if (!isLoading) {
            isLoading = true
            scope.launch {
                TempMailRepository.createMailbox()
                    .onSuccess { created ->
                        mailbox = created
                        messages = emptyList()
                        clipboardManager.setText(AnnotatedString(created.address))
                        onShowNotice("Created & copied: ${created.address}")
                    }
                    .onFailure { err ->
                        onShowNotice(err.message ?: "Unable to create temporary email.")
                    }
                isLoading = false
            }
        }
    }

    val refreshInbox: () -> Unit = {
        val current = mailbox
        if (current == null) {
            generateNewAddress()
        } else if (!isLoading) {
            isLoading = true
            scope.launch {
                TempMailRepository.fetchMessages(current)
                    .onSuccess { list ->
                        messages = list
                        onShowNotice(
                            if (list.isEmpty()) "Inbox checked — no messages yet."
                            else "Found ${list.size} message(s)."
                        )
                    }
                    .onFailure { err ->
                        onShowNotice(err.message ?: "Unable to refresh inbox.")
                    }
                isLoading = false
            }
        }
    }

    Surface(
        modifier = modifier.animateContentSize(),
        shape = RoundedCornerShape(22.dp),
        color = GlassCardFill,
        border = BorderStroke(1.dp, SubtleCardBorderGradient),
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(VibrantPurple.copy(alpha = 0.20f))
                            .border(1.dp, VibrantPurple.copy(alpha = 0.5f), CircleShape),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = Icons.Default.AlternateEmail,
                            contentDescription = null,
                            tint = SoftLavender,
                            modifier = Modifier.size(20.dp),
                        )
                    }
                    Column {
                        Text(
                            text = stringResource(R.string.tempmail_title),
                            style = MaterialTheme.typography.titleLarge,
                            color = TextPrimary,
                        )
                        Text(
                            text = stringResource(R.string.tempmail_subtitle),
                            style = MaterialTheme.typography.labelSmall,
                            color = NeonSkyBlue,
                        )
                    }
                }

                OutlinedButton(
                    onClick = { onOpenTempMailWeb(mailbox?.webUrl ?: "https://mail.tm/") },
                    modifier = Modifier
                        .height(36.dp)
                        .testTag("tempmail_open_web_button"),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 0.dp),
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, GlassBorder.copy(alpha = 0.55f)),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = SoftLavender),
                ) {
                    Text(
                        text = stringResource(R.string.tempmail_open_web),
                        style = MaterialTheme.typography.labelSmall,
                    )
                }
            }

            // Address Display Box (Ninja TempMail style)
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = ObsidianDeep.copy(alpha = 0.85f),
                border = BorderStroke(
                    1.dp,
                    if (mailbox != null) MintEmerald.copy(alpha = 0.55f) else GlassBorder.copy(alpha = 0.35f),
                ),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(
                        text = mailbox?.address ?: "Tap \"New Email\" to generate address",
                        fontFamily = JetBrainsMonoFontFamily,
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (mailbox != null) MintEmerald else TextSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f),
                    )
                    if (mailbox != null) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Surface(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .clickable {
                                    clipboardManager.setText(AnnotatedString(mailbox!!.address))
                                    onShowNotice("Copied ${mailbox!!.address}")
                                }
                                .testTag("tempmail_copy_button"),
                            shape = RoundedCornerShape(10.dp),
                            color = NeonSkyBlue.copy(alpha = 0.16f),
                            border = BorderStroke(1.dp, NeonSkyBlue.copy(alpha = 0.45f)),
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ContentCopy,
                                    contentDescription = stringResource(R.string.tempmail_copy),
                                    tint = NeonSkyBlue,
                                    modifier = Modifier.size(14.dp),
                                )
                                Text(
                                    text = stringResource(R.string.tempmail_copy),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = NeonSkyBlue,
                                )
                            }
                        }
                    }
                }
            }

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(BluePurpleHorizontalGradient),
                ) {
                    Button(
                        onClick = generateNewAddress,
                        enabled = !isLoading,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("tempmail_generate_button"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Transparent,
                            contentColor = TextPrimary,
                        ),
                        contentPadding = PaddingValues(horizontal = 12.dp),
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp,
                                color = TextPrimary,
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Email,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = stringResource(R.string.tempmail_generate),
                                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                                maxLines = 1,
                            )
                        }
                    }
                }

                OutlinedButton(
                    onClick = refreshInbox,
                    enabled = !isLoading,
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("tempmail_refresh_button"),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, NeonSkyBlue.copy(alpha = 0.55f)),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = NeonSkyBlue),
                    contentPadding = PaddingValues(horizontal = 10.dp),
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = stringResource(R.string.tempmail_refresh),
                        style = MaterialTheme.typography.labelLarge,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }

            // Inbox List
            if (mailbox != null) {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = GlassCardSoft,
                    border = BorderStroke(1.dp, GlassBorder.copy(alpha = 0.3f)),
                ) {
                    if (messages.isEmpty()) {
                        Text(
                            text = stringResource(R.string.tempmail_empty),
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                        )
                    } else {
                        Column(
                            modifier = Modifier.padding(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            messages.take(6).forEach { msg ->
                                Surface(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(10.dp))
                                        .clickable {
                                            selectedMessage = msg
                                            selectedMessageBody = null
                                            val currentBox = mailbox ?: return@clickable
                                            isBodyLoading = true
                                            scope.launch {
                                                TempMailRepository.fetchMessageBody(currentBox, msg.id)
                                                    .onSuccess { body -> selectedMessageBody = body }
                                                    .onFailure { err ->
                                                        selectedMessageBody = err.message ?: "Unable to load message."
                                                    }
                                                isBodyLoading = false
                                            }
                                        }
                                        .testTag("tempmail_msg_${msg.id}"),
                                    shape = RoundedCornerShape(10.dp),
                                    color = ObsidianDeep.copy(alpha = 0.75f),
                                    border = BorderStroke(1.dp, GlassBorder.copy(alpha = 0.25f)),
                                ) {
                                    Column(
                                        modifier = Modifier.padding(10.dp),
                                        verticalArrangement = Arrangement.spacedBy(2.dp),
                                    ) {
                                        Text(
                                            text = msg.subject,
                                            style = MaterialTheme.typography.labelLarge,
                                            color = TextPrimary,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                        )
                                        Text(
                                            text = msg.fromAddress.ifBlank { msg.fromName },
                                            style = MaterialTheme.typography.labelSmall,
                                            color = NeonSkyBlue,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                        )
                                        if (msg.intro.isNotBlank()) {
                                            Text(
                                                text = msg.intro,
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = TextSecondary,
                                                maxLines = 2,
                                                overflow = TextOverflow.Ellipsis,
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Surface(
                shape = RoundedCornerShape(12.dp),
                color = WarmAmber.copy(alpha = 0.10f),
                border = BorderStroke(1.dp, WarmAmber.copy(alpha = 0.35f)),
            ) {
                Row(
                    modifier = Modifier.padding(10.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.Top,
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = WarmAmber,
                        modifier = Modifier.size(16.dp),
                    )
                    Text(
                        text = stringResource(R.string.tempmail_policy_note),
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary,
                    )
                }
            }
        }
    }

    val openMsg = selectedMessage
    if (openMsg != null) {
        AlertDialog(
            onDismissRequest = { selectedMessage = null },
            containerColor = ObsidianElevated,
            titleContentColor = TextPrimary,
            textContentColor = TextSecondary,
            title = {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(text = openMsg.subject, style = MaterialTheme.typography.titleMedium)
                    Text(
                        text = "From: ${openMsg.fromAddress}",
                        style = MaterialTheme.typography.labelSmall,
                        color = NeonSkyBlue,
                    )
                }
            },
            text = {
                if (isBodyLoading) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                        Text("Loading message...")
                    }
                } else {
                    Text(
                        text = selectedMessageBody ?: openMsg.intro,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val copyText = selectedMessageBody ?: openMsg.intro
                        clipboardManager.setText(AnnotatedString(copyText))
                        onShowNotice("Message text copied.")
                        selectedMessage = null
                    }
                ) {
                    Text("Copy Text", color = NeonSkyBlue)
                }
            },
            dismissButton = {
                TextButton(onClick = { selectedMessage = null }) {
                    Text("Close", color = TextSecondary)
                }
            },
        )
    }
}
