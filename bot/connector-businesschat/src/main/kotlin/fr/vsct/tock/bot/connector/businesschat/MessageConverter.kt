package fr.vsct.tock.bot.connector.businesschat

import fr.vsct.tock.bot.connector.businesschat.model.Header
import fr.vsct.tock.bot.connector.businesschat.model.Message
import fr.vsct.tock.bot.connector.businesschat.model.MessageType
import fr.vsct.tock.bot.engine.action.Action
import fr.vsct.tock.bot.engine.action.SendSentence
import fr.vsct.tock.bot.engine.event.Event
import fr.vsct.tock.bot.engine.user.PlayerId
import fr.vsct.tock.bot.engine.user.PlayerType

object MessageConverter {

    fun toMessage(action: Action): Message? =
        if (action is SendSentence) {
            Message(
                sourceId = action.playerId.id,
                destinationId = action.recipientId.id,
                type = MessageType.TEXT,
                body = action.text.toString()
            )
        } else null

    fun toHeader(message: Message): Header = Header(message.id, message.sourceId, message.destinationId)

    fun toEvent(message: Message, connectorId: String): Event? =
        when (message.type) {
            MessageType.TEXT -> {
                SendSentence(
                    applicationId = connectorId,
                    playerId = PlayerId(message.sourceId, PlayerType.user),
                    recipientId = PlayerId(message.destinationId, PlayerType.bot),
                    text = message.body
                )
            }
            else -> null
        }
}

