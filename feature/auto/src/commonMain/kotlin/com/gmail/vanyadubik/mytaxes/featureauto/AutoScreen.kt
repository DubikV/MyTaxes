package com.gmail.vanyadubik.myassistant.featureauto

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.gmail.vanyadubik.myassistant.commonui.ui.components.button.AppButton
import com.gmail.vanyadubik.myassistant.commonui.ui.components.card.ErrorContainerCard
import com.gmail.vanyadubik.myassistant.commonui.ui.components.image.AppAsyncImage
import com.gmail.vanyadubik.myassistant.commonui.ui.components.image.PhotoPreviewDialog
import com.gmail.vanyadubik.myassistant.commonui.ui.components.image.rememberPhotoPickerLauncher
import com.gmail.vanyadubik.myassistant.commonui.ui.components.textfield.DateField
import com.gmail.vanyadubik.myassistant.commonui.ui.dialog.ConfirmBottomSheet
import com.gmail.vanyadubik.myassistant.commonui.utils.formatNumber
import com.gmail.vanyadubik.myassistant.domen.model.AutoServiceRecord
import com.gmail.vanyadubik.myassistant.featureauto.model.AutoUiState
import com.gmail.vanyadubik.myassistant.resource.Res
import com.gmail.vanyadubik.myassistant.resource.add_car
import com.gmail.vanyadubik.myassistant.resource.add_photo
import com.gmail.vanyadubik.myassistant.resource.add_service_record
import com.gmail.vanyadubik.myassistant.resource.auto_service_title
import com.gmail.vanyadubik.myassistant.resource.car_input_empty
import com.gmail.vanyadubik.myassistant.resource.car_name
import com.gmail.vanyadubik.myassistant.resource.car_number
import com.gmail.vanyadubik.myassistant.resource.delete
import com.gmail.vanyadubik.myassistant.resource.delete_record_message
import com.gmail.vanyadubik.myassistant.resource.delete_record_title
import com.gmail.vanyadubik.myassistant.resource.edit_service_record
import com.gmail.vanyadubik.myassistant.resource.income_date
import com.gmail.vanyadubik.myassistant.resource.no_cars
import com.gmail.vanyadubik.myassistant.resource.no_service_records
import com.gmail.vanyadubik.myassistant.resource.odometer
import com.gmail.vanyadubik.myassistant.resource.oops_error_title
import com.gmail.vanyadubik.myassistant.resource.photos
import com.gmail.vanyadubik.myassistant.resource.retry
import com.gmail.vanyadubik.myassistant.resource.save
import com.gmail.vanyadubik.myassistant.resource.service_description
import com.gmail.vanyadubik.myassistant.resource.service_input_error
import com.gmail.vanyadubik.myassistant.resource.sign_in
import com.gmail.vanyadubik.myassistant.resource.sign_in_for_auto_service
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun AutoScreen(
    onOpenLogin: () -> Unit = {},
    viewModel: AutoViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsState()
    var previewPhotoUrl by remember { mutableStateOf<String?>(null) }
    val photoPicker = rememberPhotoPickerLauncher(onPhotoPicked = viewModel::onPhotoAdded)

    PhotoPreviewDialog(
        photoUrl = previewPhotoUrl,
        onDismiss = { previewPhotoUrl = null },
    )

    AutoContent(
        state = state,
        onCarSelected = viewModel::onCarSelected,
        onShowAddCar = viewModel::showAddCarSheet,
        onShowAddRecord = viewModel::showAddRecordSheet,
        onEditRecord = viewModel::showEditRecordSheet,
        onConfirmDeleteRecord = viewModel::confirmDeleteRecord,
        onPhotoClick = { previewPhotoUrl = it },
        onOpenLogin = onOpenLogin,
        onRetryLoad = viewModel::loadRecords,
    )

    if (state.showAddCarSheet) {
        ConfirmBottomSheet(
            title = stringResource(Res.string.add_car),
            confirmText = stringResource(Res.string.save),
            onConfirm = viewModel::addCar,
            onDismiss = viewModel::hideAddCarSheet,
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                OutlinedTextField(
                    value = state.newCarNameInput,
                    onValueChange = viewModel::onNewCarNameChanged,
                    label = { Text(stringResource(Res.string.car_name)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    value = state.newCarNumberInput,
                    onValueChange = viewModel::onNewCarNumberChanged,
                    label = { Text(stringResource(Res.string.car_number)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                if (state.newCarInputError != null) {
                    Text(
                        text = stringResource(Res.string.car_input_empty),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                    )
                }
            }
        }
    }

    if (state.showAddRecordSheet) {
        val sheetTitle = if (state.editingRecordId != null) {
            stringResource(Res.string.edit_service_record)
        } else {
            stringResource(Res.string.add_service_record)
        }
        ConfirmBottomSheet(
            title = sheetTitle,
            confirmText = stringResource(Res.string.save),
            onConfirm = viewModel::addRecord,
            onDismiss = viewModel::hideAddRecordSheet,
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                DateField(
                    label = stringResource(Res.string.income_date),
                    date = state.inputDate,
                    onDateSelected = viewModel::onDateSelected,
                )
                OutlinedTextField(
                    value = state.inputOdometer,
                    onValueChange = viewModel::onOdometerChanged,
                    label = { Text(stringResource(Res.string.odometer)) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    value = state.inputDescription,
                    onValueChange = viewModel::onDescriptionChanged,
                    label = { Text(stringResource(Res.string.service_description)) },
                    minLines = 3,
                    maxLines = 5,
                    modifier = Modifier.fillMaxWidth(),
                )

                Text(stringResource(Res.string.photos), style = MaterialTheme.typography.labelLarge)

                if (state.inputPhotos.isNotEmpty()) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        state.inputPhotos.forEachIndexed { index, photo ->
                            AppAsyncImage(
                                model = photo,
                                contentDescription = null,
                                modifier = Modifier
                                    .size(72.dp)
                                    .clip(RoundedCornerShape(8.dp)),
                                contentScale = ContentScale.Crop,
                                onDelete = { viewModel.onPhotoRemoved(index) },
                                onClick = { previewPhotoUrl = photo },
                            )
                        }
                    }
                }

                if (state.inputPhotos.size < 3) {
                    AppButton(
                        text = stringResource(Res.string.add_photo),
                        onClick = { photoPicker.launch() },
                        leftIcon = Icons.Default.AddAPhoto,
                        enabled = !state.isSavingRecord,
                        isOutlinedButton = true,
                    )
                }

                if (!state.inputError.isNullOrEmpty()) {
                    Text(
                        text = if (state.inputError == "empty") {
                            stringResource(Res.string.service_input_error)
                        } else {
                            state.inputError ?: ""
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                    )
                }
            }
        }
    }

    if (state.recordToDeleteId != null) {
        ConfirmBottomSheet(
            title = stringResource(Res.string.delete_record_title),
            message = stringResource(Res.string.delete_record_message),
            confirmText = stringResource(Res.string.delete),
            onConfirm = viewModel::deleteRecord,
            onDismiss = viewModel::hideDeleteRecordConfirm,
        )
    }

    if (state.actionError != null) {
        ConfirmBottomSheet(
            title = stringResource(Res.string.oops_error_title),
            message = state.actionError,
            confirmText = stringResource(Res.string.retry),
            onConfirm = viewModel::retryLastAction,
            onDismiss = viewModel::dismissActionError,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
private fun AutoContent(
    state: AutoUiState,
    onCarSelected: (String) -> Unit,
    onShowAddCar: () -> Unit,
    onShowAddRecord: () -> Unit,
    onEditRecord: (AutoServiceRecord) -> Unit,
    onConfirmDeleteRecord: (String) -> Unit,
    onPhotoClick: (String) -> Unit,
    onOpenLogin: () -> Unit,
    onRetryLoad: () -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(Res.string.auto_service_title)) },
            )
        },
        floatingActionButton = {
            if (state.isLoggedIn && state.cars.isNotEmpty() && state.selectedCarId != null) {
                FloatingActionButton(onClick = onShowAddRecord) {
                    Icon(Icons.Default.Add, contentDescription = stringResource(Res.string.add_service_record))
                }
            }
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Column(
                modifier = Modifier.widthIn(max = 560.dp).fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                if (state.loadError != null) {
                    ErrorContainerCard(
                        message = state.loadError,
                        onRetry = onRetryLoad,
                    )
                } else if (!state.isLoggedIn) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            Text(
                                stringResource(Res.string.sign_in_for_auto_service),
                                style = MaterialTheme.typography.bodyMedium,
                            )
                            AppButton(
                                text = stringResource(Res.string.sign_in),
                                onClick = onOpenLogin,
                            )
                        }
                    }
                } else if (state.cars.isEmpty()) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            Text(
                                stringResource(Res.string.no_cars),
                                style = MaterialTheme.typography.bodyMedium,
                            )
                            AppButton(
                                text = stringResource(Res.string.add_car),
                                onClick = onShowAddCar,
                                leftIcon = Icons.Default.DirectionsCar,
                            )
                        }
                    }
                } else {
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        state.cars.forEach { car ->
                            FilterChip(
                                selected = car.id == state.selectedCarId,
                                onClick = { onCarSelected(car.id) },
                                label = { Text("${car.name} (${car.number})") },
                                leadingIcon = { Icon(Icons.Default.DirectionsCar, contentDescription = null) },
                            )
                        }
                    }

                    if (state.records.isEmpty()) {
                        Text(
                            stringResource(Res.string.no_service_records),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 16.dp),
                        )
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            state.records.forEach { record ->
                                ServiceRecordCard(
                                    record = record,
                                    onClick = { onEditRecord(record) },
                                    onDelete = { onConfirmDeleteRecord(record.id) },
                                    onPhotoClick = onPhotoClick,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ServiceRecordCard(
    record: AutoServiceRecord,
    onClick: () -> Unit,
    onDelete: () -> Unit,
    onPhotoClick: (String) -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(
                        record.date.toString(),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                    )
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.primaryContainer,
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                        ) {
                            Icon(
                                Icons.Default.Speed,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            )
                            Text(
                                "${record.odometer.toDouble().formatNumber()} км",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                            )
                        }
                    }
                }
                IconButton(onClick = onDelete) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = stringResource(Res.string.delete),
                        tint = MaterialTheme.colorScheme.error,
                    )
                }
            }

            Text(
                record.description,
                style = MaterialTheme.typography.bodyMedium,
            )

            if (record.photos.isNotEmpty()) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(top = 4.dp),
                ) {
                    record.photos.forEach { photo ->
                        AppAsyncImage(
                            model = photo,
                            contentDescription = null,
                            modifier = Modifier
                                .size(80.dp)
                                .clip(RoundedCornerShape(8.dp)),
                            contentScale = ContentScale.Crop,
                            onClick = { onPhotoClick(photo) },
                        )
                    }
                }
            }
        }
    }
}
