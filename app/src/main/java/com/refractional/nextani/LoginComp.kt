package com.refractional.nextani

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import com.refractional.nextani.utils.ApiManager

@Composable
//@Preview(showBackground = true)
fun Login(modifier: Modifier = Modifier, apiManager: ApiManager, isLoading: MutableState<Boolean>) {
    ConstraintLayout(
        modifier = Modifier.fillMaxSize()
    ) {
        val (message, loginCol) = createRefs()

        Card(
            modifier = Modifier.constrainAs(message) {
                top.linkTo(parent.top)
            }
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ){
                Icon(
                    painter = painterResource(id = R.drawable.outline_account_circle_24), contentDescription = "User icon",
                    modifier = Modifier.padding(horizontal = 20.dp)
                )
                Text(
                    text = "Please log in to your AniList account\nWe will not ask for your password",
                    modifier = Modifier.padding(end = 20.dp, top = 10.dp, bottom = 10.dp)
                )
            }
        }

        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.constrainAs(loginCol) {
                top.linkTo(message.bottom)
                bottom.linkTo(parent.bottom)
                start.linkTo(parent.start)
                end.linkTo(parent.end)
            }
        ) {
            var username by remember { mutableStateOf("") }
            OutlinedTextField(
                value = username,
                onValueChange = { username = it },
                label = { Text(text = "Username") },
                keyboardOptions = KeyboardOptions(
                    autoCorrect = false,
                    capitalization = KeyboardCapitalization.None
                ),

                modifier = Modifier
                    .padding(5.dp)
            )
            Button(
                onClick = {
                    apiManager.refreshUserData(onComplete = {isLoading.value = false})
                    isLoading.value = true
                },
                modifier = Modifier
                    .padding(5.dp)
                    .wrapContentWidth()
            ) {
                Text(text = "Login")
            }
        }
    }
}