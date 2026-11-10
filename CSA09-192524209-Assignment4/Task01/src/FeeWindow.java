import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;

public class FeeWindow extends JFrame {
    private final JTextField regNoField = new JTextField(18);
    private final JTextField amountField = new JTextField(18);
    private final JLabel statusLabel = new JLabel("Enter payment details.");
    private final JButton payButton = new JButton("Pay");
    private final JButton clearButton = new JButton("Clear");
    private final JButton receiptButton = new JButton("Receipt");
    private final FeeService service = new FeeService();
    private long receiptNumber;

    public FeeWindow() {
        super("Fee Payment");
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        setLayout(new BorderLayout(8, 8));

        JPanel fields = new JPanel(new GridLayout(2, 2, 8, 8));
        fields.add(new JLabel("Registration number:"));
        fields.add(regNoField);
        fields.add(new JLabel("Amount:"));
        fields.add(amountField);
        add(fields, BorderLayout.CENTER);

        JPanel buttons = new JPanel();
        buttons.add(payButton);
        buttons.add(clearButton);
        buttons.add(receiptButton);
        add(buttons, BorderLayout.SOUTH);
        add(statusLabel, BorderLayout.NORTH);
        receiptButton.setEnabled(false);

        payButton.addActionListener(event -> pay());
        clearButton.addActionListener(event -> clear());
        receiptButton.addActionListener(event -> showReceipt());
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent event) {
                dispose();
            }
        });

        pack();
        setLocationRelativeTo(null);
    }

    private void pay() {
        String regNo = regNoField.getText().trim();
        String amount = amountField.getText().trim();
        try {
            service.validate(regNo, amount);
        } catch (FeeException exception) {
            statusLabel.setText(exception.getMessage());
            return;
        }

        payButton.setEnabled(false);
        statusLabel.setText("Processing payment...");
        new SwingWorker<Long, Void>() {
            @Override
            protected Long doInBackground() throws FeeException {
                return service.pay(regNo, amount);
            }

            @Override
            protected void done() {
                payButton.setEnabled(true);
                try {
                    receiptNumber = get();
                    receiptButton.setEnabled(true);
                    statusLabel.setText("Payment successful. Receipt #" + receiptNumber);
                } catch (Exception exception) {
                    Throwable cause = exception.getCause();
                    statusLabel.setText(cause instanceof FeeException
                            ? cause.getMessage() : "Payment failed: " + exception.getMessage());
                    receiptButton.setEnabled(false);
                }
            }
        }.execute();
    }

    private void clear() {
        regNoField.setText("");
        amountField.setText("");
        statusLabel.setText("Enter payment details.");
        receiptNumber = 0;
        receiptButton.setEnabled(false);
    }

    private void showReceipt() {
        JOptionPane.showMessageDialog(this,
                "Registration number: " + regNoField.getText().trim()
                        + "\nAmount: " + amountField.getText().trim()
                        + "\nReceipt number: " + receiptNumber,
                "Payment receipt", JOptionPane.INFORMATION_MESSAGE);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new FeeWindow().setVisible(true));
    }
}
