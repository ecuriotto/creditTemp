package com.company.util

import com.bonitasoft.engine.api.ProcessAPI

class DataUpdateRecorder {
	
	def static recordUpdate(ProcessAPI processAPI,long activityInstanceId, String fieldName, Object oldValue, Object newValue, String dataName, Serializable records) {
		if(oldValue != newValue){
			def update = oldValue ? "$fieldName has been updated from $oldValue to $newValue" : "$fieldName has been updated to $newValue"
			records << update.toString()
			processAPI.updateActivityTransientDataInstance(dataName, activityInstanceId, records)
		}
	}
	
}
