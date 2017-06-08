package me.vickychijwani.kotlinkoans

import android.os.Bundle
import android.support.design.widget.NavigationView
import android.support.design.widget.Snackbar
import android.support.v4.view.GravityCompat
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import com.google.gson.GsonBuilder
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.app_bar_main.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory



class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

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

        val gson = GsonBuilder().create()
        val retrofit = Retrofit.Builder()
                .baseUrl("https://try.kotlinlang.org/")
                .client(KotlinKoansApplication.getInstance().getOkHttpClient())
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build()
        val api = retrofit.create(KotlinKoansApiService::class.java)
        api.getKoan("/Kotlin%20Koans/Introduction/Hello,%20world!").enqueue(object : Callback<Koan> {
            override fun onResponse(call: Call<Koan>, response: Response<Koan>) {
                if (response.isSuccessful) {
                    Log.e("TRY KOTLIN", response.body().files[0].text)
                } else {
                    Log.e("TRY KOTLIN", "ERROR 2")
                }
            }

            override fun onFailure(call: Call<Koan>, t: Throwable) {
                Log.e("TRY KOTLIN", Log.getStackTraceString(t))
            }
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
        // Handle navigation view item clicks here.
        val id = item.itemId

        drawer_layout.closeDrawer(GravityCompat.START)
        return true
    }
}
