package com.kabos.spotifydj.util

import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import timber.log.Timber


class InfiniteScrollListener(
    private val adapter: ListAdapter<*, *>,
    private val callback: () -> Unit
) : RecyclerView.OnScrollListener() {
    override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
        super.onScrolled(recyclerView, dx, dy)

        // アダプターが保持しているアイテムの合計
        val itemCount = adapter.itemCount
        // 画面に表示されているアイテム数
        val childCount = recyclerView.childCount
        val manager = recyclerView.layoutManager as LinearLayoutManager
        // 画面に表示されている一番上のアイテムの位置
        val firstPosition = manager.findFirstVisibleItemPosition()
        // 以下の条件に当てはまれば一番下までスクロールされたと判断できる。
        if (itemCount == childCount + firstPosition) {
            callback()
        }
    }
}
