package de.seitenbau.ozghub.prozessdeployment.helper;


import java.util.List;
import java.util.Set;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TestResponse
{
  private int intValue;

  private double doubleValue;

  private String strValue;

  private List<String> listValue;

  private Set<Integer> setValue;
}
