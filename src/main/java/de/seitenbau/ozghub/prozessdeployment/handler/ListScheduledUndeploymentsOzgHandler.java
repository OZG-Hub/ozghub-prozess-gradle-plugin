package de.seitenbau.ozghub.prozessdeployment.handler;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

import com.fasterxml.jackson.core.type.TypeReference;

import de.seitenbau.ozghub.prozessdeployment.common.Environment;
import de.seitenbau.ozghub.prozessdeployment.model.Message;
import de.seitenbau.ozghub.prozessdeployment.model.ScheduledUndeployment;
import de.seitenbau.ozghub.prozessdeployment.model.UndeploymentHint;
import de.seitenbau.ozghub.prozessdeployment.model.response.Aggregated;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class ListScheduledUndeploymentsOzgHandler
    extends AbstractListHandler<Aggregated<List<ScheduledUndeployment>>>
{
  public static final String API_PATH = "/prozess/scheduled/undeployment/list";

  public static final String DEFAULT_SUBJECT_TEXT = "*Betreff nicht gesetzt*";

  public static final String DEFAULT_BODY_TEXT = "*Text nicht gesetzt*";

  public static final String DEFAULT_DATE_TEXT = "*Datum nicht gesetzt*";
  public static final String DATE_PATTERN = "dd.MM.yyyy";

  public ListScheduledUndeploymentsOzgHandler(Environment environment)
  {
    super(environment,
        new TypeReference<>()
        {
        },
        API_PATH
    );
  }

  @Override
  protected void writeLogEntries(Aggregated<List<ScheduledUndeployment>> aggregatedScheduledUndeployments)
  {
    if (!aggregatedScheduledUndeployments.isComplete())
    {
      log.warn("Es konnten nicht alle geplanten Undeployments von allen Prozessengines abgerufen werden.");
    }

    log.info("Es sind " + aggregatedScheduledUndeployments.getValue().size() + " geplante Undeployments:\n"
        + getLogText(aggregatedScheduledUndeployments.getValue()));
  }

  private static String getLogText(List<ScheduledUndeployment> scheduledUndeployments)
  {
    return scheduledUndeployments.stream()
        .map(ListScheduledUndeploymentsOzgHandler::getLogText)
        .collect(Collectors.joining());
  }

  private static String getLogText(ScheduledUndeployment scheduledUndeployment)
  {
    DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(DATE_PATTERN);
    String formattedUndeploymentDate = dateTimeFormatter.format(scheduledUndeployment.undeploymentDate());
    return """
        \nDeploymentId: %s
        Undeployment Datum: %s
        Ankündigungsnachricht:
        %s
        Nachricht:
        %s
        Hinweis:
        %s
        """.formatted(
        scheduledUndeployment.deploymentId(),
        formattedUndeploymentDate,
        getLogText(scheduledUndeployment.undeploymentAnnounceMessage()),
        getLogText(scheduledUndeployment.undeploymentMessage()),
        getHintLogText(scheduledUndeployment));
  }

  private static String getLogText(Message message)
  {
    String subject = getMessageSubject(message);
    String body = getMesssageBody(message);
    return " - Betreff: " + subject + "\n"
        + " - Text: " + body;
  }

  private static String getMessageSubject(Message message)
  {
    return defaultIfNull(message.subject(), DEFAULT_SUBJECT_TEXT);
  }

  private static String getMesssageBody(Message message)
  {
    return defaultIfNull(message.body(), DEFAULT_BODY_TEXT);
  }

  private static String defaultIfNull(String string, String defaultString)
  {
    return string == null ? defaultString : string;
  }

  private static String getHintLogText(ScheduledUndeployment scheduledUndeployment)
  {
    UndeploymentHint hint = scheduledUndeployment.hint();
    String text = getHintText(hint);
    String datum = getHintDatumFormatted(hint);
    return " - Text: " + text + "\n"
        + " - Darstellung ab: " + datum;
  }

  private static String getHintDatumFormatted(UndeploymentHint hint)
  {
    return hint == null
        ? DEFAULT_DATE_TEXT
        : defaultIfNull(getHintDisplayDate(hint), DEFAULT_DATE_TEXT);
  }

  private static String getHintText(UndeploymentHint hint)
  {
    return hint == null
        ? DEFAULT_BODY_TEXT
        : defaultIfNull(hint.text(), DEFAULT_BODY_TEXT);
  }

  private static String getHintDisplayDate(UndeploymentHint hint)
  {
    LocalDate localDate = hint.startToDisplay();
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern(DATE_PATTERN);
    return localDate != null ? localDate.format(formatter) : null;
  }
}
