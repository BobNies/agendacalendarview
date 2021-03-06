package com.ognev.kotlin.agendacalendarview.utils

import android.util.Log
import rx.Observable
import rx.subjects.PublishSubject
import rx.subjects.SerializedSubject
import java.lang.Exception

class BusProvider {
    private val mBus = SerializedSubject<Any, Any>(PublishSubject.create())

    fun send(`object`: Any) {
        try {
            mBus.onNext(`object`)
        } catch (ex: Exception) {
            Log.e("BusPRovider", ex.toString())
        }

    }

    fun toObservable(): Observable<Any> {
        return mBus
    }

    companion object {

        var mInstance: BusProvider? = null

        val instance: BusProvider
            get() {
                if (mInstance == null) {
                    mInstance = BusProvider()
                }
                return mInstance!!
            }
    }

}
