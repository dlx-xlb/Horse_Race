/*
 * Copyright (C) 2020 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.HorseRacing

import android.content.ContentValues
import android.content.Context
import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import com.example.Database.DBHelper
import com.example.Database.FeedReaderContract.FeedEntry.BET_NUMBER
import com.example.Database.FeedReaderContract.FeedEntry.REWARD
import com.example.Database.FeedReaderContract.FeedEntry.TABLE_NAME
import com.example.Database.FeedReaderContract.FeedEntry.TOTALREWARD
import com.example.Database.FeedReaderContract.FeedEntry.WAGER
import com.example.Database.FeedReaderContract.FeedEntry.WEIGHT_HORSE1
import com.example.Database.FeedReaderContract.FeedEntry.WEIGHT_HORSE2
import com.example.Database.FeedReaderContract.FeedEntry.WEIGHT_HORSE3
import com.example.Database.FeedReaderContract.FeedEntry.WEIGHT_HORSE4
import com.example.Database.FeedReaderContract.FeedEntry.WINNER_NUMBER
import com.example.HorseRacing.databinding.ActivityMainBinding
import com.example.Network.currencyAPI
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.Main
import kotlin.math.roundToInt


/**
 * Activity that displays a tip calculator.
 */
class MainActivity : AppCompatActivity() {

    // Binding object instance with access to the views in the activity_main.xml layout
    private lateinit var binding: ActivityMainBinding

    private val PROGRESS_MAX = 100
    private val PROGRESS_START = 0
    private var wagerNTD: Int = 0
    private var totalWagerNTD: Int = 10000
    private var horse1 = Horse(1, 2.0, Job())
    private var horse2 = Horse(2, 2.0, Job())
    private var horse3 = Horse(3, 2.0, Job())
    private var horse4 = Horse(4, 2.0, Job())
    private val dbHelper = DBHelper(this)
    private val historyFragment = HistroyFragment(dbHelper)


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Inflate the layout XML file and return a binding object instance
        binding = ActivityMainBinding.inflate(layoutInflater)

        // Set the content view of the Activity to be the root view of the layout
        setContentView(binding.root)

        initProgressBar()
        // Setup a click listener on the calculate button to calculate the tip
        binding.raceButton.setOnClickListener { startRace() }
        binding.history.setOnClickListener { getHistory()}
        // Set up a key listener on the EditText field to listen for "enter" button presses
        binding.wagerEditText.setOnKeyListener { view, keyCode, _ ->
            handleKeyEvent(
                view,
                keyCode
            )
        }

        binding.totalWager.text = getString(R.string.total, totalWagerNTD.toString())
        updateViewData()
    }



    private fun initProgressBar(){
        binding.horse1.max = PROGRESS_MAX
        binding.horse2.max = PROGRESS_MAX
        binding.horse3.max = PROGRESS_MAX
        binding.horse4.max = PROGRESS_MAX
        binding.horse1.progress = PROGRESS_START
        binding.horse2.progress = PROGRESS_START
        binding.horse3.progress = PROGRESS_START
        binding.horse4.progress = PROGRESS_START
    }

    private fun startRace(){
        horse1.job = Job()
        horse2.job = Job()
        horse3.job = Job()
        horse4.job = Job()
        binding.horse1.startRun(horse1, (20..50).random())
        binding.horse2.startRun(horse2, (20..50).random())
        binding.horse3.startRun(horse3, (20..50).random())
        binding.horse4.startRun(horse4, (20..50).random())
    }



    private fun getHistory(){
        val db = dbHelper.readableDatabase

        val cursor = db.query(
            TABLE_NAME,
            null,
            null,
            null,
            null,
            null,
            null
        )

        if(cursor.count != historyFragment.historyList.size){
            //there is revision of data. Adding it to list
            historyFragment.setDataToList()
        }

        historyFragment.show(this.supportFragmentManager, HistroyFragment.TAG)
    }

    private fun exchangeCurrencyRate(){
        val scope = CoroutineScope(Job()+ Main)
        scope.launch {
            val stringInTextField = binding.wagerEditText.text.toString()
            var cost = stringInTextField.toDoubleOrNull()
            if (cost == null || cost == 0.0) {
                cost = 0.0
            }

            if(cost<0.0 || cost>10.0){
                binding.convertToNTD.text = "下注金額請勿超過10美元!"
                return@launch
            }

            val currencyJSON = currencyAPI.retrofitService.getCurrencyJSON(
                "560426a32344139ba9ad6d627cae0984a247da17",
                "USD",
                "TWD",
                cost.toString(),
                "json")

            if(currencyJSON.rates.twd.rateForAmount.toDouble() > totalWagerNTD){
                binding.convertToNTD.text = "下注金額超過總金額!"
                return@launch
            }
            wagerNTD = currencyJSON.rates.twd.rateForAmount.toDouble().roundToInt()
            binding.convertToNTD.text = getString(R.string.exchangeNTD, currencyJSON.rates.twd.rateForAmount)
        }
    }

    /**
     * Calculates the tip based on the user input.
     */
    private fun ProgressBar.startRun(horse: Horse, delaySecond: Int) {
        CoroutineScope(Main + horse.job).launch {
            for(i in PROGRESS_START..PROGRESS_MAX){
                delay(delaySecond.toLong())
                if(i == PROGRESS_MAX){
                    when(horse.number){
                        1 -> {
                            horse2.job.cancel()
                            horse3.job.cancel()
                            horse4.job.cancel()
                            updateWeight(1,2,3,4)
                        }

                        2-> {
                            horse1.job.cancel()
                            horse3.job.cancel()
                            horse4.job.cancel()
                            updateWeight(2,1,3,4)
                        }

                        3-> {
                            horse1.job.cancel()
                            horse2.job.cancel()
                            horse4.job.cancel()
                            updateWeight(3,1,2,4)
                        }

                        4-> {
                            horse1.job.cancel()
                            horse2.job.cancel()
                            horse3.job.cancel()
                            updateWeight(4,1,2,3)
                        }
                    }
                    calculateTotalWager(horse)
                    updateViewData()
                }
                this@startRun.progress = i
            }
        }
    }

    private fun updateWeight(winner:Int, loser1:Int, loser2:Int, loser3:Int){
        when(winner){
            1->{
                horse1.resetWeight(0.1)
                horse2.resetWeight(-0.1)
                horse3.resetWeight(-0.1)
                horse4.resetWeight(-0.1)
            }
            2->{
                horse1.resetWeight(-0.1)
                horse2.resetWeight(0.1)
                horse3.resetWeight(-0.1)
                horse4.resetWeight(-0.1)
            }
            3->{
                horse1.resetWeight(-0.1)
                horse2.resetWeight(-0.1)
                horse3.resetWeight(0.1)
                horse4.resetWeight(-0.1)
            }
            4->{
                horse1.resetWeight(-0.1)
                horse2.resetWeight(-0.1)
                horse3.resetWeight(-0.1)
                horse4.resetWeight(0.1)
            }
        }
    }

    private fun calculateTotalWager(winner: Horse){
        val betNumber = binding.horseOptions.indexOfChild(findViewById(binding.horseOptions.checkedRadioButtonId))+1
        if( betNumber == winner.number){
            //Player wins !
            val reward = (wagerNTD * (winner.weight)).roundToInt()
            totalWagerNTD = (totalWagerNTD - wagerNTD + reward)
            addToDatabase(wagerNTD, betNumber, winner.number, reward, totalWagerNTD)
        }else{
            //Player loses !
            val reward =0
            totalWagerNTD -= wagerNTD
            addToDatabase(wagerNTD, betNumber, winner.number, reward, totalWagerNTD)
        }

    }


    private fun updateViewData(){
        val db = dbHelper.readableDatabase
        val projection = arrayOf(TOTALREWARD,
                              WEIGHT_HORSE1,
                              WEIGHT_HORSE2,
                              WEIGHT_HORSE3,
                              WEIGHT_HORSE4)

        val cursor = db.query(
            TABLE_NAME,
            projection,
            null,
            null,
            null,
            null,
            null
        )

        if(cursor.moveToLast()){
            //get total reward
            var i = cursor.getColumnIndex(TOTALREWARD)
            var temp = cursor.getString(i)
            binding.totalWager.text = getString(R.string.total, temp)
            totalWagerNTD = temp.toInt()
            //get horse1 weight
            i =cursor.getColumnIndex(WEIGHT_HORSE1)
            temp = cursor.getString(i)
            binding.firstHorseOption.text = getString(R.string.first_horse, temp)
            horse1.weight = temp.toDouble()
            //get horse2 weight
            i =cursor.getColumnIndex(WEIGHT_HORSE2)
            temp = cursor.getString(i)
            binding.secondHorseOption.text = getString(R.string.second_horse, temp)
            horse2.weight = temp.toDouble()
            //get horse3 weight
            i =cursor.getColumnIndex(WEIGHT_HORSE3)
            temp = cursor.getString(i)
            binding.thirdHorseOption.text = getString(R.string.third_horse, temp)
            horse3.weight = temp.toDouble()
            //get horse4 weight
            i =cursor.getColumnIndex(WEIGHT_HORSE4)
            temp = cursor.getString(i)
            binding.fourthHorseOption.text = getString(R.string.fourth_horse, temp)
            horse4.weight = temp.toDouble()
        }else{
            //there is no data existing yet!
            binding.totalWager.text = getString(R.string.total, totalWagerNTD.toString())
            binding.firstHorseOption.text = getString(R.string.first_horse, horse1.weight.toString())
            binding.secondHorseOption.text = getString(R.string.second_horse, horse2.weight.toString())
            binding.thirdHorseOption.text = getString(R.string.third_horse, horse3.weight.toString())
            binding.fourthHorseOption.text = getString(R.string.fourth_horse, horse4.weight.toString())
        }
        cursor.close()
    }



    fun addToDatabase(wager:Int,
                      betNumber:Int,
                      winnerNumber:Int,
                      reward:Int,
                      totalReward:Int) {
            val db = dbHelper.writableDatabase

            val values = ContentValues().apply {
                put(WAGER, wager.toString())
                put(BET_NUMBER, betNumber.toString())
                put(WINNER_NUMBER, winnerNumber.toString())
                put(REWARD, reward.toString())
                put(TOTALREWARD, totalReward.toString())
                put(WEIGHT_HORSE1, horse1.weight.toString())
                put(WEIGHT_HORSE2, horse2.weight.toString())
                put(WEIGHT_HORSE3, horse3.weight.toString())
                put(WEIGHT_HORSE4, horse4.weight.toString())
            }
            db?.insert(TABLE_NAME, null, values)
    }

    /**
     * Key listener for hiding the keyboard when the "Enter" button is tapped.
     */
    private fun handleKeyEvent(view: View, keyCode: Int): Boolean {
        if (keyCode == KeyEvent.KEYCODE_ENTER) {
            // Hide the keyboard
            val inputMethodManager =
                getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
            // Show the exchanged NTD
            exchangeCurrencyRate()
            return true
        }
        return false
    }
}