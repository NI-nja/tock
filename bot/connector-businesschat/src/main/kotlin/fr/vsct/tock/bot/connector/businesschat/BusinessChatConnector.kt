package fr.vsct.tock.bot.connector.businesschat

import com.fasterxml.jackson.module.kotlin.readValue
import fr.vsct.tock.bot.connector.ConnectorBase
import fr.vsct.tock.bot.connector.ConnectorCallback
import fr.vsct.tock.bot.connector.ConnectorData
import fr.vsct.tock.bot.engine.BotRepository
import fr.vsct.tock.bot.engine.ConnectorController
import fr.vsct.tock.bot.engine.action.SendSentence
import fr.vsct.tock.bot.engine.event.Event
import fr.vsct.tock.bot.engine.monitoring.logError
import fr.vsct.tock.shared.error
import fr.vsct.tock.shared.jackson.mapper
import mu.KotlinLogging

class BusinessChatConnector(val path: String, val connectorId: String, val businessId: String) :
    ConnectorBase(BusinessChatConnectorProvider.connectorType) {

    private val logger = KotlinLogging.logger { }

    override fun send(event: Event, callback: ConnectorCallback, delayInMs: Long) {
        when (event) {
            is SendSentence -> {
                val message = MessageConverter.toMessage(event)
                if (message != null) {
                    AlcmeonBusinessChatClient.sendMessage(MessageConverter.toHeader(message), message)
                }
            }
        }
    }

    override fun register(controller: ConnectorController) {
        controller.registerServices(path) { router ->
            router.post("/message").handler { context ->
                val requestTimerData = BotRepository.requestTimer.start("business chat start")
                try {
                    val body = context.bodyAsString
                    val event = MessageConverter.toEvent(mapper.readValue(body), connectorId)
                    if(event != null) {
                        controller.handle(
                            event,
                            ConnectorData(
                                BusinessChatConnectorCallback(
                                    connectorId
                                )
                            )
                        )
                    }
                } catch (e: Exception) {
                    BotRepository.requestTimer.end(requestTimerData)
                    logger.logError(e, requestTimerData)
                } finally {
                    try {
                        BotRepository.requestTimer.end(requestTimerData)
                        context.response().end()
                    } catch (e: Throwable) {
                        logger.error(e)
                    }
                }

            }
        }
    }
}