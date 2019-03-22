package fr.vsct.tock.bot.connector.businesschat.model

import com.fasterxml.jackson.annotation.JsonAlias
import fr.vsct.tock.bot.connector.businesschat.BusinessChatConnectorMessage
import java.util.UUID

data class Message(
    val id: String = UUID.randomUUID().toString(),
    val sourceId: String,
    val destinationId: String,
    val type: MessageType,
    val body: String,
    @JsonAlias("v") val version: Int = 1,
    val group: String? = null,
    val intent: String? = null,
    val local: String? = "fr"
) : BusinessChatConnectorMessage()