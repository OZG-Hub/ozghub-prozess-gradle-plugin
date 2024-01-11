package de.seitenbau.ozghub.prozessdeployment.model.response;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProcessDeploymentList
{
  private boolean complete;

  private List<ProcessDeployment> value;
}
