package com.kabos.spotifydj.util

import androidx.lifecycle.MutableLiveData

fun <T>MutableLiveData<List<T>>.addItem(item: T) {
    val list = this.value ?: emptyList()
    this.value = list + listOf(item)
}

fun <T>MutableLiveData<List<T>>.removeAt(position: Int): T? {
    val mutableList = this.value?.toMutableList() ?: mutableListOf()
    var removeItem: T? = null
    if (mutableList.size >= position) {
        removeItem = mutableList[position]
        mutableList.removeAt(position)
        this.value = mutableList
    }
    return removeItem
}

fun <T>MutableLiveData<List<T>>.replacePosition(initial: Int, final: Int) {
    val mutableList = this.value?.toMutableList() ?: mutableListOf()
    if (mutableList.size >= initial && mutableList.size >= final) {
        val initialItem = mutableList.removeAt(initial)
        mutableList.add(final, initialItem)
        this.value = mutableList
    }
}
