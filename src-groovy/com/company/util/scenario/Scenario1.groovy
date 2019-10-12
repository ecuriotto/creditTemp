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

class Scenario1 {

	def execute(APIAccessor apiAccessor, String customerId, AccountTx tx) {
		long processInstanceId = startCase(apiAccessor, customerId, withUser(apiAccessor,'misa.kumagai'))

		def disputeInput = [
			currency: tx.currency,
			merchantIdNumber: tx.merchantIdNumber,
			amount: tx.amount,
			status: DisputeStates.INVESTIGATING,
			txDate:tx.txDate
		]

		gatherInformation(apiAccessor, withUser(apiAccessor,'misa.kumagai'),processInstanceId,[disputeInput:disputeInput])
	}
}
