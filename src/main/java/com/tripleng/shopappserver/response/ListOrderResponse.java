package com.tripleng.shopappserver.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;


@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ListOrderResponse {
    private List<OrderResponse> orderResponses;
    private int totalPages;
}
