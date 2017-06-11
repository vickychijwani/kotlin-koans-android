package me.vickychijwani.kotlinkoans.features.common

import android.content.Context
import android.support.v4.content.ContextCompat
import android.widget.LinearLayout
import me.vickychijwani.kotlinkoans.KoanRunResults
import me.vickychijwani.kotlinkoans.R
import me.vickychijwani.kotlinkoans.getRunStatus
import me.vickychijwani.kotlinkoans.util.dp

class RunResultsView : LinearLayout {

    constructor(ctx: Context) : super(ctx) {
        addView(makeTextView(context, context.getString(R.string.results_none),
                textAppearance = R.style.TextAppearance_Dim))
    }

    constructor(ctx: Context, runResults: KoanRunResults) : super(ctx) {
        val paddingInline = getOffsetDimen(ctx, R.dimen.padding_inline)
        val errorIndent = 20.dp
        val colors = mapOf(
                "ok" to colorToHex(ContextCompat.getColor(ctx, R.color.status_ok)),
                "warning" to colorToHex(ContextCompat.getColor(ctx, R.color.status_warning)),
                "error" to colorToHex(ContextCompat.getColor(ctx, R.color.status_error))
        )
        val compileErrorFileIcon = ContextCompat.getDrawable(ctx, R.drawable.file)
        val compileWarningIcon = ContextCompat.getDrawable(ctx, R.drawable.status_warning_hollow)
        val compileErrorIcon = ContextCompat.getDrawable(ctx, R.drawable.status_error_hollow)
        val compileIcons = mapOf(
                "ERROR" to compileErrorIcon,
                "WARNING" to compileWarningIcon
        )

        orientation = LinearLayout.VERTICAL

        // compile errors
        for ((fileName, errors) in runResults.compileErrors) {
            if (errors.isEmpty()) continue
            addView(makeTextView(ctx, fileName,
                    textAppearance = R.style.TextAppearance, fontFamily = "monospace",
                    drawableLeft = compileErrorFileIcon))
            for (error in errors) {
                addView(makeTextView(ctx, error.toString(),
                        textAppearance = R.style.TextAppearance_Small, fontFamily = "monospace",
                        paddingStart = errorIndent, drawableLeft = compileIcons[error.severity]))
            }
        }

        // test results + runtime errors
        runResults.testResults?.let {
            // status of each test, grouped by class
            for ((className, testResults) in runResults.testResults) {
                addView(makeTextView(ctx, className,
                        textAppearance = R.style.TextAppearance, fontFamily = "monospace",
                        paddingBottom = paddingInline,
                        drawableLeft = testResults.getRunStatus().toHollowIcon(ctx)))
                for (test in testResults) {
                    addView(makeTextView(ctx, test.methodName,
                            textAppearance = R.style.TextAppearance_Small, fontFamily = "monospace",
                            paddingStart = errorIndent, paddingBottom = paddingInline,
                            drawableLeft = test.getRunStatus().toHollowIcon(ctx)))
                }
            }

            val allTestResults = runResults.testResults.values.flatten()
            // exceptions
            for (exceptionHtml in allTestResults.mapNotNull { it.exception?.toHtml(colors) }) {
                addView(makeTextView(ctx, exceptionHtml, isHtml = true,
                        textAppearance = R.style.TextAppearance_Small,
                        fontFamily = "monospace",
                        paddingBottom = paddingInline))
            }

            // comparison failures
            for (failureHtml in allTestResults.mapNotNull { it.comparisonFailure?.toHtml(colors) }) {
                addView(makeTextView(ctx, failureHtml, isHtml = true,
                        textAppearance = R.style.TextAppearance_Small,
                        fontFamily = "monospace",
                        paddingBottom = paddingInline))
            }
        }
    }

    override fun shouldDelayChildPressedState(): Boolean {
        return false
    }

}
