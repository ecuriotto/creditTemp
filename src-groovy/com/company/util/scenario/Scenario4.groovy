package com.company.util.scenario

import static com.company.util.scenario.ScenarioProvider.*

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.OffsetDateTime
import java.time.ZoneOffset

import com.bonitasoft.engine.api.APIAccessor
import com.company.model.AccountTx
import com.company.model.DisputeStates

class Scenario4 {

	def execute(APIAccessor apiAccessor, String customerId, AccountTx tx) {
        long giovannaAlmeidaId = withUser(apiAccessor,'giovanna.almeida')
		long processInstanceId = startCase(apiAccessor, customerId, giovannaAlmeidaId)

		def disputeInput = [
			currency: tx.currency,
			merchantIdNumber: tx.merchantIdNumber,
			amount: tx.amount,
			status: DisputeStates.INVESTIGATING,
			txDate:tx.txDate
		]

		gatherInformation(apiAccessor, giovannaAlmeidaId,processInstanceId,[disputeInput:disputeInput])

        long marcMarseauId = withUser(apiAccessor,'marc.marseau')
        
        investigateTransaction(apiAccessor, marcMarseauId, processInstanceId, null)
        
        disputeInput.status = DisputeStates.PROCESSING_CHARGEBACK
		updateDispute(apiAccessor, marcMarseauId, processInstanceId, [disputeInput:disputeInput])
        
        chargeback(apiAccessor, marcMarseauId, processInstanceId, "Chargeback request aprroved")
	}
}
