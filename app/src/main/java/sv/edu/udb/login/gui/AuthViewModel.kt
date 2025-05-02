package sv.edu.udb.login.gui

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
//noinspection UsingMaterialAndMaterial3Libraries
import androidx.compose.material.Button // Nota: Usaste Material (M2) aquí, pero LoginScreen usa Material3 (M3). Considera unificar.
//noinspection UsingMaterialAndMaterial3Libraries
import androidx.compose.material.Text // Nota: Usaste Material (M2) aquí.
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow // Importa asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await // Para usar await con Tasks de Firebase (opcional, pero más "Kotlin idiomatic")

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.Modifier
import sv.edu.udb.login.R

class AuthViewModel : ViewModel() {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private lateinit var googleSignInClient: GoogleSignInClient

    // Usamos Unauthenticated como estado inicial en lugar de Idle para simplificar
    private val _authState = MutableStateFlow<AuthState>(AuthState.Unauthenticated)
    val authState: StateFlow<AuthState> = _authState.asStateFlow() // Usa asStateFlow para exponerlo

    // --- Inicialización de Google Sign-In ---
    fun initializeGoogleSignInClient(clientId: String, context: Context) {
        // Asegúrate de que solo se inicialice una vez si es necesario,
        // aunque llamarlo múltiples veces con el mismo context y gso es seguro.
        if (!::googleSignInClient.isInitialized) {
            val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(clientId)
                .requestEmail()
                .build()
            googleSignInClient = GoogleSignIn.getClient(context, gso)
            println("GoogleSignInClient inicializado.")
        }
    }

    // --- Lógica de Google Sign-In  ---
    fun signInWithGoogle(launcher: androidx.activity.result.ActivityResultLauncher<Intent>) {
        // Asegúrate de que el cliente esté inicializado antes de intentar usarlo
        if (!::googleSignInClient.isInitialized) {
            _authState.value = AuthState.Error("Google Sign-In no inicializado. Asegúrate de llamar a initializeGoogleSignInClient.")
            return
        }
        val signInIntent = googleSignInClient.signInIntent
        launcher.launch(signInIntent)
        _authState.value = AuthState.Loading("Iniciando sesión con Google...") 
    }

    fun handleSignInResult(data: Intent?) {
        val task = GoogleSignIn.getSignedInAccountFromIntent(data)
        try {
            val account = task.getResult(ApiException::class.java)!! // Non-null assertion si estás seguro o maneja null
            println("Google Sign-In exitoso, obteniendo token: ${account.idToken?.take(10)}...") // No loguear token completo
            firebaseAuthWithGoogle(account.idToken)
        } catch (e: ApiException) {
            println("Google Sign-In falló: ${e.statusCode} - ${e.message}")
            _authState.value = AuthState.Error("Inicio de sesión con Google falló (Código: ${e.statusCode})")
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String?) {
        if (idToken == null) {
            _authState.value = AuthState.Error("Token de Google ID es nulo.")
            return
        }

        val credential = GoogleAuthProvider.getCredential(idToken, null)
        _authState.value = AuthState.Loading("Autenticando con Firebase...") // Actualiza estado
        viewModelScope.launch {
            try {
                println("Intentando autenticar con Firebase usando credencial de Google...")
                val authResult = auth.signInWithCredential(credential).await() // Usando await
                val firebaseUser = authResult.user
                println("Autenticación con Firebase exitosa. Usuario: ${firebaseUser?.email}")
                _authState.value = AuthState.Authenticated(firebaseUser)
            } catch (e: Exception) {
                // Captura excepciones de la corrutina/await
                println("Error durante la autenticación de Firebase con Google: ${e.message}")
                _authState.value = AuthState.Error("Error de autenticación con Firebase: ${e.localizedMessage}")
            }
        }
        /* // Alternativa con addOnCompleteListener:
        auth.signInWithCredential(credential)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    println("Autenticación con Firebase exitosa. Usuario: ${auth.currentUser?.email}")
                    _authState.value = AuthState.Authenticated(auth.currentUser)
                } else {
                    println("Error durante la autenticación de Firebase con Google: ${task.exception?.message}")
                    _authState.value = AuthState.Error("Error de autenticación con Firebase: ${task.exception?.localizedMessage}")
                }
            }
        */
    }

    // --- !!! FUNCIÓN AÑADIDA PARA EMAIL/PASSWORD !!! ---
    fun signInWithEmailPassword(email: String, password: String) {
        // Validaciones básicas
        if (email.isBlank() || password.isBlank()) {
            _authState.value = AuthState.Error("Correo electrónico y contraseña no pueden estar vacíos.")
            return
        }

        _authState.value = AuthState.Loading("Iniciando sesión...") // Estado de carga
        viewModelScope.launch {
            try {
                println("Intentando iniciar sesión con Email/Password: $email")
                val authResult = auth.signInWithEmailAndPassword(email, password).await() // Usando await
                val firebaseUser = authResult.user
                println("Inicio de sesión con Email/Password exitoso. Usuario: ${firebaseUser?.email}")
                _authState.value = AuthState.Authenticated(firebaseUser)
            } catch (e: Exception) {
                println("Error durante inicio de sesión con Email/Password: ${e.message}")
                val mensaje = when {
                    e.localizedMessage?.contains("password is invalid", ignoreCase = true) == true ||
                            e.localizedMessage?.contains("The password is invalid", ignoreCase = true) == true -> "La contraseña es incorrecta."
                    e.localizedMessage?.contains("no user record", ignoreCase = true) == true ||
                            e.localizedMessage?.contains("There is no user record", ignoreCase = true) == true -> "No existe una cuenta con ese correo electrónico."
                    else -> "Error desconocido al iniciar sesión."
                }
                _authState.value = AuthState.Error(mensaje)
            }
        }
    }

    // --- Función para cerrar sesión (útil tenerla) ---
    fun signOut() {
        viewModelScope.launch {
            try {
                auth.signOut() // Cierra sesión en Firebase
                // También cierra sesión en Google si estaba logueado con Google
                if (::googleSignInClient.isInitialized) {
                    googleSignInClient.signOut().await()
                    println("Sesión de Google cerrada.")
                }
                _authState.value = AuthState.Unauthenticated // Actualiza estado
                println("Sesión cerrada.")
            } catch (e: Exception) {
                println("Error al cerrar sesión: ${e.message}")
                // Podrías querer poner un estado de error aquí también, o simplemente mantener el estado anterior.
                // Por simplicidad, lo dejamos en Unauthenticated o mantenemos el estado actual si falla.
                _authState.value = AuthState.Error("Error al cerrar sesión: ${e.localizedMessage}")
            }
        }
    }


    // --- Función auxiliar para establecer errores  ---
    fun setError(message: String) {
        _authState.value = AuthState.Error(message)
    }

    // --- Definición de Estados (simplificado) ---
    sealed class AuthState {
        // object Idle : AuthState() // Eliminado para simplificar, usamos Unauthenticated
        data class Loading(val message: String? = null) : AuthState() // Mensaje opcional para indicar qué carga
        data class Authenticated(val user: FirebaseUser?) : AuthState() // Usuario autenticado
        object Unauthenticated : AuthState() // No autenticado / Sesión cerrada
        data class Error(val message: String) : AuthState() // Estado de error
    }
}

// --- GoogleSignInButton Composable (ya existente, con notas) ---
@Composable
fun GoogleSignInButton(authViewModel: AuthViewModel, userViewModel: UserViewModel) {
    val context = LocalContext.current
    val clientId = "71776431591-1tje99vrv7fl86atdcp45vhqb8nlllmk.apps.googleusercontent.com"

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            println("ActivityResult: RESULT_OK, procesando...")
            authViewModel.handleSignInResult(result.data)
        } else {
            println("ActivityResult: ${result.resultCode}, inicio de sesión cancelado o fallido.")
            val currentState = authViewModel.authState.value
            if (currentState !is AuthViewModel.AuthState.Authenticated && currentState !is AuthViewModel.AuthState.Loading) {
                authViewModel.setError("Inicio de sesión con Google cancelado.")
            }
        }
    }

    LaunchedEffect(key1 = clientId, key2 = context) {
        if (clientId.isNotBlank()) {
            println("Llamando a initializeGoogleSignInClient desde GoogleSignInButton...")
            authViewModel.initializeGoogleSignInClient(clientId, context)
        } else {
            println("Error: Firebase Web Client ID no configurado.")
            authViewModel.setError("Firebase Web Client ID no configurado.")
        }
    }

    val authState by authViewModel.authState.collectAsState()

    LaunchedEffect(key1 = authState) {
        when (val state = authState) {
            is AuthViewModel.AuthState.Authenticated -> {
                val user = state.user
                val username = user?.displayName ?: user?.email ?: "Usuario Google"
                println("Usuario autenticado (Google Button Observer): $username")
                userViewModel.setUsername(username)
            }
            is AuthViewModel.AuthState.Error -> {
                println("Error detectado en GoogleSignInButton LaunchedEffect: ${state.message}")
            }
            is AuthViewModel.AuthState.Loading -> {
                println("Estado de carga detectado en GoogleSignInButton LaunchedEffect: ${state.message ?: ""}")
            }
            AuthViewModel.AuthState.Unauthenticated -> {
                println("Estado no autenticado detectado en GoogleSignInButton LaunchedEffect.")
            }
        }
    }

    androidx.compose.material3.Button(
        onClick = {
            if (clientId.isNotBlank()) {
                println("Botón Google Sign-In presionado.")
                authViewModel.signInWithGoogle(launcher)
            } else {
                authViewModel.setError("Firebase Web Client ID no configurado.")
            }
        },
        enabled = authState !is AuthViewModel.AuthState.Loading,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 60.dp)
            .height(45.dp),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(28.dp),
        colors = ButtonDefaults.buttonColors(containerColor = Color.White)
    ) {
        if(authState is AuthViewModel.AuthState.Loading) {
            androidx.compose.material3.Text("Iniciando con Google...")
        } else {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = androidx.compose.foundation.layout.Arrangement.Center
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.google),
                    contentDescription = "Logo de Google",
                    tint = Color.Unspecified,
                    modifier = Modifier.size(40.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                androidx.compose.material3.Text(
                    text = "Iniciar sesión con Google",
                    color = Color.Black
                )
            }
        }
    }
}
