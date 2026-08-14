import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import javax.swing.*;

public class App {
     // Configurações de conexão 
   private static final String URL = "jdbc:postgresql://localhost:5432/loja";
   private static final String USER = "postgres";
   private static final String PASS = "1234"; 
    public static void main(String[] args) throws Exception {
        try {
            // 1. Estabelece a conexão normalmente 
             Connection conn = DriverManager.getConnection(URL, USER, PASS);
            SwingUtilities.invokeLater(() -> {
                // 2. Passa a conexão que se manterá aberta para a janela gráfica
                JanelaMenu menu = new JanelaMenu(conn);
                     menu.setVisible(true);
                 });
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, 
                "Falha ao conectar com o banco de dados:\n" + e.getMessage(), 
                "Erro Crítico", 
                JOptionPane.ERROR_MESSAGE);
            System.exit(1); // Fecha a aplicação caso o banco esteja inacessível
        }
    }    
}
