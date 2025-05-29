package io.crosstoken.appkit.utils

import io.crosstoken.appkit.R
import io.crosstoken.appkit.client.Modal

internal fun Modal.Model.ChainImage.getImageData() = when (this) {
    is Modal.Model.ChainImage.Asset -> id
    is Modal.Model.ChainImage.Network -> url
}

internal fun Modal.Model.Chain.getImageData(): Any = when (chainImage) {
    is Modal.Model.ChainImage.Asset -> chainImage.id
    is Modal.Model.ChainImage.Network -> chainImage.url
    null -> getChainNetworkImage(chainReference)
}

internal fun getChainNetworkImage(chainReference: String) =
    Modal.Model.ChainImage.Asset(chainImageIds[chainReference] ?: R.drawable.network)

internal val chainImageIds = mapOf(
    "612055" to R.drawable.cross_green,
    "612044" to R.drawable.cross_green,
    "56" to R.drawable.bnb,
    "97" to R.drawable.bnb,
    "1" to R.drawable.eth,
    "11155111" to R.drawable.eth
)
