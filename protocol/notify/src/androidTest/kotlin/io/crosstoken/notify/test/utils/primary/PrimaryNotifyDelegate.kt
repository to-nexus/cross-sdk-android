package io.crosstoken.notify.test.utils.primary

import io.crosstoken.notify.client.Notify
import io.crosstoken.notify.client.NotifyInterface
import io.crosstoken.notify.test.utils.globalOnError

open class PrimaryNotifyDelegate : NotifyInterface.Delegate {

    override fun onNotifyNotification(notifyNotification: Notify.Event.Notification) {
    }

    override fun onSubscriptionsChanged(subscriptionsChanged: Notify.Event.SubscriptionsChanged) {
    }

    override fun onError(error: Notify.Model.Error) {
        globalOnError(error)
    }
}