package com.example.HorseRacing

import kotlinx.coroutines.CompletableJob

class Horse (var number: Int, var weight: Double, var job: CompletableJob){
    fun resetWeight(add: Double){
        if(add>0){
            //+0.1
            if(weight>=5.0){return}
        }else{
            //-0.1
            if(weight<=2.0){return}
        }
        weight += add
        weight = String.format("%.1f", weight).toDouble()
    }
}