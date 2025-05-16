package de.seitenbau.ozghub.prozessdeployment.model.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ProcessEngine
{
  /**
   * ID of the process-engine. Can be used to specify the engine for deployment.
   */
  private String id;

  /**
   * Name of the process-engine.
   */
  private String name;
}
