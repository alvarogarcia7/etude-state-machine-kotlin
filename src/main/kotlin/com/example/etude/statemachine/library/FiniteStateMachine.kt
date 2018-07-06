package com.example.etude.statemachine.library

class TransitionState<T>(override val payload: T, private val transitions: (State<T>) -> State<T>) : State<T> {
    override fun transition(): State<T> {
        return transitions.invoke(this)
    }
}

interface State<T> {
    fun transition(): State<T>
    val payload: T
}

interface State2<T> {
    fun transition(): State2<T>
}

open class FinalState2<T> : State2<T> {
    override fun transition(): State2<T> {
        return this
    }
}
class FinalState<T>(override val payload: T) : State<T> {
    override fun transition(): State<T> {
        return this
    }

}
