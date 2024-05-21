package com.refractional.nextani

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.refractional.nextani.ui.theme.NextAniTheme
import com.refractional.nextani.utils.ApiManager

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val apiManager = ApiManager(this)
        enableEdgeToEdge()
        setContent {
            NextAniTheme {
                Scaffold(
                    modifier = Modifier
                        .fillMaxSize(),
                    //topBar = { TopAppBar(title = { Text(text = "Login") })}
                ) { innerPadding ->
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .fillMaxSize()
                            .safeContentPadding()
                    ) {
                        val isLoading = remember { mutableStateOf(false) }

                        Login(
                            modifier = Modifier
                                .padding(innerPadding),
                            apiManager,
                            isLoading
                        )
                        /*MainView(
                            modifier = Modifier
                                .padding(innerPadding)
                        )*/
                        PopupLoadingSpinner(visible = isLoading)
                    }
                }
            }
        }
    }
}

@Composable
@Preview(showBackground = true)
fun MainView(modifier: Modifier = Modifier) {

}

