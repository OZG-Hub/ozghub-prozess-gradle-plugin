package de.seitenbau.ozghub.prozessdeployment.helper;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

public class PlatformVersionTest
{
  @Test
  public void constructor()
  {
    // act
    PlatformVersion actual1 = new PlatformVersion(1, 2);
    PlatformVersion actual2 = new PlatformVersion(3);

    // assert
    assertThat(actual1.major()).isEqualTo(1);
    assertThat(actual1.minor()).isEqualTo(2);
    assertThat(actual2.major()).isEqualTo(3);
    assertThat(actual2.minor()).isEqualTo(1);
  }

  @ParameterizedTest
  @MethodSource("provide_isNewerThan")
  public void isNewerThan(PlatformVersion v1, PlatformVersion v2, boolean expected)
  {
    // act
    boolean actual = v1.isNewerThan(v2);

    // assert
    assertThat(actual).isEqualTo(expected);
  }

  private static Stream<Arguments> provide_isNewerThan()
  {
    PlatformVersion v1 = new PlatformVersion(1, 0);
    PlatformVersion v2 = new PlatformVersion(1, 1);
    PlatformVersion v3 = new PlatformVersion(2, 0);

    return Stream.of(
        Arguments.of(v1, v1, false),
        Arguments.of(v1, v2, false),
        Arguments.of(v1, v3, false),
        Arguments.of(v2, v1, true),
        Arguments.of(v2, v2, false),
        Arguments.of(v2, v3, false),
        Arguments.of(v3, v1, true),
        Arguments.of(v3, v2, true),
        Arguments.of(v3, v3, false)
    );
  }

  @Test
  public void _toString()
  {
    // arrange
    PlatformVersion version = new PlatformVersion(2, 3);

    // act
    String actual = version.toString();

    // assert
    assertThat(actual).isEqualTo("1.2.3");
  }
}
