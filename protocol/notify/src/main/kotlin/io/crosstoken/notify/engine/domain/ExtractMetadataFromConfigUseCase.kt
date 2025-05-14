@file:JvmSynthetic

package io.crosstoken.notify.engine.domain

import android.net.Uri
import io.crosstoken.android.internal.common.explorer.data.model.ImageUrl
import io.crosstoken.android.internal.common.explorer.domain.usecase.GetNotifyConfigUseCase
import io.crosstoken.android.internal.common.model.AppMetaData
import io.crosstoken.notify.common.model.Scope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

internal class ExtractMetadataFromConfigUseCase(private val getNotifyConfigUseCase: GetNotifyConfigUseCase) {
    suspend operator fun invoke(appUri: Uri): Result<Pair<AppMetaData, List<Scope.Remote>>> = withContext(Dispatchers.IO) {
        val appDomain = appUri.host ?: throw IllegalStateException("Unable to parse domain from $appUri")

        return@withContext getNotifyConfigUseCase(appDomain).mapCatching { notifyConfig ->
            Pair(
                AppMetaData(description = notifyConfig.description, url = notifyConfig.dappUrl, icons = notifyConfig.imageUrl.toList(), name = notifyConfig.name),
                notifyConfig.types.map { typeDTO -> Scope.Remote(id = typeDTO.id, name = typeDTO.name, description = typeDTO.description, typeDTO.imageUrl?.sm) }
            )
        }
    }

    private fun ImageUrl?.toList(): List<String> = if (this != null) listOf(sm, md, lg) else emptyList()
}