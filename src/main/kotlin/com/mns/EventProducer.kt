
package com.mns

import com.azure.cosmos.ChangeFeedProcessor
import com.azure.cosmos.ChangeFeedProcessorBuilder
import com.azure.cosmos.ConsistencyLevel
import com.azure.cosmos.CosmosAsyncContainer
import com.azure.cosmos.CosmosClient
import com.azure.cosmos.CosmosClientBuilder
import com.azure.cosmos.CosmosContainer
import com.azure.cosmos.CosmosDatabase
import com.azure.cosmos.CosmosException
import com.azure.cosmos.models.ChangeFeedProcessorItem
import com.azure.cosmos.models.CosmosContainerProperties
import com.azure.cosmos.models.CosmosItemRequestOptions
import com.azure.cosmos.models.CosmosItemResponse
import com.azure.cosmos.models.CosmosQueryRequestOptions
import com.azure.cosmos.models.FeedResponse
import com.azure.cosmos.models.PartitionKey
import com.azure.cosmos.models.ThroughputProperties
import com.azure.cosmos.util.CosmosPagedIterable
import java.util.function.Consumer


class EventProducer {
    private var client: CosmosClient? = null
    private val databaseName = "EventStore"
    private val containerName = "Items"
    private var database: CosmosDatabase? = null
    private var container: CosmosContainer? = null
    fun close() {
        client!!.close()
    }

    @get:Throws(Exception::class)
    private val start: Unit
        //  </Main>
        get() {
            println("Using Azure Cosmos DB endpoint: " + AccountSettings.HOST)
            val preferredRegions = ArrayList<String>()
            preferredRegions.add("West US")

            //  Create sync client
            client = CosmosClientBuilder()
                .endpoint(AccountSettings.HOST)
                .key(AccountSettings.MASTER_KEY)
                .preferredRegions(preferredRegions)
                .userAgentSuffix("Test")
                .consistencyLevel(ConsistencyLevel.EVENTUAL)
                .buildClient()
            createDatabaseIfNotExists()
            createContainerIfNotExists()
//            scaleContainer()
            val orders = listOf(
                "order-1",
                "order-2",
                "order-3"
            )

            addEvents(orders)
            readItems(orders);

        }
    private fun readItems(orders: List<String>) {
        //  Using partition key for point read scenarios.
        //  This will help fast look up of items because of partition key
        orders.forEach(Consumer { orderId: String ->
            val partitionKey = PartitionKey(orderId)
            val item: CosmosItemResponse<Event>? = container!!.readItem(
                "event-1", //  <OrderID> is partition key value
                partitionKey,
                Event::class.java
            )
            val requestCharge = item?.getRequestCharge()
            val requestLatency = item?.getDuration()
            if (item != null) {
                println(
                    java.lang.String.format(
                        "Item successfully read with id %s with a charge of %.2f and within duration %s",
                        item.item, requestCharge, requestLatency
                    )
                )
            }
        })
    }




    private fun queryItems() {
        // Set some common query options
        val preferredPageSize = 10
        val queryOptions = CosmosQueryRequestOptions()
        //  Set populate query metrics to get metrics around query executions
        queryOptions.setQueryMetricsEnabled(true)
        val familiesPagedIterable: CosmosPagedIterable<Event?> = container!!.queryItems(
            "SELECT * FROM Family WHERE Family.partitionKey IN ('Andersen', 'Wakefield', 'Johnson')",
            queryOptions,
            Event::class.java
        )
        familiesPagedIterable.iterableByPage(preferredPageSize)
            .forEach(Consumer<FeedResponse<Event?>> { cosmosItemPropertiesFeedResponse: FeedResponse<Event?> ->
                println(
                    "Got a page of query result with " +
                            cosmosItemPropertiesFeedResponse.getResults().size + " items(s)"
                            + " and request charge of " + cosmosItemPropertiesFeedResponse.getRequestCharge()
                )
                println(
                    "Item Ids " + cosmosItemPropertiesFeedResponse
                        .getResults()
                        .stream()
                        .map { item: Event? -> item!!.id }
                )
            })
    }
    private fun addEvents(orders: List<String>) {
        orders.forEach { orderId ->
            val orderCreatedEvent = Event(
                id = "event-1",
                streamId = orderId,
                eventType = "OrderCreated",
                timestamp = "2023-06-06T10:00:00Z",
                payload = "test"// Payload for OrderCreated event
            )
            insertEvent(container!!, orderCreatedEvent)


            val lineItemAddedEvent = Event(
                id = "event-2",
                streamId = orderId,
                eventType = "LineItemAdded",
                timestamp = "2023-06-07T10:00:00Z",
                payload = "test"// Payload for LineItemAdded event
            )
            insertEvent(container!!, lineItemAddedEvent)


            val lineItemQuantityUpdatedEvent = Event(
                id = "event-3",
                streamId = orderId,
                eventType = "LineItemQuantityUpdated",
                timestamp = "2023-06-08T10:00:00Z",
                payload = "test"// Payload for LineItemQuantityUpdated event
            )
            insertEvent(container!!, lineItemQuantityUpdatedEvent)


            val lineItemRemovedEvent = Event(
                id = "event-4",
                streamId = orderId,
                eventType = "LineItemRemoved",
                timestamp = "2023-06-09T10:00:00Z",
                payload = "test"// Payload for LineItemRemoved event
            )
            insertEvent(container!!, lineItemRemovedEvent)


            val giftMessageAddedEvent = Event(
                id = "event-5",
                streamId = orderId,
                eventType = "GiftMessageAdded",
                timestamp = "2023-06-09T10:00:00Z",
                payload = "test"// Payload for LineItemRemoved event
            )
            insertEvent(container!!, giftMessageAddedEvent)
        }
    }

    @Throws(Exception::class)
    private fun createDatabaseIfNotExists() {
        println("Create database $databaseName if not exists.")

        //  Create database if not exists
        val databaseResponse = client!!.createDatabaseIfNotExists(databaseName)
        database = client!!.getDatabase(databaseResponse.properties.id)
    }

    @Throws(Exception::class)
    private fun createContainerIfNotExists() {
        println("Create container $containerName if not exists.")

        //  Create container if not exists

        val containerProperties = CosmosContainerProperties(containerName, "/streamId")
        val containerResponse = database!!.createContainerIfNotExists(containerProperties)
        container = database!!.getContainer(containerResponse.properties.id)
    }

    @Throws(Exception::class)
    private fun scaleContainer() {
        println("Scaling container $containerName.")
        try {
            // You can scale the throughput (RU/s) of your container up and down to meet the needs of the workload. Learn more: https://aka.ms/cosmos-request-units
            val currentThroughput = container!!.readThroughput().properties
            val newThroughput = currentThroughput.manualThroughput + 100
            container!!.replaceThroughput(ThroughputProperties.createManualThroughput(newThroughput))
            println("Scaled container to $newThroughput completed!\n")
        } catch (e: CosmosException) {
            if (e.statusCode == 400) {
                System.err.println("Cannot read container throuthput.")
                System.err.println(e.message)
            } else {
                throw e
            }
        }
    }

    private fun insertEvent(container: CosmosContainer, event: Event) {
        container.createItem(event, PartitionKey(event.streamId), CosmosItemRequestOptions())
    }
    companion object {
        /**
         * Run a Hello CosmosDB console application.
         *
         * @param args command line args.
         */
        //  <Main>
        @JvmStatic
        fun main(args: Array<String>) {
            val p = EventProducer()
            try {
                p.start
                println("completed, please hold while resources are released")
            } catch (e: Exception) {
                e.printStackTrace()
                System.err.println(String.format("Cosmos getStarted failed with %s", e))
            } finally {
                println("Closing the client")
                p.close()
            }
            System.exit(0)
        }
    }
}
