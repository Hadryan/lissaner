package com.daniel_araujo.lissaner.android

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri

/**
 * A collection of functions that create intents meant to go to another page.
 */
object ExternalIntentUtils {
    fun goToAppPlayStore(context: Context, id: String) {
        val uri: Uri = Uri.parse("market://details?id=$id")
        val goToMarket = Intent(Intent.ACTION_VIEW, uri)
        goToMarket.addFlags(
            Intent.FLAG_ACTIVITY_NO_HISTORY or
                    Intent.FLAG_ACTIVITY_NEW_DOCUMENT or
                    Intent.FLAG_ACTIVITY_MULTIPLE_TASK
        )

        try {
            context.startActivity(goToMarket)
        } catch (e: ActivityNotFoundException) {
            // Try web page.
            openWebPage(context, "https://play.google.com/store/apps/details?id=$id")
        }
    }

    fun openWebPage(context: Context, url: String) {
        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
    }
}