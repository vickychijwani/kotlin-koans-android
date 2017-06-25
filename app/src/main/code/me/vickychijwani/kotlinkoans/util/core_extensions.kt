package me.vickychijwani.kotlinkoans.util

import android.content.Context
import android.content.res.Resources
import android.util.TypedValue

fun <K, V> Map<K, V>.mergeReduce(other: Map<K, V>, reduce: (V, V) -> V = { a, b -> b }): Map<K, V> {
    val result = LinkedHashMap<K, V>(this.size + other.size)
    result.putAll(this)
    for ((key, value) in other) {
        result[key] = result[key]?.let { reduce(value, it) } ?: value
    }
    return result
}

fun <K, V> Map<K, V>.keyFor(value: V): K? {
    for ((k, v) in this) {
        if (v == value) {
            return k
        }
    }
    return null
}

val Int.dp: Int
    get() = (this * Resources.getSystem().displayMetrics.density).toInt()

fun Float.sp(context: Context)
        = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, this, context.resources.displayMetrics)
