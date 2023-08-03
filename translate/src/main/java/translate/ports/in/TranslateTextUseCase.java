package translate.ports.in;

import translate.model.Translation;
import translate.ports.out.TranslateServiceException;

public interface TranslateTextUseCase {

  Translation translate(String text, String targetLang, String sourceLang)
      throws TranslateServiceException;

  Translation translate(String text, String targetLang) throws TranslateServiceException;
}
