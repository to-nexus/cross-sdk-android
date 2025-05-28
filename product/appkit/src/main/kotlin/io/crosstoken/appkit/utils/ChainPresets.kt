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
    if (listOf("612055", "612044").contains(chainReference))
        Modal.Model.ChainImage.Asset(R.drawable.cross_green)
    else
        Modal.Model.ChainImage.Network("https://api.web3modal.com/public/getAssetImage/${networkImagesIds[chainReference]}")

internal val networkImagesIds = mapOf(
    // Ethereum
    "1" to "692ed6ba-e569-459a-556a-776476829e00",
    // Ethereum Sepolia
    "11155111" to "692ed6ba-e569-459a-556a-776476829e00",
    // BNB Smart Chain
    "56" to "93564157-2e8e-4ce7-81df-b264dbee9b00",
    // BNB Smart Chain Testnet
    "97" to "93564157-2e8e-4ce7-81df-b264dbee9b00"
)

internal val networkNames = mapOf(
    "612055" to "Cross",
    "612044" to "Cross ZoneZero",
    "56" to "BNB Smart Chain",
    "97" to "BNB Smart Chain Testnet",
    "1" to "Ethereum",
    "11155111" to "Ethereum Sepolia"
)
