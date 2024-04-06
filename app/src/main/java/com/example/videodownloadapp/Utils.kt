package com.example.videodownloadapp


import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.widget.Toast
import com.google.android.material.dialog.MaterialAlertDialogBuilder


//Storage Permission
fun appSettingOpen(context: Context){
    Toast.makeText(
        context,
        "Go to Setting and Enable All Permission",
        Toast.LENGTH_LONG
    ).show()

    val settingIntent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
    settingIntent.data = Uri.parse("package:${context.packageName}")
    context.startActivity(settingIntent)
}

fun warningPermissionDialog(context: Context,listener : DialogInterface.OnClickListener){
    MaterialAlertDialogBuilder(context)
        .setMessage("All Permission are Required for this app")
        .setCancelable(false)
        .setPositiveButton("Ok",listener)
        .create()
        .show()
}


//Check Internet
fun isConnected(context: Context):Boolean{
    val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    return run {
        val activeNetwork = connectivityManager.activeNetwork ?: return false
        val cap = connectivityManager.getNetworkCapabilities(activeNetwork) ?: return false
        when {
            cap.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
            cap.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
            else -> false
        }
    }
}