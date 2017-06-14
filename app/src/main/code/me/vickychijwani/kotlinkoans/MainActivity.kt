package me.vickychijwani.kotlinkoans

import android.animation.Animator
import android.arch.lifecycle.LifecycleRegistry
import android.arch.lifecycle.LifecycleRegistryOwner
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.os.Handler
import android.support.annotation.IdRes
import android.support.design.widget.BottomSheetBehavior
import android.support.design.widget.NavigationView
import android.support.v4.content.ContextCompat
import android.support.v4.graphics.drawable.DrawableCompat
import android.support.v4.view.GravityCompat
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_main.*
import me.vickychijwani.kotlinkoans.analytics.Analytics
import me.vickychijwani.kotlinkoans.features.IntroTour
import me.vickychijwani.kotlinkoans.features.common.HorizontalScrollView
import me.vickychijwani.kotlinkoans.features.common.RunResultsView
import me.vickychijwani.kotlinkoans.features.common.getOffsetDimen
import me.vickychijwani.kotlinkoans.features.common.makeTextView
import me.vickychijwani.kotlinkoans.features.listkoans.ListKoansViewModel
import me.vickychijwani.kotlinkoans.features.viewkoan.KoanViewModel
import me.vickychijwani.kotlinkoans.features.viewkoan.KoanViewPagerAdapter
import me.vickychijwani.kotlinkoans.util.*
import java.util.*


class MainActivity : AppCompatActivity(),
        LifecycleRegistryOwner,
        NavigationView.OnNavigationItemSelectedListener {

    @IdRes private val STARTING_MENU_ITEM_ID = 1
    private val mMenuItemIdToKoan = mutableMapOf<Int, KoanMetadata>()
    private val mKoanIdToMenuItemId = mutableMapOf<String, Int>()
    private val mKoanIds = mutableListOf<String>()

    private val APP_STATE_LAST_VIEWED_KOAN = "state:last-viewed-koan"
    private var mSelectedKoanId: String? = null
    private var mDisplayedKoan: Koan? = null

    private var mListKoansObserver: Observer<KoanFolders>? = null
    private var mViewKoanObserver: Observer<Koan>? = null

    // NOTE: must keep a strong reference to this because the preference manager does not currently
    // store a reference to it
    private var appStateChangeListener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
        when (key) {
            // don't update koan code because it's already updated, as the user themselves typed it
            //KoanRepository.APP_STATE_CODE ->
            //    ViewModelProviders.of(this).get(KoanViewModel::class.java).update()
            KoanRepository.APP_STATE_LAST_RUN_STATUS ->
                ViewModelProviders.of(this).get(ListKoansViewModel::class.java).update()
        }
    }

    // FIXME official workaround until Lifecycle component is integrated with support library
    // FIXME see note: https://developer.android.com/topic/libraries/architecture/lifecycle.html#lco
    private val lifecycleRegistry = LifecycleRegistry(this)
    override fun getLifecycle(): LifecycleRegistry {
        return lifecycleRegistry
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        run_btn.setOnClickListener {
            run_btn.isEnabled = false
            run_btn.setImageDrawable(null)
            run_progress.show()
            (view_pager.adapter as KoanViewPagerAdapter).updateUserCode()
        }
        run_status_msg.setOnClickListener {
            BottomSheetBehavior.from(run_status).toggleState()
        }

        Prefs.with(this).registerOnSharedPreferenceChangeListener(appStateChangeListener)
        val toggle = ActionBarDrawerToggle(this, drawer_layout, toolbar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close)
        drawer_layout.addDrawerListener(toggle)
        toggle.syncState()

        view_pager.adapter = KoanViewPagerAdapter(this, supportFragmentManager, Koan.EMPTY)
        view_pager.offscreenPageLimit = 10
        tabbar.setupWithViewPager(view_pager)

        // NOTE: Don't create the observer again if it exists. This is important to ensure we
        // don't add multiple observers for the same Activity. Quoting LiveData#observe() docs:
        // "If the given owner, observer tuple is already in the list, the call is ignored."
        mListKoansObserver = mListKoansObserver ?: Observer { folders ->
            if (folders == null) {
                return@Observer
            }
            populateIndex(nav_view.menu, folders)
            if (mSelectedKoanId == null) {
                val lastViewedKoanId: String? = Prefs.with(this)
                        .getString(APP_STATE_LAST_VIEWED_KOAN, mMenuItemIdToKoan[STARTING_MENU_ITEM_ID]?.id)
                lastViewedKoanId?.let { loadKoan(lastViewedKoanId) }
            }
        }
        ViewModelProviders.of(this).get(ListKoansViewModel::class.java)
                .getFolders().observe(this, mListKoansObserver)

        // NOTE: Don't create the observer again if it exists. This is important to ensure we
        // don't add multiple observers for the same Activity. Quoting LiveData#observe() docs:
        // "If the given owner, observer tuple is already in the list, the call is ignored."
        mViewKoanObserver = mViewKoanObserver ?: Observer { koan ->
            showKoan(koan!!)
        }
        ViewModelProviders.of(this).get(KoanViewModel::class.java).liveData
                .observe(this, mViewKoanObserver)

        nav_view.setNavigationItemSelectedListener(this)
    }

    override fun onStop() {
        super.onStop()
        saveSelectedKoanId()
    }

    override fun onDestroy() {
        super.onDestroy()
        Prefs.with(this).unregisterOnSharedPreferenceChangeListener(appStateChangeListener)
    }

    override fun onBackPressed() {
        val bottomSheet = BottomSheetBehavior.from(run_status)
        if (drawer_layout.isDrawerOpen(GravityCompat.START)) {
            drawer_layout.closeDrawer(GravityCompat.START)
        } else if (bottomSheet.isExpanded()) {
            bottomSheet.collapse()
        } else {
            super.onBackPressed()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val KOTLIN_DOCS_URL = "http://kotlinlang.org/docs/reference/"
        return when (item.itemId) {
            R.id.action_next        -> { loadNextKoan(); true }
            R.id.action_show_answer -> { showAnswer(); true }
            R.id.action_revert      -> { revertCode(); true }
            R.id.action_docs        -> { browse(this, KOTLIN_DOCS_URL); true }
            R.id.action_settings    -> true
            else                    -> super.onOptionsItemSelected(item)
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        val koanMetadata = mMenuItemIdToKoan[id]!!
        loadKoan(koanMetadata)
        drawer_layout.closeDrawer(GravityCompat.START)
        Analytics.logKoanSelectedFromList(koanMetadata)
        return true
    }

    fun switchToFile(fileName: String) {
        val tabPosition = (view_pager.adapter as KoanViewPagerAdapter)
                .getPositionFromPageTitle(fileName)
        if (tabPosition >= 0 && tabPosition < view_pager.adapter.count) {
            view_pager.setCurrentItem(tabPosition, true)
        }
    }

    private fun bindRunKoan() {
        val showResults = this::showRunResults
        val adapter = (view_pager.adapter as KoanViewPagerAdapter)
        adapter.getUserCodeObservables().forEach { observable ->
            observable.deleteObservers()  // there should be only 1 observer
            observable.addObserver(Observer { _, fileToRun ->
                if (fileToRun == null || fileToRun !is KoanFile) {
                    observable.deleteObservers()  // we expect no more updates
                    return@Observer
                }
                val koanToRun = getKoanToRun(fileToRun, adapter.koan)
                KoanRepository.runKoan(koanToRun, showResults)
            })
        }
    }

    private fun getKoanToRun(fileToRun: KoanFile, koan: Koan): Koan {
        val filesToRun = koan.files.map { if (it.id == fileToRun.id) fileToRun else it }
        return koan.copy(files = filesToRun)
    }

    private fun showRunResults(results: KoanRunResults) {
        run_progress.invisible()
        run_btn.setImageResource(R.drawable.play)
        run_btn.isEnabled = true

        val runStatus = results.getStatus()
        run_status_msg.text = runStatus.uiLabel
        run_status_msg.setTextColor(runStatus.toColor(this))
        run_status_msg.setCompoundDrawablesWithIntrinsicBounds(runStatus.toFilledIcon(this), null, null, null)
        run_status_details.removeAllViews()
        run_status_details.addView(RunResultsView(this, results))
        BottomSheetBehavior.from(run_status).expand()
    }

    private fun resetRunResults(koan: Koan) {
        val COLOR_STATUS_NONE = ContextCompat.getColor(this, R.color.status_none)
        if (koan.lastRunStatus != null) {
            run_status_msg.text = "${getString(R.string.last_run_status)}: ${koan.lastRunStatus.uiLabel}"
            val icon = koan.lastRunStatus.toFilledIcon(this).mutate()
            DrawableCompat.setTint(icon, COLOR_STATUS_NONE)
            run_status_msg.setCompoundDrawablesWithIntrinsicBounds(icon, null, null, null)
        } else {
            run_status_msg.text = getString(R.string.status_none)
            run_status_msg.setCompoundDrawablesWithIntrinsicBounds(R.drawable.status_none, 0, 0, 0)
        }
        run_status_msg.setTextColor(COLOR_STATUS_NONE)
        run_status_details.removeAllViews()
        run_status_details.addView(RunResultsView(this))
        BottomSheetBehavior.from(run_status).collapse()
    }

    private fun saveSelectedKoanId() {
        mSelectedKoanId?.let {
            Prefs.with(this).edit().putString(APP_STATE_LAST_VIEWED_KOAN, mSelectedKoanId).apply()
        }
    }

    private fun loadKoan(koanMetadata: KoanMetadata) {
        this.title = koanMetadata.name  // show the title immediately
        loadKoan(koanMetadata.id)
    }

    private fun loadKoan(koanId: String) {
        background_progress.show()
        if (mSelectedKoanId == koanId) return
        val viewKoanVM = ViewModelProviders.of(this).get(KoanViewModel::class.java)
        viewKoanVM.loadKoan(koanId)
        val menuItemId = mKoanIdToMenuItemId[koanId]
        menuItemId?.let { nav_view.setCheckedItem(menuItemId) }
        mSelectedKoanId = koanId
    }

    private fun loadNextKoan() {
        mSelectedKoanId?.let {
            val nextIndex = mKoanIds.indexOf(it)+1
            if (nextIndex < mKoanIds.size) {
                loadKoan(mKoanIds[nextIndex])
                Analytics.logNextKoanBtnClicked()
            } else {
                Toast.makeText(this, R.string.no_koans_left, Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun showKoan(koan: Koan) {
        if (mDisplayedKoan == null) {
            full_overlay.animate().withLayer()
                    .alpha(0f)
                    .setListener(object : Animator.AnimatorListener {
                        override fun onAnimationEnd(animation: Animator?) {
                            full_overlay.visibility = View.GONE
                        }
                        override fun onAnimationCancel(animation: Animator?) {}
                        override fun onAnimationStart(animation: Animator?) {}
                        override fun onAnimationRepeat(animation: Animator?) {}
                    })
                    .start()
            Handler(mainLooper).postDelayed({
                IntroTour(this, toolbar, tabbar, run_btn).startTour()
            }, 100)
        }
        info { "Koan selected: ${koan.name}" }
        background_progress.hide()
        this.title = koan.name
        (view_pager.adapter as KoanViewPagerAdapter).koan = koan
        view_pager.adapter.notifyDataSetChanged()
        resetRunResults(koan)
        bindRunKoan()
        saveSelectedKoanId()
        mDisplayedKoan = koan
        Analytics.logKoanViewed(koan)
    }

    private fun showAnswer() {
        val koan = mDisplayedKoan ?: return
        val solutions = koan.getModifiableFile().solutions
        if (solutions == null || solutions.isEmpty()) {
            return
        }
        val padding = getOffsetDimen(this, R.dimen.padding_large)
        val scrollView = HorizontalScrollView(this)
        scrollView.isHorizontalFadingEdgeEnabled = true
        val solution = solutions[0]
        val textView = makeTextView(this, solution, textAppearance = R.style.TextAppearance_Small,
                fontFamily = "monospace", paddingStart = padding, paddingEnd = padding,
                paddingTop = padding, paddingBottom = padding)
        textView.setTextIsSelectable(true)
        scrollView.addView(textView)
        AlertDialog.Builder(this)
                .setTitle(R.string.answer)
                .setView(scrollView)
                .setPositiveButton(R.string.code_copy, { _, _ ->
                    val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    clipboard.primaryClip = ClipData.newPlainText(
                            getString(R.string.code_copy_clipboard_label), solution)
                    Toast.makeText(this, R.string.code_copied, Toast.LENGTH_SHORT).show()
                })
                .setNegativeButton(android.R.string.cancel, { dialog, _ -> dialog.dismiss() })
                .create().show()
        Analytics.logAnswerShown(koan)
    }

    private fun revertCode() {
        AlertDialog.Builder(this)
                .setTitle(R.string.revert_prompt_title)
                .setMessage(R.string.revert_prompt_message)
                .setPositiveButton(R.string.revert, { _, _ ->
                    mDisplayedKoan?.let { Analytics.logCodeReverted(it) }
                    ViewModelProviders.of(this).get(KoanViewModel::class.java)
                            .update(deleteSavedData = true)
                })
                .setNegativeButton(android.R.string.cancel, { dialog, _ -> dialog.dismiss() })
                .create().show()
        mDisplayedKoan?.let { Analytics.logRevertCodeClicked(it) }
    }

    private fun populateIndex(menu: Menu, folders: KoanFolders) {
        val NO_STATUS_ICON = ContextCompat.getDrawable(this, R.drawable.status_none)
        menu.clear()
        mMenuItemIdToKoan.clear()
        mKoanIdToMenuItemId.clear()
        mKoanIds.clear()
        @IdRes var menuItemId = STARTING_MENU_ITEM_ID
        for (folder in folders) {
            val submenu = menu.addSubMenu(folder.name)
            // doesn't work
            // submenu.item.icon = folder.getRunStatus()?.toFilledIcon(this) ?: NO_STATUS_ICON
            for (koan in folder.koans) {
                val item = submenu.add(Menu.NONE, menuItemId, Menu.NONE, koan.name)
                item.icon = koan.lastRunStatus?.toFilledIcon(this) ?: NO_STATUS_ICON
                item.isCheckable = true
                mMenuItemIdToKoan[menuItemId] = koan
                mKoanIdToMenuItemId[koan.id] = menuItemId
                mKoanIds.add(koan.id)
                ++menuItemId
            }
        }
    }

}
