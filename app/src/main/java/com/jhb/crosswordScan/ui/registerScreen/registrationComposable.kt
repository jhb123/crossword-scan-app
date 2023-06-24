package com.jhb.crosswordScan.ui.registerScreen

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
import androidx.lifecycle.viewmodel.compose.viewModel
import com.jhb.crosswordScan.R
import com.jhb.crosswordScan.ui.common.Spinner

@Composable
fun RegistrationScreen(
    navigateOnSuccess : () -> Unit
){

    val registrationViewModel: RegistrationViewModel = viewModel(
    factory = RegistrationViewModelFactory(navigateOnSuccess))

    val uiState by registrationViewModel.uiState.collectAsState()

    RegistrationComposeable(
        uiState = uiState,
        registerCallback = {registrationViewModel.submit()},
        userNameFieldCallback = {registrationViewModel.setUserName(it)},
        emailFieldCallback = {registrationViewModel.setEmail(it)},
        passwordFieldCallback = {registrationViewModel.setPassword(it)},
        passwordConfirmFieldCallback = {registrationViewModel.setConfirmPassword(it)},
    )

}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun RegistrationComposeable(
    uiState: RegistrationUiState,
    registerCallback: () -> Unit,
    emailFieldCallback: (String) -> Unit,
    userNameFieldCallback: (String) -> Unit,
    passwordFieldCallback: (String) -> Unit,
    passwordConfirmFieldCallback: (String) -> Unit,
) {

    val userName = uiState.username
    val password = uiState.password
    val passwordConfirm = uiState.passwordConfirm
    val email = uiState.email

    Column(
        modifier = Modifier
            .fillMaxSize(1f),
        //.padding(50.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        val focusManager = LocalFocusManager.current
        var showPassword by remember { mutableStateOf(false) }
        val keyboardController = LocalSoftwareKeyboardController.current

        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .height(100.dp)
                .width(100.dp)
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

        OutlinedTextField(
            value = email,
            modifier = Modifier.padding(10.dp),
            onValueChange = { emailFieldCallback(it) },
            label = { Text(stringResource(R.string.label_email)) },
            keyboardOptions = KeyboardOptions(
                imeAction = ImeAction.Next,
                keyboardType = KeyboardType.Email
            ),
            keyboardActions = KeyboardActions(
                onNext = {
                    focusManager.moveFocus(FocusDirection.Down)
                },
            ),
            leadingIcon = {
                Icon(
                    painterResource(id = R.drawable.ic_baseline_email_24),
                    contentDescription = stringResource(id = R.string.contentDesc_emailIcon)
                )
            },

            )


        OutlinedTextField(
            value = userName,
            modifier = Modifier.padding(10.dp),
            onValueChange = { userNameFieldCallback(it) },
            label = { Text(text = stringResource(id = R.string.label_userName)) },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
            keyboardActions = KeyboardActions(
                onNext = {
                    focusManager.moveFocus(FocusDirection.Down)
                },
            ),
            leadingIcon = {
                Icon(
                    painterResource(id = R.drawable.ic_baseline_person_24),
                    contentDescription = stringResource(id = R.string.ContentDesc_userNameIcon)
                )
            },
            )

        OutlinedTextField(
            isError = !uiState.passwordsMatch,
            value = password,
            onValueChange = { passwordFieldCallback(it) },
            label = { Text(text = stringResource(id = R.string.label_password)) },
            modifier = Modifier.padding(10.dp),
            leadingIcon = {
                Icon(
                    painterResource(id = R.drawable.ic_baseline_password_24),
                    contentDescription = stringResource(id = R.string.contentDesc_passwordIcon)
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
                        contentDescription = stringResource(id = R.string.contentDesc_passwordIconVisibleToggle)
                    )
                }
            },
            visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
            keyboardActions = KeyboardActions(
                onNext = {
                    focusManager.moveFocus(FocusDirection.Down)
                },
            ),
        )

        OutlinedTextField(
            isError = !uiState.passwordsMatch,
            value = passwordConfirm,
            onValueChange = { passwordConfirmFieldCallback(it) },
            label = { Text(text = stringResource(id = R.string.label_confirmPassword)) },
            modifier = Modifier.padding(10.dp),
            leadingIcon = {
                Icon(
                    painterResource(id = R.drawable.ic_baseline_password_24),
                    contentDescription = stringResource(id = R.string.contentDesc_passwordConfirmIcon)
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
                        contentDescription = stringResource(id = R.string.contentDesc_passwordIconVisibleToggle)
                    )
                }
            },
            visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(
                onDone = {
                    keyboardController?.hide()
                    focusManager.moveFocus(FocusDirection.Down)
                },
            ),
        )

        FilledTonalButton(
            enabled = (!uiState.errorState && uiState.passwordsMatch),
            onClick = { registerCallback()},
            modifier = Modifier
                .width(150.dp)
                .padding(10.dp),
            colors = buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            )
        ) {
            Text(text = stringResource(R.string.action_register))
        }
        Text(
            text = uiState.message,
            color = MaterialTheme.colorScheme.error
        )
        if(uiState.isLoading) {
            Spinner()
        }
    }
}
