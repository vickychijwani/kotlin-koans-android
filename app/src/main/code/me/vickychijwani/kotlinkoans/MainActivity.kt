package me.vickychijwani.kotlinkoans

import android.arch.lifecycle.LifecycleRegistry
import android.arch.lifecycle.LifecycleRegistryOwner
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.os.Bundle
import android.support.annotation.IdRes
import android.support.design.widget.NavigationView
import android.support.design.widget.Snackbar
import android.support.v4.view.GravityCompat
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import kotlinx.android.synthetic.main.activity_main.*
import me.vickychijwani.kotlinkoans.features.listkoans.ListKoansViewModel
import me.vickychijwani.kotlinkoans.features.viewkoan.KoanViewModel
import me.vickychijwani.kotlinkoans.features.viewkoan.KoanViewPagerAdapter
import java.util.*


class MainActivity : AppCompatActivity(),
        LifecycleRegistryOwner,
        NavigationView.OnNavigationItemSelectedListener {

    private val TAG = MainActivity::class.java.simpleName

    @IdRes private val STARTING_MENU_ITEM_ID = 1
    private val mMenuItemIdToKoan = mutableMapOf<Int, KoanMetadata>()
    private val mKoanIdToMenuItemId = mutableMapOf<String, Int>()

    private val APP_STATE_LAST_VIEWED_KOAN = "state:last-viewed-koan"
    private var mCurrentKoanId: String? = null

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

        run_btn.setOnClickListener { view ->
            (view_pager.adapter as KoanViewPagerAdapter).updateUserCode()
        }

        val toggle = ActionBarDrawerToggle(this, drawer_layout, toolbar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close)
        drawer_layout.addDrawerListener(toggle)
        toggle.syncState()

        view_pager.adapter = KoanViewPagerAdapter(this, supportFragmentManager, Koan.EMPTY)
        view_pager.offscreenPageLimit = 10
        tabbar.setupWithViewPager(view_pager)

        if (savedInstanceState == null) {
            val listKoansVM = ViewModelProviders.of(this).get(ListKoansViewModel::class.java)
            listKoansVM.getFolders().observe(this, Observer { folders ->
                if (folders == null) {
                    return@Observer
                }
                populateIndex(nav_view.menu, folders)
                // TODO save the selected koan id in prefs on every item change, then load it here and select the corresponding id
                val lastViewedKoanId: String? = getPreferences(Context.MODE_PRIVATE)
                        .getString(APP_STATE_LAST_VIEWED_KOAN, mMenuItemIdToKoan[STARTING_MENU_ITEM_ID]?.id)
                lastViewedKoanId?.let { loadKoan(lastViewedKoanId) }
            })

            val viewKoanVM = ViewModelProviders.of(this).get(KoanViewModel::class.java)
            viewKoanVM.liveData.observe(this, Observer { koan ->
                showKoan(koan!!)
            })
        }

        nav_view.setNavigationItemSelectedListener(this)
    }

    override fun onStop() {
        super.onStop()
        if (mCurrentKoanId != null) {
            val pref = getPreferences(Context.MODE_PRIVATE)
            pref.edit().putString(APP_STATE_LAST_VIEWED_KOAN, mCurrentKoanId).apply()
        }
    }

    override fun onBackPressed() {
        if (drawer_layout.isDrawerOpen(GravityCompat.START)) {
            drawer_layout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        val id = item.itemId

        if (id == R.id.action_settings) {
            return true
        }

        return super.onOptionsItemSelected(item)
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        val koanMetadata = mMenuItemIdToKoan[id]!!
        loadKoan(koanMetadata)
        drawer_layout.closeDrawer(GravityCompat.START)
        return true
    }

    private fun bindRunKoan() {
        val adapter = (view_pager.adapter as KoanViewPagerAdapter)
        adapter.getUserCodeObservables().forEach { observable ->
            observable.deleteObservers()  // there should be only 1 observer
            observable.addObserver(Observer { _, arg ->
                Log.d(TAG, arg.toString())
                if (arg == null || arg !is KoanFile) {
                    observable.deleteObservers()  // we expect no more updates
                    return@Observer
                }
                val fileToRun: KoanFile = arg
                val filesToRun = adapter.koan.files.map { f ->
                    if (f.id == fileToRun.id) {
                        return@map fileToRun
                    } else {
                        return@map f
                    }
                }
                val koanToRun = adapter.koan.copy(files = filesToRun)
                KoanRepository.runKoan(koanToRun) { results ->
                    if (results.testResults != null) {
                        Snackbar.make(run_btn, results.testResults.entries.first().value[0].status,
                                Snackbar.LENGTH_SHORT).show()
                    } else {
                        Snackbar.make(run_btn, "Syntax error", Snackbar.LENGTH_SHORT).show()
                    }
                }
            })
        }
    }

    private fun loadKoan(koanMetadata: KoanMetadata) {
        this.title = koanMetadata.name  // show the title immediately
        loadKoan(koanMetadata.id)
    }

    private fun loadKoan(koanId: String) {
        val viewKoanVM = ViewModelProviders.of(this).get(KoanViewModel::class.java)
        viewKoanVM.loadKoan(koanId)
        val menuItemId = mKoanIdToMenuItemId[koanId]
        menuItemId?.let { nav_view.setCheckedItem(menuItemId) }
        mCurrentKoanId = koanId
    }

    private fun showKoan(koan: Koan) {
        Log.i(TAG, "Koan selected: ${koan.name}")
        this.title = koan.name
        (view_pager.adapter as KoanViewPagerAdapter).koan = koan
        view_pager.adapter.notifyDataSetChanged()
        bindRunKoan()
    }

    private fun populateIndex(menu: Menu, folders: KoanFolders) {
        menu.clear()
        @IdRes var menuItemId = STARTING_MENU_ITEM_ID
        for (folder in folders) {
            val submenu = menu.addSubMenu(folder.name)
            for (koan in folder.koans) {
                val item = submenu.add(Menu.NONE, menuItemId, Menu.NONE, koan.name)
                item.isCheckable = true
                mMenuItemIdToKoan[menuItemId] = koan
                mKoanIdToMenuItemId[koan.id] = menuItemId
                ++menuItemId
            }
        }
    }

}
