//// Copyright (c) Microsoft Corporation. All rights reserved.
//// Licensed under the MIT License.
//package com.mns
//
//import com.azure.cosmos.ConsistencyLevel
//import com.azure.cosmos.CosmosClient
//import com.azure.cosmos.CosmosClientBuilder
//import com.azure.cosmos.CosmosContainer
//import com.azure.cosmos.CosmosDatabase
//import com.azure.cosmos.CosmosException
//import com.azure.cosmos.models.CosmosContainerProperties
//import com.azure.cosmos.models.CosmosContainerResponse
//import com.azure.cosmos.models.CosmosDatabaseResponse
//import com.azure.cosmos.models.ThroughputProperties
//import com.azure.cosmos.sample.common.Families
//import java.time.Duration
//import java.util.function.Consumer
//import java.util.stream.Collectors
//
//class SyncMain {
//    private var client: CosmosClient? = null
//    private val databaseName = "ToDoList"
//    private val containerName = "Items"
//    private var database: CosmosDatabase? = null
//    private var container: CosmosContainer? = null
//    fun close() {
//        client?.close()
//    }
//
//    @get:Throws(Exception::class)
//    private val startedDemo: Unit
//        //  </Main>
//        private get() {
//            println("Using Azure Cosmos DB endpoint: " + AccountSettings.HOST)
//            val preferredRegions = ArrayList<String>()
//            preferredRegions.add("West US")
//
//            //  Create sync client
//            client = CosmosClientBuilder()
//                .endpoint(AccountSettings.HOST)
//                .key(AccountSettings.MASTER_KEY)
//                .preferredRegions(preferredRegions)
//                .userAgentSuffix("CosmosDBJavaQuickstart")
//                .consistencyLevel(ConsistencyLevel.EVENTUAL)
//                .buildClient()
//            createDatabaseIfNotExists()
//            createContainerIfNotExists()
//            scaleContainer()
//
//        }
//
//    @Throws(Exception::class)
//    private fun createDatabaseIfNotExists() {
//        println("Create database $databaseName if not exists.")
//
//        //  Create database if not exists
//        val databaseResponse: CosmosDatabaseResponse? = client?.createDatabaseIfNotExists(databaseName)
//        if (databaseResponse != null) {
//            database = client.getDatabase(databaseResponse.getProperties().getId())
//        }
//        println("Checking database " + database?.getId() + " completed!\n")
//    }
//
//    @Throws(Exception::class)
//    private fun createContainerIfNotExists() {
//        println("Create container $containerName if not exists.")
//
//        //  Create container if not exists
//        val containerProperties = CosmosContainerProperties(containerName, "/partitionKey")
//        val containerResponse: CosmosContainerResponse? = database?.createContainerIfNotExists(containerProperties)
//        if (containerResponse != null) {
//            container = database?.getContainer(containerResponse.getProperties().getId())
//        }
//        println("Checking container " + container?.getId() + " completed!\n")
//    }
//
//    @Throws(Exception::class)
//    private fun scaleContainer() {
//        println("Scaling container $containerName.")
//        try {
//            // You can scale the throughput (RU/s) of your container up and down to meet the needs of the workload. Learn more: https://aka.ms/cosmos-request-units
//            val currentThroughput: ThroughputProperties = container.readThroughput().getProperties()
//            val newThroughput: Int = currentThroughput.getManualThroughput() + 100
//            container.replaceThroughput(ThroughputProperties.createManualThroughput(newThroughput))
//            println("Scaled container to $newThroughput completed!\n")
//        } catch (e: CosmosException) {
//            if (e.getStatusCode() == 400) {
//                System.err.println("Cannot read container throuthput.")
//                System.err.println(e.message)
//            } else {
//                throw e
//            }
//        }
//    }
//
//    @Throws(Exception::class)
//    private fun createFamilies(families: List<Family>) {
//        var totalRequestCharge = 0.0
//        for (family in families) {
//
//            //  Create item using container that we created using sync client
//
//            //  Using appropriate partition key improves the performance of database operations
//            val item: CosmosItemResponse<*> =
//                container.createItem<Any>(family, PartitionKey(family.getPartitionKey()), CosmosItemRequestOptions())
//
//            //  Get request charge and other properties like latency, and diagnostics strings, etc.
//            println(
//                String.format(
//                    "Created item with request charge of %.2f within" +
//                            " duration %s",
//                    item.getRequestCharge(), item.getDuration()
//                )
//            )
//            totalRequestCharge += item.getRequestCharge()
//        }
//        println(
//            String.format(
//                "Created %d items with total request " +
//                        "charge of %.2f",
//                families.size,
//                totalRequestCharge
//            )
//        )
//    }
//
//    private fun readItems(familiesToCreate: ArrayList<Family>) {
//        //  Using partition key for point read scenarios.
//        //  This will help fast look up of items because of partition key
//        familiesToCreate.forEach(Consumer<Family> { family: Family ->
//            try {
//                val item: CosmosItemResponse<Family> =
//                    container.readItem(family.getId(), PartitionKey(family.getPartitionKey()), Family::class.java)
//                val requestCharge: Double = item.getRequestCharge()
//                val requestLatency: Duration = item.getDuration()
//                println(
//                    java.lang.String.format(
//                        "Item successfully read with id %s with a charge of %.2f and within duration %s",
//                        item.getItem().getId(), requestCharge, requestLatency
//                    )
//                )
//            } catch (e: CosmosException) {
//                e.printStackTrace()
//                System.err.println(String.format("Read Item failed with %s", e))
//            }
//        })
//    }
//
//    private fun queryItems() {
//        // Set some common query options
//        val preferredPageSize = 10
//        val queryOptions = CosmosQueryRequestOptions()
//        //  Set populate query metrics to get metrics around query executions
//        queryOptions.setQueryMetricsEnabled(true)
//        val familiesPagedIterable: CosmosPagedIterable<Family> = container.queryItems(
//            "SELECT * FROM Family WHERE Family.partitionKey IN ('Andersen', 'Wakefield', 'Johnson')",
//            queryOptions,
//            Family::class.java
//        )
//        familiesPagedIterable.iterableByPage(preferredPageSize)
//            .forEach(Consumer<FeedResponse<Family?>> { cosmosItemPropertiesFeedResponse: FeedResponse<Family?> ->
//                println(
//                    "Got a page of query result with " +
//                            cosmosItemPropertiesFeedResponse.getResults().size + " items(s)"
//                            + " and request charge of " + cosmosItemPropertiesFeedResponse.getRequestCharge()
//                )
//                println(
//                    "Item Ids " + cosmosItemPropertiesFeedResponse
//                        .getResults()
//                        .stream()
//                        .map<Any>(Family::getId)
//                        .collect<List<Any>, Any>(Collectors.toList<Any>())
//                )
//            })
//    }
//
//    companion object {
//        /**
//         * Run a Hello CosmosDB console application.
//         *
//         * @param args command line args.
//         */
//        //  <Main>
//        @JvmStatic
//        fun main(args: Array<String>) {
//            val p = SyncMain()
//            try {
//                p.startedDemo
//                println("Demo complete, please hold while resources are released")
//            } catch (e: Exception) {
//                e.printStackTrace()
//                System.err.println(String.format("Cosmos getStarted failed with %s", e))
//            } finally {
//                println("Closing the client")
//                p.close()
//            }
//            System.exit(0)
//        }
//    }
//}
