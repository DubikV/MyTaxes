package com.gmail.vanyadubik.mytaxes.common

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * This method launch and collect from flow after special status lifecycle
 * @param owner - LifecycleOwner, where need collect flow
 * @param minActiveState - started lifecycle status when flow started collected
 **/
inline fun <T> Flow<T>.launchAndCollectIn(
    owner: LifecycleOwner,
    minActiveState: Lifecycle.State = Lifecycle.State.STARTED,
    crossinline action: suspend CoroutineScope.(T) -> Unit
) = owner.lifecycleScope.launch {
    owner.repeatOnLifecycle(minActiveState) {
        collect {
            action(it)
        }
    }
}

/**
 * This method launch and collect last from flow after lifecycle status RESUMED
 **/
inline fun <T> Flow<T>.launchAndCollectInResumed(
    owner: LifecycleOwner,
    crossinline action: suspend CoroutineScope.(T) -> Unit
) = launchAndCollectIn(owner = owner, minActiveState = Lifecycle.State.RESUMED) {
    action(it)
}

/**
 * This method launch and collect last from flow after special status lifecycle
 * @param owner - LifecycleOwner, where need collect flow
 * @param delayMillis - time milliseconds delay when data collected
 * @param minActiveState - started lifecycle status when flow started collected
 **/
inline fun <T> Flow<T>.launchAndCollectLatestIn(
    owner: LifecycleOwner,
    delayMillis: Long = 0,
    minActiveState: Lifecycle.State = Lifecycle.State.STARTED,
    crossinline action: suspend CoroutineScope.(T) -> Unit
) = owner.lifecycleScope.launch {
    owner.repeatOnLifecycle(minActiveState) {
        collectLatest {
            if(delayMillis > 0) {
                owner.lifecycleScope.launch(Dispatchers.IO) {
                    delay(delayMillis)
                    owner.lifecycleScope.launch {
                        action(it)
                    }
                }
            }else{
                action(it)
            }
        }
    }
}

/**
 * This method launch and collect last from flow after lifecycle status CREATED
 **/
inline fun <T> Flow<T>.launchAndCollectLatestInCreated(
    owner: LifecycleOwner,
    delayMillis: Long = 0,
    crossinline action: suspend CoroutineScope.(T) -> Unit
) = launchAndCollectLatestIn(owner = owner, delayMillis = delayMillis, minActiveState = Lifecycle.State.CREATED){
    action(it)
}

/**
 * This method launch and collect last from flow after lifecycle status RESUMED
 **/
inline fun <T> Flow<T>.launchAndCollectLatestInResumed(
    owner: LifecycleOwner,
    delayMillis: Long = 0,
    crossinline action: suspend CoroutineScope.(T) -> Unit
) = launchAndCollectLatestIn(owner = owner, delayMillis = delayMillis, minActiveState = Lifecycle.State.RESUMED) {
    action(it)
}

fun <T> Flow<T>.asState(
    scope: CoroutineScope,
    initialValue: T
): StateFlow<T> = this.stateIn(
    scope = scope,
    started = SharingStarted.WhileSubscribed(5000),
    initialValue = initialValue
)