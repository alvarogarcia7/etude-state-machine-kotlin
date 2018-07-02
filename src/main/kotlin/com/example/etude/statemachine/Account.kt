package com.example.etude.statemachine

class Account(private val secure: Boolean = true) {
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
        val request = if (this.secure) {
            Transfer.TransferDiagram.TransferPayload.SecureTransferPayload("1234")
        } else {
            Transfer.TransferDiagram.TransferPayload.NotSecureTransferPayload("1234")
        }
        return request
    }

    fun requestOutgoingPayload(): Transfer.TransferDiagram.TransferPayload {
        if (this.secure) {
            return Transfer.TransferDiagram.TransferPayload.SecureTransferPayload("2345")
        } else {
            return Transfer.TransferDiagram.TransferPayload.NotSecureTransferPayload("2345")
        }
    }

    fun register(transferId: String, diagram: Transfer.TransferDiagram) {
        transfers[transferId] = diagram
    }

    companion object {
        fun secure(): Account {
            return Account(true)
        }

        fun notSecure(): Account {
            return Account(false)
        }
    }

}