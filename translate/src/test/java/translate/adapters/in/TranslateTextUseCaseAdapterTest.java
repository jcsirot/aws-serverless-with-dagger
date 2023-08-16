package translate.adapters.in;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Optional;
import org.junit.jupiter.api.Test;
import translate.model.Translation;
import translate.ports.out.LangDetector;
import translate.ports.out.TranslateService;

class TranslateTextUseCaseAdapterTest {

  @Test
  void translate_with_source_lang() throws Exception {
    // Given
    TranslateService translateService = mock(TranslateService.class);
    when(translateService.translate(eq("some text"), eq("en"), eq("fr"))).thenReturn("du texte");
    LangDetector langDetector = mock(LangDetector.class);
    TranslateTextUseCaseAdapter useCase = new TranslateTextUseCaseAdapter(translateService,
        langDetector);
    // When
    Translation translation = useCase.translate("some text", "fr", "en");
    // Then
    assertThat(translation.sourceLang()).isEqualTo("en");
    assertThat(translation.targetLang()).isEqualTo("fr");
    assertThat(translation.sourceText()).isEqualTo("some text");
    assertThat(translation.translatedText()).isEqualTo("du texte");
  }

  @Test
  void translate_without_source_lang() throws Exception {
    // Given
    TranslateService translateService = mock(TranslateService.class);
    when(translateService.translate(eq("some text"), eq("en"), eq("fr"))).thenReturn("du texte");
    LangDetector langDetector = mock(LangDetector.class);
    when(langDetector.detectLang("some text")).thenReturn(Optional.of("en"));
    TranslateTextUseCaseAdapter useCase = new TranslateTextUseCaseAdapter(translateService,
        langDetector);
    // When
    Translation translation = useCase.translate("some text", "fr");
    // Then
    assertThat(translation.sourceLang()).isEqualTo("en");
    assertThat(translation.targetLang()).isEqualTo("fr");
    assertThat(translation.sourceText()).isEqualTo("some text");
    assertThat(translation.translatedText()).isEqualTo("du texte");
  }
}