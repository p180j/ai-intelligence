package com.aiintelligence.quality;

import javax.validation.constraints.NotNull;

public class EvalCaseRequest {
    @NotNull public Long articleAId;@NotNull public Long articleBId;@NotNull public MatchExpectation expectedResult;public String notes;
}
