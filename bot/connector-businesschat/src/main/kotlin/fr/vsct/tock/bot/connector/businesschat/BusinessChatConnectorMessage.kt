package fr.vsct.tock.bot.connector.businesschat

import com.fasterxml.jackson.annotation.JsonIgnore
import fr.vsct.tock.bot.connector.ConnectorMessage
import fr.vsct.tock.bot.connector.ConnectorType

abstract class BusinessChatConnectorMessage : ConnectorMessage{
    override val connectorType: ConnectorType @JsonIgnore get() = BusinessChatConnectorProvider.connectorType
}