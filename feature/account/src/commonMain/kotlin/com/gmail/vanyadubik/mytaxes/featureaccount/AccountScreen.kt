package com.gmail.vanyadubik.myassistant.featureaccount

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.gmail.vanyadubik.myassistant.commonui.ui.components.AppBarActions
import com.gmail.vanyadubik.myassistant.commonui.ui.components.button.AppButton
import com.gmail.vanyadubik.myassistant.commonui.ui.components.card.ErrorContainerCard
import com.gmail.vanyadubik.myassistant.commonui.ui.components.image.AppAsyncImage
import com.gmail.vanyadubik.myassistant.commonui.ui.components.image.rememberPhotoPickerLauncher
import com.gmail.vanyadubik.myassistant.commonui.ui.dialog.ConfirmBottomSheet
import com.gmail.vanyadubik.myassistant.commonui.utils.title
import com.gmail.vanyadubik.myassistant.domen.model.Currency
import com.gmail.vanyadubik.myassistant.domen.model.FopGroup
import com.gmail.vanyadubik.myassistant.domen.model.TaxReminderMode
import com.gmail.vanyadubik.myassistant.featureaccount.model.AccountUiState
import com.gmail.vanyadubik.myassistant.resource.Res
import com.gmail.vanyadubik.myassistant.resource.account_cars_title
import com.gmail.vanyadubik.myassistant.resource.account_currency_hint
import com.gmail.vanyadubik.myassistant.resource.account_currency_none
import com.gmail.vanyadubik.myassistant.resource.account_currency_title
import com.gmail.vanyadubik.myassistant.resource.account_group_hint
import com.gmail.vanyadubik.myassistant.resource.account_group_none
import com.gmail.vanyadubik.myassistant.resource.account_group_title
import com.gmail.vanyadubik.myassistant.resource.account_tax_reminder_hint_as_needed
import com.gmail.vanyadubik.myassistant.resource.account_tax_reminder_hint_monthly
import com.gmail.vanyadubik.myassistant.resource.account_tax_reminder_title
import com.gmail.vanyadubik.myassistant.resource.account_title
import com.gmail.vanyadubik.myassistant.resource.add_car
import com.gmail.vanyadubik.myassistant.resource.car_input_empty
import com.gmail.vanyadubik.myassistant.resource.car_name
import com.gmail.vanyadubik.myassistant.resource.car_number
import com.gmail.vanyadubik.myassistant.resource.change_avatar
import com.gmail.vanyadubik.myassistant.resource.delete
import com.gmail.vanyadubik.myassistant.resource.delete_account
import com.gmail.vanyadubik.myassistant.resource.delete_account_confirm
import com.gmail.vanyadubik.myassistant.resource.delete_account_message
import com.gmail.vanyadubik.myassistant.resource.delete_account_title
import com.gmail.vanyadubik.myassistant.resource.deleting
import com.gmail.vanyadubik.myassistant.resource.logout
import com.gmail.vanyadubik.myassistant.resource.nickname
import com.gmail.vanyadubik.myassistant.resource.no_cars
import com.gmail.vanyadubik.myassistant.resource.oops_error_title
import com.gmail.vanyadubik.myassistant.resource.retry
import com.gmail.vanyadubik.myassistant.resource.save
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun AccountScreen(
    viewModel: AccountViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsState()
    val photoPicker = rememberPhotoPickerLauncher(onPhotoPicked = viewModel::onAvatarPicked)

    AccountContent(
        state = state,
        onShowEditNickname = viewModel::showEditNicknameDialog,
        onAvatarClick = { photoPicker.launch() },
        onGroupClicked = viewModel::onGroupClicked,
        onCurrencyClicked = viewModel::onCurrencyClicked,
        onTaxReminderModeClicked = viewModel::onTaxReminderModeClicked,
        onShowAddCar = viewModel::showAddCarSheet,
        onDeleteCar = viewModel::deleteCar,
        onLogout = viewModel::logout,
        onDelete = viewModel::showDeleteConfirm,
        onRetryLoad = viewModel::load,
    )

    if (state.showEditNicknameDialog) {
        ConfirmBottomSheet(
            title = stringResource(Res.string.nickname),
            confirmText = stringResource(Res.string.save),
            onConfirm = {
                viewModel.saveNickname()
                viewModel.hideEditNicknameDialog()
            },
            onDismiss = viewModel::hideEditNicknameDialog,
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                OutlinedTextField(
                    value = state.nicknameInput,
                    onValueChange = viewModel::onNicknameInputChanged,
                    label = { Text(stringResource(Res.string.nickname)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }

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
                    value = state.carNameInput,
                    onValueChange = viewModel::onCarNameChanged,
                    label = { Text(stringResource(Res.string.car_name)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    value = state.carNumberInput,
                    onValueChange = viewModel::onCarNumberChanged,
                    label = { Text(stringResource(Res.string.car_number)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                if (state.carInputError != null) {
                    Text(
                        text = stringResource(Res.string.car_input_empty),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                    )
                }
            }
        }
    }

    if (state.showDeleteConfirm) {
        ConfirmBottomSheet(
            title = stringResource(Res.string.delete_account_title),
            message = stringResource(Res.string.delete_account_message),
            confirmText = stringResource(Res.string.delete_account_confirm),
            onConfirm = viewModel::deleteAccount,
            onDismiss = viewModel::hideDeleteConfirm,
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
private fun AccountContent(
    state: AccountUiState,
    onShowEditNickname: () -> Unit,
    onAvatarClick: () -> Unit,
    onGroupClicked: (FopGroup) -> Unit,
    onCurrencyClicked: (Currency) -> Unit,
    onTaxReminderModeClicked: (TaxReminderMode) -> Unit,
    onShowAddCar: () -> Unit,
    onDeleteCar: (String) -> Unit,
    onLogout: () -> Unit,
    onDelete: () -> Unit,
    onRetryLoad: () -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(Res.string.account_title)) },
                actions = { AppBarActions() },
            )
        }
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
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Box(
                            modifier = Modifier
                                .size(80.dp)
                                .clickable { onAvatarClick() },
                            contentAlignment = Alignment.Center,
                        ) {
                            if (!state.avatarUrl.isNullOrEmpty()) {
                                AppAsyncImage(
                                    model = state.avatarUrl,
                                    contentDescription = null,
                                    modifier = Modifier
                                        .size(80.dp)
                                        .clip(CircleShape),
                                    contentScale = ContentScale.Crop,
                                    onClick = onAvatarClick,
                                )
                            } else {
                                Surface(
                                    shape = CircleShape,
                                    color = MaterialTheme.colorScheme.primaryContainer,
                                    modifier = Modifier.fillMaxSize(),
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            Icons.Default.AccountCircle,
                                            contentDescription = null,
                                            modifier = Modifier.size(56.dp),
                                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                        )
                                    }
                                }
                            }
                            Surface(
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .size(24.dp),
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        Icons.Default.Edit,
                                        contentDescription = stringResource(Res.string.change_avatar),
                                        modifier = Modifier.size(14.dp),
                                        tint = MaterialTheme.colorScheme.onPrimary,
                                    )
                                }
                            }
                        }

                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(4.dp),
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                modifier = Modifier.clickable { onShowEditNickname() },
                            ) {
                                Text(
                                    text = if (!state.nickname.isNullOrBlank()) state.nickname else stringResource(Res.string.nickname),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = if (!state.nickname.isNullOrBlank()) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                                Icon(
                                    Icons.Default.Edit,
                                    contentDescription = stringResource(Res.string.nickname),
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(18.dp),
                                )
                            }
                            Text(
                                state.email,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }

                    Text(stringResource(Res.string.account_group_title), style = MaterialTheme.typography.labelLarge)
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        FopGroup.entries.forEach { group ->
                            FilterChip(
                                selected = group == state.savedGroup,
                                onClick = { onGroupClicked(group) },
                                enabled = !state.isGroupLoading,
                                label = { Text(group.title()) },
                            )
                        }
                    }
                    Text(
                        stringResource(Res.string.account_group_hint),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    if (!state.isGroupLoading && state.savedGroup == null) {
                        Text(
                            stringResource(Res.string.account_group_none),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }

                    Text(stringResource(Res.string.account_currency_title), style = MaterialTheme.typography.labelLarge)
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Currency.entries.forEach { currency ->
                            FilterChip(
                                selected = currency == state.savedCurrency,
                                onClick = { onCurrencyClicked(currency) },
                                enabled = !state.isGroupLoading,
                                label = { Text(currency.code) },
                            )
                        }
                    }
                    Text(
                        stringResource(Res.string.account_currency_hint),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    if (!state.isGroupLoading && state.savedCurrency == null) {
                        Text(
                            stringResource(Res.string.account_currency_none),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }

                    Text(stringResource(Res.string.account_tax_reminder_title), style = MaterialTheme.typography.labelLarge)
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        TaxReminderMode.entries.forEach { mode ->
                            val selected = state.savedTaxReminderMode == mode || (state.savedTaxReminderMode == null && mode == TaxReminderMode.AS_NEEDED)
                            FilterChip(
                                selected = selected,
                                onClick = { onTaxReminderModeClicked(mode) },
                                enabled = !state.isGroupLoading,
                                label = { Text(mode.title()) },
                            )
                        }
                    }
                    val reminderHint = when (state.savedTaxReminderMode) {
                        TaxReminderMode.MONTHLY -> stringResource(Res.string.account_tax_reminder_hint_monthly)
                        else -> stringResource(Res.string.account_tax_reminder_hint_as_needed)
                    }
                    Text(
                        reminderHint,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )

                    Text(stringResource(Res.string.account_cars_title), style = MaterialTheme.typography.labelLarge)

                    if (state.cars.isEmpty()) {
                        Text(
                            stringResource(Res.string.no_cars),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            state.cars.forEach { car ->
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth().padding(12.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically,
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                                        ) {
                                            Icon(
                                                Icons.Default.DirectionsCar,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.primary,
                                            )
                                            Column {
                                                Text(car.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                                Text(
                                                    car.number,
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                )
                                            }
                                        }
                                        IconButton(onClick = { onDeleteCar(car.id) }) {
                                            Icon(
                                                Icons.Default.Delete,
                                                contentDescription = stringResource(Res.string.delete),
                                                tint = MaterialTheme.colorScheme.error,
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    AppButton(
                        text = stringResource(Res.string.add_car),
                        onClick = onShowAddCar,
                        leftIcon = Icons.Default.DirectionsCar,
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    AppButton(
                        text = stringResource(Res.string.logout),
                        onClick = onLogout,
                        enabled = !state.isDeleting,
                    )
                    AppButton(
                        text = stringResource(Res.string.delete_account),
                        onClick = onDelete,
                        isOutlinedButton = true,
                        leftIcon = Icons.Default.Delete,
                        loadingMessage = if (state.isDeleting) stringResource(Res.string.deleting) else null,
                    )
                }
            }
        }
    }
}
