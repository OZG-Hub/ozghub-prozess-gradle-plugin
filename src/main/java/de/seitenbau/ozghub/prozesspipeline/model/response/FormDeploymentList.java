package de.seitenbau.ozghub.prozesspipeline.model.response;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FormDeploymentList
{
  private List<FormDeployment> deployments;
}
