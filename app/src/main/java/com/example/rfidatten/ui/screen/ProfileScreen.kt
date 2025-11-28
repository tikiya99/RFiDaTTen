package com.example.rfidatten.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.rfidatten.viewmodel.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    profileId: Long,
    onBack: () -> Unit,
    viewModel: ProfileViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var name by remember { mutableStateOf("") }
    var age by remember { mutableStateOf("") }
    var birthday by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var isEditing by remember { mutableStateOf(false) }
    
    LaunchedEffect(profileId) {
        viewModel.loadProfile(profileId)
    }
    
    LaunchedEffect(uiState) {
        if (uiState is ProfileUiState.Success) {
            val profile = (uiState as ProfileUiState.Success).profile
            name = profile.name
            age = profile.age.toString()
            birthday = profile.birthday
            email = profile.email
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Profile") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, "Back")
                    }
                },
                actions = {
                    if (!isEditing) {
                        IconButton(onClick = { isEditing = true }) {
                            Icon(Icons.Filled.Edit, "Edit")
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (val state = uiState) {
                is ProfileUiState.Loading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                
                is ProfileUiState.Success -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp)
                    ) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                        ) {
                            Column(modifier = Modifier.padding(24.dp)) {
                                Icon(
                                    Icons.Filled.Person,
                                    contentDescription = null,
                                    modifier = Modifier
                                        .size(80.dp)
                                        .align(Alignment.CenterHorizontally),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                
                                Spacer(Modifier.height(24.dp))
                                
                                OutlinedTextField(
                                    value = name,
                                    onValueChange = { name = it },
                                    label = { Text("Name") },
                                    modifier = Modifier.fillMaxWidth(),
                                    enabled = isEditing,
                                    leadingIcon = { Icon(Icons.Filled.Person, null) }
                                )
                                
                                Spacer(Modifier.height(16.dp))
                                
                                OutlinedTextField(
                                    value = age,
                                    onValueChange = { age = it.filter { char -> char.isDigit() } },
                                    label = { Text("Age") },
                                    modifier = Modifier.fillMaxWidth(),
                                    enabled = isEditing,
                                    leadingIcon = { Icon(Icons.Filled.Numbers, null) }
                                )
                                
                                Spacer(Modifier.height(16.dp))
                                
                                OutlinedTextField(
                                    value = birthday,
                                    onValueChange = { birthday = it },
                                    label = { Text("Birthday (YYYY-MM-DD)") },
                                    modifier = Modifier.fillMaxWidth(),
                                    enabled = isEditing,
                                    leadingIcon = { Icon(Icons.Filled.Cake, null) }
                                )
                                
                                Spacer(Modifier.height(16.dp))
                                
                                OutlinedTextField(
                                    value = email,
                                    onValueChange = { email = it },
                                    label = { Text("Email") },
                                    modifier = Modifier.fillMaxWidth(),
                                    enabled = isEditing,
                                    leadingIcon = { Icon(Icons.Filled.Email, null) }
                                )
                                
                                if (isEditing) {
                                    Spacer(Modifier.height(24.dp))
                                    
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.End
                                    ) {
                                        TextButton(
                                            onClick = {
                                                isEditing = false
                                                // Reset values
                                                val profile = state.profile
                                                name = profile.name
                                                age = profile.age.toString()
                                                birthday = profile.birthday
                                                email = profile.email
                                            }
                                        ) {
                                            Text("Cancel")
                                        }
                                        Spacer(Modifier.width(8.dp))
                                        Button(
                                            onClick = {
                                                viewModel.updateProfile(
                                                    state.profile.copy(
                                                        name = name.trim(),
                                                        age = age.toIntOrNull() ?: 0,
                                                        birthday = birthday.trim(),
                                                        email = email.trim()
                                                    )
                                                )
                                                isEditing = false
                                            }
                                        ) {
                                            Icon(Icons.Filled.Save, null)
                                            Spacer(Modifier.width(8.dp))
                                            Text("Save")
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                
                is ProfileUiState.Error -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            Icons.Filled.Error,
                            contentDescription = null,
                            modifier = Modifier.size(80.dp),
                            tint = MaterialTheme.colorScheme.error
                        )
                        Spacer(Modifier.height(16.dp))
                        Text(
                            "Error Loading Profile",
                            style = MaterialTheme.typography.headlineSmall,
                            color = MaterialTheme.colorScheme.error
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            state.message,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}
