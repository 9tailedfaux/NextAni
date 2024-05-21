package com.refractional.nextani

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog

@Composable
fun PopupLoadingSpinner(
    visible: MutableState<Boolean>
) {
    if (visible.value) {
        Dialog(
            onDismissRequest = {  },
        ) {
            Card(
                modifier = Modifier
                    .wrapContentSize()
            ) {
                CircularProgressIndicator(
                    modifier = Modifier
                        .padding(25.dp)
                )
            }
        }
    }
}