package translate.adapters.out;

import javax.inject.Inject;
import javax.inject.Singleton;
import software.amazon.awssdk.services.translate.TranslateClient;
import software.amazon.awssdk.services.translate.model.TranslateTextRequest;
import software.amazon.awssdk.services.translate.model.TranslateTextResponse;
import translate.ports.out.TranslateService;
import translate.ports.out.TranslateServiceException;

/**
 * This {@code TranslateService} calls AWS Translate via a {@code TranslateClient}
 */
@Singleton
public class AWSTranslateService implements TranslateService {

  private TranslateClient client;

  @Inject
  public AWSTranslateService(TranslateClient client) {
    this.client = client;
  }

  @Override
  public String translate(String text, String sourceLang, String targetLang)
      throws TranslateServiceException {
    try {
      TranslateTextRequest request = TranslateTextRequest.builder()
          .text(text)
          .sourceLanguageCode(sourceLang)
          .targetLanguageCode(targetLang)
          .build();
      TranslateTextResponse result = client.translateText(request);
      return result.translatedText();
    } catch (Exception e) {
      throw new TranslateServiceException(e);
    }
  }
}
