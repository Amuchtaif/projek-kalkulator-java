import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class KalkulatorGUI extends JFrame implements ActionListener {
    private JTextField layar;
    private JTextArea history;
    private String ekspresi = "";

    public KalkulatorGUI() {
        setTitle("Kalkulator by Kelompok 1");
        setSize(420, 600);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        layar = new JTextField();
        layar.setFont(new Font("Poppins", Font.BOLD, 28));
        layar.setHorizontalAlignment(SwingConstants.RIGHT);
        layar.setEditable(false);
        layar.setPreferredSize(new Dimension(400, 60));
        add(layar, BorderLayout.NORTH);

        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.weightx = 1;
        gbc.weighty = 1;

        String[] tombol = {
            "(", ")", "C", "←",
            "sin", "cos", "tan", "/",
            "7", "8", "9", "*",
            "4", "5", "6", "-",
            "1", "2", "3", "+",
            "0", ".", "="
        };

        int row = 0, col = 0;
        for (String t : tombol) {
            JButton btn = new JButton(t);
            btn.setFont(new Font("Arial", Font.BOLD, 18));
            btn.setBackground(new Color(50, 60, 70));
            btn.setForeground(Color.WHITE);

            // Hover warna reverse
            Color normalColor = btn.getBackground();
            Color hoverColor = new Color(255 - normalColor.getRed(),
                                         255 - normalColor.getGreen(),
                                         255 - normalColor.getBlue());

            btn.addMouseListener(new java.awt.event.MouseAdapter() {
                @Override
                public void mouseEntered(java.awt.event.MouseEvent evt) {
                    btn.setBackground(hoverColor);
                    btn.setForeground(Color.BLACK);
                }

                @Override
                public void mouseExited(java.awt.event.MouseEvent evt) {
                    btn.setBackground(normalColor);
                    btn.setForeground(Color.WHITE);
                }
            });

            btn.addActionListener(this);

            gbc.gridx = col;
            gbc.gridy = row;

            if (t.equals("=")) {
                gbc.gridwidth = 2;
                panel.add(btn, gbc);
                col += 2;
            } else {
                gbc.gridwidth = 1;
                panel.add(btn, gbc);
                col++;
            }

            if (col >= 4) {
                col = 0;
                row++;
            }
        }

        add(panel, BorderLayout.CENTER);

        history = new JTextArea(5, 20);
        history.setFont(new Font("Monospaced", Font.PLAIN, 14));
        history.setEditable(false);
        JScrollPane scroll = new JScrollPane(history);
        add(scroll, BorderLayout.SOUTH);

        setVisible(true);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        String input = e.getActionCommand();

        switch (input) {
            case "=":
                try {
                    String toEval = ekspresi
                        .replaceAll("sin\\(", "s(")
                        .replaceAll("cos\\(", "c(")
                        .replaceAll("tan\\(", "t(");

                    double result = evaluate(toEval);
                    String displayResult = (result == (long) result) ?
                            String.format("%d", (long) result) :
                            String.format("%s", result);

                    layar.setText(displayResult);
                    history.append(ekspresi + " = " + displayResult + "\n");
                    ekspresi = "";
                } catch (Exception ex) {
                    layar.setText("Error");
                    ekspresi = "";
                }
                break;

            case "C":
                ekspresi = "";
                layar.setText("");
                break;

            case "←":
                if (!ekspresi.isEmpty()) {
                    ekspresi = ekspresi.substring(0, ekspresi.length() - 1);
                    layar.setText(ekspresi);
                }
                break;

            default:
                ekspresi += input;
                layar.setText(ekspresi);
                break;
        }
    }

    private double evaluate(String expr) {
        return new Object() {
            int pos = -1, ch;

            void nextChar() {
                ch = (++pos < expr.length()) ? expr.charAt(pos) : -1;
            }

            boolean eat(int charToEat) {
                while (ch == ' ') nextChar();
                if (ch == charToEat) {
                    nextChar();
                    return true;
                }
                return false;
            }

            double parse() {
                nextChar();
                double x = parseExpression();
                if (pos < expr.length()) throw new RuntimeException("Unexpected: " + (char)ch);
                return x;
            }

            double parseExpression() {
                double x = parseTerm();
                for (;;) {
                    if      (eat('+')) x += parseTerm();
                    else if (eat('-')) x -= parseTerm();
                    else return x;
                }
            }

            double parseTerm() {
                double x = parseFactor();
                for (;;) {
                    if      (eat('*')) x *= parseFactor();
                    else if (eat('/')) x /= parseFactor();
                    else return x;
                }
            }

            double parseFactor() {
                if (eat('+')) return parseFactor();
                if (eat('-')) return -parseFactor();

                double x;
                int startPos = this.pos;

                if (eat('(')) {
                    x = parseExpression();
                    eat(')');
                } else if ((ch >= '0' && ch <= '9') || ch == '.') {
                    while ((ch >= '0' && ch <= '9') || ch == '.') nextChar();
                    x = Double.parseDouble(expr.substring(startPos, this.pos));
                } else if (ch >= 'a' && ch <= 'z') {
                    while (ch >= 'a' && ch <= 'z') nextChar();
                    String func = expr.substring(startPos, this.pos);
                    x = parseFactor();
                    if (func.equals("s")) x = Math.sin(Math.toRadians(x));
                    else if (func.equals("c")) x = Math.cos(Math.toRadians(x));
                    else if (func.equals("t")) x = Math.tan(Math.toRadians(x));
                    else throw new RuntimeException("Unknown function: " + func);
                } else {
                    throw new RuntimeException("Unexpected: " + (char)ch);
                }

                return x;
            }
        }.parse();
    }
-
    public static void main(String[] args) {
        SwingUtilities.invokeLater(KalkulatorGUI::new);
    }
}
