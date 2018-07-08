package infrastructure.cli

import arrow.core.Option
import com.example.etude.statemachine.Account
import com.example.etude.statemachine.Transfer

private val accounts = mapOf(
        "secure1" to Account.secure(),
        "secure2" to Account.secure(),
        "notsecure1" to Account.notSecure(),
        "notsecure2" to Account.notSecure()
)

fun main(args: Array<String>) {
    Main(accounts["secure1"]!!).perform()
}

class Main(var selectedAccount: Account) {

    private val menuOptions = arrayOf(
            Pair("Send a transfer", {
                printAccounts()
                Transfer.aNew().transfer(10, "text", this.selectedAccount, readAccount())
            }),
            Pair("See all transfers", {
                printTransfers()
            }),
            Pair("Confirm a transfer", {
                printTransfers()
                val selectedTransferId = Integer.parseInt(readLine())
                this.selectedAccount.userConfirmIncoming(this.selectedAccount.pendingTransfers()[selectedTransferId].transferPayload.transferId)
            }),
            Pair("See account balance", {
                println(selectedAccount.balance())
            }),
            Pair("Choose account", {
                printAccounts()
                this.selectedAccount = readAccount()
            }),
            Pair("Quit", {}))

    fun perform() {
        while (true) {
            printMenu()
            val chosenOption = readOption()
            if (chosenOption.isDefined()) {
                chosenOption.map { performAction(it) }
            } else {
                break
            }
        }
    }

    private fun performAction(action: Int) {
        println("Will execute: " + menuOptions[action].first)
        if (action < menuOptions.size) {
                println("Could not understand this option.")
        }
        menuOptions[action].second.invoke()
    }

    private fun printTransfers() {
        println("Transfers:")
        this.selectedAccount.pendingTransfers().mapIndexed { index, transfer ->
            println("($index) - Id: ${transfer.transferPayload.transferId}, amount: ${transfer.transferPayload.request.request.amount}")
        }
    }

    private fun readAccount(): Account {
        println("Input the account number:")
        val selectedAccountNumber = readLine()
        return accounts[selectedAccountNumber]!!
    }

    private fun printAccounts() {
        println("available accounts:")
        accounts.forEach { number, _ -> println(number) }
    }

    private fun readOption(): Option<Int> {
        println("Input the number:")
        var numberOption = readLine()
        val parseInt = Integer.parseInt(numberOption)
        val action = menuOptions[parseInt]
        return if ("Quit" == action.first) {
            Option.empty()
        } else {
            Option(parseInt)
        }
    }

    private fun printMenu() {
        println("Menu:")
        menuOptions
                .mapIndexed { index, s ->
                    "($index) ${s.first}"
                }
                .forEach(::println)
    }


}
