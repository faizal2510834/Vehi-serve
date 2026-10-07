-- WARNING: Running this script will DROP existing tables and delete all existing data!
-- Execute with caution.

BEGIN
    EXECUTE IMMEDIATE 'DROP TABLE SERVICE_RECORD';
EXCEPTION
    WHEN OTHERS THEN
        IF SQLCODE != -942 THEN RAISE; END IF;
END;
/

BEGIN
    EXECUTE IMMEDIATE 'DROP TABLE VEHICLE';
EXCEPTION
    WHEN OTHERS THEN
        IF SQLCODE != -942 THEN RAISE; END IF;
END;
/

BEGIN
    EXECUTE IMMEDIATE 'DROP TABLE CUSTOMER';
EXCEPTION
    WHEN OTHERS THEN
        IF SQLCODE != -942 THEN RAISE; END IF;
END;
/

CREATE TABLE CUSTOMER (
    customer_id NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    name VARCHAR2(100) NOT NULL,
    phone VARCHAR2(15) UNIQUE NOT NULL,
    email VARCHAR2(100),
    address VARCHAR2(255)
);

CREATE TABLE VEHICLE (
    vehicle_id NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    customer_id NUMBER NOT NULL,
    reg_number VARCHAR2(20) UNIQUE NOT NULL,
    vehicle_type VARCHAR2(20) NOT NULL,
    make VARCHAR2(50),
    model VARCHAR2(50),
    manufacture_year NUMBER,
    fuel_type VARCHAR2(20),
    CONSTRAINT fk_customer FOREIGN KEY (customer_id) REFERENCES CUSTOMER(customer_id),
    CONSTRAINT chk_vehicle_type CHECK (vehicle_type IN ('Two-Wheeler', 'Four-Wheeler')),
    CONSTRAINT chk_manufacture_year CHECK (manufacture_year BETWEEN 1980 AND 2100)
);

CREATE TABLE SERVICE_RECORD (
    service_id NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    vehicle_id NUMBER NOT NULL,
    service_date DATE NOT NULL,
    odometer_km NUMBER NOT NULL,
    service_type VARCHAR2(50),
    work_done VARCHAR2(500),
    parts_replaced VARCHAR2(500),
    vehicle_condition VARCHAR2(20),
    cost NUMBER,
    next_service_date DATE,
    next_service_km NUMBER,
    remarks VARCHAR2(500),
    CONSTRAINT fk_vehicle FOREIGN KEY (vehicle_id) REFERENCES VEHICLE(vehicle_id),
    CONSTRAINT chk_vehicle_condition CHECK (vehicle_condition IN ('Good', 'Average', 'Poor', 'Critical')),
    CONSTRAINT chk_odometer_km CHECK (odometer_km >= 0),
    CONSTRAINT chk_cost CHECK (cost >= 0)
);

EXIT;
