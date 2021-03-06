/*
 * Copyright (C) 2019 VSCT
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package fr.vsct.tock.bot.connector.twitter

import com.fasterxml.jackson.module.kotlin.readValue
import com.github.salomonbrys.kodein.instance
import fr.vsct.tock.bot.connector.ConnectorBase
import fr.vsct.tock.bot.connector.ConnectorCallback
import fr.vsct.tock.bot.connector.twitter.model.Webhook
import fr.vsct.tock.bot.connector.twitter.model.incoming.IncomingEvent
import fr.vsct.tock.bot.connector.twitter.model.outcoming.DirectMessageOutcomingEvent
import fr.vsct.tock.bot.connector.twitter.model.outcoming.OutcomingEvent
import fr.vsct.tock.bot.engine.BotRepository
import fr.vsct.tock.bot.engine.ConnectorController
import fr.vsct.tock.bot.engine.action.Action
import fr.vsct.tock.bot.engine.event.Event
import fr.vsct.tock.bot.engine.monitoring.logError
import fr.vsct.tock.bot.engine.user.PlayerId
import fr.vsct.tock.bot.engine.user.UserPreferences
import fr.vsct.tock.shared.Executor
import fr.vsct.tock.shared.defaultLocale
import fr.vsct.tock.shared.error
import fr.vsct.tock.shared.injector
import fr.vsct.tock.shared.jackson.mapper
import mu.KotlinLogging
import org.apache.commons.lang3.LocaleUtils
import java.time.Duration
import java.time.ZoneOffset

internal class TwitterConnector internal constructor(
    val applicationId: String,
    val baseUrl: String,
    val path: String,
    val client: TwitterClient
) : ConnectorBase(TwitterConnectorProvider.connectorType) {

    private val url = "$baseUrl$path"

    companion object {
        private val logger = KotlinLogging.logger {}
    }

    private val executor: Executor by injector.instance()

    override fun loadProfile(callback: ConnectorCallback, userId: PlayerId): UserPreferences {

        try {
            val userProfile = client.user(userId.id)
            logger.debug { "User profile : $userProfile for $userId" }
            return UserPreferences(
                userProfile.screenName,
                "",
                null,
                ZoneOffset.of(userProfile.utcOffset ?: "Z"),
                userProfile.lang?.let {
                    try {
                        LocaleUtils.toLocale(it)
                    } catch (e: Exception) {
                        logger.error(e)
                        null
                    }
                } ?: defaultLocale,
                userProfile.profileImageUrlHttps)
        } catch (e: Exception) {
            logger.error(e)
        }
        return UserPreferences()

    }

    /**
     * Registers the connector for the specified controller.
     */
    override fun register(controller: ConnectorController) {
        controller.registerServices(path) { router ->
            logger.info("deploy rest twitter connector services for root path $path ")

            // see https://developer.twitter.com/en/docs/accounts-and-users/subscribe-account-activity/guides/securing-webhooks
            router.get(path).handler { context ->
                try {
                    logger.info { "get twitter crc" }

                    val crcToken = context.queryParam("crc_token").first()

                    logger.info { "Twitter crc_token: $crcToken" }
                    val sha256 = client.b64HmacSHA256(crcToken)

                    logger.info { "Twitter CRC response: $sha256" }

                    context.response().end("{\"response_token\":\"sha256=$sha256\"}")
                } catch (e: Throwable) {
                    logger.error(e)
                    context.fail(500)
                }


            }

            // see https://developer.twitter.com/en/docs/accounts-and-users/subscribe-account-activity/guides/account-activity-data-objects
            router.post(path).handler { context ->
                val requestTimerData = BotRepository.requestTimer.start("twitter_webhook")
                try {
                    val twitterHeader = context.request().getHeader("X-Twitter-Webhooks-Signature")
                    logger.debug { "Twitter signature:  $twitterHeader" }
                    logger.debug { "Twitter headers:  ${context.request().headers().entries()}" }
                    val body = context.bodyAsString
                    if (twitterHeader != null && isSignedByTwitter(body, twitterHeader)) {
                        try {
                            logger.debug { "Twitter request input : $body" }
                            val incomingEvent = mapper.readValue<IncomingEvent?>(body)

                            if (incomingEvent == null) {
                                logger.debug { "Unsupported twitter event" }
                            } else {
                                logger.info { incomingEvent }
                                executor.executeBlocking {
                                    val event = WebhookActionConverter.toEvent(incomingEvent, applicationId)
                                    if (event != null) {
                                        controller.handle(event)
                                    } else {
                                        logger.logError(
                                            "unable to convert $incomingEvent to event",
                                            requestTimerData
                                        )
                                    }
                                }
                            }
                        } catch (t: Throwable) {
                            logger.logError(t, requestTimerData)
                        }
                    } else {
                        logger.logError("Not signed by twitter!!! : $twitterHeader \n $body", requestTimerData)
                    }
                } catch (e: Throwable) {
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

    /**
     * Unregisters the connector.
     */
    override fun unregister(controller: ConnectorController) {
        super.unregister(controller)
        val existingWebhooks = client.webhooks()
        existingWebhooks.find { webhook: Webhook -> webhook.url == url }?.let {
            client.unregisterWebhook(it.id)
        }
    }

    /**
     * Send an event with this connector for the specified delay.
     *
     * @param event the event to send
     * @param callback the initial connector callback
     * @param delayInMs the optional delay
     */
    override fun send(event: Event, callback: ConnectorCallback, delayInMs: Long) {
        logger.debug { "event: $event" }
        if (event is Action) {
            val outcomingEvent = TwitterMessageConverter.toOutcomingEvent(event)
            if (outcomingEvent != null) {
                sendMessage(outcomingEvent, delayInMs)
            }
        }
    }

    private fun sendMessage(outcomingEvent: OutcomingEvent, delayInMs: Long) {
        executor.executeBlocking(Duration.ofMillis(delayInMs)) {
            when (outcomingEvent.event) {
                is DirectMessageOutcomingEvent -> {
                    client.sendDirectMessage(outcomingEvent)
                }
            }
        }
    }

    private fun isSignedByTwitter(payload: String, twitterSignature: String): Boolean {
        return "sha256=${client.b64HmacSHA256(payload)}" == twitterSignature
    }


}