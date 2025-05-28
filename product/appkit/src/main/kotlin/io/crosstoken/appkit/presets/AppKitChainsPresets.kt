package io.crosstoken.appkit.presets

import io.crosstoken.appkit.client.Modal
import io.crosstoken.appkit.utils.EthUtils

object AppKitChainsPresets {
    val crossToken = Modal.Model.Token(name = "Cross", symbol = "CROSS", decimal = 18)
    val ethToken = Modal.Model.Token(name = "Ether", symbol = "ETH", decimal = 18)

    val ethChains: Map<String, Modal.Model.Chain> = mapOf(
        "612055" to Modal.Model.Chain(
            chainName = "Cross",
            chainNamespace = "eip155",
            chainReference = "612055",
            requiredMethods = EthUtils.ethRequiredMethods,
            optionalMethods = EthUtils.ethOptionalMethods,
            events = EthUtils.ethEvents,
            token = crossToken,
            rpcUrl = "https://mainnet.crosstoken.io:22001",
            blockExplorerUrl = "https://mainnet.crossscan.io"
        ),
        "612044" to Modal.Model.Chain(
            chainName = "Cross ZoneZero",
            chainNamespace = "eip155",
            chainReference = "612044",
            requiredMethods = EthUtils.ethRequiredMethods,
            optionalMethods = EthUtils.ethOptionalMethods,
            events = EthUtils.ethEvents,
            token = crossToken,
            rpcUrl = "https://testnet.crosstoken.io:22001",
            blockExplorerUrl = "https://testnet.crossscan.io"
        ),
        "56" to Modal.Model.Chain(
            chainName = "BNB Smart Chain",
            chainNamespace = "eip155",
            chainReference = "56",
            requiredMethods = EthUtils.ethRequiredMethods,
            optionalMethods = EthUtils.ethOptionalMethods,
            events = EthUtils.ethEvents,
            token = Modal.Model.Token("BNB", "BNB", 18),
            rpcUrl = "https://rpc.ankr.com/bsc",
            blockExplorerUrl = "https://bscscan.com"
        ),
        "97" to Modal.Model.Chain(
            chainName = "BNB Smart Chain Testnet",
            chainNamespace = "eip155",
            chainReference = "97",
            requiredMethods = EthUtils.ethRequiredMethods,
            optionalMethods = EthUtils.ethOptionalMethods,
            events = EthUtils.ethEvents,
            token = Modal.Model.Token("tBNB", "tBNB", 18),
            rpcUrl = "https://bsc-testnet-rpc.publicnode.com",
            blockExplorerUrl = "https://testnet.bscscan.com"
        ),
        "1" to Modal.Model.Chain(
            chainName = "Ethereum",
            chainNamespace = "eip155",
            chainReference = "1",
            requiredMethods = EthUtils.ethRequiredMethods,
            optionalMethods = EthUtils.ethOptionalMethods,
            events = EthUtils.ethEvents,
            token = ethToken,
            rpcUrl = "https://cloudflare-eth.com",
            blockExplorerUrl = "https://etherscan.io"
        ),
        "11155111" to Modal.Model.Chain(
            chainName = "Ethereum Sepolia",
            chainNamespace = "eip155",
            chainReference = "11155111",
            requiredMethods = EthUtils.ethRequiredMethods,
            optionalMethods = EthUtils.ethOptionalMethods,
            events = EthUtils.ethEvents,
            token = ethToken,
            rpcUrl = "https://rpc.sepolia.dev",
            blockExplorerUrl = "https://sepolia.etherscan.io/"
        )
    )
}
