package io.crosstoken.notify.test.utils

import io.crosstoken.android.Core
import io.crosstoken.notify.client.Notify
import junit.framework.TestCase.fail
import timber.log.Timber

internal fun globalOnError(error: Notify.Model.Error) {
    Timber.e("globalOnError: ${error.throwable.stackTraceToString()}")
    fail(error.throwable.message)
}

internal fun globalOnError(error: Core.Model.Error) {
    Timber.e("globalOnError: ${error.throwable.stackTraceToString()}")
    fail(error.throwable.message)
}