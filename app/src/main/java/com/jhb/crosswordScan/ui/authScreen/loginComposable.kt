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
import androidx.compose.ui.draw.alpha
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
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
                .height(150.dp)
                .width(150.dp)
                //.background(color = Color.Red)
                .padding(10.dp)
                .clip(shape = RoundedCornerShape(25))
            //)
        ) {
            Image(
                painter = painterResource(id = R.drawable.logobackground),
                contentDescription = stringResource(id = R.string.logo),
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.FillBounds
            )
            Image(
                painter = painterResource(id = R.drawable.logo),
                contentDescription = stringResource(id = R.string.logo),
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.FillBounds,
            )
        }
        if (uiState.wip) {
            Text(text = "work in progress", color = MaterialTheme.colorScheme.error)
        }
        OutlinedTextField(
            enabled = !uiState.wip,
            value = uiState.userName ?: "",
            modifier = Modifier.padding(5.dp),
            onValueChange = { userNameFieldCallback(it) },
            label = { Text(text = stringResource(id = R.string.label_userName)) },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next, autoCorrect = false),
            keyboardActions = KeyboardActions(
                onNext = {
                    focusManager.moveFocus(FocusDirection.Down)
                },

                ),
            leadingIcon = {
                Icon(
                    painterResource(id = R.drawable.ic_baseline_person_24),
                    contentDescription = stringResource(id = R.string.ContentDesc_userNameIcon),
                )
            },
            isError = !uiState.validLogin
            )

        OutlinedTextField(
            enabled = !uiState.wip,
            value = uiState.userPassword ?: "",
            onValueChange = { passwordFieldCallback(it) },
            label = { Text(text = stringResource(id = R.string.label_password)) },
            modifier = Modifier.padding(5.dp),
            leadingIcon = {
                Icon(
                    painterResource(id = R.drawable.ic_baseline_password_24),
                    contentDescription = stringResource(id = R.string.contentDesc_passwordIcon),
                )
            },
            trailingIcon = {
                IconButton(
                    onClick = { showPassword = !showPassword },
                ) {
                    Icon(
                        painterResource(
                            id = if (showPassword) {
                                R.drawable.ic_baseline_visibility_24
                            } else R.drawable.ic_baseline_visibility_off_24
                        ),
                        contentDescription = stringResource(id = R.string.contentDesc_passwordIconVisibleToggle),
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
                    if (uiState.userName != null && uiState.userPassword != null) {
                        loginCallback(uiState.userName, uiState.userPassword)
                    }
                    focusManager.clearFocus()

                },
            ),
            isError = !uiState.validLogin
        )
        Row(horizontalArrangement = Arrangement.Center,modifier = Modifier.defaultMinSize(minHeight = 30.dp)) {
            uiState.serverErrorText?.let {
                Text(
                    text = it,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.fillMaxWidth(0.8f),
                    textAlign = TextAlign.Center
                )
            }
        }

        Row(horizontalArrangement = Arrangement.SpaceEvenly) {


            FilledTonalButton(
                enabled = !uiState.wip,
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
                Text(text = stringResource(R.string.action_login))
            }

        }
        OutlinedButton(
            enabled = !uiState.wip,
            onClick = { registerCallback() },
            modifier = Modifier
                .width(250.dp)
                .padding(10.dp),
        ) {
            Text(text = stringResource(R.string.action_register))
        }
    }

    if(uiState.isLoading) {
        Box(
            contentAlignment = Alignment.Center, // you apply alignment to all children
            modifier = Modifier.fillMaxSize()
        ) {
            Surface(
                color = MaterialTheme.colorScheme.scrim,
                modifier = Modifier
                    .fillMaxSize()
                    .alpha(0.3f)
            ) {}
            Surface(
                shape = MaterialTheme.shapes.large,
                modifier = Modifier
                    .width(150.dp)
                    .height(150.dp)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                )
                {
                    Spinner()
                    Text(text = stringResource(R.string.logging_in))
                }
            }
        }
    }

}

@Preview
@Composable
fun Preview() {
    val fakeUi : AuthUiState = AuthUiState(
        serverErrorText = "There was an issue.",
        validLogin=true,
        isLoading = true

    )
    LoginComposable(fakeUi,{},{},{_,_->Unit},{})
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

