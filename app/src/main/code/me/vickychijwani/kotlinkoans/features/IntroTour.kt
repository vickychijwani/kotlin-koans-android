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
import me.vickychijwani.kotlinkoans.R
import me.vickychijwani.kotlinkoans.util.Prefs
import me.vickychijwani.kotlinkoans.util.dp
import me.vickychijwani.kotlinkoans.util.getScreenHeight
import me.vickychijwani.kotlinkoans.util.getScreenWidth


internal typealias TapTargetCallback = (index: Int) -> Unit

class IntroTour(val ctx: Activity, val toolbar: Toolbar, val tabbar: TabLayout,
                val runBtn: FloatingActionButton) {

    private val APP_STATE_INTRO_SEEN = "state:intro-seen"

    val introSeen: Boolean
    private lateinit var sequence: TapTargetSequence

    init {
        introSeen = Prefs.with(ctx).getBoolean(APP_STATE_INTRO_SEEN, false)
        if (! introSeen) {
            initialize()
        }
    }

    private fun initialize() {
        val tourSteps = mutableListOf<TapTarget>()
        val screenWidth = getScreenWidth(ctx)
        val screenHeight = getScreenHeight(ctx)

        val descriptionTab: View? = (tabbar.getChildAt(0) as? ViewGroup)?.getChildAt(0)
        val codeEditorTab: View? = (tabbar.getChildAt(0) as? ViewGroup)?.getChildAt(1)
        descriptionTab?.let {
            val tourDescription = TapTarget.forView(descriptionTab,
                    "Hi there! (1/6)", "Kotlin Kōans is a series of small coding challenges for learning core Kotlin concepts quickly. The first challenge description is here.")
                    .transparentTarget(true)
                    .targetRadius(46)
                    .outerCircleColor(R.color.tour_amber)
                    .outerCircleAlpha(1f)
                    .titleTextColor(R.color.text_primary)
                    .descriptionTextColor(R.color.text_primary)
            tourSteps.add(tourDescription)
        }

        codeEditorTab?.let {
            val tourCodeEditor = TapTarget.forView(codeEditorTab,
                    "Type your code in this tab (2/6)", "The other tabs have tests and supporting code which can't be modified")
                    .transparentTarget(true)
                    .outerCircleColor(R.color.tour_green)
                    .outerCircleAlpha(1f)
            tourSteps.add(tourCodeEditor)
        }

        val tourRunBtn = TapTarget.forView(runBtn, "Run it (3/6)", "Tap here to run your Kotlin code at any time. The code is saved on each run.")
                .transparentTarget(true)
                .outerCircleColor(R.color.tour_indigo)
                .outerCircleAlpha(1f)
        tourSteps.add(tourRunBtn)

        val runStatusBounds = Rect(160.dp, screenHeight-36.dp, 208.dp, screenHeight+4.dp)
        val tourRunStatus = TapTarget.forBounds(runStatusBounds, "See your test results (4/6)", "When you solve a challenge, it'll be saved to your overall progress!")
                .transparentTarget(true)
                .targetRadius(64)
                .outerCircleColor(R.color.tour_blue_grey)
                .outerCircleAlpha(1f)
        tourSteps.add(tourRunStatus)

        val helpBounds = Rect(screenWidth-68.dp, 8.dp, screenWidth-12.dp, 64.dp)
        val tourHelp = TapTarget.forBounds(helpBounds, "If you get stuck… (5/6)", "Try starting afresh with the Revert Code option, or sneak a peek at the answer!")
                .transparentTarget(true)
                .targetRadius(60)
                .outerCircleColor(R.color.tour_blue)
                .outerCircleAlpha(1f)
        tourSteps.add(tourHelp)

        val tourNavigation = TapTarget.forToolbarNavigationIcon(toolbar, "Most importantly… have fun! (6/6)", "Tap here to see your progress")
                .outerCircleColor(R.color.tour_pink)
                .outerCircleAlpha(1f)
                .titleTextSize(18)
        tourSteps.add(tourNavigation)

        sequence = TapTargetSequence(ctx)
                .targets(tourSteps)
                .continueOnCancel(true)
                .considerOuterCircleCanceled(true)
                .listener(object : TapTargetSequence.Listener {
                    override fun onSequenceCanceled(lastTarget: TapTarget?) {}
                    override fun onSequenceStep(lastTarget: TapTarget?, targetClicked: Boolean) {}
                    override fun onSequenceFinish() {
                        Prefs.with(ctx).edit().putBoolean(APP_STATE_INTRO_SEEN, true).apply()
                    }
                })
    }

    fun startTour() {
        if (!introSeen) {
            sequence.start()
        }
    }

}
