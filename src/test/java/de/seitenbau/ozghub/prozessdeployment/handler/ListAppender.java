package de.seitenbau.ozghub.prozessdeployment.handler;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.Core;
import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.config.Property;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.plugins.PluginAttribute;
import org.apache.logging.log4j.core.config.plugins.PluginElement;
import org.apache.logging.log4j.core.config.plugins.PluginFactory;

import lombok.Getter;

@Plugin(
    name = "ListAppender",
    category = Core.CATEGORY_NAME,
    elementType = Appender.ELEMENT_TYPE)
public class ListAppender extends AbstractAppender
{
  @Getter
  private final List<String> eventList = new ArrayList<>();

  protected ListAppender(String name, Filter filter)
  {
    super(name, filter, null, true, Property.EMPTY_ARRAY);
  }

  @PluginFactory
  public static ListAppender createAppender(
      @PluginAttribute("name") String name,
      @PluginElement("Filter") Filter filter)
  {
    return new ListAppender(name, filter);
  }

  @Override
  public void append(LogEvent event)
  {
    eventList.add(event.getLevel() + " " + event.getMessage().getFormattedMessage());
  }
}