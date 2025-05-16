# Auction Bidding Bot

This project implements a modular auction bidding bot that competes in a two-party auction scenario where they bid for units of a product using limited monetary resources. The bot adapts its bidding strategy dynamically using multiple heuristics, including aggressive, conservative, and adaptive tactics. The architecture is extensible, testable, and designed for strategy-based evaluation.

---

## 🚀 Features

- Supports dynamic strategy switching based on auction state
- Implements multiple bidding strategies:
  - `ZeroBidStrategy`
  - `AggressiveStrategy`
  - `ConservativeStrategy`
  - `AdaptiveStrategy`
- New strategies can be easily added
- Follows clean OOP practices and SOLID principles
- Includes unit tests for key components and strategy logic

---

## 📦 Requirements

- **Java 21** or higher
  - Uses `Math.clamp()` introduced in Java 21
- **JUnit 5** for unit testing (via Maven)

---

## 🛠️ How to Run
> This project **does not include a runnable `main` method or CLI interface**.  
> It is intended for evaluation through **unit tests** and integration in a simulation or competition environment.
> Use `org/kamran/auction/KamransBidder.java` class as Bidder implementor.

### Run Unit Tests
- With Maven:
  ```bash
  mvn test
