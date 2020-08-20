package com.android.service.main.phone


/**
 * 通话记录信息实体
 */
class RecordEntity {
    var phoneNumber: String? = null
    var startTime: Long? = 0
    var endTime: Long? = 0
    var callLongId = 0
    var seed = ""
    var startFilePath: String? = null
    var endFilePath: String? = null
    override fun toString(): String {
        return "RecordEntity(phoneNumber=$phoneNumber, startTime=$startTime, endTime=$endTime, callLongId=$callLongId, seed='$seed', startFileName=$startFilePath, endFileName=$endFilePath)"
    }

    fun createEndFileName() {
        val replace = startFilePath?.replace("beginRecordTime", startTime.toString())
        val replace1 = replace?.replace("endRecordTime", endTime.toString())
        endFilePath = replace1
    }

}