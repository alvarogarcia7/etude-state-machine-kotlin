package com.example.etude.statemachine

import com.example.etude.statemachine.library.FinalState2
import com.example.etude.statemachine.library.State2

class Transfer {
    fun transfer(amount: Int, description: String, from: Account, to: Account) {
        TransferDiagram.Initial(TransferRequest(from, to, Request(amount, description))).transition()
    }

    companion object {
        fun aNew(): Transfer {
            return Transfer()
        }
    }

    data class TransferRequest(val from: OutgoingTransfer, val to: IncomingTransfer, val request: Request)

    data class Request(val amount: Int, val description: String)

    data class IncomingTransferRequest(val incomingTransferId: String, val TransferRequest: TransferRequest)
    data class CompleteTransferRequest(val outgoingTransferId: String, val incomingTransferRequest: IncomingTransferRequest)


    sealed class TransferDiagram : com.example.etude.statemachine.library.State2<TransferRequest> {

        data class Initial(val transferRequest: TransferRequest) : State2<TransferRequest> {
            override fun transition(): State2<TransferRequest> {
                val outgoingPayload = transferRequest.from.requestOutgoingPayload(transferRequest)
                val incomingTransferRequest = IncomingTransferRequest(outgoingPayload.transferId, transferRequest)
                return when (outgoingPayload) {
                    is TransferPayload.SecureTransferPayload -> {
                        val newState = WaitingForOutgoingConfirmation(incomingTransferRequest)
                        transferRequest.from.register(outgoingPayload, newState)
                        newState
                    }
                    is TransferPayload.NotSecureTransferPayload -> {
                        val newState = IncomingRequest(incomingTransferRequest)
                        transferRequest.from.register(outgoingPayload, newState)
                        newState.transition()
                        newState
                    }
                }
            }
        }


        data class WaitingForOutgoingConfirmation(val transferRequest: IncomingTransferRequest) : State2<TransferRequest> {
            override fun transition(): State2<TransferRequest> {
                return IncomingRequest(transferRequest).transition()
            }
        }

        data class IncomingRequest(val transferRequest: IncomingTransferRequest) : State2<TransferRequest> {
            override fun transition(): State2<TransferRequest> {
                val to = transferRequest.TransferRequest.to
                val payload = to.requestIncomingPayload(transferRequest.TransferRequest)
                val transferRequest1 = CompleteTransferRequest(payload.transferId, transferRequest)
                val newState = when (payload) {
                    is TransferPayload.SecureTransferPayload -> {
                        WaitingForIncomingConfirmation(transferRequest1)
                    }
                    is TransferPayload.NotSecureTransferPayload -> {
                        PerformingActions(transferRequest1)
                    }
                }
                to.register(payload, newState)
                when (newState) {
                    is PerformingActions -> {
                        newState.transition()
                    }
                }
                return newState
            }
        }

        data class WaitingForIncomingConfirmation(val transferRequest: CompleteTransferRequest) : State2<TransferRequest> {
            override fun transition(): State2<TransferRequest> {
                return PerformingActions(transferRequest).transition()
            }
        }

        data class PerformingActions(private val transferRequest: CompleteTransferRequest) : State2<TransferRequest> {
            override fun transition(): State2<TransferRequest> {
                val trRequest = transferRequest.incomingTransferRequest.TransferRequest
                trRequest.from.confirmOutgoing(transferRequest.incomingTransferRequest.incomingTransferId)
                trRequest.to.confirmIncoming(transferRequest.outgoingTransferId)
                return Confirmed(transferRequest)
            }
        }

        data class Confirmed(val transferRequest: CompleteTransferRequest) : FinalState2<TransferRequest>()
    }

    sealed class TransferPayload {
        abstract val transferId: String
        abstract val request: TransferRequest

        data class SecureTransferPayload(override val transferId: String, override val request: TransferRequest) : TransferPayload()
        data class NotSecureTransferPayload(override val transferId: String, override val request: TransferRequest) : TransferPayload()
    }


}
