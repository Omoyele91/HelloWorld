package com.stanbic.bua.dto;


import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class ValidateCementOrderResponse {
    private String responseCode,responseMessage;
    private OtherResponseDetails otherResponseDetails;
}
