package de.seitenbau.ozghub.prozessdeployment.model.response;

import java.io.Serial;
import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

/**
 * Ein Objekt, das aus mehreren Ergebnissen von Methodenaufrufen zusammengesetzt ist.
 * Speichert zusätzlich zum zusammengesetzten Wert, ob alle Methodenaufrufe ein Ergebnis geliefert haben.
 *
 * @param <T> der Typ des Ergebnisses.
 */
@Getter
@EqualsAndHashCode
@ToString
public final class Aggregated<T> implements Serializable
{
  @Serial
  private static final long serialVersionUID = 1L;

  /**
   * Das aus mehreren Methodenaufrufen zusammengesetzte Gesamtergebnis.
   */
  private final T value;

  /**
   * True, wenn alle Methodenaufrufe, aus denen das zusammengesetzte Ergebnis berechnet wurde,
   * tatsächlich ein Ergebnis geliefert haben.
   * False, wenn mindestens ein Aufruf kein Ergebnis geliefert hat (Timeout, Fehler oder ähnliches).
   */
  private final boolean complete;

  private Aggregated(T value, boolean complete)
  {
    this.value = value;
    this.complete = complete;
  }

  public static <T> Aggregated<T> complete(T value)
  {
    return new Aggregated<>(value, true);
  }

  @JsonCreator(mode = JsonCreator.Mode.PROPERTIES)
  public static <T> Aggregated<T> of(@JsonProperty("value") T value,
      @JsonProperty("complete") boolean complete)
  {
    return new Aggregated<>(value, complete);
  }
}
