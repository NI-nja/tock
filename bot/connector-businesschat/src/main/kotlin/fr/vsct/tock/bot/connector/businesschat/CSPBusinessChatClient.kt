package fr.vsct.tock.bot.connector.businesschat

import fr.vsct.tock.bot.connector.businesschat.model.Handover
import fr.vsct.tock.bot.connector.businesschat.model.Header
import fr.vsct.tock.bot.connector.businesschat.model.Message

internal interface CSPBusinessChatClient {
    fun sendMessage(header: Header, message: Message)
    fun sendHandOver(header: Header, handover: Handover)
}