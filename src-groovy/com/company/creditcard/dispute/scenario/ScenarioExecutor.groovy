package com.company.creditcard.dispute.scenario

import java.util.concurrent.ThreadLocalRandom

import com.bonitasoft.engine.api.APIAccessor
import com.company.creditcard.dispute.model.Account
import com.company.creditcard.dispute.model.AccountTx
import com.company.creditcard.dispute.model.Customer
import com.company.creditcard.dispute.scenario.process.CreditCardDisputeProcess

class ScenarioExecutor {

    def run(APIAccessor apiAccessor, List<Customer> customers) {
        new Scenario1().execute(new CreditCardDisputeProcess(apiAccessor), customers[0].customerId, pickTx(customers[0].account))
        new Scenario2().execute(new CreditCardDisputeProcess(apiAccessor), customers[1].customerId, pickTx(customers[1].account, 500, 0))
        new Scenario3().execute(new CreditCardDisputeProcess(apiAccessor), customers[2].customerId, pickTx(customers[2].account, 10000, 501))
        new Scenario4().execute(new CreditCardDisputeProcess(apiAccessor), customers[3].customerId, pickTx(customers[3].account, 500, 0))
        new Scenario5().execute(new CreditCardDisputeProcess(apiAccessor), customers[4].customerId, pickTx(customers[4].account))
    }

    def AccountTx pickTx(Account account, double maxValue, double minValue) {
        def tx = pickTx(account)
        while (!(tx.amount >= minValue && tx.amount < maxValue)) {
            tx = pickTx(account)
        }
        tx
    }

    def AccountTx pickTx(Account account) {
        account.transactions.get(ThreadLocalRandom.current().nextInt(0, account.transactions.size()-1))
    }
}
