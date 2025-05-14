package io.crosstoken.android

object CoreClient : CoreInterface by CoreProtocol.instance {

    interface CoreDelegate : CoreInterface.Delegate

}