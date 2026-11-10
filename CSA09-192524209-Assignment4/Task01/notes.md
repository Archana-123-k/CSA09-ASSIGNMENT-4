# Task 1 — Fee payment window

## Design deficiencies in the original design

| # | Deficiency | What the user would experience |
|---|---|---|
| 1 | UI and payment/database logic are mixed together | Changes to the screen can break payment code and vice versa. |
| 2 | Strings are compared with `==` | Equal text can be treated as different, so a valid selection may fail. |
| 3 | There is no input validation | Malformed registration numbers or out-of-range amounts reach the database. |
| 4 | Database work and `sleep()` run on the event thread | The window freezes and stops repainting/responding during payment. |
| 5 | There is no useful feedback | The user cannot tell whether payment succeeded or why it failed. |
| 6 | The Receipt button is ignored | A successful payment has no usable receipt action. |
| 7 | SQL is built by concatenating input | Crafted values can alter the SQL statement (SQL injection). |
| 8 | Connections/windows are not closed | Database resources leak and the application may not exit cleanly. |
| 9 | Checked exceptions are unhandled | Failures can terminate an action without a clear explanation. |

## Design defence

`FeeService` owns validation and JDBC work, while `FeeWindow` only presents state and responds to user actions. Prepared statements protect the insert, try-with-resources releases JDBC resources, and a `SwingWorker` keeps blocking database work off the event-dispatch thread. Each button has a clear action, and a receipt is available only after payment succeeds. Database configuration is optional and failures are shown in the status label rather than crashing the window.

## Alternative concept: dispatch through one listener

One shared listener can compare the event source against all `k` buttons, requiring O(k) comparisons in the worst case; separate per-button lambdas dispatch directly in O(1). The shared listener may use fewer listener objects, while one listener per button uses `k` listener objects (O(k) additional space) and keeps each action localized. This implementation uses a lambda per button.

## Run and screenshot checklist

- Compile: `javac --release 17 -d out src\*.java`
- Run: `java -cp out FeeWindow`
- Take a screenshot of the initial form to prove the registration/amount inputs and disabled Receipt state.
- Take a screenshot after an attempted payment with MySQL unavailable to prove the clear status-label error and responsive window.
- With a configured `fee_payments` table, take a screenshot of a successful payment and its receipt to prove persistence and Receipt enablement.
