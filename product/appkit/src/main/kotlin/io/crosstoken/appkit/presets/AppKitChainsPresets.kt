package io.crosstoken.appkit.presets

import io.crosstoken.appkit.client.Modal
import io.crosstoken.appkit.R
import io.crosstoken.appkit.utils.EthUtils

object AppKitChainsPresets {
    val crossToken = Modal.Model.Token(name = "Cross", symbol = "CROSS", decimal = 18)
    val bnbToken = Modal.Model.Token(name = "BNB", symbol = "BNB", decimal = 18)
    val ethToken = Modal.Model.Token(name = "Ether", symbol = "ETH", decimal = 18)
    val kaiaToken = Modal.Model.Token(name = "KAIA", symbol = "KAIA", decimal = 18)

    val ethChains: Map<String, Modal.Model.Chain> = mapOf(
        "612055" to Modal.Model.Chain(
            chainName = "Cross Mainnet",
            chainNamespace = "eip155",
            chainReference = "612055",
            requiredMethods = EthUtils.ethRequiredMethods,
            optionalMethods = EthUtils.ethOptionalMethods,
            events = EthUtils.ethEvents,
            token = crossToken,
            chainImage = Modal.Model.ChainImage.Asset(R.drawable.cross_green),
            rpcUrl = "https://mainnet.crosstoken.io:22001",
            blockExplorerUrl = "https://mainnet.crossscan.io"
        ),
        "612044" to Modal.Model.Chain(
            chainName = "CROSS Testnet",
            chainNamespace = "eip155",
            chainReference = "612044",
            requiredMethods = EthUtils.ethRequiredMethods,
            optionalMethods = EthUtils.ethOptionalMethods,
            events = EthUtils.ethEvents,
            token = crossToken,
            chainImage = Modal.Model.ChainImage.Asset(R.drawable.cross_green),
            rpcUrl = "https://testnet.crosstoken.io:22001",
            blockExplorerUrl = "https://testnet.crossscan.io"
        ),
        "56" to Modal.Model.Chain(
            chainName = "BNB Smart Chain (Mainnet)",
            chainNamespace = "eip155",
            chainReference = "56",
            requiredMethods = EthUtils.ethRequiredMethods,
            optionalMethods = EthUtils.ethOptionalMethods,
            events = EthUtils.ethEvents,
            token = bnbToken,
            chainImage = Modal.Model.ChainImage.Asset(R.drawable.bnb),
            rpcUrl = "https://rpc.ankr.com/bsc",
            blockExplorerUrl = "https://bscscan.com"
        ),
        "97" to Modal.Model.Chain(
            chainName = "BNB Smart Chain (Testnet)",
            chainNamespace = "eip155",
            chainReference = "97",
            requiredMethods = EthUtils.ethRequiredMethods,
            optionalMethods = EthUtils.ethOptionalMethods,
            events = EthUtils.ethEvents,
            token = bnbToken,
            chainImage = Modal.Model.ChainImage.Asset(R.drawable.bnb),
            rpcUrl = "https://bsc-testnet-rpc.publicnode.com",
            blockExplorerUrl = "https://testnet.bscscan.com"
        ),

        "8217" to Modal.Model.Chain(
            chainName = "Kaia Mainnet",
            chainNamespace = "eip155",
            chainReference = "8217",
            requiredMethods = EthUtils.ethRequiredMethods,
            optionalMethods = EthUtils.ethOptionalMethods,
            events = EthUtils.ethEvents,
            token = kaiaToken,
            chainImage = Modal.Model.ChainImage.Asset(R.drawable.kaia),
            rpcUrl = "https://kaia-mainnet-ext.crosstoken.io/815b8a6e389b34a4f82cfd1e501692dee2f4e8f5",
            blockExplorerUrl = "https://kaiascan.io/"
        ),
        "1001" to Modal.Model.Chain(
            chainName = "Kaia Kairos",
            chainNamespace = "eip155",
            chainReference = "1001",
            requiredMethods = EthUtils.ethRequiredMethods,
            optionalMethods = EthUtils.ethOptionalMethods,
            events = EthUtils.ethEvents,
            token = kaiaToken,
            chainImage = Modal.Model.ChainImage.Asset(R.drawable.kaia),
            rpcUrl = "https://kaia-testnet.crosstoken.io/fda0d5a47e2d0768e9329444295a3f0681fff365",
            blockExplorerUrl = "https://kairos.io/"
        ),
        "1" to Modal.Model.Chain(
            chainName = "Ethereum",
            chainNamespace = "eip155",
            chainReference = "1",
            requiredMethods = EthUtils.ethRequiredMethods,
            optionalMethods = EthUtils.ethOptionalMethods,
            events = EthUtils.ethEvents,
            token = ethToken,
            chainImage = Modal.Model.ChainImage.Asset(R.drawable.eth),
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
            chainImage = Modal.Model.ChainImage.Asset(R.drawable.eth),
            rpcUrl = "https://rpc.sepolia.dev",
            blockExplorerUrl = "https://sepolia.etherscan.io/"
        )
    )
}
