package com.tips.tipuous.ui.composeables

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReusableTopAppBar(
    modifier: Modifier = Modifier,
    onNavigateBack: () -> Unit,
    title: @Composable () -> Unit,
    actions: @Composable () -> Unit,
    includeBackArrow: Boolean = true,
    isEnabled: Boolean = true,
) {
    CenterAlignedTopAppBar(
        title = title,
        navigationIcon = {
            if (includeBackArrow) {
                IconButton(onClick = onNavigateBack, enabled = isEnabled) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        modifier = Modifier,
                    )
                }
            }
        },
        actions = { actions() },
        modifier = modifier,
    )
}
