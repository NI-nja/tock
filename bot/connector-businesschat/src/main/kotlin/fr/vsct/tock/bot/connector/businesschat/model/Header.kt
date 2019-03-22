package fr.vsct.tock.bot.connector.businesschat.model

import java.util.UUID

data class Header(val id: String = UUID.randomUUID().toString(),
                  val sourceId: String,
                  val destinationId: String)