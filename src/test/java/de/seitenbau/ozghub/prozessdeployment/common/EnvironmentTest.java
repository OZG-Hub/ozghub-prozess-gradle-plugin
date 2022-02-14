package de.seitenbau.ozghub.prozessdeployment.common;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

public class EnvironmentTest
{
  @Test
  public void constructor()
  {
    // act
    Environment actual = new Environment("a", "b", "c");

    // assert
    assertThat(actual.getUrl()).isEqualTo("a");
    assertThat(actual.getUser()).isEqualTo("b");
    assertThat(actual.getPassword()).isEqualTo("c");
  }
}
