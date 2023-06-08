// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.mns.unused

import com.azure.cosmos.ConsistencyLevel
import com.azure.cosmos.CosmosClient
import com.azure.cosmos.CosmosClientBuilder
import com.azure.cosmos.CosmosDatabase
import com.azure.cosmos.models.CosmosContainerProperties
import com.azure.cosmos.models.CosmosContainerRequestOptions
import com.azure.cosmos.models.CosmosDatabaseRequestOptions
import org.slf4j.LoggerFactory

class AnalyticalContainerCRUDQuickstart {
    private var client: CosmosClient? = null
    private val databaseName = "AzureSampleFamilyDB"
    private val containerName = "FamilyContainer"
    private var database: CosmosDatabase? = null
    fun close() {
        client!!.close()
    }

    @Throws(Exception::class)
    private fun containerCRUDDemo() {
        logger.info("Using Azure Cosmos DB endpoint: {}", "AccountSettings.HOST")

        //  Create sync client
        client = CosmosClientBuilder()
            .endpoint("AccountSettings.HOST")
            .key("AccountSettings.MASTER_KEY")
            .consistencyLevel(ConsistencyLevel.EVENTUAL)
            .contentResponseOnWriteEnabled(true)
            .buildClient()
        createDatabaseIfNotExists()
        createContainerIfNotExists()

        // deleteAContainer() is called at shutdown()
    }

    // Database Create
    @Throws(Exception::class)
    private fun createDatabaseIfNotExists() {
        logger.info("Create database {} if not exists...", databaseName)

        //  Create database if not exists
        val databaseResponse = client!!.createDatabaseIfNotExists(databaseName)
        database = client!!.getDatabase(databaseResponse.properties.id)
        logger.info("Done.")
    }

    // Container create
    @Throws(Exception::class)
    private fun createContainerIfNotExists() {
        logger.info("Create container {} if not exists.", containerName)

        //  Create container if not exists
        val containerProperties = CosmosContainerProperties(containerName, "/lastName")

        // Set analytical store properties
        containerProperties.setAnalyticalStoreTimeToLiveInSeconds(-1)

        //  Create container
        val databaseResponse = database!!.createContainerIfNotExists(containerProperties)
        val container = database!!.getContainer(databaseResponse.properties.id)
        logger.info("Done.")
    }

    // Container delete
    @Throws(Exception::class)
    private fun deleteAContainer() {
        logger.info("Delete container {} by ID.", containerName)

        // Delete container
        val containerResp = database!!.getContainer(containerName).delete(CosmosContainerRequestOptions())
        logger.info("Status code for container delete: {}", containerResp.statusCode)
        logger.info("Done.")
    }

    // Database delete
    @Throws(Exception::class)
    private fun deleteADatabase() {
        logger.info("Last step: delete database {} by ID.", databaseName)

        // Delete database
        val dbResp = client!!.getDatabase(databaseName).delete(CosmosDatabaseRequestOptions())
        logger.info("Status code for database delete: {}", dbResp.statusCode)
        logger.info("Done.")
    }

    // Cleanup before close
    private fun shutdown() {
        try {
            //Clean shutdown
            deleteAContainer()
            deleteADatabase()
        } catch (err: Exception) {
            logger.error("Deleting Cosmos DB resources failed, will still attempt to close the client. See stack trace below.")
            err.printStackTrace()
        }
        client!!.close()
        logger.info("Done with sample.")
    }

    companion object {
        protected var logger = LoggerFactory.getLogger(AnalyticalContainerCRUDQuickstart::class.java)

        /**
         * Sample to demonstrate the following ANALYTICAL STORE container CRUD operations:
         * -Create
         * -Update throughput
         * -Read by ID
         * -Read all
         * -Delete
         */
        @JvmStatic
        fun main(args: Array<String>) {
            val p = AnalyticalContainerCRUDQuickstart()
            try {
                logger.info("Starting SYNC main")
                p.containerCRUDDemo()
                logger.info("Demo complete, please hold while resources are released")
            } catch (e: Exception) {
                e.printStackTrace()
                logger.error(String.format("Cosmos getStarted failed with %s", e))
            } finally {
                logger.info("Closing the client")
                p.shutdown()
            }
        }
    }
}