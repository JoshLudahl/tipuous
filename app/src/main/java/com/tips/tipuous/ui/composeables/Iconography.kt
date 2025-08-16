package com.tips.tipuous.ui.composeables

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag

@Composable
fun TopAppBarIcon(onClickIcon: () -> Unit) {
    FilledIconButton(
        onClick = onClickIcon,
        colors =
            IconButtonDefaults.filledIconButtonColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
            ),
    ) {
        Icon(
            Icons.Rounded.Share,
            contentDescription = "Back",
            modifier = Modifier.testTag("share"),
        )
    }
}
