package io.crosstoken.sign.engine

import io.crosstoken.sign.engine.model.EngineDO
import java.util.concurrent.ConcurrentLinkedQueue

internal val sessionRequestEventsQueue = ConcurrentLinkedQueue<EngineDO.SessionRequestEvent>()