package translate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.amazonaws.services.lambda.runtime.tests.annotations.Event;
import org.junit.jupiter.params.ParameterizedTest;
import translate.model.Translation;
import translate.ports.in.TranslateTextUseCase;
import translate.ports.out.TranslateServiceException;

public class AppTest {

  @ParameterizedTest
  @Event(value = "event.json", type = APIGatewayProxyRequestEvent.class)
  public void successfulResponse(APIGatewayProxyRequestEvent event) throws Exception {
    TranslateTextUseCase useCase = mock(TranslateTextUseCase.class);
    when(useCase.translate(eq("hello world"), eq("fr"), eq("en"))).thenReturn(
        new Translation("hello world", "en", "fr", "Bonjour"));
    App handler = new App(useCase);
    APIGatewayProxyResponseEvent result = handler.handleRequest(event, null);
    assertThat(result.getStatusCode()).isEqualTo(200);
    String content = result.getBody();
    assertNotNull(content);
    assertThat(content).contains("\"message\"");
    assertThat(content).contains("\"sourceLang\"");
    assertThat(content).contains("\"targetLang\"");
    assertThat(content).contains("\"translation\"");
    assertThat(content).contains("\"Bonjour\"");
  }

  @ParameterizedTest
  @Event(value = "event-fail.json", type = APIGatewayProxyRequestEvent.class)
  public void failedResponse(APIGatewayProxyRequestEvent event) throws Exception {
    TranslateTextUseCase useCase = mock(TranslateTextUseCase.class);
    when(useCase.translate(anyString(), eq("xx"), eq("en"))).thenThrow(
        new TranslateServiceException("Invalid target lang"));
    App handler = new App(useCase);
    APIGatewayProxyResponseEvent result = handler.handleRequest(event, null);
    assertThat(result.getStatusCode()).isEqualTo(400);
    String content = result.getBody();
    assertNotNull(content);
    assertThat(content).contains("\"error\"");
    assertThat(content).contains("\"Invalid target lang\"");

  }
}
