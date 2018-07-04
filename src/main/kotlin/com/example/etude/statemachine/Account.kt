package com.example.etude.statemachine

class Account(private val incomingSecure: Boolean = true,
              private val outgoingSecure: Boolean = true) {
    private var balance = 0
    private val transfers: MutableMap<String, Transfer.TransferDiagram> = mutableMapOf()

    fun confirmOutgoing(transferId: String) {
        this.balance -= when (transfers[transferId]?.payload) {
            is Transfer.TransferDiagram.Final -> {
                (transfers[transferId]?.payload as Transfer.TransferDiagram.Final).request.XTransferRequest.TransferRequest.request.amount
            }
            is Transfer.TransferDiagram.Initial -> {
                (transfers[transferId]?.payload as Transfer.TransferDiagram.Initial).request.request.amount
            }
            is Transfer.TransferDiagram.Temporary -> {
                (transfers[transferId]?.payload as Transfer.TransferDiagram.Temporary).request.TransferRequest.request.amount
            }
            else -> {
                0
            }
        }
    }

    fun confirmIncoming(transferId: String) {
        this.balance += when (transfers[transferId]?.payload) {
            is Transfer.TransferDiagram.Final -> {
                (transfers[transferId]?.payload as Transfer.TransferDiagram.Final).request.XTransferRequest.TransferRequest.request.amount
            }
            is Transfer.TransferDiagram.Initial -> {
                (transfers[transferId]?.payload as Transfer.TransferDiagram.Initial).request.request.amount
            }
            is Transfer.TransferDiagram.Temporary -> {
                (transfers[transferId]?.payload as Transfer.TransferDiagram.Temporary).request.TransferRequest.request.amount
            }
            else -> {
                0
            }
        }
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