package com.example.etude.statemachine

class Account(private val incomingSecure: Boolean = true,
              private val outgoingSecure: Boolean = true) {
    private var balance = 0
    private val transfers: MutableMap<String, Transfer.TransferDiagram> = mutableMapOf()

    fun confirmOutgoing(transferId: String) {
        this.balance -= 1000
    }

    fun confirmIncoming(transferId: String) {
        this.balance += 1000
    }

    fun userConfirmOutgoing(transferId: String) {
        transfers[transferId] = transfers[transferId]?.transition()!!.payload
    }

    fun userConfirmIncoming(transferId: String) {
        transfers[transferId] = transfers[transferId]?.transition()!!.payload
    }

    fun balance(): Int {
        return balance
    }

    fun requestIncomingPayload(): Transfer.TransferDiagram.TransferPayload {
        return if (incomingSecure) {
            Transfer.TransferDiagram.TransferPayload.SecureTransferPayload("1234")
        } else {
            Transfer.TransferDiagram.TransferPayload.NotSecureTransferPayload("1234")
        }
    }

    fun requestOutgoingPayload(): Transfer.TransferDiagram.TransferPayload {
        return if (outgoingSecure) {
            Transfer.TransferDiagram.TransferPayload.SecureTransferPayload("2345")
        } else {
            Transfer.TransferDiagram.TransferPayload.NotSecureTransferPayload("2345")
        }
    }

    fun register(transferId: String, diagram: Transfer.TransferDiagram) {
        transfers[transferId] = diagram
    }

    companion object {
        fun secure(): Account {
            return Account(true, true)
        }

        fun notSecure(): Account {
            return Account(false, false)
        }
    }

}