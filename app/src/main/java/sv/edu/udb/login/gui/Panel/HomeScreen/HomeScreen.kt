package sv.edu.udb.login.gui.Panel.HomeScreen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import sv.edu.udb.login.ui.theme.LoginTheme

// data class para representar cada función del panel
data class AdminFunction(val title: String, val icon: ImageVector, val onClick: () -> Unit)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(paddingValues: PaddingValues) {
    val scrollState = rememberScrollState()

    // funciones del panel de administración
    val adminFunctions = listOf(
        AdminFunction("Crear Evento", Icons.Filled.AddCircle, { /* TODO: Implementar Crear Evento */ }),
        AdminFunction("Ver Eventos", Icons.Filled.DateRange, { /* TODO: Implementar Ver Eventos */ }),
        AdminFunction("RSVP Evento", Icons.Filled.CheckCircle, { /* TODO: Implementar RSVP Evento */ }),
        AdminFunction("Historial Eventos", Icons.Filled.Face, { /* TODO: Implementar Historial Eventos */ }),
        AdminFunction("Comentarios", Icons.Filled.MailOutline, { /* TODO: Implementar Comentarios */ }),
        AdminFunction("Calificaciones", Icons.Filled.CheckCircle, { /* TODO: Implementar Calificaciones */ }),
        AdminFunction("Compartir Evento", Icons.Filled.Share, { /* TODO: Implementar Compartir Evento */ }),
        AdminFunction("Gestionar Usuarios", Icons.Filled.Person, { /* TODO: Implementar Gestión de Usuarios */ }),
    )

    Column(
        modifier = Modifier
            .padding(paddingValues)
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Divide las funciones en filas de dos
        adminFunctions.chunked(2).forEach { rowFunctions ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                rowFunctions.forEach { function ->
                    AdminFunctionItem(function = function)
                }
                // Si la fila tiene solo un elemento, agrega un Spacer para mantener la alineación
                if (rowFunctions.size == 1) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminFunctionItem(function: AdminFunction) {
    Card(
        onClick = function.onClick,
        modifier = Modifier
            .width(160.dp)
            .height(120.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = function.icon,
                contentDescription = function.title,
                modifier = Modifier.size(48.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = function.title,
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    LoginTheme {
        HomeScreen(paddingValues = PaddingValues())
    }
}
