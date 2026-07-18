package com.smarthr.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

@Data
@Configuration
@ConfigurationProperties(prefix = "payroll")
public class PayrollProperties {

    private double monthlyWorkingHours = 191.0;
    private CnssProperties cnss = new CnssProperties();
    private AmoProperties amo = new AmoProperties();
    private ProfessionalExpensesProperties professionalExpenses = new ProfessionalExpensesProperties();
    private FamilyDeductionProperties familyDeduction = new FamilyDeductionProperties();
    private OvertimeProperties overtime = new OvertimeProperties();
    private InsuranceProperties insurance = new InsuranceProperties();
    private List<TaxBracket> taxBrackets = new ArrayList<>();

    @Data
    public static class InsuranceProperties {
        private double rate = 0.0;
        private double fixedAmount = 0.0;
    }

    @Data
    public static class CnssProperties {
        private double rate = 0.0448;
        private double limit = 6000.0;
    }

    @Data
    public static class AmoProperties {
        private double rate = 0.0226;
    }

    @Data
    public static class ProfessionalExpensesProperties {
        private double rate = 0.20;
        private double limit = 2500.0;
    }

    @Data
    public static class FamilyDeductionProperties {
        private double amountPerPerson = 30.0;
        private int maxPersons = 6;
    }

    @Data
    public static class OvertimeProperties {
        private double standardRate = 1.25;
    }

    @Data
    public static class TaxBracket {
        private double min;
        private double max;
        private double rate;
        private double deduction;
    }
}
