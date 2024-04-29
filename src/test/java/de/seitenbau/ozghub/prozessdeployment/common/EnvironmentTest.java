package de.seitenbau.ozghub.prozessdeployment.common;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

public class EnvironmentTest
{
  @Test
  public void constructor()
  {
    // act
    Environment actual = new Environment("a", "b", "c");

    // assert
    assertThat(actual.url()).isEqualTo("a");
    assertThat(actual.user()).isEqualTo("b");
    assertThat(actual.password()).isEqualTo("c");
  }

  @ParameterizedTest
  @MethodSource("provide_exception")
  public void constructor_exception(String url, String user, String passowrd, String paramterName)
  {
    // act & assert
    assertThatIllegalArgumentException()
        .isThrownBy(() -> new Environment(url, user, passowrd))
        .withMessage("Der Parameter '" + paramterName + "' muss gesetzt und nicht leer sein.");
  }

  private static Stream<Arguments> provide_exception()
  {
    return Stream.of(
        Arguments.of(null, "user", "passord", "url"),
        Arguments.of("", "user", "passord", "url"),
        Arguments.of(" ", "user", "passord", "url"),
        Arguments.of("url", null, "passord", "user"),
        Arguments.of("url", "", "passord", "user"),
        Arguments.of("url", " ", "passord", "user"),
        Arguments.of("url", "user", null, "password"),
        Arguments.of("url", "user", "", "password"),
        Arguments.of("url", "user", " ", "password")
    );
  }
}
