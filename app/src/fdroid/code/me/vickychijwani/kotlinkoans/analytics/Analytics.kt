package me.vickychijwani.kotlinkoans.analytics

import android.content.Context
import android.os.Bundle
import me.vickychijwani.kotlinkoans.data.Koan
import me.vickychijwani.kotlinkoans.data.KoanMetadata
import me.vickychijwani.kotlinkoans.data.RunStatus

object Analytics {

    //lateinit var mFirebase: FirebaseAnalytics

    fun initialize(ctx: Context) {
        //mFirebase = FirebaseAnalytics.getInstance(ctx.applicationContext)
    }

    fun logKoanViewed(koan: Koan) {
        log(Event("koan_viewed")
                .with("koan", koan.name)
                .with("koan_id", koan.id))
    }

    fun logRunStatus(koan: Koan, status: RunStatus) {
        log(Event("run_status")
                .with("koan", koan.name)
                .with("koan_id", koan.id)
                .with("status", status.uiLabel)     // status.uiLabel could change over time
                .with("status_id", status.id))              // status.id will never change
        if (status == RunStatus.OK) {
            log(Event("passed")
                    .with("koan", koan.name)
                    .with("koan_id", koan.id))
        }
    }

    fun logNextKoanBtnClicked() {
        log(Event("next_koan_btn_clicked"))
    }

    fun logKoanSelectedFromList(koanMetadata: KoanMetadata) {
        log(Event("koan_selected_from_list")
                .with("koan", koanMetadata.name)
                .with("koan_id", koanMetadata.id))
    }

    fun logAnswerShown(koan: Koan) {
        log(Event("answer_shown")
                .with("koan", koan.name)
                .with("koan_id", koan.id))
    }

    fun logRevertCodeClicked(koan: Koan) {
        log(Event("revert_code_clicked")
                .with("koan", koan.name)
                .with("koan_id", koan.id))
    }

    fun logCodeReverted(koan: Koan) {
        log(Event("code_reverted")
                .with("koan", koan.name)
                .with("koan_id", koan.id))
    }


    // private methods
    private fun log(event: Event) {
        //mFirebase.logEvent(event.eventName, event.bundle)
    }


    // private Bundle extension methods for a fluent API
    private class Event(val eventName: String) {
        internal val bundle = Bundle()
        fun with(key: String, value: Boolean): Event {
            bundle.putBoolean(key, value)
            return this
        }
        fun with(key: String, value: Int): Event {
            bundle.putInt(key, value)
            return this
        }
        fun with(key: String, value: Float): Event {
            bundle.putFloat(key, value)
            return this
        }
        fun with(key: String, value: String): Event {
            bundle.putString(key, value)
            return this
        }
    }

}
