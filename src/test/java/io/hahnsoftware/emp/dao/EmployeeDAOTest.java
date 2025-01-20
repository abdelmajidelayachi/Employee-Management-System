package io.hahnsoftware.emp.dao;

import io.hahnsoftware.emp.model.*;
import io.hahnsoftware.emp.util.DatabaseConnection;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mindrot.jbcrypt.BCrypt;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.*;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmployeeDAOTest {

    @Mock private Connection mockConnection;
    @Mock private PreparedStatement mockPreparedStatement;
    @Mock private ResultSet mockResultSet;

    private Employee testEmployee;
    private Department testDepartment;
    private MockedStatic<DatabaseConnection> mockedDatabaseConnection = null;

    @BeforeEach
    void setUp() throws SQLException {
        // Clean up any existing mock before creating a new one
        if (mockedDatabaseConnection != null) {
            mockedDatabaseConnection.close();
        }

        // Mock static DatabaseConnection
        mockedDatabaseConnection = mockStatic(DatabaseConnection.class);
        mockedDatabaseConnection.when(DatabaseConnection::getConnection).thenReturn(mockConnection);

        // Initialize test data
        testDepartment = new Department();
        testDepartment.setId(1L);
        testDepartment.setName("IT Department");

        Employee manager = new Employee();
        manager.setId(1L);
        manager.setRole(UserRole.MANAGER);
        testDepartment.setManager(manager);

        testEmployee = new Employee();
        testEmployee.setId(1L);
        testEmployee.setEmployeeId("EMP001");
        testEmployee.setFullName("John Doe");
        testEmployee.setUsername("johndoe");
        testEmployee.setPasswordHash(BCrypt.hashpw("password123", BCrypt.gensalt()));
        testEmployee.setRole(UserRole.EMPLOYEE);
        testEmployee.setJobTitle("Software Engineer");
        testEmployee.setDepartment(testDepartment);
        testEmployee.setHireDate(LocalDate.now());
        testEmployee.setStatus(EmploymentStatus.ACTIVE);
        testEmployee.setEmail("john@example.com");
        testEmployee.setPhone("1234567890");
        testEmployee.setAddress("123 Main St");

        // Set up the action user for audit logging
        Employee adminUser = new Employee();
        adminUser.setId(999L);
        adminUser.setRole(UserRole.ADMINISTRATOR);
        AuditDAO.setActionUser(adminUser);

        // Mock Connection behavior
        lenient().when(mockConnection.prepareStatement(anyString(), any(String[].class)))
                .thenReturn(mockPreparedStatement);
        lenient().when(mockConnection.prepareStatement(anyString()))
                .thenReturn(mockPreparedStatement);
        lenient().when(mockConnection.createStatement())
                .thenReturn(mock(Statement.class));
    }

    @AfterEach
    void cleanUp() {
        if (mockedDatabaseConnection != null) {
            mockedDatabaseConnection.close();
            mockedDatabaseConnection = null;
        }
    }

    @Test
    void createEmployee_Success() throws SQLException {
        lenient().when(mockPreparedStatement.executeUpdate()).thenReturn(1);
        lenient().when(mockPreparedStatement.getGeneratedKeys()).thenReturn(mockResultSet);
        lenient().when(mockResultSet.next()).thenReturn(true);
        lenient().when(mockResultSet.getLong(1)).thenReturn(1L);


        EmployeeDAO employeeDAO = new EmployeeDAO();
        Employee result = employeeDAO.create(testEmployee, "password123", true);

        assertNotNull(result);
        assertEquals(testEmployee.getEmployeeId(), result.getEmployeeId());
        verify(mockPreparedStatement, times(2)).executeUpdate(); // Verify 2 executeUpdate calls
        verify(mockConnection, times(2)).commit(); // Changed to expect 2 commit calls
    }
    @Test
    void findByUsername_Success() throws SQLException {

        lenient().when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        lenient().when(mockResultSet.next()).thenReturn(true);
        setupMockResultSet(mockResultSet, testEmployee);

        // Act
        EmployeeDAO employeeDAO = new EmployeeDAO();
        Employee result = employeeDAO.findByUsername("johndoe");

        // Assert
        assertNotNull(result);
        assertEquals(testEmployee.getUsername(), result.getUsername());
    }

    @Test
    void validateCredentials_Success() throws SQLException {

        lenient().when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        lenient().when(mockResultSet.next()).thenReturn(true);
        setupMockResultSet(mockResultSet, testEmployee);

        // Act
        EmployeeDAO employeeDAO = new EmployeeDAO();
        Employee result = employeeDAO.validateCredentials("johndoe", "password123");

        // Assert
        assertNotNull(result);
        assertEquals(testEmployee.getUsername(), result.getUsername());
    }

    private void setupMockResultSet(ResultSet rs, Employee employee) throws SQLException {
        lenient().when(rs.getLong("id")).thenReturn(employee.getId());
        lenient().when(rs.getString("employee_id")).thenReturn(employee.getEmployeeId());
        lenient().when(rs.getString("full_name")).thenReturn(employee.getFullName());
        lenient().when(rs.getString("username")).thenReturn(employee.getUsername());
        lenient().when(rs.getString("password_hash")).thenReturn(employee.getPasswordHash());
        lenient().when(rs.getString("role")).thenReturn(employee.getRole().name());
        lenient().when(rs.getString("job_title")).thenReturn(employee.getJobTitle());
        lenient().when(rs.getLong("department_id")).thenReturn(employee.getDepartment().getId());
        lenient().when(rs.getDate("hire_date")).thenReturn(Date.valueOf(employee.getHireDate()));
        lenient().when(rs.getString("status")).thenReturn(employee.getStatus().name());
        lenient().when(rs.getString("email")).thenReturn(employee.getEmail());
        lenient().when(rs.getString("phone")).thenReturn(employee.getPhone());
        lenient().when(rs.getString("address")).thenReturn(employee.getAddress());
        lenient().when(rs.wasNull()).thenReturn(false);
    }
}