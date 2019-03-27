package fr.vsct.tock.bot.connector.businesschat.model

import com.fasterxml.jackson.annotation.JsonProperty
import fr.vsct.tock.bot.connector.businesschat.BusinessChatConnectorMessage
import java.util.UUID

data class Message(
    val id: String = UUID.randomUUID().toString(),
    val sourceId: String,
    val destinationId: String,
    val type: MessageType,
    val body: String,
    @JsonProperty("v") val version: Int = 1,
    val group: String? = null,
    val intent: String? = null
) : BusinessChatConnectorMessage()