package com.example.etude.statemachine

import com.example.etude.statemachine.library.FinalState2
import com.example.etude.statemachine.library.State2
import java.util.*

class Account(private val incomingSecure: Boolean = true,
              private val outgoingSecure: Boolean = true) : OutgoingTransfer, IncomingTransfer {
    private var balance = 0

    private val pendingTransfers: MutableMap<String, TransferStatus> = mutableMapOf()

    override fun confirmOutgoing(transferId: String) {
        val transferStatus = pendingTransfers[transferId]!!
        pendingTransfers.remove(transferId)
        this.balance -= transferStatus.transferPayload.request.request.amount
    }

    override fun confirmIncoming(transferId: String) {
        val transferStatus = pendingTransfers[transferId]!!
        pendingTransfers.remove(transferId)
        this.balance += transferStatus.transferPayload.request.request.amount
    }

    override fun userConfirmOutgoing(transferId: String) {
        val transferStatus = pendingTransfers[transferId]!!
        val newStatus = transferStatus.copy(diagram = transferStatus.diagram.transition())
        pendingTransfers[transferId] = newStatus
    }

    override fun userConfirmIncoming(transferId: String) {
        val transferStatus = pendingTransfers[transferId]!!
        val newStatus = transferStatus.copy(diagram = transferStatus.diagram.transition())
        pendingTransfers[transferId] = newStatus
    }

    fun balance(): Int {
        return balance
    }

    override fun requestIncomingPayload(request: Transfer.TransferRequest): Transfer.TransferPayload {
        return if (incomingSecure) {
            Transfer.TransferPayload.SecureTransferPayload(transferIdGenerator.next(), request)
        } else {
            Transfer.TransferPayload.NotSecureTransferPayload(transferIdGenerator.next(), request)
        }
    }

    override fun requestOutgoingPayload(request: Transfer.TransferRequest): Transfer.TransferPayload {
        return if (outgoingSecure) {
            Transfer.TransferPayload.SecureTransferPayload(transferIdGenerator.next(), request)
        } else {
            Transfer.TransferPayload.NotSecureTransferPayload(transferIdGenerator.next(), request)
        }
    }

    override fun register(transferPayload: Transfer.TransferPayload, diagram: State2<Transfer.TransferRequest>) {
        pendingTransfers[transferPayload.transferId] = TransferStatus(transferPayload, diagram)
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

    fun pendingTransfers(): List<TransferStatus> {
        return pendingTransfers
                .toList()
                .map { it.second }
                .filter { (_, transferStatus) ->
                    when (transferStatus) {
                        is FinalState2 -> false
                        else -> true
                    }
                }
    }

}

data class TransferStatus(val transferPayload: Transfer.TransferPayload, val diagram: State2<Transfer.TransferRequest>)

interface IncomingTransfer {
    fun confirmIncoming(transferId: String)
    fun userConfirmIncoming(transferId: String)
    fun requestIncomingPayload(request: Transfer.TransferRequest): Transfer.TransferPayload
    fun register(transferId: Transfer.TransferPayload, diagram: State2<Transfer.TransferRequest>)
}

interface OutgoingTransfer {
    fun confirmOutgoing(transferId: String)
    fun userConfirmOutgoing(transferId: String)
    fun requestOutgoingPayload(request: Transfer.TransferRequest): Transfer.TransferPayload
    fun register(transferId: Transfer.TransferPayload, diagram: State2<Transfer.TransferRequest>)
}

interface TransferIdGenerator {
    fun next(): String
}

class RandomTransferIdGenerator : TransferIdGenerator {
    override fun next(): String {
        return UUID.randomUUID().toString()
    }
}
