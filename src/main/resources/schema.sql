
-- Departments Table
CREATE TABLE departments (
                             id NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                             name VARCHAR2(100) NOT NULL,
                             manager_id NUMBER
);

-- Employees Table
CREATE TABLE employees (
                           id NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                           employee_id VARCHAR2(20) UNIQUE NOT NULL,
                           full_name VARCHAR2(100) NOT NULL,
                           username VARCHAR2(50) UNIQUE NOT NULL,
                           role VARCHAR2(20) NOT NULL,
                           password_hash VARCHAR2(256) NOT NULL,
                           job_title VARCHAR2(100) NOT NULL,
                           department_id NUMBER REFERENCES departments(id),
                           hire_date DATE NOT NULL,
                           status VARCHAR2(20) NOT NULL,
                           email VARCHAR2(100) UNIQUE NOT NULL,
                           phone VARCHAR2(20),
                           address VARCHAR2(200),
                           created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                           updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);



-- Audit Log Table
CREATE TABLE audit_log (
                           id NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                           action VARCHAR2(50) NOT NULL,
                           entity_type VARCHAR2(50) NOT NULL,
                           entity_id NUMBER NOT NULL,
                           employee_id NUMBER REFERENCES employees(id),
                           timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                           changes CLOB
);

-- Add foreign key for department manager
ALTER TABLE departments
    ADD CONSTRAINT fk_department_manager
        FOREIGN KEY (manager_id) REFERENCES employees(id);

-- Indexes
CREATE INDEX idx_employee_search ON employees(employee_id, full_name, job_title);
CREATE INDEX idx_employee_department ON employees(department_id);
CREATE INDEX idx_audit_entity ON audit_log(entity_type, entity_id);
CREATE INDEX idx_audit_timestamp ON audit_log(timestamp);