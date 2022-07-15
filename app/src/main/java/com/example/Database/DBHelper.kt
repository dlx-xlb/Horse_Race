package com.example.Database

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.provider.BaseColumns

object FeedReaderContract {
    object FeedEntry: BaseColumns{
        val TABLE_NAME = "Race"
        val WAGER = "wager" //賭注金額
        val BET_NUMBER = "betnumber" //賭幾號?
        val WINNER_NUMBER = "winner" //幾號贏?
        val REWARD = "reward" //獎金
        val TOTALREWARD = "totalreward" //總獎金

        //各馬賠率
        val WEIGHT_HORSE1 = "weight1"
        val WEIGHT_HORSE2 = "weight2"
        val WEIGHT_HORSE3 = "weight3"
        val WEIGHT_HORSE4 = "weight4"
    }
}


class DBHelper(context:Context)
    : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION){

    private val SQL_CREATE_ENTRIES =
        "CREATE TABLE ${FeedReaderContract.FeedEntry.TABLE_NAME} (" +
                "${BaseColumns._ID} INTEGER PRIMARY KEY," +
                "${FeedReaderContract.FeedEntry.WAGER} TEXT," +
                "${FeedReaderContract.FeedEntry.BET_NUMBER} TEXT," +
                "${FeedReaderContract.FeedEntry.WINNER_NUMBER} TEXT," +
                "${FeedReaderContract.FeedEntry.REWARD} TEXT," +
                "${FeedReaderContract.FeedEntry.TOTALREWARD} TEXT," +
                "${FeedReaderContract.FeedEntry.WEIGHT_HORSE1} TEXT," +
                "${FeedReaderContract.FeedEntry.WEIGHT_HORSE2} TEXT," +
                "${FeedReaderContract.FeedEntry.WEIGHT_HORSE3} TEXT," +
                "${FeedReaderContract.FeedEntry.WEIGHT_HORSE4} TEXT)"

    private val SQL_DELETE_ENTRIES = "DROP TABLE IF EXISTS ${FeedReaderContract.FeedEntry.TABLE_NAME}"


    // below is the method for creating a database by a sqlite query
    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(SQL_CREATE_ENTRIES)
    }

    override fun onUpgrade(db: SQLiteDatabase, p1: Int, p2: Int) {
        // this method is to check if table already exists
        db.execSQL(SQL_DELETE_ENTRIES)
        onCreate(db)
    }

    companion object {
        // If you change the database schema, you must increment the database version.
        const val DATABASE_VERSION = 1
        const val DATABASE_NAME = "DBHelper.db"
    }
}