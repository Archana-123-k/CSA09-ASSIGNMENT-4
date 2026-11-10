import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class FeeService {
    private static final String DEFAULT_URL =
            "jdbc:mysql://localhost:3306/campus_events?serverTimezone=UTC";
    private static final String INSERT_SQL =
            "INSERT INTO fee_payments (reg_no, amount) VALUES (?, ?)";

    private final String url;
    private final String user;
    private final String password;

    public FeeService() {
        this(System.getProperty("fee.db.url", DEFAULT_URL),
                System.getProperty("fee.db.user", "root"),
                System.getProperty("fee.db.password", ""));
    }

    public FeeService(String url, String user, String password) {
        this.url = url;
        this.user = user;
        this.password = password;
    }

    public void validate(String regNo, String amountText) throws FeeException {
        if (regNo == null || !regNo.matches("[A-Za-z0-9]{6,15}")) {
            throw new FeeException("Registration number must be 6-15 letters or digits.");
        }

        BigDecimal amount;
        try {
            amount = new BigDecimal(amountText == null ? "" : amountText.trim());
        } catch (NumberFormatException exception) {
            throw new FeeException("Enter a valid payment amount.", exception);
        }
        if (amount.compareTo(BigDecimal.ONE) < 0
                || amount.compareTo(new BigDecimal("500000")) > 0) {
            throw new FeeException("Amount must be between 1 and 500000.");
        }
    }

    public long pay(String regNo, String amountText) throws FeeException {
        validate(regNo, amountText);
        BigDecimal amount = new BigDecimal(amountText.trim());
        try (Connection connection = DriverManager.getConnection(url, user, password);
             PreparedStatement statement = connection.prepareStatement(
                     INSERT_SQL, PreparedStatement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, regNo);
            statement.setBigDecimal(2, amount);
            statement.executeUpdate();
            try (var keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getLong(1);
                }
            }
            throw new FeeException("Payment was saved, but no receipt number was returned.");
        } catch (SQLException exception) {
            throw new FeeException(
                    "Payment could not be saved. Check that the database and fee_payments table are available.",
                    exception);
        }
    }
}
