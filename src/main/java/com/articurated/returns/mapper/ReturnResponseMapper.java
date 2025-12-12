package com.articurated.returns.mapper;

import com.articurated.returns.domain.Return;
import com.articurated.returns.dto.ReturnResponse;

public interface ReturnResponseMapper {
    ReturnResponse toResponse(Return returnEntity);
}
