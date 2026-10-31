# Hostel Complaint Management System - Java (Servlet + JSP + JDBC)

Advance Internet Programming (26CAH-614) - Keshav Kumar (26MCC20092)

A web application in which students register and raise hostel complaints (fan, water, Wi-Fi, mess ...),
and the warden tracks, filters and resolves them from a dashboard.

**Technologies:** Java 17, Jakarta Servlet 6 / JSP (Tomcat 10.1), JDBC, SQLite, HTML5, CSS3, JavaScript (AJAX), JUnit 5, Maven.

## Features
- Student registration and login (PBKDF2 salted password hashing, session login)
- Raise a complaint with category, priority, room and description; unique ticket number (HCMS-2026-0001)
- Status life cycle: Pending -> In Progress -> Resolved / Rejected, with a time-stamped timeline and comments
- Warden dashboard: statistics, bar charts, search and filters, one-click AJAX status change
- JSON API: `GET /api/stats`, `POST /api/complaint/{id}/status`
- Security: role based access (403), CSRF token on every POST, HTML escaping, parameterised SQL

## Run in Apache NetBeans
1. Install JDK 17 or newer and Apache Tomcat 10.1.
2. NetBeans: *File > Open Project* and select this folder. Add Tomcat 10.1 under *Tools > Servers*.
3. Right click the project > *Run* (F6).
4. Open http://localhost:8080/HostelComplaintSystem/

## Run from the command line
    mvn clean package
    copy target/HostelComplaintSystem.war into <tomcat>/webapps/

## Demo accounts
| Role    | E-mail             | Password   |
|---------|--------------------|------------|
| Warden  | warden@hostel.edu  | admin123   |
| Student | keshav@student.edu | student123 |

Other students: rahul@student.edu, aman@student.edu, priya@student.edu (password `student123`).

## Tests
Right click the project > *Test*, or `mvn test` (14 JUnit test cases).

## Database
SQLite file is created automatically on first start at `<user home>/HostelComplaintDB/hostel.db`
(tables + demo data). Delete this file to reset the data.

## Project structure
    src/main/java/edu/cu/hcms
        model/   User, Complaint, Update, Stats (records)
        dao/     UserDao, ComplaintDao, Seed        (JDBC)
        util/    Db, PasswordUtil, Validator, Html, Json, Config
        web/     SecurityFilter, AppInit, Login/Register/Logout/Dashboard/Complaint/Admin/Api servlets
    src/main/webapp
        WEB-INF/jsp/   JSP pages (header/footer are included in every page)
        static/        style.css, app.js
    src/test/java      HcmsTests (14 tests)
