package kotlin.a

import kotlin.b.BClassOne
import kotlin.b.BClassTwo
import kotlin.b.BObjectOne

/**
 * Does something very important with everything else
 */
class AClassOne {

	private val classes: List<Any> = listOf(AClassTwo(), AClassThree(), BClassOne(), BClassTwo())
	private val hash: Int = BObjectOne.hashCode()

	fun doSomething():Any = classes[hash % classes.size]

}