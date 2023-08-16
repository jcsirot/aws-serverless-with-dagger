package translate;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import java.util.HashMap;
import java.util.Map;
import javax.inject.Inject;
import translate.infra.DaggerTranslateTextUseCaseFactory;
import translate.model.Translation;
import translate.ports.in.TranslateTextUseCase;
import translate.ports.out.TranslateServiceException;

/**
 * Handler for requests to Lambda function.
 */
public class App implements
    RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

  private final TranslateTextUseCase translateTextUseCase;

  public App() {
    this(DaggerTranslateTextUseCaseFactory.create().translateTextUseCase());
  }

  @Inject
  public App(TranslateTextUseCase translateTextUseCase) {
    this.translateTextUseCase = translateTextUseCase;
  }

  public APIGatewayProxyResponseEvent handleRequest(final APIGatewayProxyRequestEvent input,
      final Context context) {
    Map<String, String> headers = new HashMap<>();
    headers.put("Content-Type", "application/json");
    headers.put("X-Custom-Header", "application/json");

    APIGatewayProxyResponseEvent response = new APIGatewayProxyResponseEvent()
        .withHeaders(headers);

    Map<String, String> queryStringParameters = input.getQueryStringParameters();
    if (queryStringParameters == null) {
      return error400(response, "Missing 'q' parameter in query");
    }

    String query = queryStringParameters.get("q");
    if (query == null) {
      return error400(response, "Missing 'q' parameter in query");
    }
    String sourceLang = queryStringParameters.get("source");
    String targetLang = queryStringParameters.get("target");
    if (targetLang == null) {
      return error400(response, "Missing 'target' parameter in query");
    }

    try {
      Translation translation =
          sourceLang == null ? translateTextUseCase.translate(query, targetLang)
              : translateTextUseCase.translate(query, targetLang, sourceLang);
      return success(response, translation);
    } catch (TranslateServiceException te) {
      return error400(response, te.getMessage());
    } catch (Exception e) {
      return error500(response, e.getMessage());
    }
  }

  private static APIGatewayProxyResponseEvent error400(APIGatewayProxyResponseEvent response,
      String message) {
    return error(response, 400, message);
  }

  private static APIGatewayProxyResponseEvent error500(APIGatewayProxyResponseEvent response,
      String message) {
    return error(response, 500, message);
  }


  private static APIGatewayProxyResponseEvent error(APIGatewayProxyResponseEvent response,
      int code, String message) {
    String output = String.format("{ \"error\": \"%s\"}", message);
    return response
        .withStatusCode(code)
        .withBody(output);
  }

  private static APIGatewayProxyResponseEvent success(APIGatewayProxyResponseEvent response,
      Translation translation) {
    String output = String.format(
        "{ \"message\": \"%s\", \"sourceLang\":\"%s\", \"targetLang\":\"%s\", \"translation\": \"%s\" }",
        translation.sourceText(), translation.sourceLang(), translation.targetLang(),
        translation.translatedText());
    return response
        .withStatusCode(200)
        .withBody(output);
  }
}
