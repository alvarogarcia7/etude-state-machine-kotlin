package com.example.etude.statemachine

import com.nhaarman.mockito_kotlin.doReturn
import com.nhaarman.mockito_kotlin.mock
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
        readingTransferId(from, "1234")
        val to = Account.secure()
        readingTransferId(to, "2345")
        createBalance(from, to)

        transactionStateMachine.transfer(1000, "rent", from, to)
        sameBalance(from, to)
        from.userConfirmOutgoing("1234")
        sameBalance(from, to)
        to.userConfirmIncoming("2345")

        differentBalance(from, to)
    }

    private fun readingTransferId(account: Account, transferId: String) {
        account.setTransferId(transferIdGenerator(transferId))
    }

    private fun transferIdGenerator(transferId: String): TransferIdGenerator {
        return mock {
            on { next() }.doReturn(transferId)
        }
    }


    @Test
    fun `transfer different amounts`() {
        val transactionStateMachine = Transfer.aNew()
        val from = Account.notSecure()
        val to = Account.notSecure()

        transactionStateMachine.transfer(900, "rent", from, to)

        assertThat(to.balance()).isEqualTo(900)
        assertThat(from.balance()).isEqualTo(-900)
    }

    @Test
    fun `secure to a not-secure account`() {
        val from = Account.secure()
        readingTransferId(from, "1234")
        val transactionStateMachine = Transfer.aNew()
        val to = Account.notSecure()
        createBalance(from, to)

        transactionStateMachine.transfer(1000, "rent", from, to)
        sameBalance(from, to)
        from.userConfirmOutgoing("1234")

        differentBalance(from, to)
    }

    @Test
    fun `not-secure to a not-secure account`() {
        val from = Account.notSecure()
        val to = Account.notSecure()
        val transactionStateMachine = Transfer.aNew()
        createBalance(from, to)

        transactionStateMachine.transfer(1000, "rent", from, to)

        differentBalance(from, to)
    }

    @Test
    fun `not-secure to a secure account`() {
        val to = Account.secure()
        readingTransferId(to, "2345")
        val transactionStateMachine = Transfer.aNew()
        val from = Account.notSecure()
        createBalance(from, to)

        transactionStateMachine.transfer(1000, "rent", from, to)
        sameBalance(from, to)
        to.userConfirmIncoming("2345")

        differentBalance(from, to)
    }

    private fun differentBalance(from: Account, to: Account) {
        assertThat(from.balance()).isNotEqualTo(balances["from"])
        assertThat(from.balance()).isLessThan(balances["from"])
        assertThat(to.balance()).isNotEqualTo(balances["to"])
        assertThat(to.balance()).isGreaterThan(balances["to"])

        noPendingTransfers(from)
        noPendingTransfers(to)
    }

    private fun sameBalance(from: Account, to: Account) {
        assertThat(from.balance()).isEqualTo(balances["from"])
        assertThat(to.balance()).isEqualTo(balances["to"])
    }


    private fun createBalance(from: Account, to: Account) {
        balances["from"] = from.balance()
        noPendingTransfers(from)
        balances["to"] = to.balance()
        noPendingTransfers(to)
    }

    private fun noPendingTransfers(account: Account) {
        assertThat(account.pendingTransfers()).isEmpty()
    }
}


