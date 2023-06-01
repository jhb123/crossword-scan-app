package com.jhb.crosswordScan.ui.authScreen

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults.buttonColors
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.jhb.crosswordScan.R
import com.jhb.crosswordScan.data.SessionData.tokenState
import com.jhb.crosswordScan.data.SessionData.userDataState

@Composable
fun logoutComposeable(
    uiState: State<AuthUiState>,
    logoutCallback: () -> Unit,
) {


    //SessionData.readUser()
    val userFromFile = userDataState.collectAsState()
    val tokenFromFile = tokenState.collectAsState()

    val userName = uiState.value.userName
    val password = uiState.value.userPassword

    Column(
        modifier = Modifier
            .fillMaxSize(1f),
        //.padding(50.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .height(200.dp)
                .width(200.dp)
                //.background(color = Color.Red)
                .padding(10.dp)
                .clip(shape = RoundedCornerShape(25))
            //)
        ) {
            Image(
                painter = painterResource(id = R.drawable.logobackground),
                contentDescription = "logo",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.FillBounds
            )
            Image(
                painter = painterResource(id = R.drawable.logo),
                contentDescription = "logo foreground",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.FillBounds,
            )
        }

        Row(horizontalArrangement = Arrangement.SpaceEvenly) {


            FilledTonalButton(
                onClick = { logoutCallback() },
                modifier = Modifier
                    .width(150.dp)
                    .padding(10.dp),
                colors = buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )

            ) {
                Text(text = stringResource(R.string.logout))
            }
        }

        userFromFile.value?.let { Text(text = it.userName) }
        userFromFile.value?.let { Text(text = it.password) }
        userFromFile.value?.let { Text(text = it.email) }
        tokenFromFile.value?.let { Text(text = it) }
    }

}


