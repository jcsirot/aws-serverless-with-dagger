package translate.adapters.in;

import javax.inject.Inject;
import translate.model.Translation;
import translate.ports.in.TranslateTextUseCase;
import translate.ports.out.LangDetector;
import translate.ports.out.TranslateService;
import translate.ports.out.TranslateServiceException;

public class TranslateTextUseCaseAdapter implements TranslateTextUseCase {

  private final TranslateService translateService;
  private final LangDetector langDetector;

  @Inject
  public TranslateTextUseCaseAdapter(TranslateService translateService, LangDetector langDetector) {
    this.translateService = translateService;
    this.langDetector = langDetector;
  }

  @Override
  public Translation translate(String text, String targetLang, String sourceLang)
      throws TranslateServiceException {
    String translatedText = translateService.translate(text, sourceLang, targetLang);
    return new Translation(text, sourceLang, targetLang, translatedText);
  }

  @Override
  public Translation translate(String text, String targetLang) throws TranslateServiceException {
    String sourceLang = langDetector.detectLang(text)
        .orElseThrow(() -> new TranslateServiceException("Could not detect input main language"));
    return translate(text, targetLang, sourceLang);
  }
}
