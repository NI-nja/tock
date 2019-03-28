package fr.vsct.tock.bot.connector.businesschat

import fr.vsct.tock.bot.connector.businesschat.model.Handover
import fr.vsct.tock.bot.connector.businesschat.model.Header
import fr.vsct.tock.bot.connector.businesschat.model.Message
import fr.vsct.tock.shared.addJacksonConverter
import fr.vsct.tock.shared.basicAuthInterceptor
import fr.vsct.tock.shared.create
import fr.vsct.tock.shared.longProperty
import fr.vsct.tock.shared.property
import fr.vsct.tock.shared.retrofitBuilderWithTimeoutAndLogger
import mu.KotlinLogging
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Header as RetrofitHeader
import retrofit2.http.POST

object AlcmeonBusinessChatClient : CSPBusinessChatClient {

    private val logger = KotlinLogging.logger { }
    private val ALCMEON_URL: String =
        property("alcmeon_businesschat_url", "https://api.alcmeon.com/")
    private val companyId = property("company_id", "company_id")
    private val password = property("alcmeon_password", "secret")
    private val businessChatClientApi: BusinessChatClientApi
    interface BusinessChatClientApi {

        @POST("/bzc/message")
        fun sendMessage(
            @RetrofitHeader("id") id: String,
            @RetrofitHeader("Source-Id") sourceId: String,
            @RetrofitHeader("Destination-Id") destinationId: String,
            @Body message: Message
        ): Call<ResponseBody>

        @POST("/pass-thread-control")
        fun sendHandOver(@RetrofitHeader("id") id: String,
                         @RetrofitHeader("Source-Id") sourceId: String,
                         @RetrofitHeader("Destination-Id") destinationId: String,
                         @Body handover: Handover) : Call<ResponseBody>
    }

    init {
        businessChatClientApi = retrofitBuilderWithTimeoutAndLogger(
            longProperty("tock_alcmeon_business_chat_request_timeout_ms", 30000),
            logger
            , interceptors = listOf(basicAuthInterceptor(companyId, password))
        )
            .baseUrl(ALCMEON_URL)
            .addJacksonConverter()
            .build()
            .create()
    }

    override fun sendMessage(header: Header, message: Message) {
        val response =
            businessChatClientApi.sendMessage(header.id, header.sourceId, header.destinationId, message).execute()
        if (response.isSuccessful) {
            logger.info { "successful call to alcmeon business chat " }
        } else {
            logger.error { "error while sending message to alcmeon business chat " }
        }
    }

    override fun sendHandOver(header: Header, handover: Handover) {
        val response =
            businessChatClientApi.sendHandOver(header.id, header.sourceId, header.destinationId, handover).execute()
        if (response.isSuccessful) {
            logger.info { "successful call to alcmeon business chat " }
        } else {
            logger.error { "error while sending message to alcmeon business chat " }
        }
    }
}