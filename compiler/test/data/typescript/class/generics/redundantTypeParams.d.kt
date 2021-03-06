@file:Suppress("INTERFACE_WITH_SUPERCLASS", "OVERRIDING_FINAL_MEMBER", "RETURN_TYPE_MISMATCH_ON_OVERRIDE", "CONFLICTING_OVERLOADS", "EXTERNAL_DELEGATION")

import kotlin.js.*
import kotlin.js.Json
import org.khronos.webgl.*
import org.w3c.dom.*
import org.w3c.dom.events.*
import org.w3c.dom.parsing.*
import org.w3c.dom.svg.*
import org.w3c.dom.url.*
import org.w3c.fetch.*
import org.w3c.files.*
import org.w3c.notifications.*
import org.w3c.performance.*
import org.w3c.workers.*
import org.w3c.xhr.*

external interface MyPromiseLike<T>

external interface MyPromise<T>

external interface MyPromiseConstructor {
    fun <T> ping(a: T)
    fun <T> pong(): T
    fun <Y, Z> bang(condition: (y: Y) -> Boolean): () -> Z
    fun all(values: dynamic /* JsTuple<dynamic, dynamic> | JsTuple<dynamic, dynamic, dynamic> */): MyPromise<dynamic /* JsTuple<T1, T2> | JsTuple<T1, T2, T3> */>
}