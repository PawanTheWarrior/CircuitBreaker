package com.rest.loans.services;

import com.rest.loans.dtos.InterestRate;
import com.rest.loans.entities.Loan;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;
import java.util.*;

@Service
public class LoanService {

    @Autowired
    private RestTemplate restTemplate;

    private Map<String,Loan> loanMap;

    public LoanService(){
        loanMap = new HashMap<>();
    }


    private static final String SERVICE_NAME = "loan-service";

    private static final String RATE_SERVICE_URL = "http://localhost:9000/api/rates/";

    @CircuitBreaker(name = SERVICE_NAME, fallbackMethod = "getDefaultLoans")
    public List<Loan> getAllLoansByType(String type) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<InterestRate> entity = new HttpEntity<InterestRate>(null, headers);
        ResponseEntity<InterestRate> response = restTemplate.exchange(
            (RATE_SERVICE_URL + type),
            HttpMethod.GET, entity,
            InterestRate.class
        );
        InterestRate rate = response.getBody();
        List<Loan> loanList = new ArrayList<Loan>();
        if (rate != null) {
            loanList = findByType(type);
            for (Loan loan : loanList) {
                loan.setInterest(loan.getAmount() * (rate.getRateValue() / 100));
            }
        }
        return loanList;
    }

    public List<Loan> getDefaultLoans(Exception e) {
        return new ArrayList<Loan>();
    }

    private List<Loan> findByType(String type){
        List<Loan> loanList = new ArrayList<Loan>();
        if(loanMap.containsKey(type)){
             loanList.add(loanMap.get(type));
             return loanList;
        }
        return null;
    }

    @PostConstruct
    public void setupData() {
        loanMap.put("PERSONAL",createLoanObj(1,"PERSONAL",200000.0,0.0));
        loanMap.put("HOUSING",createLoanObj(2,"HOUSING",200000.0,0.0));
    }

    private Loan createLoanObj(Integer id,String type,Double amount,Double interest){
        Loan loanObj = new Loan();
        loanObj.setId(id);
        loanObj.setType(type);
        loanObj.setAmount(amount);
        loanObj.setInterest(interest);
        return loanObj;
    }
}
