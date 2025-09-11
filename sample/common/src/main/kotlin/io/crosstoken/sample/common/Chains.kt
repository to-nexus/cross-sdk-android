package io.crosstoken.sample.common

import androidx.annotation.DrawableRes
import org.json.JSONArray
import org.json.JSONObject
import java.security.SecureRandom

fun getPersonalSignBody(account: String): String {
    val msg = "My email is john@doe.com - ${System.currentTimeMillis()}".encodeToByteArray()
        .joinToString(separator = "", prefix = "0x") { eachByte -> "%02x".format(eachByte) }
    val customData = """{"metadata":"This is metadata for signed message"}"""
    return "[\"$msg\", \"$account\", $customData]"
}

fun getEthSignBody(account: String): String {
    val msg = "My email is john@doe.com - ${System.currentTimeMillis()}".encodeToByteArray()
        .joinToString(separator = "", prefix = "0x") { eachByte -> "%02x".format(eachByte) }
    return "[\"$account\", \"$msg\"]"
}

fun getEthSendTransaction(account: String): String {
    val customData = """
        {
        "metadata":{
        "activity":"You are about to send custom transaction to the contract.",
        "currentFormat":"This is a JSON formatted custom data.",
        "providedFormat":"Plain text(string), HTML(string), JSON(key value object) are supported.",
        "txTime":"${System.currentTimeMillis()}",
        "randomValue":"0x${ByteArray(12).apply { SecureRandom().nextBytes(this) }.joinToString("") { "%02x".format(it) }}"
        }
        }""".trimIndent().replace("\n", "")
    return "[{\"from\":\"$account\",\"to\":\"0x70012948c348CBF00806A3C79E3c5DAdFaAa347B\",\"data\":\"0x\",\"gasLimit\":\"0x5208\",\"gasPrice\":\"0x0649534e00\",\"value\":\"0\",\"nonce\":\"0x07\"}, $customData]"
}

fun getEthSignTypedData(account: String): String {
    val stringifiedParams =
        """{\"types\":{\"EIP712Domain\":[{\"name\":\"name\",\"type\":\"string\"},{\"name\":\"version\",\"type\":\"string\"},{\"name\":\"chainId\",\"type\":\"uint256\"},{\"name\":\"verifyingContract\",\"type\":\"address\"}],\"Person\":[{\"name\":\"name\",\"type\":\"string\"},{\"name\":\"wallet\",\"type\":\"address\"}],\"Mail\":[{\"name\":\"from\",\"type\":\"Person\"},{\"name\": \"to\",\"type\":\"Person\"},{\"name\":\"contents\",\"type\":\"string\"}]},\"primaryType\":\"Mail\",\"domain\":{\"name\":\"Ether Mail\",\"version\":\"1\",\"chainId\":1,\"verifyingContract\":\"0xCcCCccccCCCCcCCCCCCcCcCccCcCCCcCcccccccC\"},\"message\":{\"from\": {\"name\":\"Cow\",\"wallet\":\"0xCD2a3d9F938E13CD947Ec05AbC7FE734Df8DD826\"},\"to\":{\"name\":\"Bob\",\"wallet\":\"0xbBbBBBBbbBBBbbbBbbBbbbbBBbBbbbbBbBbbBBbB\"},\"contents\":\"Hello, Bob!\"}}"""

    return "[\"$account\",\"$stringifiedParams\"]"
}

fun getGetWalletAssetsParams(account: String): String {
    return JSONObject()
        .put("account", account)
        .put("chainFilter", JSONArray().put("0xa")) //hardcoded chain filter
        .toString()
}
enum class Chains(
    val chainName: String,
    val chainNamespace: String,
    val chainReference: String,
    @DrawableRes val icon: Int,
    val color: String,
    val methods: List<String>,
    val events: List<String>,
    val order: Int,
    val chainId: String = "$chainNamespace:$chainReference"
) {

    CROSS_MAIN(
        chainName = "Cross Mainnet",
        chainNamespace = Info.Eth.chain,
        chainReference = "612055",
        icon = R.drawable.ic_cross,
        color = "#ff20e2bb",
        methods = Info.Eth.defaultMethods,
        events = Info.Eth.defaultEvents,
        order = 1
    ),
    BSC_MAIN(
        chainName = "BNB Smart Chain (Mainnet)",
        chainNamespace = Info.Eth.chain,
        chainReference = "56",
        icon = R.drawable.bnb,
        color = "#F3BA2F",
        methods = Info.Eth.defaultMethods,
        events = Info.Eth.defaultEvents,
        order = 2
    ),
    /*ETHEREUM_MAIN(
        chainName = "Ethereum",
        chainNamespace = Info.Eth.chain,
        chainReference = "1",
        icon = R.drawable.ic_ethereum,
        color = "#617de8",
        methods = Info.Eth.defaultMethods,
        events = Info.Eth.defaultEvents,
        order = 3
    ),*/
    KAIA_MAINNET (
        chainName = "Kaia Mainnet",
        chainNamespace = Info.Eth.chain,
        chainReference = "8217",
        icon = R.drawable.kaia,
        color = "#4E35FF",
        methods = Info.Eth.defaultMethods,
        events = Info.Eth.defaultEvents,
        order = 4
    ),

    CROSS_ZONEZERO(
        chainName = "CROSS Testnet",
        chainNamespace = Info.Eth.chain,
        chainReference = "612044",
        icon = R.drawable.ic_cross,
        color = "#ff20e2bb",
        methods = Info.Eth.defaultMethods,
        events = Info.Eth.defaultEvents,
        order = 5
    ),

    /*ETHEREUM_SEPOLIA(
        chainName = "Ethereum Sepolia",
        chainNamespace = Info.Eth.chain,
        chainReference = "11155111",
        icon = R.drawable.ic_ethereum,
        color = "#617de8",
        methods = Info.Eth.defaultMethods,
        events = Info.Eth.defaultEvents,
        order = 6
    ),*/
    BSC_TESTNET(
        chainName = "BNB Smart Chain (Testnet)",
        chainNamespace = Info.Eth.chain,
        chainReference = "97",
        icon = R.drawable.bnb,
        color = "#F3BA2F",
        methods = Info.Eth.defaultMethods,
        events = Info.Eth.defaultEvents,
        order = 7
    ),
    KAIA_TESTNET (
        chainName = "Kaia Kairos",
        chainNamespace = Info.Eth.chain,
        chainReference = "1001",
        icon = R.drawable.kaia,
        color = "#4E35FF",
        methods = Info.Eth.defaultMethods,
        events = Info.Eth.defaultEvents,
        order = 8
    );

    sealed class Info {
        abstract val chain: String
        abstract val defaultEvents: List<String>
        abstract val defaultMethods: List<String>

        object Eth : Info() {
            override val chain = "eip155"
            override val defaultEvents: List<String> = listOf("chainChanged", "accountsChanged")
            override val defaultMethods: List<String> = listOf("eth_sendTransaction", "personal_sign", "eth_sign", "eth_signTypedData")
        }
    }
}
