package fr.vsct.tock.bot.connector.businesschat

import fr.vsct.tock.bot.connector.ConnectorCallbackBase

/**
* The BusinessChat [ConnectorCallback].
*/
class BusinessChatConnectorCallback(
    applicationId: String
) : ConnectorCallbackBase(applicationId, businessChatConnectorType)