package io.crosstoken.android.pulse.domain

import io.crosstoken.android.internal.common.di.AndroidCommonDITags
import io.crosstoken.android.internal.common.scope
import io.crosstoken.android.internal.common.wcKoinApp
import io.crosstoken.android.internal.utils.currentTimeInSeconds
import io.crosstoken.android.pulse.data.PulseService
import io.crosstoken.android.pulse.model.Event
import io.crosstoken.android.pulse.model.SDKType
import io.crosstoken.android.pulse.model.properties.Props
import io.crosstoken.foundation.util.Logger
import io.crosstoken.util.generateId
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope
import org.koin.core.qualifier.named

class SendEventUseCase(
    private val pulseService: PulseService,
    private val logger: Logger,
    private val bundleId: String,
) : SendEventInterface {
    private val enableW3MAnalytics: Boolean by lazy { wcKoinApp.koin.get(named(AndroidCommonDITags.ENABLE_WEB_3_MODAL_ANALYTICS)) }

    override fun send(props: Props, sdkType: SDKType, timestamp: Long?, id: Long?) {
        if (enableW3MAnalytics) {
            scope.launch {
                supervisorScope {
                    try {
                        val event = Event(props = props, bundleId = bundleId, timestamp = timestamp ?: currentTimeInSeconds, eventId = id ?: generateId())
                        val response = pulseService.sendEvent(body = event, sdkType = sdkType.type)
                        if (!response.isSuccessful) {
                            logger.error("Failed to send event: ${event.props.type}")
                        } else {
                            logger.log("Event sent successfully: ${event.props.type}")
                        }
                    } catch (e: Exception) {
                        logger.error("Failed to send event: ${props.type}, error: $e")
                    }
                }
            }
        }
    }
}

interface SendEventInterface {
    fun send(props: Props, sdkType: SDKType = SDKType.APPKIT, timestamp: Long? = currentTimeInSeconds, id: Long? = generateId())
}