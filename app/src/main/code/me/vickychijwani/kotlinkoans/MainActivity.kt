package me.vickychijwani.kotlinkoans

import android.arch.lifecycle.LifecycleRegistry
import android.arch.lifecycle.LifecycleRegistryOwner
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
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
import kotlinx.android.synthetic.main.app_bar_main.*
import me.vickychijwani.kotlinkoans.features.listkoans.ListKoansViewModel
import me.vickychijwani.kotlinkoans.features.viewkoan.ViewKoanViewModel


class MainActivity : AppCompatActivity(),
        LifecycleRegistryOwner,
        NavigationView.OnNavigationItemSelectedListener {

    @IdRes private val STARTING_MENU_ITEM_ID = 1
    private val mMenuItemIdToKoan = mutableMapOf<Int, KoanMetadata>()
    private val mKoanIdToMenuItemId = mutableMapOf<String, Int>()

    private lateinit var viewKoanVM: ViewKoanViewModel

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

        fab.setOnClickListener { view ->
            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show()
        }

        val toggle = ActionBarDrawerToggle(this, drawer_layout, toolbar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close)
        drawer_layout.addDrawerListener(toggle)
        toggle.syncState()

        val listKoansVM = ViewModelProviders.of(this).get(ListKoansViewModel::class.java)
        listKoansVM.getFolders().observe(this, Observer { folders ->
            if (folders == null) {
                return@Observer
            }
            populateIndex(nav_view.menu, folders)
            // selecting the first koan for now
            // TODO save the selected koan id in prefs on every item change, then load it here and select the corresponding id
            nav_view.setCheckedItem(STARTING_MENU_ITEM_ID)
            val koanMetadata = mMenuItemIdToKoan[STARTING_MENU_ITEM_ID]!!
            loadKoan(koanMetadata)
        })

        viewKoanVM = ViewModelProviders.of(this).get(ViewKoanViewModel::class.java)
        viewKoanVM.liveData.observe(this, Observer { koan ->
            showKoan(koan!!)
        })

        nav_view.setNavigationItemSelectedListener(this)
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

    private fun loadKoan(koanMetadata: KoanMetadata) {
        this.title = koanMetadata.name  // show the title immediately
        viewKoanVM.loadKoan(koanMetadata.id)
    }

    private fun showKoan(koan: Koan) {
        Log.i("TAG", "Koan selected: ${koan.name}")
        this.title = koan.name
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
