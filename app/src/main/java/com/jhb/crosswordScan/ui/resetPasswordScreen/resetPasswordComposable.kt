package com.jhb.crosswordScan.ui.resetPasswordScreen

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
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
fun resetPasswordScreen(
    navigateOnSuccess: ()->Unit
){
    val resetPasswordViewModel: ResetPasswordViewModel = viewModel(factory =
    ResetPasswordViewModelFactory(navigateOnSuccess)
    )

    val uiState by resetPasswordViewModel.uiState.collectAsState()
    //val resetState by resetPasswordViewModel.resetState.collectAsState()

//    if(resetState){
//
//        navigateOnSuccess()
//    }

    val email = uiState.email
    val newpassword = uiState.newpassword
    val resetCode = uiState.resetCode
    val isLoading = uiState.isLoading
    val serverMessage = uiState.serverMessage



    resetPasswordComposable(
        email = email,
        newpassword = newpassword,
        resetCode = resetCode,
        isLoading = isLoading,
        serverMessage = serverMessage,
        setEmailCallback = {resetPasswordViewModel.setEmail(it)},
        setNewPasswordCallback = {resetPasswordViewModel.setNewPassword(it)},
        setResetCodeCallback = {resetPasswordViewModel.setResetCode(it)},
        requestReset = {resetPasswordViewModel.requestPasswordReset()},
        resetCallback = {resetPasswordViewModel.setNewPassword()},
    )

}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun resetPasswordComposable(
    email : String,
    newpassword : String,
    resetCode : String,
    isLoading : Boolean,
    serverMessage: String,
    setEmailCallback: (String) -> Unit,
    setNewPasswordCallback: (String) -> Unit,
    setResetCodeCallback: (String) -> Unit,
    requestReset: () -> Unit,
    resetCallback: () -> Unit,
){

    Column(
        modifier = Modifier
            //.fillMaxHeight(1f)
            //.width(350.dp),
            .fillMaxSize(1f),

        //.padding(50.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        val focusManager = LocalFocusManager.current
        var showPassword by remember { mutableStateOf(false) }
        val keyboardController = LocalSoftwareKeyboardController.current

        val textBoxWidth = 300.dp

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
            modifier = Modifier
                .padding(10.dp)
                .width(textBoxWidth),
            onValueChange = { setEmailCallback(it) },
            label = { Text(text = stringResource(id = R.string.label_email)) },
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
            singleLine = true

            )
        FilledTonalButton(
            onClick = { requestReset() },
            modifier = Modifier
                .width(250.dp)
                .padding(10.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            )
        ) {
            Text(text = stringResource(R.string.action_requestPasswordReset))
        }

        OutlinedTextField(
            value = resetCode,
            onValueChange = { setResetCodeCallback(it) },
            label = { Text(stringResource(R.string.label_passwordResetCode)) },
            modifier = Modifier
                .padding(10.dp)
                .width(textBoxWidth),
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
            keyboardActions = KeyboardActions(
                onNext = {
                    focusManager.moveFocus(FocusDirection.Down)
                },
            ),

        )

        OutlinedTextField(
            value = newpassword,
            onValueChange = { setNewPasswordCallback(it) },
            label = { Text(stringResource(R.string.label_newPassword)) },
            leadingIcon = {
                Icon(
                    painterResource(id = R.drawable.ic_baseline_password_24),
                    contentDescription = stringResource(R.string.contentDesc_newPasswordIcon)
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
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done, keyboardType = KeyboardType.Password),
            keyboardActions = KeyboardActions(
                onNext = {
                    keyboardController?.hide()
                    focusManager.moveFocus(FocusDirection.Down)
                },
            ),
            singleLine = true,
            modifier = Modifier
                .padding(10.dp)
                .width(textBoxWidth)

        )

        FilledTonalButton(
            onClick = { resetCallback()},
            modifier = Modifier
                .width(250.dp)
                .padding(10.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            )
        ) {
            Text(text = stringResource(R.string.action_resetPassword))
        }
        Text(text = serverMessage,color = MaterialTheme.colorScheme.error)

        if(isLoading){
            Spinner()
        }
    }
}
