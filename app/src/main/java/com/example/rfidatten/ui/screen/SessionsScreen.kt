package com.example.rfidatten.ui.screen

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.rfidatten.data.entity.Session
import com.example.rfidatten.util.ExportHelper
import com.example.rfidatten.viewmodel.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SessionsScreen(
    onBack: () -> Unit,
    viewModel: SessionViewModel = viewModel(),
    cardManagerViewModel: CardManagerViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val showCreateDialog by viewModel.showCreateDialog.collectAsState()
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Create", "Current", "Past")
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Sessions") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            TabRow(selectedTabIndex = selectedTab) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = { Text(title) }
                    )
                }
            }
            
            when (selectedTab) {
                0 -> CreateSessionTab(
                    onCreateClick = { viewModel.showCreateDialog() },
                    cardManagerViewModel = cardManagerViewModel
                )
                1 -> CurrentSessionsTab(
                    uiState = uiState,
                    onStartSession = { viewModel.startSession(it) },
                    onStopSession = { viewModel.stopSession(it) },
                    onDeleteSession = { viewModel.deleteSession(it) }
                )
                2 -> PastSessionsTab(
                    uiState = uiState,
                    onExportSession = { session ->
                        viewModel
                    }
                )
            }
        }
    }
    
    if (showCreateDialog) {
        CreateSessionDialog(
            onDismiss = { viewModel.hideCreateDialog() },
            onCreate = { name, startTime, endTime, participants ->
                viewModel.createSession(name, startTime, endTime, participants)
            },
            cardManagerViewModel = cardManagerViewModel,
            sessionViewModel = viewModel
        )
    }
}

@Composable
private fun CreateSessionTab(
    onCreateClick: () -> Unit,
    cardManagerViewModel: CardManagerViewModel
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Filled.AddCircle,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(Modifier.height(16.dp))
        Text(
            "Create New Session",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.height(8.dp))
        Text(
            "Set up a new attendance session",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(24.dp))
        Button(
            onClick = onCreateClick,
            modifier = Modifier.fillMaxWidth(0.6f)
        ) {
            Icon(Icons.Filled.Add, null)
            Spacer(Modifier.width(8.dp))
            Text("Create Session")
        }
    }
}

@Composable
private fun CurrentSessionsTab(
    uiState: SessionUiState,
    onStartSession: (Long) -> Unit,
    onStopSession: (Long) -> Unit,
    onDeleteSession: (Session) -> Unit
) {
    when (val state = uiState) {
        is SessionUiState.Loading -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }
        
        is SessionUiState.Success -> {
            if (state.currentSessions.isEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        Icons.Filled.EventBusy,
                        contentDescription = null,
                        modifier = Modifier.size(80.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    )
                    Spacer(Modifier.height(16.dp))
                    Text(
                        "No Current Sessions",
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(state.currentSessions) { session ->
                        SessionCard(
                            session = session,
                            isCurrent = true,
                            onStartClick = { onStartSession(session.sessionId) },
                            onStopClick = { onStopSession(session.sessionId) },
                            onDeleteClick = { onDeleteSession(session) }
                        )
                    }
                }
            }
        }
        
        is SessionUiState.Error -> {
            ErrorContent(state.message)
        }
    }
}

@Composable
private fun PastSessionsTab(
    uiState: SessionUiState,
    onExportSession: (Session) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val sessionViewModel: SessionViewModel = viewModel()
    
    when (val state = uiState) {
        is SessionUiState.Loading -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }
        
        is SessionUiState.Success -> {
            if (state.pastSessions.isEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        Icons.Filled.History,
                        contentDescription = null,
                        modifier = Modifier.size(80.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    )
                    Spacer(Modifier.height(16.dp))
                    Text(
                        "No Past Sessions",
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(state.pastSessions) { session ->
                        PastSessionCard(
                            session = session,
                            onExportClick = {
                                scope.launch {
                                    val attendance = sessionViewModel.getAttendanceForExport(session.sessionId)
                                    val result = ExportHelper.exportAttendanceToCSV(context, session, attendance)
                                    result.onSuccess { file ->
                                        Toast.makeText(
                                            context,
                                            "Exported to ${file.name}",
                                            Toast.LENGTH_LONG
                                        ).show()
                                        ExportHelper.shareCSVFile(context, file)
                                    }.onFailure { error ->
                                        Toast.makeText(
                                            context,
                                            "Export failed: ${error.message}",
                                            Toast.LENGTH_LONG
                                        ).show()
                                    }
                                }
                            }
                        )
                    }
                }
            }
        }
        
        is SessionUiState.Error -> {
            ErrorContent(state.message)
        }
    }
}

@Composable
private fun SessionCard(
    session: Session,
    isCurrent: Boolean,
    onStartClick: () -> Unit,
    onStopClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    var showDeleteDialog by remember { mutableStateOf(false) }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    session.sessionName,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                
                if (session.isActive) {
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = MaterialTheme.colorScheme.primary
                    ) {
                        Text(
                            "ACTIVE",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }
            }
            
            Spacer(Modifier.height(8.dp))
            
            Text("Start: ${formatDateTime(session.startTime)}")
            Text("End: ${formatDateTime(session.endTime)}")
            
            Spacer(Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (!session.isActive) {
                    Button(
                        onClick = onStartClick,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Filled.PlayArrow, null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Start")
                    }
                } else {
                    Button(
                        onClick = onStopClick,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Icon(Icons.Filled.Stop, null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Stop")
                    }
                }
                
                OutlinedButton(
                    onClick = { showDeleteDialog = true },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Filled.Delete, null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Delete")
                }
            }
        }
    }
    
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Session") },
            text = { Text("Are you sure you want to delete this session?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDeleteClick()
                        showDeleteDialog = false
                    }
                ) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun PastSessionCard(
    session: Session,
    onExportClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                session.sessionName,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(Modifier.height(8.dp))
            
            Text("Start: ${formatDateTime(session.startTime)}")
            Text("End: ${formatDateTime(session.endTime)}")
            
            Spacer(Modifier.height(12.dp))
            
            Button(
                onClick = onExportClick,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Filled.Download, null)
                Spacer(Modifier.width(8.dp))
                Text("Export Attendance")
            }
        }
    }
}

@Composable
private fun CreateSessionDialog(
    onDismiss: () -> Unit,
    onCreate: (String, Long, Long, Set<Long>) -> Unit,
    cardManagerViewModel: CardManagerViewModel,
    sessionViewModel: SessionViewModel
) {
    var sessionName by remember { mutableStateOf("") }
    var startDate by remember { mutableStateOf("") }
    var startTime by remember { mutableStateOf("") }
    var endDate by remember { mutableStateOf("") }
    var endTime by remember { mutableStateOf("") }
    var showParticipantDialog by remember { mutableStateOf(false) }
    val selectedParticipants by sessionViewModel.selectedParticipants.collectAsState()
    
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text(
                    "Create New Session",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(Modifier.height(16.dp))
                
                OutlinedTextField(
                    value = sessionName,
                    onValueChange = { sessionName = it },
                    label = { Text("Session Name") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(Modifier.height(12.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = startDate,
                        onValueChange = { startDate = it },
                        label = { Text("Start Date") },
                        placeholder = { Text("YYYY-MM-DD") },
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = startTime,
                        onValueChange = { startTime = it },
                        label = { Text("Time") },
                        placeholder = { Text("HH:MM") },
                        modifier = Modifier.weight(1f)
                    )
                }
                
                Spacer(Modifier.height(12.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = endDate,
                        onValueChange = { endDate = it },
                        label = { Text("End Date") },
                        placeholder = { Text("YYYY-MM-DD") },
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = endTime,
                        onValueChange = { endTime = it },
                        label = { Text("Time") },
                        placeholder = { Text("HH:MM") },
                        modifier = Modifier.weight(1f)
                    )
                }
                
                Spacer(Modifier.height(12.dp))
                
                OutlinedButton(
                    onClick = { showParticipantDialog = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Filled.Group, null)
                    Spacer(Modifier.width(8.dp))
                    Text("Select Participants (${selectedParticipants.size})")
                }
                
                Spacer(Modifier.height(20.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel")
                    }
                    Spacer(Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val startMillis = parseDateTime(startDate, startTime)
                            val endMillis = parseDateTime(endDate, endTime)
                            if (sessionName.isNotBlank() && startMillis > 0 && endMillis > 0) {
                                onCreate(sessionName.trim(), startMillis, endMillis, selectedParticipants)
                            }
                        },
                        enabled = sessionName.isNotBlank() && startDate.isNotBlank() && 
                                  startTime.isNotBlank() && endDate.isNotBlank() && endTime.isNotBlank()
                    ) {
                        Text("Create")
                    }
                }
            }
        }
    }
    
    if (showParticipantDialog) {
        SelectParticipantsDialog(
            onDismiss = { showParticipantDialog = false },
            cardManagerViewModel = cardManagerViewModel,
            sessionViewModel = sessionViewModel
        )
    }
}

@Composable
private fun SelectParticipantsDialog(
    onDismiss: () -> Unit,
    cardManagerViewModel: CardManagerViewModel,
    sessionViewModel: SessionViewModel
) {
    val cardsState by cardManagerViewModel.uiState.collectAsState()
    val selectedParticipants by sessionViewModel.selectedParticipants.collectAsState()
    
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text(
                    "Select Participants",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(Modifier.height(8.dp))
                
                Text(
                    "Leave empty to allow all cards",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Spacer(Modifier.height(16.dp))
                
                when (val state = cardsState) {
                    is CardManagerUiState.Success -> {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 300.dp)
                        ) {
                            items(state.cardsWithProfiles) { cardWithProfile ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            sessionViewModel.toggleParticipant(cardWithProfile.card.cardId)
                                        }
                                        .padding(vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Checkbox(
                                        checked = selectedParticipants.contains(cardWithProfile.card.cardId),
                                        onCheckedChange = {
                                            sessionViewModel.toggleParticipant(cardWithProfile.card.cardId)
                                        }
                                    )
                                    Spacer(Modifier.width(8.dp))
                                    Column {
                                        Text(cardWithProfile.profile.name)
                                        Text(
                                            "Card: ${cardWithProfile.card.cardNumber}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    }
                    else -> {
                        CircularProgressIndicator()
                    }
                }
                
                Spacer(Modifier.height(16.dp))
                
                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Done")
                }
            }
        }
    }
}

@Composable
private fun ErrorContent(message: String) {
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
            "Error",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.error
        )
        Spacer(Modifier.height(8.dp))
        Text(message)
    }
}

private fun formatDateTime(timestamp: Long): String {
    val format = SimpleDateFormat("MMM dd, yyyy hh:mm a", Locale.getDefault())
    return format.format(Date(timestamp))
}

private fun parseDateTime(date: String, time: String): Long {
    return try {
        val format = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
        format.parse("$date $time")?.time ?: 0L
    } catch (e: Exception) {
        0L
    }
}
