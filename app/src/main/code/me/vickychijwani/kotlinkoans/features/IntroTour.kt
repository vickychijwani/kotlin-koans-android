package me.vickychijwani.kotlinkoans.features

import android.app.Activity
import android.graphics.Rect
import android.support.design.widget.FloatingActionButton
import android.support.design.widget.TabLayout
import android.support.v7.widget.Toolbar
import android.view.View
import android.view.ViewGroup
import com.getkeepsafe.taptargetview.TapTarget
import com.getkeepsafe.taptargetview.TapTargetSequence
import com.tsengvn.typekit.Typekit
import me.vickychijwani.kotlinkoans.R
import me.vickychijwani.kotlinkoans.util.dp
import me.vickychijwani.kotlinkoans.util.getScreenHeight
import me.vickychijwani.kotlinkoans.util.getScreenWidth


internal typealias TapTargetCallback = (index: Int) -> Unit

class IntroTour(val ctx: Activity, val toolbar: Toolbar, val tabbar: TabLayout,
                val runBtn: FloatingActionButton, val finishCallback: () -> Unit) {

    private lateinit var sequence: TapTargetSequence

    init {
        initialize()
    }

    private fun initialize() {
        val tourSteps = mutableListOf<TapTarget>()
        val screenWidth = getScreenWidth(ctx)
        val screenHeight = getScreenHeight(ctx)

        val step1Title = ctx.getString(R.string.tour_step1_title)
        val step1Desc  = ctx.getString(R.string.tour_step1_desc)
        val step2Title = ctx.getString(R.string.tour_step2_title)
        val step2Desc  = ctx.getString(R.string.tour_step2_desc)
        val step3Title = ctx.getString(R.string.tour_step3_title)
        val step3Desc  = ctx.getString(R.string.tour_step3_desc)
        val step4Title = ctx.getString(R.string.tour_step4_title)
        val step4Desc  = ctx.getString(R.string.tour_step4_desc)
        val step5Title = ctx.getString(R.string.tour_step5_title)
        val step5Desc  = ctx.getString(R.string.tour_step5_desc)
        val step6Title = ctx.getString(R.string.tour_step6_title)
        val step6Desc  = ctx.getString(R.string.tour_step6_desc)

        val descriptionTab: View? = (tabbar.getChildAt(0) as? ViewGroup)?.getChildAt(0)
        val codeEditorTab: View? = (tabbar.getChildAt(0) as? ViewGroup)?.getChildAt(1)
        descriptionTab?.let {
            val tourDescription = TapTarget.forView(descriptionTab, step1Title, step1Desc)
                    .transparentTarget(true)
                    .targetRadius(46)
            tourSteps.add(tourDescription)
        }

        codeEditorTab?.let {
            val tourCodeEditor = TapTarget.forView(codeEditorTab, step2Title, step2Desc)
                    .transparentTarget(true)
            tourSteps.add(tourCodeEditor)
        }

        val tourRunBtn = TapTarget.forView(runBtn, step3Title, step3Desc)
                .transparentTarget(true)
        tourSteps.add(tourRunBtn)

        val runStatusBounds = Rect(160.dp, screenHeight-36.dp, 208.dp, screenHeight+4.dp)
        val tourRunStatus = TapTarget.forBounds(runStatusBounds, step4Title, step4Desc)
                .transparentTarget(true)
                .targetRadius(64)
        tourSteps.add(tourRunStatus)

        val helpBounds = Rect(screenWidth-68.dp, 8.dp, screenWidth-12.dp, 64.dp)
        val tourHelp = TapTarget.forBounds(helpBounds, step5Title, step5Desc)
                .transparentTarget(true)
                .targetRadius(60)
        tourSteps.add(tourHelp)

        val tourNavigation = TapTarget.forToolbarNavigationIcon(toolbar, step6Title, step6Desc)
                .transparentTarget(false)
        tourSteps.add(tourNavigation)

        // set custom fonts
        tourSteps.forEach { t ->
            t.textTypeface(Typekit.getInstance().get(Typekit.Style.Normal))
                    .outerCircleColor(R.color.tour_lime)
                    .titleTextColor(R.color.text_primary)
                    .titleTextSize(18)
                    .descriptionTextColor(R.color.text_primary)
                    .descriptionTextSize(15)
                    .outerCircleAlpha(1f)
        }

        sequence = TapTargetSequence(ctx)
                .targets(tourSteps)
                .continueOnCancel(true)
                .considerOuterCircleCanceled(true)
                .listener(object : TapTargetSequence.Listener {
                    override fun onSequenceCanceled(lastTarget: TapTarget?) {}
                    override fun onSequenceStep(lastTarget: TapTarget?, targetClicked: Boolean) {}
                    override fun onSequenceFinish() {
                        finishCallback()
                    }
                })
    }

    fun startTour() {
        sequence.start()
    }

}
