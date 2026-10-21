# 🏍️ Bike Rental System

A mini console-based **Bike Rental Management System** built with **Java**, **JDBC**, and **MySQL** (via XAMPP). Made as a college mini project.

## 📌 Features

- Add new bikes with model, type, and hourly rate
- View all bikes or only available bikes
- Add and view customers (name, phone, license number)
- Rent a bike (automatically marks it as *Rented*)
- Return a bike with automatic bill calculation (billed per hour, rounded up, minimum 1 hour)
- View full rental history

## 🛠️ Tech Stack

| Component | Technology |
|-----------|------------|
| Language  | Java (JDK 14 or above) |
| Database  | MySQL (XAMPP) |
| Connectivity | JDBC (MySQL Connector/J) |
| Interface | Console (command line) |

## 🗄️ Database Design

**Database name:** `bike_rental`

| Table | Columns |
|-------|---------|
| `bikes` | bike_id, model, bike_type, rate_per_hour, status |
| `customers` | customer_id, name, phone, license_no |
| `rentals` | rental_id, bike_id, customer_id, rent_time, return_time, total_amount, status |

`rentals` is linked to `bikes` and `customers` through foreign keys.

## 📁 Project Structure

```
bike-rental-system/
├── BikeRentalSystem.java   # Main Java program
├── bike_rental.sql         # Database and table creation script
└── README.md
```

## ⚙️ Setup and Installation

### Prerequisites
- [JDK 14+](https://www.oracle.com/java/technologies/downloads/) installed
- [XAMPP](https://www.apachefriends.org/) installed
- [MySQL Connector/J](https://dev.mysql.com/downloads/connector/j/) JAR file

### Steps

1. **Clone the repository**
```bash
   git clone https://github.com/your-username/bike-rental-system.git
   cd bike-rental-system
```

2. **Start XAMPP**
   Open the XAMPP Control Panel and start **Apache** and **MySQL**.

3. **Create the database**
   - Open `http://localhost/phpmyadmin`
   - Go to the **SQL** tab
   - Paste the contents of `bike_rental.sql` and click **Go**

4. **Add the MySQL Connector**
   Download the Connector/J JAR and place it in the project folder (for example `mysql-connector-j-8.x.jar`).

5. **Compile and run**

   Linux / macOS:
```bash
   javac BikeRentalSystem.java
   java -cp ".:mysql-connector-j-8.x.jar" BikeRentalSystem
```

   Windows:
```bash
   javac BikeRentalSystem.java
   java -cp ".;mysql-connector-j-8.x.jar" BikeRentalSystem
```

   > Using an IDE (Eclipse / IntelliJ / NetBeans)? Add the connector JAR to the project's libraries and run `BikeRentalSystem.java`.

## 🔧 Configuration

The database settings are at the top of `BikeRentalSystem.java`:

```java
static final String URL  = "jdbc:mysql://localhost:3306/bike_rental";
static final String USER = "root";
static final String PASS = "";   // XAMPP default is an empty password
```

Change these if your MySQL setup is different.

## ▶️ Usage

When the program starts, you will see this menu:

```
===== BIKE RENTAL SYSTEM =====
1. Add Bike
2. View All Bikes
3. View Available Bikes
4. Add Customer
5. View Customers
6. Rent a Bike
7. Return a Bike
8. View Rental History
0. Exit
```

**Typical flow:** Add a customer → View available bikes → Rent a bike (note the Rental ID) → Return the bike using the Rental ID → Bill is displayed.

## 💰 Billing Logic

```
Total = (hours rented, rounded up, minimum 1) × rate per hour
```

Example: 70 minutes on a bike at ₹100/hr is billed as 2 hours = ₹200.

## 🐞 Troubleshooting

| Problem | Solution |
|---------|----------|
| `No suitable driver found` | MySQL Connector JAR is missing from the classpath |
| `Communications link failure` | MySQL is not running in XAMPP |
| `Unknown database 'bike_rental'` | Run `bike_rental.sql` in phpMyAdmin |
| `Access denied for user 'root'` | Update `USER` / `PASS` in the Java file |

## 🚀 Future Improvements

- GUI using Java Swing / JavaFX
- Login system for admin and users
- Late return fee and discounts
- Search and filter bikes by type or price
- Export bills as PDF

## 👨‍💻 Author

**Vasu Kheni**
ADIT | AI&DS / 2026

## 📄 License

This project is created for educational purposes.
