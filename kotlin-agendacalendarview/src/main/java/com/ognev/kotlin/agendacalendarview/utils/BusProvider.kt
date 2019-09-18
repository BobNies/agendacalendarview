package com.ognev.kotlin.agendacalendarview.utils

import rx.Observable
import rx.subjects.PublishSubject
import rx.subjects.SerializedSubject

class BusProvider {
    private val mBus = SerializedSubject<Any, Any>(PublishSubject.create())

    fun send(`object`: Any) {
        mBus.onNext(`object`)
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
