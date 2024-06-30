package com.lcl.lclmeasurementtool.datastore

import javax.inject.Qualifier

@Qualifier
@Retention(AnnotationRetention.RUNTIME)
annotation class Dispatcher(val dispatcher: LCLDispatchers)

enum class LCLDispatchers {
    IO
}

