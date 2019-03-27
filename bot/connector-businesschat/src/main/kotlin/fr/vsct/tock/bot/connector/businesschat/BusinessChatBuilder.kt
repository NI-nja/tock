package fr.vsct.tock.bot.connector.businesschat

import fr.vsct.tock.bot.connector.ConnectorMessage
import fr.vsct.tock.bot.connector.businesschat.model.Message
import fr.vsct.tock.bot.connector.businesschat.model.MessageType
import fr.vsct.tock.bot.engine.BotBus

/**
 * Adds a Business Chat [ConnectorMessage] if the current connector is Business Chat.
 * You need to call [BotBus.send] or [BotBus.end] later to send this message.
 */
fun BotBus.withBusinesscChat(messageProvider: () -> BusinessChatConnectorMessage): BotBus {
    return withMessage(businessChatConnectorType, messageProvider)
}


/**
 * Creates a [BusinessChatText].
 *
 * @param text the text sent
 *
 */
fun BotBus.BusinessChatText(
    text: String
): BusinessChatConnectorMessage =
    Message(
        sourceId = botId.id,
        destinationId = userId.id,
        type = MessageType.text,
        body = translate(text).toString()
    )