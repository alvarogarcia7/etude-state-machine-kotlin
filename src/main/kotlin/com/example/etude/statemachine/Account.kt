package com.example.etude.statemachine

import java.util.*

class Account(private val incomingSecure: Boolean = true,
              private val outgoingSecure: Boolean = true) : OutgoingTransfer, IncomingTransfer {
    private var balance = 0
    private val transfers: MutableMap<String, Transfer.TransferDiagram> = mutableMapOf()

    override fun confirmOutgoing(transferId: String) {
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

    override fun confirmIncoming(transferId: String) {
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

    override fun userConfirmOutgoing(transferId: String) {
        transfers[transferId] = transfers[transferId]?.transition()!!.payload
    }

    override fun userConfirmIncoming(transferId: String) {
        transfers[transferId] = transfers[transferId]?.transition()!!.payload
    }

    fun balance(): Int {
        return balance
    }

    override fun requestIncomingPayload(request: Transfer.TransferRequest): Transfer.TransferDiagram.TransferPayload {
        return if (incomingSecure) {
            Transfer.TransferDiagram.TransferPayload.SecureTransferPayload(transferIdGenerator.next(), request)
        } else {
            Transfer.TransferDiagram.TransferPayload.NotSecureTransferPayload(transferIdGenerator.next(), request)
        }
    }

    override fun requestOutgoingPayload(request: Transfer.TransferRequest): Transfer.TransferDiagram.TransferPayload {
        return if (outgoingSecure) {
            Transfer.TransferDiagram.TransferPayload.SecureTransferPayload(transferIdGenerator.next(), request)
        } else {
            Transfer.TransferDiagram.TransferPayload.NotSecureTransferPayload(transferIdGenerator.next(), request)
        }
    }


    override fun register(transferId: String, diagram: Transfer.TransferDiagram) {
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

    private var transferIdGenerator: TransferIdGenerator = RandomTransferIdGenerator()

    fun setTransferId(generator: TransferIdGenerator) {
        transferIdGenerator = generator
    }

}

interface IncomingTransfer {
    fun confirmIncoming(transferId: String)
    fun userConfirmIncoming(transferId: String)
    fun requestIncomingPayload(request: Transfer.TransferRequest): Transfer.TransferDiagram.TransferPayload
    fun register(transferId: String, diagram: Transfer.TransferDiagram)
}

interface OutgoingTransfer {
    fun confirmOutgoing(transferId: String)
    fun userConfirmOutgoing(transferId: String)
    fun requestOutgoingPayload(request: Transfer.TransferRequest): Transfer.TransferDiagram.TransferPayload
    fun register(transferId: String, diagram: Transfer.TransferDiagram)
}

interface TransferIdGenerator {
    fun next(): String
}

class RandomTransferIdGenerator : TransferIdGenerator {
    override fun next(): String {
        return UUID.randomUUID().toString()
    }
}
