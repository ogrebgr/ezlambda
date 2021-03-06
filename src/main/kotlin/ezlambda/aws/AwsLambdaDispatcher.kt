package ezlambda.aws

import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2ProxyRequestEvent
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2ProxyResponseEvent
import ezlambda.generic.AbstractLambdaDispatcher
import ezlambda.misc.HttpMethod
import org.slf4j.LoggerFactory


open class AwsLambdaDispatcher constructor(isPathInfoEnabled: Boolean) :
    AbstractLambdaDispatcher<AwsLambda>(isPathInfoEnabled) {

    private val logger = LoggerFactory.getLogger(this.javaClass)

    override fun handleRequest(
            input: APIGatewayV2ProxyRequestEvent,
            context: Context
    ): APIGatewayV2ProxyResponseEvent {

        val match = match(input.path, HttpMethod.fromString(input.httpMethod))
        return if (match != null) {
            try {
                match.lambda.handleRequest(AwsRequestData(input, context))
            } catch (e: Exception) {
                val ret = APIGatewayV2ProxyResponseEvent()
                ret.statusCode = 500
                ret.body = "500 Internal server error"
                ret.headers = mapOf("Content-type" to "text/plain")

                logger.error("Error handling {}, Error: {}", match.routePath, e.message)
                logger.error("Exception: ", e)

                ret
            }
        } else {
            val ret = APIGatewayV2ProxyResponseEvent()
            ret.statusCode = 404
            ret.body = "404 Not found"
            ret.headers = mapOf("Content-type" to "text/plain")
            ret
        }
    }
}