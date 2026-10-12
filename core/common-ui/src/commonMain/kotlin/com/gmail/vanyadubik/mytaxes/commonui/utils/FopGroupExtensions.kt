package com.gmail.vanyadubik.mytaxes.commonui.utils

import androidx.compose.runtime.Composable
import com.gmail.vanyadubik.mytaxes.domen.model.FopGroup
import com.gmail.vanyadubik.mytaxes.resource.Res
import com.gmail.vanyadubik.mytaxes.resource.fop_group_1
import com.gmail.vanyadubik.mytaxes.resource.fop_group_2
import com.gmail.vanyadubik.mytaxes.resource.fop_group_3_3
import com.gmail.vanyadubik.mytaxes.resource.fop_group_3_5
import org.jetbrains.compose.resources.stringResource

@Composable
fun FopGroup.title(): String = stringResource(
    when (this) {
        FopGroup.GROUP_1 -> Res.string.fop_group_1
        FopGroup.GROUP_2 -> Res.string.fop_group_2
        FopGroup.GROUP_3_5 -> Res.string.fop_group_3_5
        FopGroup.GROUP_3_3 -> Res.string.fop_group_3_3
    }
)
