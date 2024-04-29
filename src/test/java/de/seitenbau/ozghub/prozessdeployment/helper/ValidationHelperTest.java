package de.seitenbau.ozghub.prozessdeployment.helper;

import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

public class ValidationHelperTest
{
  @Test
  public void validateNotBlank()
  {
    // assert: no exception
    ValidationHelper.validateNotBlank("x", "");
  }

  @ParameterizedTest
  @MethodSource("provide_validateNotBlank")
  public void validateNotBlank_exception(String str)
  {
    // act & assert
    assertThatIllegalArgumentException()
        .isThrownBy(() -> ValidationHelper.validateNotBlank(str, "x"))
        .withMessage("Der Parameter 'x' muss gesetzt und nicht leer sein.");
  }

  private static Stream<String> provide_validateNotBlank()
  {
    return Stream.of(null, "", " ");
  }
}
