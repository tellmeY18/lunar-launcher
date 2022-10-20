/*
 * Lunar Launcher
 * Copyright (C) 2022 Md Rasel Hossain
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package rasel.lunar.launcher.apps

import android.app.ActivityOptions
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Rect
import android.icu.text.SimpleDateFormat
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.widget.Toast
import androidx.fragment.app.FragmentActivity
import rasel.lunar.launcher.R
import rasel.lunar.launcher.helpers.UniUtils
import java.util.*


internal class AppMenuUtils(
    private val appMenus: AppMenus, private val fragmentActivity: FragmentActivity,
    private val context: Context, private val packageManager: PackageManager,
    private val packageName: String) {

    fun launchAsFreeform() {
        val freeformIntent = context.packageManager.getLaunchIntentForPackage(packageName)
        freeformIntent!!.addFlags(Intent.FLAG_ACTIVITY_LAUNCH_ADJACENT or
                    Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_MULTIPLE_TASK)
        val rect = Rect(
            0, UniUtils().screenHeight(fragmentActivity) / 2,
            UniUtils().screenWidth(fragmentActivity), UniUtils().screenHeight(fragmentActivity))
        var activityOptions = activityOptions
        activityOptions = activityOptions.setLaunchBounds(rect)
        context.startActivity(freeformIntent, activityOptions.toBundle())
        appMenus.dismiss()
    }

    fun openAppInfo() {
        val infoIntent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        infoIntent.data = Uri.parse("package:$packageName")
        infoIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(infoIntent)
        appMenus.dismiss()
    }

    fun openAppStore() {
        try {
            val storeIntent = Intent(Intent.ACTION_VIEW)
            storeIntent.data = Uri.parse("market://details?id=$packageName")
            storeIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(storeIntent)
        } catch (activityNotFoundException: ActivityNotFoundException) {
            Toast.makeText(context, context.getString(R.string.null_app_store_message), Toast.LENGTH_SHORT).show()
            activityNotFoundException.printStackTrace()
        }
        appMenus.dismiss()
    }

    fun uninstallApp() {
        val uninstallIntent = Intent(Intent.ACTION_DELETE)
        uninstallIntent.data = Uri.parse("package:$packageName")
        uninstallIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(uninstallIntent)
        appMenus.dismiss()
    }

    private val activityOptions: ActivityOptions
        get() {
            val activityOptions = ActivityOptions.makeBasic()
            val freeformStackId = 5
            try {
                val method =
                    ActivityOptions::class.java.getMethod("setLaunchWindowingMode", Int::class.javaPrimitiveType)
                method.invoke(activityOptions, freeformStackId)
            } catch (exception: Exception) {
                exception.printStackTrace()
            }
            return activityOptions
        }

    fun dateTimeFormat(long: Long) : String {
        val sdf = SimpleDateFormat.getDateTimeInstance()
        return sdf.format(Date(long))
    }

    fun permissionsForPackage() : String {
        val packageInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            packageManager.getPackageInfo(packageName, PackageManager.PackageInfoFlags.of(PackageManager.GET_PERMISSIONS.toLong()))
        } else {
            @Suppress("DEPRECATION") packageManager.getPackageInfo(packageName, PackageManager.GET_PERMISSIONS)
        }
        return if (packageInfo.requestedPermissions.isNotEmpty()) {
            val stringBuilder = StringBuilder()
            for (i in 0 until packageInfo.requestedPermissions.size) {
                if (i != packageInfo.requestedPermissions.size - 1)
                    stringBuilder.append("${packageInfo.requestedPermissions[i]}\n\n")
                else
                    stringBuilder.append(packageInfo.requestedPermissions[i])
            }
            stringBuilder.toString()
        } else {
            ""
        }
    }
}