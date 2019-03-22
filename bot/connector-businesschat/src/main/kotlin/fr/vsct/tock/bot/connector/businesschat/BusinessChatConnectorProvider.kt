package fr.vsct.tock.bot.connector.businesschat

import fr.vsct.tock.bot.connector.Connector
import fr.vsct.tock.bot.connector.ConnectorConfiguration
import fr.vsct.tock.bot.connector.ConnectorProvider
import fr.vsct.tock.bot.connector.ConnectorType
import fr.vsct.tock.bot.connector.ConnectorTypeConfiguration
import fr.vsct.tock.bot.connector.ConnectorTypeConfigurationField
import fr.vsct.tock.translator.UserInterfaceType

val businessChatConnectorType = ConnectorType("business chat", UserInterfaceType.textAndVoiceAssistant)

internal object BusinessChatConnectorProvider : ConnectorProvider {

    private const val BUSINESS_ID = "businessId"
    override val connectorType: ConnectorType get() = businessChatConnectorType

    override fun connector(connectorConfiguration: ConnectorConfiguration): Connector {
        with(connectorConfiguration) {
            return BusinessChatConnector(path, connectorId, connectorConfiguration.parameters[BUSINESS_ID] ?: "")
        }
    }

    override fun configuration(): ConnectorTypeConfiguration {
        return ConnectorTypeConfiguration(
            businessChatConnectorType,
            listOf(
                ConnectorTypeConfigurationField(
                    "Business Id",
                    BUSINESS_ID,
                    true
                )
            )
        )
    }
}

internal class BusinessChatConnectorProviderService : ConnectorProvider by BusinessChatConnectorProvider