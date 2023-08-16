package translate.adapters.out;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import software.amazon.awssdk.services.translate.TranslateClient;
import software.amazon.awssdk.services.translate.model.TranslateTextRequest;
import software.amazon.awssdk.services.translate.model.TranslateTextResponse;
import translate.ports.out.TranslateService;

class AWSTranslateServiceTest {

  @Test
  void translate() throws Exception {
    TranslateClient client = mock(TranslateClient.class);
    // Given
    when(client.translateText(argThat((TranslateTextRequest req) -> {
      return "some text".equals(req.text());
    }))).thenReturn(TranslateTextResponse.builder().translatedText("du texte").build());
    TranslateService service = new AWSTranslateService(client);
    // When
    String translation = service.translate("some text", "en", "fr");
    // Then
    assertThat(translation).isEqualTo("du texte");
  }
}