package com.lssgoo.planner.features.onboarding.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.lssgoo.planner.data.remote.PlannerApiService
import com.lssgoo.planner.features.settings.models.UserProfile
import com.lssgoo.planner.ui.viewmodel.PlannerViewModel
import com.lssgoo.planner.util.DeviceUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

enum class OnboardingStep {
    WELCOME,
    EMAIL_INPUT,
    OTP_VERIFY,
    COMPLETE
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OnboardingScreen(
    viewModel: PlannerViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current
    val apiService = remember { PlannerApiService(context) }

    var currentStep by remember { mutableStateOf(OnboardingStep.WELCOME) }
    var email by remember { mutableStateOf("") }
    var otpCode by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var userName by remember { mutableStateOf("") }

    val systemBarsPadding = WindowInsets.systemBars.asPaddingValues()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
                        MaterialTheme.colorScheme.surface
                    )
                )
            )
    ) {
        Box(
            modifier = Modifier
                .size(350.dp)
                .offset(x = (-175).dp, y = (-75).dp)
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.04f), CircleShape)
        )
        Box(
            modifier = Modifier
                .size(250.dp)
                .align(Alignment.BottomEnd)
                .offset(x = 125.dp, y = 75.dp)
                .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.04f), CircleShape)
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(systemBarsPadding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            AnimatedContent(
                targetState = currentStep,
                transitionSpec = {
                    slideInHorizontally { width -> width } + fadeIn() togetherWith
                            slideOutHorizontally { width -> -width } + fadeOut()
                },
                label = "onboarding_step"
            ) { step ->
                when (step) {
                    OnboardingStep.WELCOME -> WelcomeStepContent(
                        onGetStarted = { currentStep = OnboardingStep.EMAIL_INPUT },
                        onSkip = {
                            isLoading = true
                            errorMessage = null
                            scope.launch(Dispatchers.IO) {
                                try {
                                    val deviceId = DeviceUtils.getDeviceId(context)
                                    val response = apiService.guestLogin(deviceId)
                                    if (response?.data != null) {
                                        val profile = UserProfile(
                                            firstName = "Guest",
                                            isOnboardingComplete = true
                                        )
                                        viewModel.saveUserProfile(profile)
                                        viewModel.setOnboardingComplete(true)
                                    } else {
                                        val profile = UserProfile(
                                            firstName = "Guest",
                                            isOnboardingComplete = true
                                        )
                                        viewModel.saveUserProfile(profile)
                                        viewModel.setOnboardingComplete(true)
                                    }
                                } catch (e: Exception) {
                                    val profile = UserProfile(
                                        firstName = "Guest",
                                        isOnboardingComplete = true
                                    )
                                    viewModel.saveUserProfile(profile)
                                    viewModel.setOnboardingComplete(true)
                                } finally {
                                    isLoading = false
                                }
                            }
                        },
                        isLoading = isLoading
                    )

                    OnboardingStep.EMAIL_INPUT -> EmailInputStep(
                        email = email,
                        onEmailChange = { email = it; errorMessage = null },
                        onSendOtp = {
                            if (email.isNotBlank() && email.contains("@")) {
                                isLoading = true
                                errorMessage = null
                                scope.launch(Dispatchers.IO) {
                                    try {
                                        val response = apiService.sendOtp(email.trim())
                                        if (response?.status == "OK") {
                                            currentStep = OnboardingStep.OTP_VERIFY
                                        } else {
                                            errorMessage = response?.message ?: "Failed to send OTP"
                                        }
                                    } catch (e: Exception) {
                                        errorMessage = "Network error. Please try again."
                                    } finally {
                                        isLoading = false
                                    }
                                }
                            } else {
                                errorMessage = "Please enter a valid email"
                            }
                        },
                        onBack = { currentStep = OnboardingStep.WELCOME },
                        isLoading = isLoading,
                        error = errorMessage
                    )

                    OnboardingStep.OTP_VERIFY -> OtpVerifyStep(
                        email = email,
                        otpCode = otpCode,
                        onOtpChange = { otpCode = it; errorMessage = null },
                        onVerify = {
                            if (otpCode.length == 6) {
                                isLoading = true
                                errorMessage = null
                                scope.launch(Dispatchers.IO) {
                                    try {
                                        val guestToken = apiService.getClient().getAccessToken()
                                        val response = apiService.verifyOtp(
                                            email.trim(),
                                            otpCode.trim(),
                                            guestToken
                                        )
                                        if (response?.data != null) {
                                            @Suppress("UNCHECKED_CAST")
                                            val user = response.data["user"] as? Map<String, Any>
                                            userName = (user?.get("firstName") as? String) ?: "User"
                                            currentStep = OnboardingStep.COMPLETE
                                        } else {
                                            errorMessage = response?.message ?: "Invalid OTP code"
                                        }
                                    } catch (e: Exception) {
                                        errorMessage = "Verification failed. Please try again."
                                    } finally {
                                        isLoading = false
                                    }
                                }
                            } else {
                                errorMessage = "Please enter the 6-digit code"
                            }
                        },
                        onResend = {
                            scope.launch(Dispatchers.IO) {
                                try {
                                    apiService.sendOtp(email.trim())
                                } catch (_: Exception) {}
                            }
                        },
                        onBack = { currentStep = OnboardingStep.EMAIL_INPUT; otpCode = "" },
                        isLoading = isLoading,
                        error = errorMessage
                    )

                    OnboardingStep.COMPLETE -> CompleteStepContent(
                        userName = userName.ifBlank { "User" },
                        onLaunch = {
                            val profile = UserProfile(
                                firstName = userName.ifBlank { "User" },
                                email = email,
                                isOnboardingComplete = true
                            )
                            viewModel.saveUserProfile(profile)
                            viewModel.setOnboardingComplete(true)
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun WelcomeStepContent(
    onGetStarted: () -> Unit,
    onSkip: () -> Unit,
    isLoading: Boolean
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxWidth()
    ) {
        Icon(
            imageVector = Icons.Default.RocketLaunch,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Welcome to Planner",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Organize your goals, tasks, habits, and more — all in one place.",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(48.dp))

        Button(
            onClick = onGetStarted,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
        ) {
            Text("Get Started", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.width(8.dp))
            Icon(Icons.Default.ArrowForward, contentDescription = null)
        }

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedButton(
            onClick = onSkip,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(16.dp),
            enabled = !isLoading
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    strokeWidth = 2.dp
                )
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text("Skip & Continue as Guest", fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun EmailInputStep(
    email: String,
    onEmailChange: (String) -> Unit,
    onSendOtp: () -> Unit,
    onBack: () -> Unit,
    isLoading: Boolean,
    error: String?
) {
    val focusManager = LocalFocusManager.current

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxWidth()
    ) {
        Icon(
            imageVector = Icons.Default.Email,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Enter your email",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "We'll send you a verification code to sign in",
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(32.dp))

        OutlinedTextField(
            value = email,
            onValueChange = onEmailChange,
            label = { Text("Email address") },
            leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Email,
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(
                onDone = { focusManager.clearFocus(); onSendOtp() }
            ),
            singleLine = true,
            isError = error != null,
            shape = RoundedCornerShape(12.dp)
        )

        if (error != null) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = error,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = { focusManager.clearFocus(); onSendOtp() },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(16.dp),
            enabled = email.isNotBlank() && !isLoading
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    strokeWidth = 2.dp,
                    color = MaterialTheme.colorScheme.onPrimary
                )
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text("Send Code", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
        }

        Spacer(modifier = Modifier.height(12.dp))

        TextButton(onClick = onBack) {
            Icon(Icons.Default.ArrowBack, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text("Back", fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun OtpVerifyStep(
    email: String,
    otpCode: String,
    onOtpChange: (String) -> Unit,
    onVerify: () -> Unit,
    onResend: () -> Unit,
    onBack: () -> Unit,
    isLoading: Boolean,
    error: String?
) {
    val focusManager = LocalFocusManager.current

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxWidth()
    ) {
        Icon(
            imageVector = Icons.Default.Lock,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Verify your email",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Enter the 6-digit code sent to\n$email",
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(32.dp))

        OutlinedTextField(
            value = otpCode,
            onValueChange = { if (it.length <= 6) onOtpChange(it) },
            label = { Text("Verification code") },
            leadingIcon = { Icon(Icons.Default.Pin, contentDescription = null) },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number,
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(
                onDone = { focusManager.clearFocus(); onVerify() }
            ),
            singleLine = true,
            isError = error != null,
            shape = RoundedCornerShape(12.dp)
        )

        if (error != null) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = error,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = { focusManager.clearFocus(); onVerify() },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(16.dp),
            enabled = otpCode.length == 6 && !isLoading
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    strokeWidth = 2.dp,
                    color = MaterialTheme.colorScheme.onPrimary
                )
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text("Verify & Sign In", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Didn't get the code?", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            TextButton(onClick = onResend) {
                Text("Resend", fontWeight = FontWeight.Bold)
            }
        }

        TextButton(onClick = onBack) {
            Icon(Icons.Default.ArrowBack, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text("Change email", fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun CompleteStepContent(
    userName: String,
    onLaunch: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxWidth()
    ) {
        Icon(
            imageVector = Icons.Default.CheckCircle,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "You're all set!",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Welcome, $userName! Your account is ready.",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(48.dp))

        Button(
            onClick = onLaunch,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
        ) {
            Text("Launch Planner", fontWeight = FontWeight.ExtraBold, style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.width(8.dp))
            Icon(Icons.Default.RocketLaunch, contentDescription = null)
        }
    }
}
