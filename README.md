# Vehi-Serve

**Vehicle Service Management System** — a desktop application for small
vehicle service shops to keep track of customers, vehicles, service records,
and upcoming service schedules.

Built with Java Swing and Oracle Database.

---

## Table of Contents

1. [What the System Does](#what-the-system-does)
2. [Features per Tab](#features-per-tab)
3. [Step-by-Step Walkthrough](#step-by-step-walkthrough)
4. [Business Rules and Validations](#business-rules-and-validations)
5. [Next-Service Prediction Logic](#next-service-prediction-logic)
6. [Architecture](#architecture)
7. [Database Design](#database-design)
8. [Repository Map](#repository-map)
9. [Setup from Scratch (Windows)](#setup-from-scratch-windows)
10. [Running the Automated Tests](#running-the-automated-tests)
11. [Testing Summary](#testing-summary)
12. [Troubleshooting](#troubleshooting)
13. [Known Limitations](#known-limitations)
14. [Screenshots](#screenshots)
15. [Future Work](#future-work)
16. [Credits](#credits)

---

## What the System Does

A small service shop (for bikes and cars) needs a simple way to:

1. **Register customers** — store their name, phone number, email and address.
2. **Register vehicles** — link each vehicle to its owner by phone number.
3. **Record a service visit** — date, odometer reading, work done, parts
   replaced, cost, and condition of the vehicle.
4. **Get a next-service suggestion** — the system calculates when and at what
   kilometre the vehicle should come back, based on vehicle type, condition
   and age.
5. **Search history** — look up past service records by customer name, phone,
   registration number, or service type.

The person using the system is the shop receptionist or owner. They open the
application, work through the tabs from left to right, and the data is saved
in the database immediately.

---

## Features per Tab

### Customers Tab

- Add a new customer (name, phone, email, address).
- View all customers in a table.
- Click a row to edit or delete a customer.
- Phone must be exactly 10 digits and unique.
- Deleting a customer is blocked if they still own vehicles.
- **Hand-off**: after saving a *new* customer, the application automatically
  switches to the Vehicles tab and pre-fills the customer's phone number so
  you can register their vehicle right away.

### Vehicles Tab

- **Phone search**: type the owner's 10-digit phone number and click "Find"
  (or press Enter). The owner's name appears in a label below the field.
- If the phone is not found, a "Customer not found" message appears and saving
  is blocked.
- Editing the phone field after a match clears the selection.
- Click a vehicle row in the table to see its owner and edit its details.
- "Clear" resets the search field, owner label and form.
- Save, Update, Delete with the same validation and error handling as the
  Customers tab.

### Services Tab

- Find a vehicle by registration number (spaces and hyphens are stripped
  automatically, e.g. "ka-01 1234" becomes "KA011234").
- View the vehicle's service history in a table.
- Fill in a new service entry: date, odometer, type, condition, cost, work
  done, parts replaced, remarks.
- **Suggest Next Service**: click the button to auto-fill the next service
  date and next service kilometre based on the prediction logic (see below).
- Save the service record. Delete a selected record from history.

### History Search Tab

- Type a keyword (customer name, phone, registration number, or service type)
  and press Enter or click "Search".
- Results appear in a table showing date, registration, vehicle, customer,
  service type, odometer, cost, and next-service information.
- Click a row to see work done, parts replaced, and remarks in a detail panel.
- Results are capped at 500 rows.
- An empty search returns all service records (up to the 500-row cap).

---

## Step-by-Step Walkthrough

Here is a typical session from start to finish.

1. **Register a customer.** Go to the Customers tab. Enter:
   - Name: `Rajesh Kumar`
   - Phone: `9876543210`
   - Email: `rajesh@example.com`
   - Address: `12 Main Street, Chennai`
   - Click **Save**. A success message shows the generated ID.
   - The app automatically switches to the Vehicles tab and fills in the phone.

2. **Register a vehicle.** You are now on the Vehicles tab with the owner
   already found. Enter:
   - Reg Number: `ka-01 1234` (stored as `KA011234`)
   - Vehicle Type: `Four-Wheeler`
   - Fuel Type: `Petrol`
   - Make: `Maruti`
   - Model: `Swift`
   - Year: `2020`
   - Click **Save**.

3. **Record a service.** Go to the Services tab. Enter the registration
   number `ka-01 1234` and click **Find**. The owner name and vehicle info
   appear. Fill in:
   - Service Date: `2026-10-08`
   - Odometer: `45000`
   - Service Type: `General Service`
   - Condition: `Good`
   - Cost: `3500.00`
   - Click **Suggest Next Service**. The system fills in the next date and
     next KM automatically.
   - Add work done, parts replaced, remarks as needed.
   - Click **Save Service**.

4. **Search history.** Go to the History Search tab. Type `Rajesh` and press
   Enter. The service record you just created appears. Click the row to see
   work done and parts replaced in the detail panel.

---

## Business Rules and Validations

All validation is done in `Validator.java` before any data reaches the
database. The table below lists every rule.

| Field | Rule | Error Message |
|---|---|---|
| Customer Name | Required, max 100 characters | "Name cannot be empty." / "Name cannot exceed 100 characters." |
| Phone | Required, exactly 10 digits (`\d{10}`) | "Phone number must be exactly 10 digits." |
| Email | Optional; if provided, must match `^[A-Za-z0-9+_.-]+@(.+)$`, max 100 chars | "Invalid email format." |
| Address | Optional, max 255 characters | "Address cannot exceed 255 characters." |
| Customer ID | Must be > 0 when saving a vehicle | "No customer selected." |
| Reg Number | Required, letters and digits only (spaces/hyphens stripped), max 20 chars, unique | "Registration number cannot be empty." / "...cannot exceed 20 characters." |
| Manufacture Year | 1980 to current year + 1 (computed at runtime) | "Manufacture year must be between 1980 and *N*." |
| Make / Model | Optional, max 50 characters each | "*Field* cannot exceed 50 characters." |
| Service Date | Required, format `yyyy-MM-dd` (strict), cannot be in the future | "Service date cannot be in the future." |
| Odometer | Required, whole number, 0 to 2,000,000 | "Odometer reading cannot be negative." / "...cannot exceed 2,000,000." / "...must be a whole number." |
| Cost | Optional, non-negative, max 2 decimal places | "Cost cannot be negative." / "Cost cannot have more than 2 decimal places." |
| Next Service Date | Optional; if given, must be ≥ service date | "Next service date cannot be before the current service date." |
| Next Service KM | Optional; if given, must be > current odometer | "Next service KM must be greater than current odometer reading." |
| Next Service Fields | At least one of next date or next KM must be provided | "Please provide either Next Service Date or Next Service KM." |
| Remarks / Work Done / Parts Replaced | Optional, max 500 characters each | "*Field* cannot exceed 500 characters." |
| Unique Phone | Enforced by DB unique constraint (ORA-00001) | "A customer with this phone number already exists." |
| Unique Reg Number | Enforced by DB unique constraint (ORA-00001) | "A vehicle with this registration number already exists." |
| Delete Customer | Blocked if vehicles exist (ORA-02292) | "This customer still has vehicles and cannot be deleted." |
| Delete Vehicle | Blocked if service records exist (ORA-02292) | "This vehicle has service records and cannot be deleted." |

---

## Next-Service Prediction Logic

The prediction is a **rule-based estimate**, not manufacturer data. It is
implemented in `ServicePredictor.java`.

### Base Intervals

| Vehicle Type | Base Days | Base KM |
|---|---|---|
| Two-Wheeler | 180 days | 3,000 km |
| Four-Wheeler | 365 days | 10,000 km |

### Condition Factors

| Condition | Factor |
|---|---|
| Good | 1.0 |
| Average | 0.9 |
| Poor | 0.8 |
| Critical | 0.6 |

### Age Factor

- Vehicle age = service year − manufacture year.
- If age ≥ 11 years, the age factor is **0.9**. Otherwise it is **1.0**.

### Calculation

```
combinedFactor = conditionFactor × ageFactor

finalDays    = round(baseDays × combinedFactor)
finalKmToAdd = round((baseKm × combinedFactor) / 100) × 100   ← rounds to nearest 100

nextDate = serviceDate + finalDays
nextKm   = currentOdometer + finalKmToAdd
```

A floor of 30 days / 500 km is applied, though the lowest reachable output
with the current factors (180 × 0.6 × 0.9 = 97 days) is well above the floor.

### Worked Example

> **Four-Wheeler, Poor condition, manufactured 2014, serviced 2026-01-15,
> odometer 50,000 km.**
>
> - Age = 2026 − 2014 = 12 → age factor = 0.9
> - Condition factor (Poor) = 0.8
> - Combined factor = 0.8 × 0.9 = 0.72
> - Final days = round(365 × 0.72) = round(262.8) = **263 days**
> - Final KM = round((10000 × 0.72) / 100) × 100 = round(72.0) × 100 = **7,200 km**
> - Next date = 2026-01-15 + 263 days = **2026-10-05**
> - Next KM = 50,000 + 7,200 = **57,200 km**

---

## Architecture

### Layer Diagram

```
┌────────────────────────────────────────────────┐
│                    Main.java                   │
│          (launches Swing on the EDT)           │
├────────────────────────────────────────────────┤
│                  MainFrame                     │
│         JTabbedPane with 4 tabs:               │
│   Customers │ Vehicles │ Services │ History    │
├────────────────────────────────────────────────┤
│              UI Panels (Swing)                 │
│  CustomerPanel  VehiclePanel  ServicePanel     │
│  SearchPanel         (all extend BasePanel)    │
├────────────────────────────────────────────────┤
│              Validator (static)                │
│       ServicePredictor (static)                │
├────────────────────────────────────────────────┤
│               DAO Layer                        │
│  CustomerDAO   VehicleDAO   ServiceRecordDAO   │
├────────────────────────────────────────────────┤
│          DBConnection (singleton)              │
│       reads config/db.properties               │
├────────────────────────────────────────────────┤
│       Oracle Database (XEPDB1 / vsms)          │
│   CUSTOMER ─< VEHICLE ─< SERVICE_RECORD       │
└────────────────────────────────────────────────┘
```

### Key Design Points

- **BasePanel**: an abstract `JPanel` subclass providing `showError()`,
  `showInfo()` and `confirm()` via `JOptionPane`. All four tab panels extend
  it.
- **MainFrame**: a `JFrame` holding a `JTabbedPane`. Its
  `switchToVehiclesAndSearch(phone)` method implements the hand-off from
  CustomerPanel after a new customer save.
- **Validator**: all validation logic is in static methods in a single class.
  UI panels call Validator before calling any DAO method.
- **Exception translation**: each DAO has a `handleSQLException` or
  `translateSQLException` method that maps Oracle error codes to
  user-friendly messages:
  - `ORA-00001` (unique constraint) → "A customer/vehicle with this ... already exists."
  - `ORA-02291` (parent key not found) → "The selected customer/vehicle does not exist."
  - `ORA-02292` (child record found) → "This customer/vehicle still has ... and cannot be deleted."
- **PreparedStatement**: all SQL uses `PreparedStatement` with `?` bind
  variables. No string concatenation of user input into SQL.
- **try-with-resources**: every `Connection`, `PreparedStatement` and
  `ResultSet` is wrapped in a try-with-resources block.
- **Generated keys**: inserts use
  `conn.prepareStatement(sql, new String[]{"CUSTOMER_ID"})` (or
  `"VEHICLE_ID"`, `"SERVICE_ID"`) to retrieve the Oracle identity column
  value after an insert.
- **EDT**: `Main.java` launches the frame with
  `SwingUtilities.invokeLater(...)`. The hand-off after saving a customer
  also dispatches to the EDT.

---

## Database Design

The schema is defined in `db/schema.sql`. Three tables, no cascade deletes.

### CUSTOMER

| Column | Type | Constraints |
|---|---|---|
| customer_id | NUMBER (identity) | PRIMARY KEY |
| name | VARCHAR2(100) | NOT NULL |
| phone | VARCHAR2(15) | UNIQUE, NOT NULL |
| email | VARCHAR2(100) | — |
| address | VARCHAR2(255) | — |

### VEHICLE

| Column | Type | Constraints |
|---|---|---|
| vehicle_id | NUMBER (identity) | PRIMARY KEY |
| customer_id | NUMBER | NOT NULL, FK → CUSTOMER |
| reg_number | VARCHAR2(20) | UNIQUE, NOT NULL |
| vehicle_type | VARCHAR2(20) | NOT NULL, CHECK IN ('Two-Wheeler', 'Four-Wheeler') |
| make | VARCHAR2(50) | — |
| model | VARCHAR2(50) | — |
| manufacture_year | NUMBER | chk_manufacture_year CHECK BETWEEN 1980 AND 2100 |
| fuel_type | VARCHAR2(20) | — |

### SERVICE_RECORD

| Column | Type | Constraints |
|---|---|---|
| service_id | NUMBER (identity) | PRIMARY KEY |
| vehicle_id | NUMBER | NOT NULL, FK → VEHICLE |
| service_date | DATE | NOT NULL |
| odometer_km | NUMBER | NOT NULL, CHECK ≥ 0 |
| service_type | VARCHAR2(50) | — |
| work_done | VARCHAR2(500) | — |
| parts_replaced | VARCHAR2(500) | — |
| vehicle_condition | VARCHAR2(20) | CHECK IN ('Good', 'Average', 'Poor', 'Critical') |
| cost | NUMBER | chk_cost CHECK ≥ 0 (nullable) |
| next_service_date | DATE | — |
| next_service_km | NUMBER | — |
| remarks | VARCHAR2(500) | — |

### Foreign Keys

- `VEHICLE.customer_id` → `CUSTOMER.customer_id` (no cascade)
- `SERVICE_RECORD.vehicle_id` → `VEHICLE.vehicle_id` (no cascade)

Deleting a parent row when child rows exist raises ORA-02292, which the DAO
translates into a user-friendly message.

### Fields Without DB CHECK Constraints

The following fields are validated only in the Java application layer and
have **no** database-level CHECK constraint:

- `fuel_type` — the UI offers a dropdown (Petrol, Diesel, EV, CNG, Hybrid)
  but the database accepts any VARCHAR2(20).
- `service_type` — free-text, no CHECK.
- `next_service_date`, `next_service_km` — no range or relationship CHECK.

---

## Repository Map

```
vehiServe/
├── .gitignore                  # Ignores bin/, config/db.properties, *.class, sources.txt
├── README.md                   # Project documentation
├── compile.bat                 # Compiles all .java files into bin/
├── run.bat                     # Runs Main class with ojdbc11.jar on classpath
├── config/
│   ├── db.properties.example   # Template: copy to db.properties and fill in password
│   └── db.properties           # (git-ignored) your local database credentials
├── db/
│   ├── schema.sql              # CREATE TABLE statements (drops existing tables first)
│   └── verify.sql              # Verification script
├── docs/
│   └── screenshots/
│       └── .gitkeep            # Placeholder for screenshots
├── lib/
│   └── ojdbc11.jar             # Oracle JDBC driver (Oracle Database 21c)
└── src/
    ├── Main.java               # Entry point — launches MainFrame on the EDT
    ├── ConnectionTest.java     # Standalone DB connection smoke test
    ├── dao/
    │   ├── CustomerDAO.java    # CRUD + search + findByPhone for CUSTOMER
    │   ├── VehicleDAO.java     # CRUD + search + findByRegNumber for VEHICLE
    │   └── ServiceRecordDAO.java  # CRUD + history + due services + full history search
    ├── exception/
    │   ├── DatabaseException.java    # Checked exception for DB errors
    │   └── ValidationException.java  # Checked exception for input errors
    ├── model/
    │   ├── Customer.java       # Customer POJO
    │   ├── Vehicle.java        # Vehicle POJO
    │   ├── ServiceRecord.java  # ServiceRecord POJO
    │   └── ServiceHistoryRow.java  # Read-only row for search results (joins 3 tables)
    ├── test/
    │   ├── CustomerDAOTest.java       # 14 automated tests
    │   ├── VehicleDAOTest.java        # 21 automated tests
    │   ├── ServiceRecordDAOTest.java  # 21 automated tests
    │   ├── ServicePredictorTest.java  # 22 automated tests
    │   └── SearchDAOTest.java         # 12 automated tests
    ├── ui/
    │   ├── BasePanel.java      # Abstract JPanel with showError/showInfo/confirm
    │   ├── MainFrame.java      # JFrame + JTabbedPane (4 tabs) + hand-off method
    │   ├── CustomerPanel.java  # Customers tab UI
    │   ├── VehiclePanel.java   # Vehicles tab UI (phone search)
    │   ├── ServicePanel.java   # Services tab UI (reg lookup, suggest next service)
    │   └── SearchPanel.java    # History Search tab UI
    └── util/
        ├── DBConnection.java     # Singleton, reads config/db.properties
        ├── Validator.java        # All input validation (static methods)
        └── ServicePredictor.java # Next-service prediction algorithm
```

---

## Setup from Scratch (Windows)

### Prerequisites

- **JDK 17** (or later). Verified with Temurin 17.0.16; JDK 11 or later should work (not tested).
- **Oracle Database 21c Express Edition (XE)** with the pluggable database
  `XEPDB1` running.
- **SQL\*Plus** (ships with Oracle XE).

### Step 1: Create the Database User

This step is **not** stored in the repository. Run SQL\*Plus as the admin:

```
sqlplus sys@//localhost:1521/XEPDB1 as sysdba
```

Then create the user:

```sql
CREATE USER vsms IDENTIFIED BY YOUR_PASSWORD_HERE;
GRANT CONNECT, RESOURCE TO vsms;
ALTER USER vsms QUOTA UNLIMITED ON USERS;
```

> **Note:** Replace `YOUR_PASSWORD_HERE` with your own password. Never commit
> real passwords to the repository.

### Step 2: Create the Tables

> **WARNING:** `db/schema.sql` drops the existing CUSTOMER, VEHICLE and SERVICE_RECORD tables, so running it deletes all data.

```
sqlplus vsms@//localhost:1521/XEPDB1
```

Enter your password, then run the schema script:

```sql
@db/schema.sql
```

This drops any existing tables and creates `CUSTOMER`, `VEHICLE` and
`SERVICE_RECORD`.

### Step 3: Configure the Connection

Copy the example config:

```
copy config\db.properties.example config\db.properties
```

Edit `config\db.properties` and replace `YOUR_PASSWORD_HERE` with the
password you chose in Step 1:

```properties
db.url=jdbc:oracle:thin:@//localhost:1521/XEPDB1
db.user=vsms
db.password=YOUR_PASSWORD_HERE
```

The file `config/db.properties` is git-ignored and will not be committed.

### Step 4: Compile

```
.\compile.bat
```

This compiles all Java files into the `bin/` directory.

### Step 5: Test the Connection

```
java -cp "bin;lib\ojdbc11.jar" ConnectionTest
```

You should see:

```
Starting Connection Test...
Configuration loaded successfully.
Connection established successfully!
Database Product Name: Oracle
Database Product Version: 
Oracle Database 21c Express Edition Release 21.0.0.0.0 - Production
Version 21.3.0.0.0
```

### Step 6: Run the Application

```
.\run.bat
```

The application window opens with four tabs: Customers, Vehicles, Services,
History Search.

---

## Running the Automated Tests

There are five test classes. Each is a standalone `main()` method that runs
against the live database.

> **Warning:** The tests create and delete their own data, but they expect
> the tables to be empty before running. If there is leftover data, some
> assertions (like exact row counts) may fail.

### Commands

Each test class can be compiled and run individually. Below are both
PowerShell and cmd forms.

Run `.\compile.bat` first (it also compiles the tests). Then run:

**PowerShell:**

```powershell
java -cp 'bin;lib\ojdbc11.jar' test.CustomerDAOTest
java -cp 'bin;lib\ojdbc11.jar' test.VehicleDAOTest
java -cp 'bin;lib\ojdbc11.jar' test.ServiceRecordDAOTest
java -cp 'bin;lib\ojdbc11.jar' test.ServicePredictorTest
java -cp 'bin;lib\ojdbc11.jar' test.SearchDAOTest
```

**cmd:**

```cmd
java -cp "bin;lib\ojdbc11.jar" test.CustomerDAOTest
java -cp "bin;lib\ojdbc11.jar" test.VehicleDAOTest
java -cp "bin;lib\ojdbc11.jar" test.ServiceRecordDAOTest
java -cp "bin;lib\ojdbc11.jar" test.ServicePredictorTest
java -cp "bin;lib\ojdbc11.jar" test.SearchDAOTest
```

### Expected Results (from the last full verified run)

| Test Class | Pass Count |
|---|---|
| CustomerDAOTest | 14 passed, 0 failed |
| VehicleDAOTest | 21 passed, 0 failed |
| ServiceRecordDAOTest | 21 passed, 0 failed |
| ServicePredictorTest | 22 passed, 0 failed |
| SearchDAOTest | 12 passed, 0 failed |

**Total: 90 automated tests.**

---

## Testing Summary

### Automated (DAO, Validator, Predictor)

All five test classes exercise the DAO layer, Validator rules and
ServicePredictor logic end-to-end against the live Oracle database. They
cover:

- Valid CRUD operations (insert, update, delete).
- Every Validator rule (empty fields, length limits, year boundaries, date
  format, odometer limits, cost precision, next-service constraints).
- Oracle constraint enforcement (duplicate phone/reg, foreign key violations,
  child-record-exists on delete).
- Search functions (partial name, phone, reg number with spaces/hyphens,
  case insensitivity, SQL injection safety, wildcard literal escaping,
  500-row cap).
- ServicePredictor for all 16 type × condition × age combinations, plus
  invalid input handling and age derivation edge cases.

### Manual Only (Swing UI, Hand-off)

The following features have **no automated tests** and must be verified
manually:

- **All Swing screens**: form layout, button enable/disable, table selection,
  clear behaviour.
- **Vehicles tab phone search**: Find by phone, "Customer not found",
  validation messages for invalid phone, clearing selection on edit, owner
  display on vehicle row click.
- **Customer → Vehicles hand-off**: after saving a new customer, the app
  switches to Vehicles and pre-fills the phone. This only fires on
  `addCustomer`, not on update or delete.
- **Suggest Next Service button**: fills in next date and next KM fields,
  shows rule label.
- **History Search details panel**: clicking a row shows work done, parts
  replaced and remarks.

---

## Troubleshooting

### ORA-01017: invalid username/password; logon denied

Your password in `config/db.properties` does not match the database user.
Double-check the password you set when creating the `vsms` user.
To fix a locked or incorrect password, connect as system using:
```
sqlplus system@//localhost:1521/XEPDB1
```
Run `ALTER SESSION SET CONTAINER = XEPDB1;` and check `SHOW CON_NAME` says `XEPDB1`. Then run:
```sql
ALTER USER vsms IDENTIFIED BY "YOUR_PASSWORD_HERE" ACCOUNT UNLOCK;
```

### ORA-12154: TNS: could not resolve the connect identifier

This means the SQL*Plus connect identifier is malformed. Use exactly `//localhost:1521/XEPDB1` (two slashes, no password inside it).

### Wrong Container: Connected to CDB$ROOT Instead of XEPDB1

If SQL\*Plus connects to the root container, your tables will not be found.
Always connect to the pluggable database:

```
sqlplus vsms@//localhost:1521/XEPDB1
```

Do not use `sqlplus vsms` without specifying the service name.

### PowerShell: .\compile.bat Not Recognized

PowerShell requires the `.\` prefix to run scripts in the current directory:

```powershell
.\compile.bat
.\run.bat
```

### Class Not Found / NoClassDefFoundError

- Make sure you compiled first (`.\compile.bat`).
- Make sure you are running from the project root directory (the folder
  containing `compile.bat`, `run.bat`, `src/`, `bin/`, `lib/`).
- Check that `lib\ojdbc11.jar` exists (it is included in the repository).

### Configuration File Not Found

The application looks for `config/db.properties` relative to the current
working directory. Always `cd` into the project root before running.

---

## Known Limitations

1. **No edit for service records.** Once saved, a service record can only be
   deleted, not updated.
2. **500-row search cap.** The History Search tab returns at most 500 rows
   per query (`FETCH FIRST 500 ROWS ONLY`).
3. **Vehicles without services are invisible in search.** The History Search
   uses `INNER JOIN`, so vehicles that have never been serviced do not appear.
4. **No date filter or export.** There is no way to filter search results by
   date range or export them to CSV/PDF.
5. **`fuel_type` and `service_type` are not DB-checked.** The application
   uses dropdowns and free text, but the database accepts any value.
6. **DB year CHECK still allows up to 2100.** The `chk_manufacture_year`
   constraint in `schema.sql` is `BETWEEN 1980 AND 2100`. The tighter
   runtime limit (current year + 1) is enforced only in `Validator.java`.
   Direct SQL inserts can bypass it.
7. **No optimistic locking.** If two users edit the same record
   simultaneously, the last save wins silently.
8. **Hand-off fails silently.** If the `switchToVehiclesAndSearch` method
   throws an exception, it is caught and ignored — the user stays on the
   Customers tab without any error message.
9. **UI has no automated tests.** All Swing screens and interactions are
   tested manually only.
10. **Tested on Windows only.** The batch files (`compile.bat`, `run.bat`)
    are Windows-specific. The Java code itself should work on other platforms
    with equivalent shell scripts, but this has not been tested.

---

## Screenshots

Screenshots are stored in `docs/screenshots/` and should be added manually.

| Screen | File |
|---|---|
| Customers tab | `docs/screenshots/customers.png` |
| Vehicles tab | `docs/screenshots/vehicles.png` |
| Services tab | `docs/screenshots/services.png` |
| History Search tab | `docs/screenshots/search.png` |

> These placeholders are for the project author to fill in with actual
> screenshots of the running application.

---

## Future Work

- Add an Update function for service records.
- Add date-range filtering to History Search.
- Export search results to CSV or PDF.
- Add a Dashboard tab with summary statistics (total customers, vehicles due
  for service, revenue).
- Add shell scripts for Linux/macOS.
- Replace manual UI testing with a UI test framework.
- Add optimistic locking (version column or timestamp check).
- Tighten the database `manufacture_year` CHECK to match the application's
  dynamic rule, or remove the DB CHECK and rely solely on the application.
- Add a `service_type` lookup table with a foreign key.

---

## Credits

- **Author:** Faizal ,Eniya Sree ,Elamathi ,Gokul Prasath
- **Course:** Object Oriented Programming Using JAVA
- **Institution:** SSN College Of Engineering ,Kalavakkam ,Chennai
- **Department:** Information Technology

---
