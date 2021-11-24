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
public class ProcessDeploymentList
{
  private boolean complete;

  private List<ProcessDeployment> value;
}
