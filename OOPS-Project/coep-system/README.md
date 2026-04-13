# COEP System - College of Engineering Pune
## Student Management System (Core Java CLI Project)

---

## Project Overview

A fully terminal-based Java application demonstrating:
- **OOP**: Abstraction, Inheritance, Polymorphism, Encapsulation
- **Generics**: `List<User>`, `Map<Integer, User>`, `List<Course>`, etc.
- **Exception Handling**: try-catch on all I/O and input operations
- **CSV Persistence**: All data stored in `data/database/*.csv`
- **Concurrency**: `ExecutorService` in `NotificationService` for async notifications

---

## Project Structure

```
coep-system/
├── main/
│   └── Main.java                  # Entry point, main menu
├── users/
│   ├── User.java                  # Abstract base class
│   ├── Student.java               # Extends User
│   ├── Teacher.java               # Extends User
│   ├── Admin.java                 # Extends User
│   └── ExamCellStaff.java         # Extends User
├── courses/
│   ├── Course.java                # Course model
│   ├── Assignment.java            # Assignment model
│   └── Submission.java            # Submission model
├── exams/
│   ├── ExamForm.java              # Exam form model
│   └── Result.java                # Exam result model
├── services/
│   ├── UserService.java           # User CRUD + CSV
│   ├── CourseService.java         # Course CRUD + enrollments CSV
│   ├── AssignmentService.java     # Assignment/submission CSV
│   ├── ExamService.java           # Exam forms + results CSV
│   └── NotificationService.java   # Async notifications (Threads)
├── controllers/
│   ├── StudentController.java     # Student menu handling
│   ├── TeacherController.java     # Teacher menu handling
│   ├── AdminController.java       # Admin menu handling
│   └── ExamCellController.java    # Exam Cell menu handling
├── utils/
│   ├── CSVHandler.java            # readAll / writeAll / appendRow
│   └── InputValidator.java        # Safe terminal input
├── data/
│   └── database/
│       ├── users.csv
│       ├── courses.csv
│       ├── enrollments.csv
│       ├── assignments.csv
│       ├── submissions.csv
│       ├── exam_forms.csv
│       └── results.csv
├── run.sh                         # One-click build & run (Linux/macOS)
└── README.md
```

---

## Prerequisites

- **Java JDK 11 or higher** (must have `javac`)
- Terminal / Command Prompt

### Install JDK

**Ubuntu/Debian:**
```bash
sudo apt-get update
sudo apt-get install default-jdk
```

**macOS (Homebrew):**
```bash
brew install openjdk
```

**Windows:**  
Download from https://adoptium.net and add to PATH.

---

## How to Compile & Run

### Option A — One-click script (Linux/macOS)
```bash
chmod +x run.sh
./run.sh
```

### Option B — Manual (all platforms)

**Step 1 — Compile** (run from the `coep-system/` directory):
```bash
javac -d out \
  utils/*.java \
  users/*.java \
  courses/*.java \
  exams/*.java \
  services/*.java \
  controllers/*.java \
  main/*.java
```

**Step 2 — Run:**
```bash
java -cp out main.Main
```

### Windows CMD equivalent:
```cmd
mkdir out
javac -d out utils\*.java users\*.java courses\*.java exams\*.java services\*.java controllers\*.java main\*.java
java -cp out main.Main
```

---

## Default Login IDs (from users.csv)

| ID | Name                  | Role       |
|----|-----------------------|------------|
| 1  | Rahul Sharma          | STUDENT    |
| 2  | Priya Patel           | STUDENT    |
| 3  | Amit Desai            | STUDENT    |
| 4  | Dr. Sunita Kulkarni   | TEACHER    |
| 5  | Prof. Rajan Mehta     | TEACHER    |
| 6  | Admin User            | ADMIN      |
| 7  | Exam Cell Officer     | EXAM_CELL  |

> Login requires only the numeric ID (no password).

---

## OOP Concepts Demonstrated

| Concept        | Where Used |
|----------------|------------|
| **Abstraction**    | `User.java` — abstract class with abstract `viewDashboard()` |
| **Inheritance**    | `Student`, `Teacher`, `Admin`, `ExamCellStaff` extend `User` |
| **Polymorphism**   | Each subclass overrides `viewDashboard()`; `Main.java` dispatches polymorphically |
| **Encapsulation**  | All model fields `private` with getters/setters |
| **Generics**       | `List<User>`, `Map<Integer,User>`, `List<Course>`, `List<Result>`, etc. |
| **Exception Handling** | try-catch in `CSVHandler`, `InputValidator`, all services |
| **Concurrency**    | `ExecutorService` (3-thread pool) in `NotificationService` |

---

## CSV File Details

### users.csv
```
id,name,role,department
1,Rahul Sharma,STUDENT,Computer Engineering
```

### courses.csv
```
courseId,courseName,teacherId,department,credits
C101,Data Structures and Algorithms,4,Computer Engineering,4
```

### enrollments.csv
```
enrollmentId,studentId,courseId,enrollmentDate
E001,1,C101,2024-01-15
```

### assignments.csv
```
assignmentId,title,courseId,teacherId,dueDate,maxMarks
A001,Linked List Implementation,C101,4,2024-02-10,20
```

### submissions.csv
```
submissionId,assignmentId,studentId,submissionDate,marksObtained,graded
S001,A001,1,2024-02-08,18,true
```

### exam_forms.csv
```
formId,studentId,courseId,examType,status,appliedDate
F001,1,C101,SEMESTER,APPROVED,2024-04-01
```

### results.csv
```
resultId,studentId,courseId,examType,marksObtained,maxMarks,grade,published
R001,1,C101,SEMESTER,78,100,A,true
```

---

## Sample Session (Student)

```
Select an option: 1
Enter your User ID: 1
Welcome, Rahul Sharma!

STUDENT DASHBOARD
  1. Enroll in Course
  2. Submit Assignment
  3. Fill Exam Form
  4. View My Results
  5. View My Enrollments
  0. Logout
```

---

## Grading Scale

| Percentage | Grade |
|------------|-------|
| >= 90      | O (Outstanding) |
| >= 75      | A |
| >= 60      | B |
| >= 50      | C |
| >= 40      | D |
| < 40       | F |
