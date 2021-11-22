package de.seitenbau.ozghub.prozesspipeline.model.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeploymentList
{
  private boolean complete;

  private List<Deployment> value;
}
