package com.mns.unused//import com.azure.cosmos.*
//
//
//data class Event(
//    val id: String,
//    val streamId: String,
//    val eventType: String,
//    val timestamp: String,
//    val payload: Any
//)
//
//
//
//suspend fun createEventStoreTableIfNotExists(cosmosClient: CosmosClient, tableName: String) {
//    val databaseName = "YourDatabaseName"
//
//
//
//    val databaseResponse = cosmosClient.createDatabaseIfNotExists(databaseName).await()
//    val databaseClient = cosmosClient.getDatabase(databaseResponse.database.id)
//
//
//
//    val partitionKeyDefinition = PartitionKeyDefinition("/streamId")
//    val containerProperties = CosmosContainerProperties(tableName, partitionKeyDefinition)
//    val throughputProperties = ThroughputProperties(400)
//
//
//
//    val containerResponse = databaseClient.createContainerIfNotExists(containerProperties, throughputProperties).await()
//    val container = databaseClient.getContainer(containerResponse.container.id)
//
//
//
//    println("Event store table '$tableName' created or already exists.")
//}
//
//
//
//suspend fun insertEvent(container: CosmosAsyncContainer, event: Event) {
//    container.createItem(event, PartitionKey(event.streamId)).await()
//}
//
//
//
//// Usage example
//fun main() {
//    val cosmosEndpoint = "YourCosmosDBEndpoint"
//    val cosmosKey = "YourCosmosDBKey"
//    val tableName = "YourTableName"
//
//
//    val cosmosClient = CosmosClientBuilder()
//        .endpoint(cosmosEndpoint)
//        .key(cosmosKey)
//        .buildAsyncClient()
//
//
//    val eventStoreTableCreated = createEventStoreTableIfNotExists(cosmosClient, tableName)
//
//
//    // Check if the table was created or already exists
//    if (eventStoreTableCreated) {
//        println("Event store table created.")
//    } else {
//        println("Event store table already exists.")
//    }
//
//
//    // Insert example streams
//    val container = cosmosClient.getDatabase("YourDatabaseName").getContainer("YourTableName")
//
//
//    val orders = listOf(
//        "order-1",
//        "order-2",
//        "order-3"
//    )
//
//
//
//    orders.forEach { orderId ->
//        val orderCreatedEvent = Event(
//            id = "event-1",
//            streamId = orderId,
//            eventType = "OrderCreated",
//            timestamp = "2023-06-06T10:00:00Z",
//            payload = // Payload for OrderCreated event
//        )
//        insertEvent(container, orderCreatedEvent)
//
//
//        val lineItemAddedEvent = Event(
//            id = "event-2",
//            streamId = orderId,
//            eventType = "LineItemAdded",
//            timestamp = "2023-06-07T10:00:00Z",
//            payload = // Payload for LineItemAdded event
//        )
//        insertEvent(container, lineItemAddedEvent)
//
//
//        val lineItemQuantityUpdatedEvent = Event(
//            id = "event-3",
//            streamId = orderId,
//            eventType = "LineItemQuantityUpdated",
//            timestamp = "2023-06-08T10:00:00Z",
//            payload = // Payload for LineItemQuantityUpdated event
//        )
//        insertEvent(container, lineItemQuantityUpdatedEvent)
//
//
//        val lineItemRemovedEvent = Event(
//            id = "event-4",
//            streamId = orderId,
//            eventType = "LineItemRemoved",
//            timestamp = "2023-06-09T10:00:00Z",
//            payload = // Payload for LineItemRemoved event
//        )
//        insertEvent(container, lineItemRemovedEvent)
//
//
//        val giftMessageAddedEvent = Event(
//            id = "event-5",
//            streamId = orderId,
//            eventType = "GiftMessageAdded",
//            timestamp = "2023-06-09T10:00:00Z",
//            payload = // Payload for LineItemRemoved event
//        )
//        insertEvent(container, giftMessageAddedEvent)
//    }
//}
