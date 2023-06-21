package com.jhb.crosswordScan.ui.authScreen

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.material3.ButtonDefaults.buttonColors
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.jhb.crosswordScan.R
import com.jhb.crosswordScan.ui.common.Spinner


@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun LoginComposable(
    uiState: AuthUiState,
    userNameFieldCallback: (String) -> Unit,
    passwordFieldCallback: (String) -> Unit,
    loginCallback: (String, String) -> Unit,
    registerCallback: () -> Unit,
    forgotPasswordCallback: () -> Unit,
) {

    Column(
        modifier = Modifier
            .fillMaxSize(1f),
        //.padding(50.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        val focusManager = LocalFocusManager.current

        var showPassword by remember { mutableStateOf(false) }

        val keyboardController = LocalSoftwareKeyboardController.current


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

        OutlinedTextField(
            value = uiState.userName ?: "",
            modifier = Modifier.padding(10.dp),
            onValueChange = { userNameFieldCallback(it) },
            label = { Text("Username") },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next, autoCorrect = false),
            keyboardActions = KeyboardActions(
                onNext = {
                    focusManager.moveFocus(FocusDirection.Down)
                },

                ),
            leadingIcon = {
                Icon(
                    painterResource(id = R.drawable.ic_baseline_person_24),
                    contentDescription = "username icon"
                )
            },

            )

        OutlinedTextField(
            value = uiState.userPassword ?: "",
            onValueChange = { passwordFieldCallback(it) },
            label = { Text("Password") },
            modifier = Modifier.padding(10.dp),
            leadingIcon = {
                Icon(
                    painterResource(id = R.drawable.ic_baseline_password_24),
                    contentDescription = "password icon"
                )
            },
            trailingIcon = {
                IconButton(onClick = { showPassword = !showPassword }) {
                    Icon(
                        painterResource(
                            id = if (showPassword) {
                                R.drawable.ic_baseline_visibility_24
                            } else R.drawable.ic_baseline_visibility_off_24
                        ),
                        contentDescription = "password visibility"
                    )
                }
            },
            visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(
                imeAction = ImeAction.Done,
                keyboardType = KeyboardType.Password
            ),
            keyboardActions = KeyboardActions(
                onDone = {
                    keyboardController?.hide()
                },
            ),
        )

        Row(horizontalArrangement = Arrangement.SpaceEvenly) {


            FilledTonalButton(
                onClick = {
                    //composableScope.launch {
                        if (uiState.userName != null && uiState.userPassword != null) {
                            loginCallback(uiState.userName, uiState.userPassword)

                        }
                    //}
                },
                modifier = Modifier
                    .width(250.dp)
                    .padding(10.dp),
                colors = buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )

            ) {
                Text(text = stringResource(R.string.login))
            }

        }
        OutlinedButton(
            onClick = { registerCallback() },
            modifier = Modifier
                .width(250.dp)
                .padding(10.dp),
        ) {
            Text(text = stringResource(R.string.register))
        }

        OutlinedButton(
            onClick = { forgotPasswordCallback() },
            modifier = Modifier
                .width(250.dp)
                .padding(10.dp),
        ) {
            Text(text = stringResource(R.string.forgotPasswordButton))
        }

        uiState.serverErrorText?.let {
            Text(
                text = it,
                color = MaterialTheme.colorScheme.error
            )
        }

        if(uiState.isLoading) {
            Spinner()
        }

    }


}


//        LazyColumn(contentPadding = PaddingValues(10.dp),
//            modifier = Modifier
//                .padding(0.dp)
//                .background(MaterialTheme.colorScheme.background)
//                .height(300.dp)
//        ){
//            if (uiState.value.users != null){
//                items(uiState.value.users!!) { user ->
//                    Text(user.userName)
//                }
//            }
//        }
//    }
