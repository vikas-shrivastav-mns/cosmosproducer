package com.mns

data class Event(
    val id: String,
    val streamId: String,
    val eventType: String,
    val timestamp: String,
    val payload: String
){
//    constructor(id: String, streamId: String, eventType: String, timestamp: String) : this(id, streamId, eventType, timestamp, "")
    constructor(): this("", "", "", "", "")
}

