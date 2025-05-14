package io.crosstoken.appkit.ui.components.button.views

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.navigation.findNavController
import io.crosstoken.appkit.R
import io.crosstoken.appkit.ui.components.button.ConnectButton
import io.crosstoken.appkit.ui.components.button.rememberAppKitState
import io.crosstoken.appkit.utils.toConnectButtonSize

class ConnectButton @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    init {
        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.ConnectButton, 0, 0)
        val connectButtonSize = typedArray.getInteger(R.styleable.ConnectButton_connect_button_size, 0).toConnectButtonSize()
        typedArray.recycle()

        LayoutInflater.from(context)
            .inflate(R.layout.view_button, this, true)
            .findViewById<ComposeView>(R.id.root)
            .apply {
                setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
                setContent {
                    val appKitState = rememberAppKitState(navController = findNavController())
                    ConnectButton(state = appKitState, buttonSize = connectButtonSize)
                }
            }
    }
}
