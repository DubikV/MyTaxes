package com.gmail.vanyadubik.myassistant.common

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.lifecycle.compose.LocalLifecycleOwner
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest

/**
 * This function need use for launch flow in composable functions
 * */
@Composable
fun <T>FlowConsumerState(flow: StateFlow<T>, block: (T) -> Unit) {
    val blockState by rememberUpdatedState(block)
    val lifecycleOwner = LocalLifecycleOwner.current
    LaunchedEffect(Unit) {
        flow.launchAndCollectInResumed(lifecycleOwner) {
            blockState(it)
        }
    }
}


@Composable
fun <T>FlowConsumer(flow: Flow<T>, block: (T) -> Unit) {
    val blockState by rememberUpdatedState(block)
    val lifecycleOwner = LocalLifecycleOwner.current
    LaunchedEffect(Unit) {
        flow.launchAndCollectLatestInCreated(lifecycleOwner) {
            blockState(it)
        }
    }
}

@Composable
fun <T>FlowConsumerCollectLatest(flow: Flow<T>, block: (T) -> Unit) {
    val blockState by rememberUpdatedState(block)
    LaunchedEffect(Unit) {
        flow.collectLatest {
            blockState(it)
        }
    }
}