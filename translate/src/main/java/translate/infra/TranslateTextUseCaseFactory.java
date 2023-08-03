package translate.infra;

import dagger.Component;
import javax.inject.Singleton;
import translate.ports.in.TranslateTextUseCase;
import translate.ports.out.TranslateService;

@Component(modules = Module.class)
@Singleton
public interface TranslateTextUseCaseFactory {
  TranslateTextUseCase translateTextUseCase();
}
