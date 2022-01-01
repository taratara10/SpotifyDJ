package com.kabos.spotifydj.util

import com.google.android.material.textfield.TextInputLayout

fun TextInputLayout.setErrorMessageByBoolean(isValid: Boolean, message: String) {
    this.error = if (isValid) null else message
}