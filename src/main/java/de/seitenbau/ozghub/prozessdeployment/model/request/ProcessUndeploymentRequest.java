package de.seitenbau.ozghub.prozessdeployment.model.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProcessUndeploymentRequest
{
  private Message undeploymentMessage;
}
