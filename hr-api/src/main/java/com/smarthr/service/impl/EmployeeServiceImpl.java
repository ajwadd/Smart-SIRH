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
import com.smarthr.dto.EmployeeDTO;
import com.smarthr.dto.EmployeeSaveRequest;
import com.smarthr.entity.Department;
import com.smarthr.entity.Employee;
import com.smarthr.entity.Position;
import com.smarthr.enums.EmployeeStatus;
import com.smarthr.exception.ErrorConstants;
import com.smarthr.exception.ResourceNotFoundException;
import com.smarthr.mapper.EmployeeMapper;
import com.smarthr.repository.DepartmentRepository;
import com.smarthr.repository.EmployeeRepository;
import com.smarthr.repository.PositionRepository;
import com.smarthr.service.EmployeeService;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.hibernate.envers.AuditReader;
import org.hibernate.envers.AuditReaderFactory;
import org.hibernate.envers.query.AuditEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.awt.Color;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmployeeServiceImpl implements EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final DepartmentRepository departmentRepository;
    private final PositionRepository positionRepository;
    private final EmployeeMapper employeeMapper;
    private final EntityManager entityManager;

    @Override
    @Transactional(readOnly = true)
    public Page<EmployeeDTO> getAllEmployees(String keyword, Pageable pageable) {
        Page<Employee> employeePage;
        if (keyword != null && !keyword.trim().isEmpty()) {
            employeePage = employeeRepository.search(keyword, pageable);
        } else {
            employeePage = employeeRepository.findAll(pageable);
        }
        return employeePage.map(employeeMapper::toDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public EmployeeDTO getEmployeeById(UUID id) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorConstants.EMPLOYEE_NOT_FOUND, 
                        "Employé introuvable avec l'ID: " + id));
        return employeeMapper.toDTO(employee);
    }

    @Override
    @Transactional
    public EmployeeDTO createEmployee(EmployeeSaveRequest request) {
        Employee employee = new Employee();
        employeeMapper.updateEntity(request, employee);

        String employeeNumber = generateUniqueEmployeeNumber();
        employee.setEmployeeNumber(employeeNumber);

        setEmployeeRelations(employee, request);

        Employee savedEmployee = employeeRepository.save(employee);
        log.info("Employé créé avec succès avec le matricule: {}", savedEmployee.getEmployeeNumber());
        return employeeMapper.toDTO(savedEmployee);
    }

    @Override
    @Transactional
    public EmployeeDTO updateEmployee(UUID id, EmployeeSaveRequest request) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorConstants.EMPLOYEE_NOT_FOUND, 
                        "Employé introuvable avec l'ID: " + id));

        employeeMapper.updateEntity(request, employee);
        setEmployeeRelations(employee, request);

        Employee updatedEmployee = employeeRepository.save(employee);
        log.info("Employé avec le matricule: {} mis à jour", updatedEmployee.getEmployeeNumber());
        return employeeMapper.toDTO(updatedEmployee);
    }

    @Override
    @Transactional
    public void deleteEmployee(UUID id) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorConstants.EMPLOYEE_NOT_FOUND, 
                        "Employé introuvable avec l'ID: " + id));
        employeeRepository.delete(employee);
        log.info("Employé avec l'ID: {} supprimé", id);
    }

    @Override
    @Transactional
    public EmployeeDTO archiveEmployee(UUID id) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorConstants.EMPLOYEE_NOT_FOUND, 
                        "Employé introuvable avec l'ID: " + id));
        employee.setStatus(EmployeeStatus.INACTIVE);
        Employee updatedEmployee = employeeRepository.save(employee);
        log.info("Employé avec le matricule: {} archivé (INACTIVE)", updatedEmployee.getEmployeeNumber());
        return employeeMapper.toDTO(updatedEmployee);
    }

    @Override
    @Transactional
    public List<EmployeeDTO> importEmployeesFromExcel(MultipartFile file) {
        List<EmployeeDTO> importedEmployees = new ArrayList<>();
        try (Workbook workbook = WorkbookFactory.create(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);
            Iterator<Row> rows = sheet.iterator();

            if (rows.hasNext()) {
                rows.next();
            }

            while (rows.hasNext()) {
                Row currentRow = rows.next();
                
                String firstName = getCellValueAsString(currentRow.getCell(0));
                String lastName = getCellValueAsString(currentRow.getCell(1));
                String email = getCellValueAsString(currentRow.getCell(2));
                String phone = getCellValueAsString(currentRow.getCell(3));
                String hireDateStr = getCellValueAsString(currentRow.getCell(4));

                if (firstName.isEmpty() || lastName.isEmpty() || email.isEmpty()) {
                    continue;
                }

                Employee employee = Employee.builder()
                        .employeeNumber(generateUniqueEmployeeNumber())
                        .firstName(firstName)
                        .lastName(lastName)
                        .email(email)
                        .phone(phone)
                        .hireDate(hireDateStr.isEmpty() ? LocalDate.now() : LocalDate.parse(hireDateStr))
                        .status(EmployeeStatus.ACTIVE)
                        .build();

                Employee saved = employeeRepository.save(employee);
                importedEmployees.add(employeeMapper.toDTO(saved));
            }
        } catch (IOException e) {
            throw new RuntimeException(ErrorConstants.EXCEL_IMPORT_ERROR + ": Erreur lors de l'importation du fichier Excel: " + e.getMessage(), e);
        }
        return importedEmployees;
    }

    @Override
    @Transactional(readOnly = true)
    public ByteArrayInputStream exportEmployeesToExcel() {
        List<Employee> employees = employeeRepository.findAll();

        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Employés");

            org.apache.poi.ss.usermodel.Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerFont.setColor(IndexedColors.WHITE.getIndex());

            CellStyle headerCellStyle = workbook.createCellStyle();
            headerCellStyle.setFont(headerFont);
            headerCellStyle.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
            headerCellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            String[] columns = {"Matricule", "Nom", "Prénom", "Email", "Téléphone", "Statut", "Date d'embauche"};
            Row headerRow = sheet.createRow(0);
            for (int col = 0; col < columns.length; col++) {
                Cell cell = headerRow.createCell(col);
                cell.setCellValue(columns[col]);
                cell.setCellStyle(headerCellStyle);
            }

            int rowIdx = 1;
            for (Employee employee : employees) {
                Row row = sheet.createRow(rowIdx++);

                row.createCell(0).setCellValue(employee.getEmployeeNumber());
                row.createCell(1).setCellValue(employee.getLastName());
                row.createCell(2).setCellValue(employee.getFirstName());
                row.createCell(3).setCellValue(employee.getEmail());
                row.createCell(4).setCellValue(employee.getPhone());
                row.createCell(5).setCellValue(employee.getStatus().name());
                row.createCell(6).setCellValue(employee.getHireDate().toString());
            }

            workbook.write(out);
            return new ByteArrayInputStream(out.toByteArray());
        } catch (IOException e) {
            throw new RuntimeException(ErrorConstants.EXCEL_IMPORT_ERROR + ": Erreur lors de la génération du fichier Excel: " + e.getMessage());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public ByteArrayInputStream exportEmployeesToPdf() {
        List<Employee> employees = employeeRepository.findAll();
        Document document = new Document();
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        try {
            PdfWriter.getInstance(document, out);
            document.open();

            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, Color.DARK_GRAY);
            Paragraph title = new Paragraph("Liste des Employés - SmartHR AI", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            title.setSpacingAfter(20);
            document.add(title);

            PdfPTable table = new PdfPTable(6);
            table.setWidthPercentage(100);
            table.setWidths(new int[]{3, 3, 3, 4, 3, 2});

            Font headFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, Color.WHITE);
            String[] headers = {"Matricule", "Nom", "Prénom", "Email", "Téléphone", "Statut"};
            for (String header : headers) {
                PdfPCell cell = new PdfPCell(new Phrase(header, headFont));
                cell.setBackgroundColor(new Color(0, 51, 102));
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                cell.setPadding(5);
                table.addCell(cell);
            }

            Font cellFont = FontFactory.getFont(FontFactory.HELVETICA, 9);
            for (Employee employee : employees) {
                table.addCell(new PdfPCell(new Phrase(employee.getEmployeeNumber(), cellFont)));
                table.addCell(new PdfPCell(new Phrase(employee.getLastName(), cellFont)));
                table.addCell(new PdfPCell(new Phrase(employee.getFirstName(), cellFont)));
                table.addCell(new PdfPCell(new Phrase(employee.getEmail(), cellFont)));
                table.addCell(new PdfPCell(new Phrase(employee.getPhone() != null ? employee.getPhone() : "", cellFont)));
                table.addCell(new PdfPCell(new Phrase(employee.getStatus().name(), cellFont)));
            }

            document.add(table);
            document.close();
            return new ByteArrayInputStream(out.toByteArray());
        } catch (DocumentException e) {
            throw new RuntimeException(ErrorConstants.PDF_EXPORT_ERROR + ": Erreur lors de la génération du PDF: " + e.getMessage());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<?> getEmployeeRevisionHistory(UUID id) {
        AuditReader auditReader = AuditReaderFactory.get(entityManager);
        return auditReader.createQuery()
                .forRevisionsOfEntity(Employee.class, false, true)
                .add(AuditEntity.id().eq(id))
                .getResultList();
    }

    private String generateUniqueEmployeeNumber() {
        String year = String.valueOf(LocalDate.now().getYear());
        long count = employeeRepository.count() + 1;
        return "EMP-" + year + "-" + String.format("%04d", count);
    }

    private void setEmployeeRelations(Employee employee, EmployeeSaveRequest request) {
        if (request.getDepartmentId() != null) {
            Department department = departmentRepository.findById(request.getDepartmentId())
                    .orElseThrow(() -> new ResourceNotFoundException(ErrorConstants.DEPARTMENT_NOT_FOUND, 
                            "Département introuvable avec l'ID: " + request.getDepartmentId()));
            employee.setDepartment(department);
        } else {
            employee.setDepartment(null);
        }

        if (request.getPositionId() != null) {
            Position position = positionRepository.findById(request.getPositionId())
                    .orElseThrow(() -> new ResourceNotFoundException(ErrorConstants.POSITION_NOT_FOUND, 
                            "Poste introuvable avec l'ID: " + request.getPositionId()));
            employee.setPosition(position);
        } else {
            employee.setPosition(null);
        }

        if (request.getManagerId() != null) {
            Employee manager = employeeRepository.findById(request.getManagerId())
                    .orElseThrow(() -> new ResourceNotFoundException(ErrorConstants.EMPLOYEE_NOT_FOUND, 
                            "Manager introuvable avec l'ID: " + request.getManagerId()));
            employee.setManager(manager);
        } else {
            employee.setManager(null);
        }
    }

    private String getCellValueAsString(Cell cell) {
        if (cell == null) {
            return "";
        }
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue().trim();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getLocalDateTimeCellValue().toLocalDate().toString();
                }
                return String.valueOf((long) cell.getNumericCellValue());
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            default:
                return "";
        }
    }
}
