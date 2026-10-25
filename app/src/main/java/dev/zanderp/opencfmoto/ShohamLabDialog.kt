package dev.zanderp.opencfmoto

import android.content.Context
import androidx.appcompat.app.AlertDialog

/** Temporary diagnostic controls. Kept separate from production Setup. */
object ShohamLabDialog {
    fun show(context: Context) {
        val entries = arrayOf(
            "0x10450 / QUERY_TIME",
            "0x10600 / TIME_SYNC",
            "Block 0x10631 reply",
            "Block 0x10740 handling",
            "Keep QR profile (no CLIENT_INFO switch)",
            "Bluetooth clock listener",
            "Keep bike Wi-Fi",
            "Connection Isolation (tests 8–16)",
            "Reset all experiments"
        )
        fun summary(): Array<String> = arrayOf(
            "${entries[0]}: ${AppSettings.clockLabQuery(context).id}",
            "${entries[1]}: ${AppSettings.clockLabTimeSync(context).id}",
            "${entries[2]}: ${if (AppSettings.suppress10630(context)) "ON" else "OFF"}",
            "${entries[3]}: ${if (AppSettings.suppress10740(context)) "ON" else "OFF"}",
            "${entries[4]}: ${if (AppSettings.freezeQrProfile(context)) "ON" else "OFF"}",
            "${entries[5]}: ${if (AppSettings.bluetoothClockSync(context)) "ON" else "OFF"}",
            "${entries[6]}: ${if (AppSettings.keepWifiAfterDisconnect(context)) "ON" else "OFF"}",
            "${entries[7]}: ${if (AppSettings.isolationTest(context) == 0) "Normal" else "Test ${AppSettings.isolationTest(context)}"}",
            entries[8]
        )
        val dialog = AlertDialog.Builder(context)
            .setTitle("Shoham Lab — הגדרות ניסוי")
            .setItems(summary()) { _, index ->
                when (index) {
                    0 -> choose(context, "0x10450", arrayOf("Empty ack", "Carbit", "Zontes", "No reply")) {
                        AppSettings.setClockLabQuery(context, arrayOf(ClockQueryMode.EMPTY, ClockQueryMode.CARBIT, ClockQueryMode.ZONTES, ClockQueryMode.NO_ACK)[it])
                    }
                    1 -> choose(context, "0x10600", arrayOf("Echo bike", "Phone time", "No reply")) {
                        AppSettings.setClockLabTimeSync(context, arrayOf(ClockTimeSyncMode.ECHO, ClockTimeSyncMode.PHONE, ClockTimeSyncMode.NO_ACK)[it])
                    }
                    2 -> { AppSettings.setSuppress10630(context, !AppSettings.suppress10630(context)); show(context) }
                    3 -> { AppSettings.setSuppress10740(context, !AppSettings.suppress10740(context)); show(context) }
                    4 -> { AppSettings.setFreezeQrProfile(context, !AppSettings.freezeQrProfile(context)); show(context) }
                    5 -> { AppSettings.setBluetoothClockSync(context, !AppSettings.bluetoothClockSync(context)); show(context) }
                    6 -> { AppSettings.setKeepWifiAfterDisconnect(context, !AppSettings.keepWifiAfterDisconnect(context)); show(context) }
                    7 -> choose(context, "Connection Isolation — choose one test", arrayOf(
                        "Normal (no isolation)",
                        "8 — AA + Wi-Fi only; no PXC connection",
                        "9 — PXC + media control; no video frames",
                        "10 — Delay first video frames by 15 seconds",
                        "11 — Video ON; suppress optional 10630/10740 replies",
                        "12 — Normal projection; timestamped baseline",
                        "13 — RLY 113; zero video frames",
                        "14 — Send one video frame only",
                        "15 — Send ten video frames only",
                        "16 — Normal video; suppress optional control after first frame"
                    )) {
                        val test = if (it == 0) 0 else it + 7
                        AppSettings.setIsolationTest(context, test)
                        LogBus.log("[SHOHAM-ISOLATION] selected test=$test (reconnect required)")
                    }
                    8 -> AlertDialog.Builder(context).setTitle("Reset Shoham Lab?")
                        .setMessage("Restore the baseline experimental settings?")
                        .setPositiveButton("Reset") { _, _ ->
                            AppSettings.setIsolationTest(context, 0)
                            AppSettings.setClockLabQuery(context, ClockQueryMode.EMPTY)
                            AppSettings.setClockLabTimeSync(context, ClockTimeSyncMode.ECHO)
                            AppSettings.setSuppress10630(context, false)
                            AppSettings.setSuppress10740(context, false)
                            AppSettings.setFreezeQrProfile(context, false)
                            AppSettings.setBluetoothClockSync(context, false)
                            AppSettings.setKeepWifiAfterDisconnect(context, false)
                            show(context)
                        }.setNegativeButton("Cancel") { _, _ -> show(context) }.show()
                }
            }.setNegativeButton("Close", null).create()
        dialog.show()
    }
    private fun choose(context: Context, title: String, options: Array<String>, update: (Int) -> Unit) {
        AlertDialog.Builder(context).setTitle(title).setItems(options) { _, which ->
            update(which)
            show(context)
        }.setNegativeButton("Cancel") { _, _ -> show(context) }.show()
    }
}
