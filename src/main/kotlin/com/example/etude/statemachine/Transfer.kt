package com.example.etude.statemachine

import com.example.etude.statemachine.library.FinalState
import com.example.etude.statemachine.library.FinalState2
import com.example.etude.statemachine.library.State
import com.example.etude.statemachine.library.State2

class Transfer {
    fun transfer(amount: Int, description: String, from: Account, to: Account) {
        this.perform(TransferRequest(from, to, Request(amount, description)))
    }

    private fun perform(transferRequest: TransferRequest) {
        TransferDiagram.Initial(transferRequest, transferRequest.from.requestOutgoingPayload(transferRequest)).transition()
    }

    companion object {
        fun aNew(): Transfer {
            return Transfer()
        }
    }

    data class TransferRequest(val from: OutgoingTransfer, val to: IncomingTransfer, val request: Request)

    data class XTransferRequest(val incomingTransferId: String, val TransferRequest: TransferRequest)
    data class XXTransferRequest(val outgoingTransferId: String, val XTransferRequest: XTransferRequest)

    data class Request(val amount: Int, val description: String)

    data class IncomingTransferRequest(val incomingTransferId: String, val TransferRequest: TransferRequest)
    data class CompleteTransferRequest(val outgoingTransferId: String, val incomingTransferRequest: IncomingTransferRequest)


    sealed class TransferDiagram : com.example.etude.statemachine.library.State<TransferDiagram> {
        override val payload: TransferDiagram
            get() = this

        data class Initial(val request: TransferRequest, private val payload2: TransferPayload) : TransferDiagram() {
            override fun transition(): State<TransferDiagram> {
                val payload21 = request.to.requestIncomingPayload(request)
                val xTransferRequest = XTransferRequest(payload21.transferId, request)
                val diagram = Temporary(this, payload21, xTransferRequest)
                request.from.register(payload21.transferId, diagram)
                when (payload2) {
                    is TransferPayload.NotSecureTransferPayload -> {
                        diagram.transition()
                    }
                }
                return diagram
            }

        }

        data class Temporary(val diagram: TransferDiagram, private val payload2: TransferPayload, val request: XTransferRequest) : TransferDiagram() {
            override fun transition(): State<TransferDiagram> {
                val outgoingTransferId = request.TransferRequest.to.requestIncomingPayload(request.TransferRequest)
                val request1 = XXTransferRequest(outgoingTransferId.transferId, request)
                val diagram = Final(this, payload2, request1)
                request.TransferRequest.to.register(outgoingTransferId.transferId, diagram)
                when (payload2) {
                    is TransferPayload.NotSecureTransferPayload -> {
                        diagram.transition()
                    }
                }
                return diagram
            }
        }

        data class Final(val diagram: TransferDiagram, val payload2: TransferPayload, val request: XXTransferRequest) : TransferDiagram() {
            override fun transition(): State<TransferDiagram> {
                request.XTransferRequest.TransferRequest.from.confirmOutgoing(request.XTransferRequest.incomingTransferId)
                request.XTransferRequest.TransferRequest.to.confirmIncoming(request.outgoingTransferId)
                return FinalState(this)
            }
        }

        sealed class TransferPayload {
            abstract val transferId: String
            abstract val request: TransferRequest

            data class SecureTransferPayload(override val transferId: String, override val request: TransferRequest) : TransferPayload()
            data class NotSecureTransferPayload(override val transferId: String, override val request: TransferRequest) : TransferPayload()
        }


    }

    sealed class MRTransferDiagram : com.example.etude.statemachine.library.State2<MRTransferDiagram> {

        data class Initial(val transferRequest: TransferRequest) : MRTransferDiagram() {
            override fun transition(): State2<MRTransferDiagram> {
                val outgoingPayload = transferRequest.from.requestOutgoingPayload(transferRequest)
                val incomingTransferRequest = IncomingTransferRequest(outgoingPayload.transferId, transferRequest)
                when (outgoingPayload) {
                    is TransferDiagram.TransferPayload.SecureTransferPayload -> {
                        return WaitingForOutgoingConfirmation(incomingTransferRequest)
                    }
                    is TransferDiagram.TransferPayload.NotSecureTransferPayload -> {
                        return IncomingRequest(incomingTransferRequest)
                    }
                }
            }
        }


        data class WaitingForOutgoingConfirmation(val transferRequest: IncomingTransferRequest) : MRTransferDiagram() {
            override fun transition(): State2<MRTransferDiagram> {
                return IncomingRequest(transferRequest)
            }
        }

        data class IncomingRequest(val transferRequest: IncomingTransferRequest) : MRTransferDiagram() {
            override fun transition(): State2<MRTransferDiagram> {
                val payload = transferRequest.TransferRequest.to.requestIncomingPayload(transferRequest.TransferRequest)
                val transferRequest1 = CompleteTransferRequest(payload.transferId, transferRequest)
                return when (payload) {
                    is TransferDiagram.TransferPayload.SecureTransferPayload -> {
                        WaitingForIncomingConfirmation(transferRequest1)
                    }
                    is TransferDiagram.TransferPayload.NotSecureTransferPayload -> {
                        PerformingActions(transferRequest1)
                    }
                }
            }
        }

        data class WaitingForIncomingConfirmation(val transferRequest: CompleteTransferRequest) : MRTransferDiagram() {
            override fun transition(): State2<MRTransferDiagram> {
                return PerformingActions(transferRequest)
            }
        }

        data class PerformingActions(val transferRequest: CompleteTransferRequest) : MRTransferDiagram() {
            override fun transition(): State2<MRTransferDiagram> {
                val trRequest = transferRequest.incomingTransferRequest.TransferRequest
                trRequest.from.confirmOutgoing(transferRequest.incomingTransferRequest.incomingTransferId)
                trRequest.to.confirmIncoming(transferRequest.outgoingTransferId)
                return Confirmed(transferRequest)
            }
        }

        data class Confirmed(val transferRequest: CompleteTransferRequest) : FinalState2<MRTransferDiagram>()
    }


}
