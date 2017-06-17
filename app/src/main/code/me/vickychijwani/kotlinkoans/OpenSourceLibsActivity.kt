package me.vickychijwani.kotlinkoans

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import kotlinx.android.synthetic.main.activity_open_source.*


class OpenSourceLibsActivity : AppCompatActivity() {

    companion object {
        private val LIBRARIES = listOf(
                Library("TapTargetView", "KeepSafe", "https://github.com/KeepSafe/TapTargetView"),
                Library("CodeFlask", "Claudio Holanda", "https://github.com/kazzkiq/CodeFlask.js/"),

                Library("Retrofit", "Square Inc.", "http://square.github.io/retrofit/"),
                Library("Gson", "Google Inc.", "https://github.com/google/gson"),
                Library("OkHttp", "Square Inc.", "http://square.github.io/okhttp/"),

                Library("LeakCanary", "Square Inc.", "https://github.com/square/leakcanary"),
                Library("DebugDrawer", "Mantas Palaima", "https://github.com/palaima/DebugDrawer")
        ).sortedBy { it.name }
    }

    private var mLibsAdapter: LibsAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_open_source)

        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setTitle(R.string.open_source_libs)

        mLibsAdapter = LibsAdapter(this, LIBRARIES, View.OnClickListener { v ->
            val pos = libs_list.getChildLayoutPosition(v)
            if (pos == RecyclerView.NO_POSITION) return@OnClickListener
            val library = mLibsAdapter!!.getItem(pos)
            val intent = Intent(Intent.ACTION_VIEW)
            intent.data = Uri.parse(library.url)
            startActivity(intent)
        })
        libs_list.adapter = mLibsAdapter
        libs_list.layoutManager = LinearLayoutManager(this)
        libs_list.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL))
    }


    internal class LibsAdapter(ctx: Context, private val mLibraries: List<Library>,
                               private val mItemClickListener: View.OnClickListener)
        : RecyclerView.Adapter<LibsAdapter.LibraryViewHolder>() {

        private val mLayoutInflater: LayoutInflater =
                ctx.getSystemService(LAYOUT_INFLATER_SERVICE) as LayoutInflater

        init {
            setHasStableIds(true)
        }

        override fun getItemCount(): Int {
            return mLibraries.size
        }

        fun getItem(position: Int): Library {
            return mLibraries[position]
        }

        override fun getItemId(position: Int): Long {
            return getItem(position).name.hashCode().toLong()
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LibraryViewHolder {
            val view = mLayoutInflater.inflate(R.layout.open_source_list_item, parent, false)
            return LibraryViewHolder(view, mItemClickListener)
        }

        override fun onBindViewHolder(viewHolder: LibraryViewHolder, position: Int) {
            val library = getItem(position)
            viewHolder.name.text = library.name
            viewHolder.author.text = library.author
        }

        internal class LibraryViewHolder(view: View, clickListener: View.OnClickListener) : RecyclerView.ViewHolder(view) {
            var name: TextView = view.findViewById(R.id.lib_name) as TextView
            var author: TextView = view.findViewById(R.id.lib_author) as TextView
            init {
                view.setOnClickListener(clickListener)
            }
        }

    }

    internal class Library(val name: String, val author: String, val url: String)

}
