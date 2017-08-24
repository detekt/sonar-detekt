package kotlin.a

import kotlin.b.BClassOne
import kotlin.b.BClassTwo
import kotlin.b.BObjectOne

/**
 * Does something very important with everything else
 */
class AClassOne {

	val classes: List<Any> = listOf(AClassTwo(), AClassThree(), BClassOne(), BClassTwo())
	val hash: Int = BObjectOne.hashCode()

	fun doSomething():Any = classes[hash % classes.size]

}