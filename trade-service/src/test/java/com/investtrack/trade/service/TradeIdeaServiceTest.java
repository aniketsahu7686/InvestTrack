package com.investtrack.trade.service;

import com.investtrack.common.dto.TradeIdeaRequest;
import com.investtrack.common.dto.TradeIdeaResponse;
import com.investtrack.common.enums.Timeframe;
import com.investtrack.common.enums.TradeReason;
import com.investtrack.common.enums.TradeStatus;
import com.investtrack.common.exception.BusinessValidationException;
import com.investtrack.common.exception.ResourceNotFoundException;
import com.investtrack.trade.entity.TradeIdea;
import com.investtrack.trade.mapper.TradeIdeaMapper;
import com.investtrack.trade.publisher.TradeEventPublisher;
import com.investtrack.trade.repository.TradeIdeaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TradeIdeaServiceTest {

    @Mock
    private TradeIdeaRepository tradeIdeaRepository;

    @Mock
    private TradeIdeaMapper tradeIdeaMapper;

    @Mock
    private TradeEventPublisher tradeEventPublisher;

    @InjectMocks
    private TradeIdeaService tradeIdeaService;

    private TradeIdeaRequest validRequest;
    private TradeIdea tradeEntity;
    private TradeIdeaResponse tradeResponse;
    private UUID userId;
    private UUID tradeId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        tradeId = UUID.randomUUID();

        validRequest = new TradeIdeaRequest();
        validRequest.setStockSymbol("RELIANCE");
        validRequest.setEntryPrice(new BigDecimal("2500.00"));
        validRequest.setStopLoss(new BigDecimal("2450.00"));
        validRequest.setTargetPrice(new BigDecimal("2600.00"));
        validRequest.setTimeframe(Timeframe.SWING);
        validRequest.setReason(TradeReason.TECHNICAL);
        validRequest.setRiskPercentage(new BigDecimal("2.00"));
        validRequest.setNotes("Breakout above resistance");

        tradeEntity = new TradeIdea();
        tradeEntity.setId(tradeId);
        tradeEntity.setUserId(userId);
        tradeEntity.setTraderUsername("testtrader");
        tradeEntity.setStockSymbol("RELIANCE");
        tradeEntity.setEntryPrice(new BigDecimal("2500.00"));
        tradeEntity.setStopLoss(new BigDecimal("2450.00"));
        tradeEntity.setTargetPrice(new BigDecimal("2600.00"));
        tradeEntity.setRiskRewardRatio(new BigDecimal("2.00"));
        tradeEntity.setTimeframe(Timeframe.SWING);
        tradeEntity.setReason(TradeReason.TECHNICAL);
        tradeEntity.setStatus(TradeStatus.OPEN);
        tradeEntity.setCreatedAt(LocalDateTime.now());

        tradeResponse = new TradeIdeaResponse();
        tradeResponse.setId(tradeId);
        tradeResponse.setStockSymbol("RELIANCE");
        tradeResponse.setEntryPrice(new BigDecimal("2500.00"));
        tradeResponse.setStopLoss(new BigDecimal("2450.00"));
        tradeResponse.setTargetPrice(new BigDecimal("2600.00"));
        tradeResponse.setRiskRewardRatio(new BigDecimal("2.00"));
        tradeResponse.setStatus(TradeStatus.OPEN);
        tradeResponse.setTraderUsername("testtrader");
    }

    @Nested
    @DisplayName("createTrade()")
    class CreateTradeTests {

        @Test
        @DisplayName("Should create trade successfully with valid data")
        void shouldCreateTradeSuccessfully() {
            when(tradeIdeaMapper.toEntity(any(TradeIdeaRequest.class))).thenReturn(tradeEntity);
            when(tradeIdeaRepository.save(any(TradeIdea.class))).thenReturn(tradeEntity);
            when(tradeIdeaMapper.toResponse(any(TradeIdea.class))).thenReturn(tradeResponse);

            TradeIdeaResponse result = tradeIdeaService.createTrade(validRequest, userId, "testtrader");

            assertThat(result).isNotNull();
            assertThat(result.getStockSymbol()).isEqualTo("RELIANCE");
            assertThat(result.getStatus()).isEqualTo(TradeStatus.OPEN);
            verify(tradeIdeaRepository).save(any(TradeIdea.class));
        }

        @Test
        @DisplayName("Should reject trade when stop loss >= entry price")
        void shouldRejectWhenStopLossAboveEntry() {
            validRequest.setStopLoss(new BigDecimal("2600.00"));

            assertThatThrownBy(() -> tradeIdeaService.createTrade(validRequest, userId, "testtrader"))
                    .isInstanceOf(BusinessValidationException.class)
                    .hasMessageContaining("Stop loss must be below entry price");
        }

        @Test
        @DisplayName("Should reject trade when target price <= entry price")
        void shouldRejectWhenTargetBelowEntry() {
            validRequest.setTargetPrice(new BigDecimal("2400.00"));

            assertThatThrownBy(() -> tradeIdeaService.createTrade(validRequest, userId, "testtrader"))
                    .isInstanceOf(BusinessValidationException.class)
                    .hasMessageContaining("Target price must be above entry price");
        }

        @Test
        @DisplayName("Should reject trade when R:R < 1")
        void shouldRejectWhenRiskRewardBelowOne() {
            // Entry: 2500, SL: 2400 (risk=100), Target: 2550 (reward=50) → R:R = 0.5
            validRequest.setStopLoss(new BigDecimal("2400.00"));
            validRequest.setTargetPrice(new BigDecimal("2550.00"));

            when(tradeIdeaMapper.toEntity(any(TradeIdeaRequest.class))).thenReturn(tradeEntity);

            assertThatThrownBy(() -> tradeIdeaService.createTrade(validRequest, userId, "testtrader"))
                    .isInstanceOf(BusinessValidationException.class)
                    .hasMessageContaining("Risk/Reward ratio must be >= 1");
        }

        @Test
        @DisplayName("Should uppercase stock symbol")
        void shouldUppercaseStockSymbol() {
            validRequest.setStockSymbol("reliance");
            when(tradeIdeaMapper.toEntity(any(TradeIdeaRequest.class))).thenReturn(tradeEntity);
            when(tradeIdeaRepository.save(any(TradeIdea.class))).thenReturn(tradeEntity);
            when(tradeIdeaMapper.toResponse(any(TradeIdea.class))).thenReturn(tradeResponse);

            tradeIdeaService.createTrade(validRequest, userId, "testtrader");

            verify(tradeIdeaRepository).save(argThat(trade ->
                    trade.getStockSymbol().equals("RELIANCE")
            ));
        }
    }

    @Nested
    @DisplayName("getTradeById()")
    class GetTradeByIdTests {

        @Test
        @DisplayName("Should return trade when found")
        void shouldReturnTradeWhenFound() {
            when(tradeIdeaRepository.findById(tradeId)).thenReturn(Optional.of(tradeEntity));
            when(tradeIdeaMapper.toResponse(tradeEntity)).thenReturn(tradeResponse);

            TradeIdeaResponse result = tradeIdeaService.getTradeById(tradeId);

            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(tradeId);
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException when not found")
        void shouldThrowWhenNotFound() {
            UUID notExistId = UUID.randomUUID();
            when(tradeIdeaRepository.findById(notExistId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> tradeIdeaService.getTradeById(notExistId))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("autoCloseTrade()")
    class AutoCloseTradeTests {

        @Test
        @DisplayName("Should auto-close trade with TARGET_HIT")
        void shouldAutoCloseTargetHit() {
            when(tradeIdeaRepository.findById(tradeId)).thenReturn(Optional.of(tradeEntity));
            when(tradeIdeaRepository.save(any(TradeIdea.class))).thenReturn(tradeEntity);

            tradeIdeaService.autoCloseTrade(tradeId, TradeStatus.TARGET_HIT, new BigDecimal("2600.00"));

            verify(tradeIdeaRepository).save(argThat(trade ->
                    trade.getStatus() == TradeStatus.TARGET_HIT && trade.getClosedAt() != null
            ));
            verify(tradeEventPublisher).publishTradeStatusEvent(any());
        }

        @Test
        @DisplayName("Should auto-close trade with SL_HIT")
        void shouldAutoCloseSlHit() {
            when(tradeIdeaRepository.findById(tradeId)).thenReturn(Optional.of(tradeEntity));
            when(tradeIdeaRepository.save(any(TradeIdea.class))).thenReturn(tradeEntity);

            tradeIdeaService.autoCloseTrade(tradeId, TradeStatus.SL_HIT, new BigDecimal("2450.00"));

            verify(tradeIdeaRepository).save(argThat(trade ->
                    trade.getStatus() == TradeStatus.SL_HIT && trade.getClosedAt() != null
            ));
            verify(tradeEventPublisher).publishTradeStatusEvent(any());
        }

        @Test
        @DisplayName("Should skip if trade is already closed")
        void shouldSkipIfAlreadyClosed() {
            tradeEntity.setStatus(TradeStatus.TARGET_HIT);
            when(tradeIdeaRepository.findById(tradeId)).thenReturn(Optional.of(tradeEntity));

            tradeIdeaService.autoCloseTrade(tradeId, TradeStatus.SL_HIT, new BigDecimal("2450.00"));

            verify(tradeIdeaRepository, never()).save(any());
            verify(tradeEventPublisher, never()).publishTradeStatusEvent(any());
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException for missing trade")
        void shouldThrowForMissingTrade() {
            UUID missingId = UUID.randomUUID();
            when(tradeIdeaRepository.findById(missingId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> tradeIdeaService.autoCloseTrade(missingId, TradeStatus.TARGET_HIT, BigDecimal.ZERO))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }
}
