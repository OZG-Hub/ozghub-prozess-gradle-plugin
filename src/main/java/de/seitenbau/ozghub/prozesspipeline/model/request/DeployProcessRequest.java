package de.seitenbau.ozghub.prozesspipeline.model.request;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
public class DeployProcessRequest implements Serializable
{
  private String deploymentArchiveBase64;

  private Map<String, ProcessMetadata> metadata;
}
