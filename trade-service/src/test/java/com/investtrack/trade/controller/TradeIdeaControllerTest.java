package com.investtrack.trade.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.investtrack.common.dto.TradeIdeaRequest;
import com.investtrack.common.dto.TradeIdeaResponse;
import com.investtrack.common.enums.Timeframe;
import com.investtrack.common.enums.TradeReason;
import com.investtrack.common.enums.TradeStatus;
import com.investtrack.common.security.JwtUtils;
import com.investtrack.trade.security.JwtAuthenticationFilter;
import com.investtrack.trade.service.TradeIdeaService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.bean.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(
        controllers = TradeIdeaController.class,
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = JwtAuthenticationFilter.class
        )
)
class TradeIdeaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private TradeIdeaService tradeIdeaService;

    @MockBean
    private JwtUtils jwtUtils;

    private TradeIdeaRequest validRequest;
    private TradeIdeaResponse tradeResponse;
    private UUID tradeId;

    @BeforeEach
    void setUp() {
        tradeId = UUID.randomUUID();

        validRequest = new TradeIdeaRequest();
        validRequest.setStockSymbol("RELIANCE");
        validRequest.setEntryPrice(new BigDecimal("2500.00"));
        validRequest.setStopLoss(new BigDecimal("2450.00"));
        validRequest.setTargetPrice(new BigDecimal("2600.00"));
        validRequest.setTimeframe(Timeframe.SWING);
        validRequest.setReason(TradeReason.TECHNICAL);
        validRequest.setRiskPercentage(new BigDecimal("2.00"));

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

    @Test
    @DisplayName("GET /trades — should return paginated trade ideas")
    void shouldReturnPaginatedTrades() throws Exception {
        Page<TradeIdeaResponse> page = new PageImpl<>(List.of(tradeResponse));
        when(tradeIdeaService.getTrades(any(), any(), any(), any(), any(), any(), any(Pageable.class)))
                .thenReturn(page);

        mockMvc.perform(get("/trades"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].stockSymbol").value("RELIANCE"));
    }

    @Test
    @DisplayName("GET /trades/{id} — should return trade by ID")
    void shouldReturnTradeById() throws Exception {
        when(tradeIdeaService.getTradeById(tradeId)).thenReturn(tradeResponse);

        mockMvc.perform(get("/trades/{id}", tradeId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.stockSymbol").value("RELIANCE"))
                .andExpect(jsonPath("$.riskRewardRatio").value(2.00));
    }

    @Test
    @DisplayName("GET /trades/user/{userId} — should return user's trades")
    void shouldReturnTradesByUser() throws Exception {
        UUID userId = UUID.randomUUID();
        Page<TradeIdeaResponse> page = new PageImpl<>(List.of(tradeResponse));
        when(tradeIdeaService.getTradesByUserId(eq(userId), any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/trades/user/{userId}", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].traderUsername").value("testtrader"));
    }

    @Test
    @DisplayName("POST /trades — should return 401 when unauthenticated")
    void shouldReturn401WhenUnauthenticated() throws Exception {
        mockMvc.perform(post("/trades")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "testtrader")
    @DisplayName("POST /trades — should return 400 for invalid request")
    void shouldReturn400ForInvalidRequest() throws Exception {
        TradeIdeaRequest invalidRequest = new TradeIdeaRequest();
        // Missing all required fields

        mockMvc.perform(post("/trades")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }
}
