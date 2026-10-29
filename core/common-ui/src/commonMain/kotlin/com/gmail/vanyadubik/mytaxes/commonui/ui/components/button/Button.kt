package com.gmail.vanyadubik.myassistant.commonui.ui.components.button

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun AppButton(
    text: String,
    onClick: () -> Unit,
    enabled: Boolean = true,
    isOutlinedButton: Boolean = false,
    loadingMessage: String? = null,
    leftIcon: ImageVector? = null,
    leftIconSize: Dp = 24.dp,
    leftIconPadding: Dp = 8.dp,
    textAlign: TextAlign = TextAlign.Center,
    maxLines: Int = Int.MAX_VALUE,
    contentPadding: PaddingValues = ButtonDefaults.ContentPadding,
    colors: ButtonColors = if (isOutlinedButton) {
        ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.background,
            contentColor = MaterialTheme.colorScheme.primary,
        )
    } else {
        ButtonDefaults.buttonColors()
    },
    border: BorderStroke = if (isOutlinedButton) {
        BorderStroke(1.dp,MaterialTheme.colorScheme.primary)
    } else {
        BorderStroke(1.dp, colors.contentColor.copy(alpha = 0.2f))
    },
    modifier: Modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp),
) {
    if(!isOutlinedButton) {
        Button(
            onClick = { if (loadingMessage.isNullOrEmpty()) onClick() },
            modifier = modifier,
            enabled = enabled,
            contentPadding = contentPadding,
            shape = RoundedCornerShape(12),
            border = border,
            colors = colors,
        ) {
            ButtonContent(
                text = text,
                loadingMessage = loadingMessage,
                colors = colors,
                textAlign = textAlign,
                maxLines = maxLines,
                leftIcon = leftIcon,
                leftIconSize = leftIconSize,
                leftIconPadding = leftIconPadding,
            )
        }
    }else {
        OutlinedButton(
            onClick = { if (loadingMessage.isNullOrEmpty()) onClick() },
            modifier = modifier,
            enabled = enabled,
            shape = RoundedCornerShape(12),
            border = border,
            colors = colors,
            contentPadding = contentPadding
        ) {
            ButtonContent(
                text = text,
                loadingMessage = loadingMessage,
                colors = colors,
                textAlign = textAlign,
                maxLines = maxLines,
                leftIcon = leftIcon,
                leftIconSize = leftIconSize,
                leftIconPadding = leftIconPadding,
            )
        }
    }
}

@Composable
private fun ButtonContent(
    text: String,
    loadingMessage: String? = null,
    colors: ButtonColors = ButtonDefaults.buttonColors(),
    textAlign: TextAlign = TextAlign.Center,
    maxLines: Int = Int.MAX_VALUE,
    leftIcon: ImageVector? = null,
    leftIconSize: Dp = 24.dp,
    leftIconPadding: Dp = 8.dp,
){
    if (!loadingMessage.isNullOrEmpty()) {
        CircularProgressIndicator(
            modifier = Modifier.size(30.dp),
            color = colors.contentColor,   //MaterialTheme.colorScheme.onPrimary,
            strokeWidth = 2.dp
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            loadingMessage,
            color = colors.contentColor
//                color = MaterialTheme.colorScheme.onPrimary
        )
    } else {
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            val iconWidth = if (leftIcon != null) leftIconPadding + leftIconSize + leftIconPadding else 0.dp
            if(leftIcon != null) {
                Box(
                    modifier = Modifier
                        .padding(horizontal = leftIconPadding)
                ){
                    Icon(
                        imageVector = leftIcon,
                        contentDescription = text,
                        modifier = Modifier.size(leftIconSize)
                    )
                }
            }
            Text(
                text,
                maxLines = maxLines,
                color = colors.contentColor,
//                    color = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.weight(1f, fill = false)
                    .wrapContentWidth(Alignment.CenterHorizontally),
                textAlign = textAlign,
                overflow = TextOverflow.Ellipsis
            )
            if(textAlign == TextAlign.Center)
                Spacer(modifier = Modifier.width(iconWidth))
        }
    }
}