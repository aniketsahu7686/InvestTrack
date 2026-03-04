package com.investtrack.trade.service;

import com.investtrack.common.dto.TradeIdeaRequest;
import com.investtrack.common.dto.TradeIdeaResponse;
import com.investtrack.common.enums.Timeframe;
import com.investtrack.common.enums.TradeReason;
import com.investtrack.common.enums.TradeStatus;
import com.investtrack.common.event.TradeStatusEvent;
import com.investtrack.common.exception.BusinessValidationException;
import com.investtrack.common.exception.ResourceNotFoundException;
import com.investtrack.trade.entity.TradeIdea;
import com.investtrack.trade.mapper.TradeIdeaMapper;
import com.investtrack.trade.publisher.TradeEventPublisher;
import com.investtrack.trade.repository.TradeIdeaRepository;
import com.investtrack.trade.specification.TradeIdeaSpecification;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Service layer for trade idea management.
 * Handles creation, retrieval, filtering, and auto-close logic.
 *
 * <p>Business rules:</p>
 * <ul>
 *   <li>Risk/Reward ratio must be >= 1</li>
 *   <li>Stop-loss must be below entry price (for long trades)</li>
 *   <li>Target must be above entry price (for long trades)</li>
 *   <li>No editing after submission</li>
 *   <li>R:R is auto-calculated: (target - entry) / (entry - stopLoss)</li>
 * </ul>
 */
@Service
@RequiredArgsConstructor
public class TradeIdeaService {

    private static final Logger log = LoggerFactory.getLogger(TradeIdeaService.class);

    private final TradeIdeaRepository tradeIdeaRepository;
    private final TradeIdeaMapper tradeIdeaMapper;
    private final TradeEventPublisher tradeEventPublisher;

    /**
     * Creates a new trade idea after validation.
     * The riskRewardRatio is auto-calculated and must be >= 1.
     *
     * @param request  the trade idea request DTO
     * @param userId   the authenticated user's UUID
     * @param username the authenticated user's username
     * @return the created trade idea
     */
    @Transactional
    public TradeIdeaResponse createTrade(TradeIdeaRequest request, UUID userId, String username) {
        log.info("Creating trade idea for user {} on symbol {}", username, request.getStockSymbol());

        // Validate price logic: SL < entry < target (for long trades)
        validatePriceLogic(request);

        // Auto-calculate risk/reward ratio
        BigDecimal riskRewardRatio = calculateRiskRewardRatio(
                request.getEntryPrice(),
                request.getStopLoss(),
                request.getTargetPrice()
        );

        // Business rule: R:R must be >= 1
        if (riskRewardRatio.compareTo(BigDecimal.ONE) < 0) {
            throw new BusinessValidationException(
                    "Risk/Reward ratio must be >= 1. Calculated: " + riskRewardRatio);
        }

        // Map and persist
        TradeIdea tradeIdea = tradeIdeaMapper.toEntity(request);
        tradeIdea.setUserId(userId);
        tradeIdea.setTraderUsername(username);
        tradeIdea.setRiskRewardRatio(riskRewardRatio);
        tradeIdea.setStockSymbol(request.getStockSymbol().toUpperCase());

        TradeIdea savedTrade = tradeIdeaRepository.save(tradeIdea);
        log.info("Trade idea created: id={}, symbol={}, R:R={}",
                savedTrade.getId(), savedTrade.getStockSymbol(), riskRewardRatio);

        return tradeIdeaMapper.toResponse(savedTrade);
    }

    /**
     * Retrieves a trade idea by its ID.
     */
    @Transactional(readOnly = true)
    public TradeIdeaResponse getTradeById(UUID tradeId) {
        TradeIdea trade = tradeIdeaRepository.findById(tradeId)
                .orElseThrow(() -> new ResourceNotFoundException("Trade", "id", tradeId));
        return tradeIdeaMapper.toResponse(trade);
    }

    /**
     * Retrieves all trades for a specific user (non-paginated).
     */
    @Transactional(readOnly = true)
    public List<TradeIdeaResponse> getTradesByUserId(UUID userId) {
        return tradeIdeaRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(tradeIdeaMapper::toResponse)
                .toList();
    }

    /**
     * Retrieves trades for a specific user with pagination.
     */
    @Transactional(readOnly = true)
    public Page<TradeIdeaResponse> getTradesByUserId(UUID userId, Pageable pageable) {
        Specification<TradeIdea> spec = Specification.where(TradeIdeaSpecification.hasUserId(userId));
        return tradeIdeaRepository.findAll(spec, pageable)
                .map(tradeIdeaMapper::toResponse);
    }

    /**
     * Retrieves trades with dynamic filters and pagination.
     */
    @Transactional(readOnly = true)
    public Page<TradeIdeaResponse> getTrades(Timeframe timeframe,
                                              TradeStatus status,
                                              TradeReason reason,
                                              String stockSymbol,
                                              BigDecimal minRiskReward,
                                              UUID userId,
                                              Pageable pageable) {
        Specification<TradeIdea> spec = Specification.where(TradeIdeaSpecification.hasTimeframe(timeframe))
                .and(TradeIdeaSpecification.hasStatus(status))
                .and(TradeIdeaSpecification.hasReason(reason != null ? reason.name() : null))
                .and(TradeIdeaSpecification.hasMinRiskReward(minRiskReward))
                .and(TradeIdeaSpecification.hasStockSymbol(stockSymbol))
                .and(TradeIdeaSpecification.hasUserId(userId));

        return tradeIdeaRepository.findAll(spec, pageable)
                .map(tradeIdeaMapper::toResponse);
    }

    /**
     * Auto-closes a trade when a price update triggers target or stop-loss hit.
     * Called by the PriceUpdateListener.
     *
     * @param tradeId   the trade to close
     * @param newStatus the new status (TARGET_HIT or SL_HIT)
     * @param exitPrice the price at which the trade was closed (LTP)
     */
    @Transactional
    public void autoCloseTrade(UUID tradeId, TradeStatus newStatus, BigDecimal exitPrice) {
        TradeIdea trade = tradeIdeaRepository.findById(tradeId)
                .orElseThrow(() -> new ResourceNotFoundException("Trade", "id", tradeId));

        if (trade.getStatus() != TradeStatus.OPEN) {
            log.warn("Trade {} is already closed with status {}", tradeId, trade.getStatus());
            return;
        }

        TradeStatus previousStatus = trade.getStatus();
        trade.setStatus(newStatus);
        trade.setClosedAt(LocalDateTime.now());
        tradeIdeaRepository.save(trade);

        log.info("Trade auto-closed: id={}, status={}, exitPrice={}", tradeId, newStatus, exitPrice);

        // Publish event for analytics service
        TradeStatusEvent event = TradeStatusEvent.builder()
                .tradeId(trade.getId())
                .userId(trade.getUserId())
                .stockSymbol(trade.getStockSymbol())
                .previousStatus(previousStatus)
                .newStatus(newStatus)
                .entryPrice(trade.getEntryPrice())
                .exitPrice(exitPrice)
                .stopLoss(trade.getStopLoss())
                .targetPrice(trade.getTargetPrice())
                .riskRewardRatio(trade.getRiskRewardRatio())
                .closedAt(trade.getClosedAt())
                .build();

        tradeEventPublisher.publishTradeStatusEvent(event);
    }

    /**
     * Validates that the price structure is logical.
     * For long trades: stopLoss < entryPrice < targetPrice.
     */
    private void validatePriceLogic(TradeIdeaRequest request) {
        BigDecimal entry = request.getEntryPrice();
        BigDecimal sl = request.getStopLoss();
        BigDecimal target = request.getTargetPrice();

        if (sl.compareTo(entry) >= 0) {
            throw new BusinessValidationException("Stop loss must be below entry price");
        }
        if (target.compareTo(entry) <= 0) {
            throw new BusinessValidationException("Target price must be above entry price");
        }
    }

    /**
     * Calculates risk/reward ratio: (target - entry) / (entry - stopLoss).
     */
    private BigDecimal calculateRiskRewardRatio(BigDecimal entry, BigDecimal stopLoss, BigDecimal target) {
        BigDecimal reward = target.subtract(entry);
        BigDecimal risk = entry.subtract(stopLoss);

        if (risk.compareTo(BigDecimal.ZERO) == 0) {
            throw new BusinessValidationException("Risk cannot be zero — entry and stop loss cannot be the same");
        }

        return reward.divide(risk, 2, RoundingMode.HALF_UP);
    }
}
