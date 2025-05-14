package io.crosstoken.appkit.domain.model

import io.crosstoken.appkit.client.Modal

object InvalidSessionException: Throwable("Session topic is missing")

internal fun Throwable.toModalError() = Modal.Model.Error(this)