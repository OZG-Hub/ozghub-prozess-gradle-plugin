package de.seitenbau.ozghub.prozesspipeline.model.response;

import java.util.Set;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProcessUndeploymentResponse
{
  /** Prozess-Keys von Prozessen, welche undeployt wurden. */
  private Set<String> processKeys;
}
