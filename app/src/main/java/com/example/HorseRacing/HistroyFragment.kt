package com.example.HorseRacing

import android.app.Activity
import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.provider.BaseColumns
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.example.Database.DBHelper
import com.example.Database.FeedReaderContract
import com.example.Database.FeedReaderContract.FeedEntry.BET_NUMBER
import com.example.Database.FeedReaderContract.FeedEntry.REWARD
import com.example.Database.FeedReaderContract.FeedEntry.TOTALREWARD
import com.example.Database.FeedReaderContract.FeedEntry.WAGER
import com.example.Database.FeedReaderContract.FeedEntry.WINNER_NUMBER
import kotlinx.coroutines.NonCancellable.cancel
import kotlinx.coroutines.NonCancellable.start

class HistroyFragment(val dbHelper:DBHelper): DialogFragment() {

    var historyList = mutableListOf<String>()

    var index:Int = 0
    fun setDataToList(){
        historyList.clear()
        val db = dbHelper.readableDatabase
        val projection = arrayOf(
            BaseColumns._ID,
            FeedReaderContract.FeedEntry.WAGER,
            FeedReaderContract.FeedEntry.BET_NUMBER,
            FeedReaderContract.FeedEntry.WINNER_NUMBER,
            FeedReaderContract.FeedEntry.REWARD,
            FeedReaderContract.FeedEntry.TOTALREWARD
        )
        val cursor = db.query(
            FeedReaderContract.FeedEntry.TABLE_NAME,
            projection,
            null,
            null,
            null,
            null,
            null
        )

        with(cursor) {
            var data:String
            var id:String
            var wager:String
            var betNumber:String
            var winnerNumber:String
            var reward:String
            var totalReward:String

            while (moveToNext()) {

                id = getString(getColumnIndexOrThrow(BaseColumns._ID))
                wager = getString(getColumnIndexOrThrow(WAGER))
                betNumber = getString(getColumnIndexOrThrow(BET_NUMBER))
                winnerNumber = getString(getColumnIndexOrThrow(WINNER_NUMBER))
                reward = getString(getColumnIndexOrThrow(REWARD))
                totalReward = getString(getColumnIndexOrThrow(TOTALREWARD))
                data = "場次: "+ id + " 下注: " + wager + " 號碼: " + betNumber + " 冠軍: " + winnerNumber +
                        "\n獎金: " + reward + " 總金額: " + totalReward + "\n"
                historyList.add(data)
            }
            cursor.close()
        }
    }


    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        //getDataToList()
        val array = historyList.toTypedArray()
        return activity?.let {
            // Use the Builder class for convenient dialog construction
            val builder = AlertDialog.Builder(it)
            builder.setTitle("請直接點擊紀錄以刪除")
                .setItems(array,
                    DialogInterface.OnClickListener { dialog, id ->
                        val deleteString = array[id]
                        val deleteID = deleteString.split(" ")[1]
                        val db = dbHelper.writableDatabase
                        val selection = "${BaseColumns._ID} LIKE ?"
                        val selectionArgs = arrayOf(deleteID)
                        db.delete(FeedReaderContract.FeedEntry.TABLE_NAME, selection, selectionArgs)
                        this.dismiss()
                    }
                )

            // Create the AlertDialog object and return it
            builder.create()
        } ?: throw IllegalStateException("Activity cannot be null")
    }

    companion object {
        const val TAG = "HistoryFragmentDialog"
    }
}
