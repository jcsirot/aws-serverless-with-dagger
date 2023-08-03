package translate.infra;

import dagger.Binds;
import dagger.Provides;
import javax.inject.Singleton;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.comprehend.ComprehendClient;
import software.amazon.awssdk.services.translate.TranslateClient;
import translate.adapters.in.TranslateTextUseCaseAdapter;
import translate.adapters.out.AWSComprehendLangDetector;
import translate.adapters.out.AWSTranslateService;
import translate.ports.out.TranslateService;
import translate.ports.in.TranslateTextUseCase;
import translate.ports.out.LangDetector;

@dagger.Module
public abstract class Module {

  @Provides
  @Singleton
  public static TranslateClient translateClient() {
    return TranslateClient.builder().build();
  }

  @Provides
  @Singleton
  public static ComprehendClient comprehendClient() {
    return ComprehendClient.builder().build();
  }

  @Binds
  public abstract TranslateService translateService(AWSTranslateService service);

  @Binds
  public abstract LangDetector langDetector(AWSComprehendLangDetector service);

  @Binds
  public abstract TranslateTextUseCase translateTextUseCase(TranslateTextUseCaseAdapter adapter);
}
