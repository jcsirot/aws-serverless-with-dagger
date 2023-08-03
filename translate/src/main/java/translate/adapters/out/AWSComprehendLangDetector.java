package translate.adapters.out;

import java.util.Optional;
import javax.inject.Inject;
import software.amazon.awssdk.services.comprehend.ComprehendClient;
import software.amazon.awssdk.services.comprehend.model.DetectDominantLanguageRequest;
import software.amazon.awssdk.services.comprehend.model.DetectDominantLanguageResponse;
import translate.ports.out.LangDetector;

public class AWSComprehendLangDetector implements LangDetector {

  private ComprehendClient comprehendClient;

  @Inject
  public AWSComprehendLangDetector(ComprehendClient comprehendClient) {
    this.comprehendClient = comprehendClient;
  }

  @Override
  public Optional<String> detectLang(String text) {
    DetectDominantLanguageResponse detectDominantLanguageResponse = comprehendClient
        .detectDominantLanguage(
            DetectDominantLanguageRequest.builder().text(text).build());
    if (detectDominantLanguageResponse.hasLanguages()) {
      return Optional.of(detectDominantLanguageResponse.languages().get(0).languageCode());
    }
    return Optional.empty();
  }
}
