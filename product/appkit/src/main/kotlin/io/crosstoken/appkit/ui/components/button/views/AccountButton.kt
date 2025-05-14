package io.crosstoken.appkit.ui.components.button.views

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.navigation.findNavController
import io.crosstoken.appkit.R
import io.crosstoken.appkit.ui.components.button.AccountButton
import io.crosstoken.appkit.ui.components.button.rememberAppKitState
import io.crosstoken.appkit.utils.toAccountButtonType

class AccountButton @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    init {
        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.AccountButton, 0, 0)
        val accountButtonType = typedArray.getInteger(R.styleable.AccountButton_account_button_type, 0).toAccountButtonType()
        typedArray.recycle()

        LayoutInflater.from(context)
            .inflate(R.layout.view_button, this, true)
            .findViewById<ComposeView>(R.id.root)
            .apply {
                setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
                setContent {
                    val appKitState = rememberAppKitState(navController = findNavController())
                    AccountButton(
                        state = appKitState,
                        accountButtonType = accountButtonType
                    )
                }
            }
    }
}