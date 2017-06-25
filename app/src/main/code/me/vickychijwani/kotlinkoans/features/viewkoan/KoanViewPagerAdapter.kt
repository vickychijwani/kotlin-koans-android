package me.vickychijwani.kotlinkoans.features.viewkoan

import android.content.Context
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import me.vickychijwani.kotlinkoans.R
import me.vickychijwani.kotlinkoans.data.Koan


class KoanViewPagerAdapter(ctx: Context, val fm: FragmentManager, var koan: Koan) : FragmentPagerAdapter(fm) {

    private val TAB_DESCRIPTION: String = ctx.getString(R.string.koan)

    // FragmentPagerAdapter#instantiateItem() calls getItem() to create a new instance
    override fun getItem(position: Int): Fragment {
        if (position == 0) {
            return KoanDescriptionFragment.newInstance()
        } else {
            return KoanCodeFragment.newInstance(position-1)
        }
    }

    override fun getPageTitle(position: Int): CharSequence {
        if (position == 0) {
            return TAB_DESCRIPTION
        } else {
            return koan.files[position-1].name
        }
    }

    override fun getCount(): Int {
        if (koan == Koan.EMPTY) {
            return 0
        }
        return 1 + koan.files.size
    }

    fun getKoanToRun(): Koan {
        val fileToRun = fm.fragments
                .mapNotNull { (it as? KoanCodeFragment)?.getUserCode() }
                .first()
        return koan.copy(files = koan.files.map { if (it.id == fileToRun.id) fileToRun else it })
    }

    fun getPositionFromPageTitle(pageTitle: String): Int {
        val validPositions = 0..count - 1
        return validPositions.first { getPageTitle(it) == pageTitle }
    }

}
