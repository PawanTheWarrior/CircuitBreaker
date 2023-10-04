package com.rest.loans.entities;

import lombok.*;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Loan {
    Integer id;
    String type;
    Double amount;
    Double interest;
}
