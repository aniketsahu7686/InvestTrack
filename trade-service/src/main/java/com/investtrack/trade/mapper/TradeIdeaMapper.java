package com.investtrack.trade.mapper;

import com.investtrack.common.dto.TradeIdeaRequest;
import com.investtrack.common.dto.TradeIdeaResponse;
import com.investtrack.trade.entity.TradeIdea;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * MapStruct mapper for TradeIdea entity ↔ DTO conversions.
 * Fields like userId, riskRewardRatio, and status are set by the service layer.
 */
@Mapper(componentModel = "spring")
public interface TradeIdeaMapper {

    /**
     * Maps a request DTO to a TradeIdea entity.
     * System-controlled fields are ignored (set by service layer).
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "userId", ignore = true)
    @Mapping(target = "traderUsername", ignore = true)
    @Mapping(target = "riskRewardRatio", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "closedAt", ignore = true)
    TradeIdea toEntity(TradeIdeaRequest request);

    /**
     * Maps a TradeIdea entity to a response DTO.
     */
    TradeIdeaResponse toResponse(TradeIdea tradeIdea);
}
