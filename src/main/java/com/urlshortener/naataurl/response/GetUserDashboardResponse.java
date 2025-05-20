package com.urlshortener.naataurl.response;

import lombok.Data;

import java.util.List;

@Data
public class GetUserDashboardResponse {
    private UserResponse userResponse;
    private List<GetUrlInfoResponse> urlsMappedList;
    private Long availableCredits;
}
