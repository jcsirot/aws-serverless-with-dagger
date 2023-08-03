package translate.model;

public record Translation(String sourceText,
                          String sourceLang,
                          String targetLang,
                          String translatedText) {

}
