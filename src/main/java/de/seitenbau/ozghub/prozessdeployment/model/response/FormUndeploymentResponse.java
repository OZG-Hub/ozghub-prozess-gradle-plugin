package de.seitenbau.ozghub.prozessdeployment.model.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FormUndeploymentResponse
{
  /** ID des undeployten Formulars. */
  private String id;
}
