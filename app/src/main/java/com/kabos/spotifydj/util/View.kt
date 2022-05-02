package com.kabos.spotifydj.util

import android.view.View

fun View.setInvalidAppearance(isInvalid: Boolean) {
    // 表示するViewをクリックできないことを示すため、透明度を50%にする
    val transparency = if (isInvalid) 0.5F else 1F
    this.transitionAlpha = transparency
    this.isClickable = !isInvalid
}
