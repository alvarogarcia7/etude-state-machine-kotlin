package com.example.etude.statemachine

import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test

class FiniteStateMachineShould {

    private var balances: MutableMap<String, Int> = mutableMapOf()

    @Before
    fun setUp() {
        this.balances = mutableMapOf()
    }

    @Test
    fun `both secure`() {
        val transactionStateMachine = Transfer.aNew()
        val from = Account.secure()
        val to = Account.secure()
        createBalance(from, to)

        transactionStateMachine.transfer(1000, "rent", from, to)
        sameBalance(from, to)
        from.userConfirmOutgoing("1234")
        sameBalance(from, to)
        to.userConfirmIncoming("2345")

        differentBalance(from, to)
    }

    @Test
    fun `secure to a not-secure account`() {
        val transactionStateMachine = Transfer.aNew()
        val from = Account.secure()
        val to = Account.notSecure()
        createBalance(from, to)

        transactionStateMachine.transfer(1000, "rent", from, to)
        sameBalance(from, to)
        from.userConfirmOutgoing("1234")

        differentBalance(from, to)
    }

    private fun differentBalance(from: Account, to: Account) {
        assertThat(from.balance()).isNotEqualTo(balances["from"])
        assertThat(to.balance()).isNotEqualTo(balances["to"])
    }

    private fun sameBalance(from: Account, to: Account) {
        assertThat(from.balance()).isEqualTo(balances["from"])
        assertThat(to.balance()).isEqualTo(balances["to"])
    }


    private fun createBalance(from: Account, to: Account) {
        balances["from"] = from.balance()
        balances["to"] = to.balance()
    }
}


