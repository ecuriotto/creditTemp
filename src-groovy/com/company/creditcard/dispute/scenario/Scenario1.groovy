package com.company.creditcard.dispute.scenario

import com.company.creditcard.dispute.model.AccountTx
import com.company.creditcard.dispute.model.DisputeStates
import com.company.creditcard.dispute.scenario.process.CreditCardDisputeProcess
import com.company.creditcard.dispute.scenario.util.IdAccessor

class Scenario1 implements IdAccessor {

    def execute(CreditCardDisputeProcess process, String customerId, AccountTx tx) {
        long processInstanceId = process.startCase(customerId, withUser(process.apiAccessor, 'misa.kumagai'))

        def disputeInput = [
            currency: tx.currency,
            merchantIdNumber: tx.merchantIdNumber,
            amount: tx.amount,
            status: DisputeStates.INVESTIGATING,
            txDate:tx.txDate
        ]

        process.gatherInformation(withUser(process.apiAccessor, 'misa.kumagai'),processInstanceId,[disputeInput:disputeInput])
    }
}
