package de.seitenbau.ozghub.prozesspipeline.model.response;

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
