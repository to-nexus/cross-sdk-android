package io.crosstoken.modal.ui.components.logo

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Row
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.crosstoken.modal.ui.ComponentPreview
import io.crosstoken.modal.ui.components.common.HorizontalSpacer
import io.crosstoken.modalcore.R

@Composable
fun CrossLogo(modifier: Modifier = Modifier, color: Color) {
    Row(
        modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            imageVector = ImageVector.vectorResource(id = R.drawable.ic_cross_logo),
            contentDescription = "CrossLogo",
            colorFilter = ColorFilter.tint(color)
        )
        HorizontalSpacer(width = 4.dp)
        Text(
            text = "Cross",
            style = TextStyle(
                color = color,
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold
            )
        )
    }
}

@Preview
@Composable
private fun PreviewCrossLogo() {
    ComponentPreview {
        CrossLogo(color = Color.Blue)
    }
}