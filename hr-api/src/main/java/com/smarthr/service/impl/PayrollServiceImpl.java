package com.smarthr.service.impl;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.Element;
import com.lowagie.text.FontFactory;
import com.lowagie.text.Font;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.smarthr.dto.PayrollDTO;
import com.smarthr.dto.PayrollSaveRequest;
import com.smarthr.entity.Contract;
import com.smarthr.entity.Employee;
import com.smarthr.entity.Payroll;
import com.smarthr.enums.EmployeeStatus;
import com.smarthr.exception.ErrorConstants;
import com.smarthr.exception.ResourceNotFoundException;
import com.smarthr.mapper.PayrollMapper;
import com.smarthr.repository.ContractRepository;
import com.smarthr.repository.EmployeeRepository;
import com.smarthr.repository.PayrollRepository;
import com.smarthr.service.PayrollService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.smarthr.config.PayrollProperties;

import java.awt.Color;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PayrollServiceImpl implements PayrollService {

    private final PayrollRepository payrollRepository;
    private final EmployeeRepository employeeRepository;
    private final ContractRepository contractRepository;
    private final PayrollMapper payrollMapper;
    private final PayrollProperties payrollProperties;

    @Override
    @Transactional
    public PayrollDTO generatePayroll(PayrollSaveRequest request) {
        Employee employee = employeeRepository.findById(request.getEmployeeId())
                .orElseThrow(() -> new ResourceNotFoundException(ErrorConstants.EMPLOYEE_NOT_FOUND, 
                        "Employé introuvable avec l'ID: " + request.getEmployeeId()));

        double baseSalary = getEmployeeBaseSalary(employee);
        double bonus = request.getBonus() != null ? request.getBonus() : 0.0;
        double overtimeHours = request.getOvertimeHours() != null ? request.getOvertimeHours() : 0.0;
        double deductions = request.getDeductions() != null ? request.getDeductions() : 0.0;

        double hourlyRate = baseSalary / payrollProperties.getMonthlyWorkingHours();
        double overtimePay = overtimeHours * (hourlyRate * payrollProperties.getOvertime().getStandardRate());

        double grossSalary = baseSalary + bonus + overtimePay;

        double cnssBasis = Math.min(grossSalary, payrollProperties.getCnss().getLimit());
        double cnssContribution = cnssBasis * payrollProperties.getCnss().getRate();

        double mutualInsurance = grossSalary * payrollProperties.getAmo().getRate();

        double professionalExpenses = Math.min(grossSalary * payrollProperties.getProfessionalExpenses().getRate(), 
                payrollProperties.getProfessionalExpenses().getLimit());

        double insurance = payrollProperties.getInsurance().getFixedAmount() + (grossSalary * payrollProperties.getInsurance().getRate());

        double netTaxable = grossSalary - cnssContribution - mutualInsurance - professionalExpenses;

        double tax = calculateDynamicIR(netTaxable);

        double netSalary = grossSalary - cnssContribution - mutualInsurance - tax - deductions - insurance;

        Payroll payroll = Payroll.builder()
                .employee(employee)
                .payMonth(request.getPayMonth())
                .baseSalary(baseSalary)
                .bonus(bonus)
                .overtimeHours(overtimeHours)
                .overtimePay(overtimePay)
                .grossSalary(grossSalary)
                .cnssContribution(cnssContribution)
                .mutualInsurance(mutualInsurance)
                .tax(tax)
                .insurance(insurance)
                .deductions(deductions)
                .netSalary(netSalary)
                .build();

        Payroll saved = payrollRepository.save(payroll);
        return payrollMapper.toDTO(saved);
    }

    @Override
    @Transactional
    public List<PayrollDTO> generateBulkMonthlyPayrolls(LocalDate payMonth) {
        List<Employee> activeEmployees = employeeRepository.findByStatus(EmployeeStatus.ACTIVE);
        List<PayrollDTO> results = new ArrayList<>();

        for (Employee employee : activeEmployees) {
            PayrollSaveRequest req = new PayrollSaveRequest();
            req.setEmployeeId(employee.getId());
            req.setPayMonth(payMonth);
            req.setBonus(0.0);
            req.setOvertimeHours(0.0);
            req.setDeductions(0.0);
            
            try {
                results.add(generatePayroll(req));
            } catch (Exception e) {
                log.error("Erreur lors de la génération du bulletin de paie pour l'employé: " + employee.getFullName(), e);
            }
        }
        return results;
    }

    @Override
    @Transactional(readOnly = true)
    public PayrollDTO getPayrollById(UUID id) {
        Payroll payroll = payrollRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorConstants.PAYROLL_NOT_FOUND, 
                        "Bulletin de paie introuvable avec l'ID: " + id));
        return payrollMapper.toDTO(payroll);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PayrollDTO> getPayrollsByEmployeeId(UUID employeeId) {
        return payrollRepository.findByEmployeeId(employeeId).stream()
                .map(payrollMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public ByteArrayInputStream exportPayrollToPdf(UUID payrollId) {
        Payroll payroll = payrollRepository.findById(payrollId)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorConstants.PAYROLL_NOT_FOUND, 
                        "Bulletin de paie introuvable avec l'ID: " + payrollId));
        Employee employee = payroll.getEmployee();

        Document document = new Document();
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        try {
            PdfWriter.getInstance(document, out);
            document.open();

            Font companyFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14, Color.BLUE);
            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16, Color.DARK_GRAY);
            Font sectionFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, Color.BLACK);
            Font regularFont = FontFactory.getFont(FontFactory.HELVETICA, 9, Color.BLACK);
            Font boldFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, Color.BLACK);

            Paragraph company = new Paragraph("SmartHR AI - Enterprise Management System", companyFont);
            company.setSpacingAfter(15);
            document.add(company);

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/yyyy");
            Paragraph title = new Paragraph("BULLETIN DE PAIE - MOIS DE " + payroll.getPayMonth().format(formatter), titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            title.setSpacingAfter(25);
            document.add(title);

            PdfPTable infoTable = new PdfPTable(2);
            infoTable.setWidthPercentage(100);
            infoTable.setSpacingAfter(20);

            infoTable.addCell(createCell("Matricule: " + employee.getEmployeeNumber(), regularFont, false));
            infoTable.addCell(createCell("Nom Complet: " + employee.getFullName(), regularFont, false));
            infoTable.addCell(createCell("CIN: " + (employee.getCin() != null ? employee.getCin() : "N/A"), regularFont, false));
            infoTable.addCell(createCell("Date d'embauche: " + employee.getHireDate().toString(), regularFont, false));
            infoTable.addCell(createCell("Département: " + (employee.getDepartment() != null ? employee.getDepartment().getName() : "N/A"), regularFont, false));
            infoTable.addCell(createCell("N° CNSS: " + (employee.getCnss() != null ? employee.getCnss() : "N/A"), regularFont, false));

            document.add(infoTable);

            PdfPTable payTable = new PdfPTable(4);
            payTable.setWidthPercentage(100);
            payTable.setWidths(new int[]{4, 2, 2, 2});
            payTable.setSpacingAfter(20);

            payTable.addCell(createCell("Rubrique", boldFont, true));
            payTable.addCell(createCell("Base", boldFont, true));
            payTable.addCell(createCell("Gains", boldFont, true));
            payTable.addCell(createCell("Retenues", boldFont, true));

            payTable.addCell(createCell("Salaire de base", regularFont, false));
            payTable.addCell(createCell(formatAmount(payroll.getBaseSalary()), regularFont, false));
            payTable.addCell(createCell(formatAmount(payroll.getBaseSalary()), regularFont, false));
            payTable.addCell(createCell("", regularFont, false));

            if (payroll.getBonus() > 0) {
                payTable.addCell(createCell("Primes / Bonus", regularFont, false));
                payTable.addCell(createCell("", regularFont, false));
                payTable.addCell(createCell(formatAmount(payroll.getBonus()), regularFont, false));
                payTable.addCell(createCell("", regularFont, false));
            }

            if (payroll.getOvertimePay() > 0) {
                payTable.addCell(createCell("Heures supplémentaires (" + payroll.getOvertimeHours() + "h)", regularFont, false));
                payTable.addCell(createCell("", regularFont, false));
                payTable.addCell(createCell(formatAmount(payroll.getOvertimePay()), regularFont, false));
                payTable.addCell(createCell("", regularFont, false));
            }

            payTable.addCell(createCell("Cotisation CNSS (4.48%)", regularFont, false));
            payTable.addCell(createCell(formatAmount(Math.min(payroll.getGrossSalary(), 6000.0)), regularFont, false));
            payTable.addCell(createCell("", regularFont, false));
            payTable.addCell(createCell(formatAmount(payroll.getCnssContribution()), regularFont, false));

            payTable.addCell(createCell("Cotisation AMO / Mutuelle (2.26%)", regularFont, false));
            payTable.addCell(createCell(formatAmount(payroll.getGrossSalary()), regularFont, false));
            payTable.addCell(createCell("", regularFont, false));
            payTable.addCell(createCell(formatAmount(payroll.getMutualInsurance()), regularFont, false));

            payTable.addCell(createCell("Impôt sur le Revenu (IR)", regularFont, false));
            payTable.addCell(createCell("", regularFont, false));
            payTable.addCell(createCell("", regularFont, false));
            payTable.addCell(createCell(formatAmount(payroll.getTax()), regularFont, false));

            if (payroll.getInsurance() != null && payroll.getInsurance() > 0) {
                payTable.addCell(createCell("Assurance Privée Complémentaire", regularFont, false));
                payTable.addCell(createCell("", regularFont, false));
                payTable.addCell(createCell("", regularFont, false));
                payTable.addCell(createCell(formatAmount(payroll.getInsurance()), regularFont, false));
            }

            if (payroll.getDeductions() > 0) {
                payTable.addCell(createCell("Retenues diverses", regularFont, false));
                payTable.addCell(createCell("", regularFont, false));
                payTable.addCell(createCell("", regularFont, false));
                payTable.addCell(createCell(formatAmount(payroll.getDeductions()), regularFont, false));
            }

            document.add(payTable);

            PdfPTable totalsTable = new PdfPTable(2);
            totalsTable.setWidthPercentage(40);
            totalsTable.setHorizontalAlignment(Element.ALIGN_RIGHT);

            totalsTable.addCell(createCell("Salaire Brut:", boldFont, false));
            totalsTable.addCell(createCell(formatAmount(payroll.getGrossSalary()) + " MAD", boldFont, false));

            totalsTable.addCell(createCell("Total Retenues:", boldFont, false));
            double insuranceVal = payroll.getInsurance() != null ? payroll.getInsurance() : 0.0;
            double totalRetenues = payroll.getCnssContribution() + payroll.getMutualInsurance() + payroll.getTax() + payroll.getDeductions() + insuranceVal;
            totalsTable.addCell(createCell(formatAmount(totalRetenues) + " MAD", boldFont, false));

            PdfPCell netLabelCell = createCell("NET A PAYER:", boldFont, false);
            netLabelCell.setBackgroundColor(Color.LIGHT_GRAY);
            totalsTable.addCell(netLabelCell);

            PdfPCell netValueCell = createCell(formatAmount(payroll.getNetSalary()) + " MAD", boldFont, false);
            netValueCell.setBackgroundColor(Color.LIGHT_GRAY);
            totalsTable.addCell(netValueCell);

            document.add(totalsTable);

            document.close();
            return new ByteArrayInputStream(out.toByteArray());
        } catch (DocumentException e) {
            throw new RuntimeException(ErrorConstants.PDF_EXPORT_ERROR + ": Erreur lors de la génération du bulletin PDF: " + e.getMessage());
        }
    }

    private double getEmployeeBaseSalary(Employee employee) {
        List<Contract> contracts = contractRepository.findByEmployeeId(employee.getId());
        if (contracts.isEmpty()) {
            return 3120.0;
        }
        Contract activeContract = contracts.get(contracts.size() - 1);
        return activeContract.getSalary() != null ? activeContract.getSalary() : 3120.0;
    }

    private double calculateDynamicIR(double netTaxable) {
        for (PayrollProperties.TaxBracket bracket : payrollProperties.getTaxBrackets()) {
            if (netTaxable >= bracket.getMin() && netTaxable <= bracket.getMax()) {
                return (netTaxable * bracket.getRate()) - bracket.getDeduction();
            }
        }
        return 0.0;
    }

    private PdfPCell createCell(String text, Font font, boolean isHeader) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        if (isHeader) {
            cell.setBackgroundColor(new Color(0, 51, 102));
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        } else {
            cell.setBorder(PdfPCell.NO_BORDER);
        }
        cell.setPadding(6);
        return cell;
    }

    private String formatAmount(double value) {
        return String.format(Locale.US, "%,.2f", value);
    }
}
