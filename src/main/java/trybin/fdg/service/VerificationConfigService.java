package trybin.fdg.service;

import trybin.fdg.context.DataGenerateContext;

import java.util.List;

/**
 * @author TryBin
 */
public interface VerificationConfigService {
    List<String> execute(DataGenerateContext dataGenerateContext);

    Boolean isAdopt(List<String> exceptionContainer);
}
