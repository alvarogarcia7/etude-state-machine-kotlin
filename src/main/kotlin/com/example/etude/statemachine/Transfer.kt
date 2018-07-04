package com.example.etude.statemachine

import com.example.etude.statemachine.library.FinalState
import com.example.etude.statemachine.library.State

class Transfer {
    fun transfer(amount: Int, description: String, from: Account, to: Account) {
        this.perform(TransferRequest(from, to, Request(amount, description)))
    }

    private fun perform(transferRequest: TransferRequest) {
        TransferDiagram.Initial(transferRequest, transferRequest.from.requestOutgoingPayload()).transition()
    }

    companion object {
        fun aNew(): Transfer {
            return Transfer()
        }
    }

    data class TransferRequest(val from: Account, val to: Account, val request: Request)

    data class XTransferRequest(val incomingTransferId: String, val TransferRequest: TransferRequest)
    data class XXTransferRequest(val outgoingTransferId: String, val XTransferRequest: XTransferRequest)

    data class Request(val amount: Int, val description: String)


    sealed class TransferDiagram : com.example.etude.statemachine.library.State<TransferDiagram> {
        override val payload: TransferDiagram
            get() = this

        data class Initial(val request: TransferRequest, val payload2: TransferPayload) : TransferDiagram() {
            override fun transition(): State<TransferDiagram> {
                when (payload2) {
                    is TransferPayload.NotSecureTransferPayload -> {
                        val payload21 = request.to.requestIncomingPayload()
                        val xTransferRequest = XTransferRequest(payload21.transferId, request)
                        val diagram = Temporary(this, payload21, xTransferRequest)
                        request.from.register(payload21.transferId, diagram)
                        return diagram
                    }
                    is TransferPayload.SecureTransferPayload -> {
                        val payload21 = request.to.requestIncomingPayload()
                        val xTransferRequest = XTransferRequest(payload21.transferId, request)
                        val diagram = Temporary(this, payload21, xTransferRequest)
                        request.from.register(payload21.transferId, diagram)
                        return diagram
                    }
                }
            }

        }

        data class Temporary(val diagram: TransferDiagram, val payload2: TransferPayload, val request: XTransferRequest) : TransferDiagram() {
            override fun transition(): State<TransferDiagram> {
                when (payload2) {
                    is TransferPayload.NotSecureTransferPayload -> {
                        val outgoingTransferId = request.TransferRequest.to.requestOutgoingPayload()
                        val request1 = XXTransferRequest(outgoingTransferId.transferId, request)
                        val diagram = Final(this, payload2, request1)
                        request.TransferRequest.to.register(outgoingTransferId.transferId, diagram)
                        diagram.transition()
                        return diagram
                    }
                    is TransferPayload.SecureTransferPayload -> {
                        val outgoingTransferId = request.TransferRequest.to.requestOutgoingPayload()
                        val request1 = XXTransferRequest(outgoingTransferId.transferId, request)
                        val diagram = Final(this, payload2, request1)
                        request.TransferRequest.to.register(outgoingTransferId.transferId, diagram)
                        return diagram
                    }
                }
            }
        }

        data class Final(val diagram: TransferDiagram, val payload2: TransferPayload, val request: XXTransferRequest) : TransferDiagram() {
            override fun transition(): State<TransferDiagram> {
                request.XTransferRequest.TransferRequest.from.confirmIncoming(request.XTransferRequest.incomingTransferId)
                request.XTransferRequest.TransferRequest.to.confirmOutgoing(request.outgoingTransferId)
                return FinalState(this)
            }
        }

        sealed class TransferPayload {
            abstract val transferId: String

            data class SecureTransferPayload(override val transferId: String) : TransferPayload()
            data class NotSecureTransferPayload(override val transferId: String) : TransferPayload()
        }


    }


}
