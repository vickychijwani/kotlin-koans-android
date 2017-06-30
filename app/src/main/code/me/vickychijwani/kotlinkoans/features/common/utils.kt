package me.vickychijwani.kotlinkoans.features.common

import android.content.Context
import android.graphics.drawable.Drawable
import android.os.Build
import android.support.annotation.DimenRes
import android.support.annotation.Dimension
import android.support.annotation.StyleRes
import android.support.v4.widget.TextViewCompat
import android.support.v7.widget.AppCompatTextView
import android.text.Html
import android.text.Spanned
import android.text.method.LinkMovementMethod
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.tsengvn.typekit.Typekit
import me.vickychijwani.kotlinkoans.R

fun makeTextView(ctx: Context, text: String, isHtml: Boolean = false, @StyleRes textAppearance: Int? = null,
                 paddingStart: Int = 0, paddingEnd: Int = 0, paddingTop: Int = 0, paddingBottom: Int = 0,
                 fontFamily: String = "sans-serif", drawableLeft: Drawable? = null,
                 wrap: Boolean = true, bold: Boolean = false, textAllCaps: Boolean = false)
        : TextView {
    val tv = AppCompatTextView(ctx)
    val finalText = if (textAllCaps) text.toUpperCase() else text
    tv.text = if (!isHtml) finalText else textToHtml(finalText).trim()
    tv.setPadding(paddingStart, paddingTop, paddingEnd, paddingBottom)

    textAppearance?.let { TextViewCompat.setTextAppearance(tv, textAppearance) }
    val (typekitStyle, lineSpacingMult) = when {
        fontFamily == "monospace" && bold   -> FontStyle("monospace-bold", 1f)
        fontFamily == "monospace"           -> FontStyle("monospace", 1f)
        fontFamily == "sans-serif" && bold  -> FontStyle(Typekit.Style.Bold.toString(), 0.9f)
        else                                -> FontStyle(Typekit.Style.Normal.toString(), 0.9f)
    }
    tv.typeface = Typekit.getInstance().get(typekitStyle)
    tv.setLineSpacing(0f, lineSpacingMult)

    drawableLeft?.let {
        tv.setCompoundDrawablesWithIntrinsicBounds(drawableLeft, null, null, null)
        tv.compoundDrawablePadding = getOffsetDimen(ctx, R.dimen.padding_inline)
    }
    tv.setHorizontallyScrolling(!wrap)
    tv.setTextIsSelectable(true)
    if (isHtml) {
        tv.movementMethod = LinkMovementMethod.getInstance()
    }
    return tv
}

fun makeVerticalSpacer(ctx: Context, @Dimension height: Int): View {
    val v = View(ctx)
    v.layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, height)
    return v
}

fun getOffsetDimen(ctx: Context, @DimenRes dimen: Int): Int {
    return ctx.resources?.getDimensionPixelOffset(dimen) ?: 0
}

fun getSizeDimen(ctx: Context, @DimenRes dimen: Int): Int {
    return ctx.resources?.getDimensionPixelSize(dimen) ?: 0
}

fun colorToHex(c: Int) = String.format("#%06X", 0xFFFFFF and c)

fun textToHtml(text: String): Spanned {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        Html.fromHtml(text, Html.FROM_HTML_MODE_LEGACY)
    } else {
        Html.fromHtml(text)
    }
}

data class FontStyle(val typekitStyle: String, val lineSpacingMult: Float)
