# HappyShop

A JavaFX project developed in IntelliJ using Maven (JDK 25, JavaFX 25), designed for CI553 coursework.

## ➡️ Setup

After opening the project in IntelliJ:
- Go to the `systemSetup` package.
- Run `SetOrderFileSystem` and `SetDatabase` **once only** to set up initial files and database.


## 🚀 Running the System

- Run `Launcher` each time you want to start the system.
- Extra Notes:
  If the database throws exceptions following a **branch switch** or when running on a **new machine**,
  run `SetDatabase` again to recreate a clean database.
## 🆕 What I added (v1.0)

- **Organised trolley:** the same product is merged into one line and sorted by ID.
- **Low stock at checkout:** products without enough stock are removed and a pop-up explains why.
- **Remove an item:** select a line in the trolley and click Remove.
- **Change quantity:** + and - buttons, limited by the stock available.
- **Warehouse staff login:** the Warehouse opens only after a correct login. Three wrong attempts lock login for 3 minutes.

## 🔒 Warehouse login

- Username: `staff`
- Password: given in my coursework report (it is not stored in this repo, only a salted hash of it).

## 🧪 Tests

Run all tests by right-clicking `src/test/java` → **Run 'All Tests'**. There are 36 JUnit tests.

## ⚠️ JavaFX Warning:  
If IntelliJ shows a harmless JavaFX native‑access warning during development,
see the comment in Launcher.java for details.

## 📌 Summary
- JavaFX project
- Developed in IntelliJ with Maven
- Easy setup and run
