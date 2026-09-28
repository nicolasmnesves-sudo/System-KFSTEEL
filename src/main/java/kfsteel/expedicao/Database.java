package kfsteel.expedicao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class Database {
    private static final String URL = "jdbc:sqlite:expedicao_silos.db";

    public static Connection connect() throws SQLException {
        return DriverManager.getConnection(URL);
    }

    public static void initDatabase() {
        String createUsuarios = "CREATE TABLE IF NOT EXISTS usuarios (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "usuario TEXT UNIQUE NOT NULL, " +
                "senha TEXT NOT NULL);";

        String insertUsuario = "INSERT OR IGNORE INTO usuarios (id, usuario, senha) VALUES (1, 'admin', '12345');";

        String createProjetos = "CREATE TABLE IF NOT EXISTS projetos (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "numero_pedido TEXT UNIQUE NOT NULL, " +
                "cor_etiqueta TEXT NOT NULL);";

        // pedido_id passa a referenciar projetos.id
        String createItens = "CREATE TABLE IF NOT EXISTS itens_pedido (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "pedido_id INTEGER, " +
                "codigo_barras TEXT UNIQUE, " +
                "descricao TEXT, " +
                "tipo TEXT, " +
                "status TEXT DEFAULT 'PENDENTE');";

        String insertProjeto = "INSERT OR IGNORE INTO projetos (id, numero_pedido, cor_etiqueta) VALUES (1042, '1042', 'Amarela');";

        // Corrigido: antes havia um ';' no meio do INSERT e só as 4 primeiras linhas eram gravadas
        String insertItens = "INSERT OR IGNORE INTO itens_pedido (id, pedido_id, codigo_barras, descricao, tipo, status) VALUES " +
                "(1, 1042, 'SILO-001', 'Chapa Lateral de Silo 5m', 'Conjunto', 'PENDENTE'), " +
                "(2, 1042, 'SILO-002', 'Funil Inferior de Descarga', 'Subconjunto', 'PENDENTE'), " +
                "(3, 1042, 'SILO-003', 'Anel de Reforço Superior', 'Peça', 'PENDENTE'), " +
                "(4, 1042, 'SILO-004', 'Kit de Parafusos M12 Zincados', 'Peça', 'PENDENTE'), " +
                "(5, 1042, 'SILO-005', 'Chapa Lateral de Silo 5m', 'Conjunto', 'PENDENTE'), " +
                "(6, 1042, 'SILO-006', 'Funil Inferior de Descarga', 'Subconjunto', 'PENDENTE'), " +
                "(7, 1042, 'SILO-007', 'Anel de Reforço Superior', 'Peça', 'PENDENTE'), " +
                "(8, 1042, 'SILO-008', 'Kit de porcas M12', 'Peça', 'PENDENTE');";

        try (Connection conn = connect(); Statement stmt = conn.createStatement()) {
            stmt.execute(createUsuarios);
            stmt.execute(insertUsuario);
            stmt.execute(createProjetos);
            stmt.execute(createItens);
            stmt.execute(insertProjeto);
            stmt.execute(insertItens);
        } catch (SQLException e) {
            System.err.println("Erro ao inicializar banco de dados: " + e.getMessage());
        }
    }

    // ------------------------------------------------------------------
    // USUÁRIOS
    // ------------------------------------------------------------------

    public static boolean autenticar(String usuario, String senha) {
        String sql = "SELECT id FROM usuarios WHERE usuario = ? AND senha = ?";
        try (Connection conn = connect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, usuario);
            pstmt.setString(2, senha);
            return pstmt.executeQuery().next();
        } catch (SQLException e) {
            System.err.println("Erro na consulta de autenticação: " + e.getMessage());
            return false;
        }
    }

    public static boolean cadastrarUsuario(String usuario, String senha) {
        String sql = "INSERT INTO usuarios (usuario, senha) VALUES (?, ?)";
        try (Connection conn = connect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, usuario);
            pstmt.setString(2, senha);
            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.err.println("Erro ao cadastrar usuário: " + e.getMessage());
            return false;
        }
    }

    // ------------------------------------------------------------------
    // PROJETOS (PEDIDOS)
    // ------------------------------------------------------------------

    public static List<Projeto> listarProjetos(String filtro) {
        String sql = "SELECT p.id, p.numero_pedido, p.cor_etiqueta, " +
                "COUNT(i.id) AS total, " +
                "COALESCE(SUM(CASE WHEN i.status = 'SEPARADO' THEN 1 ELSE 0 END), 0) AS separados " +
                "FROM projetos p LEFT JOIN itens_pedido i ON i.pedido_id = p.id " +
                "WHERE p.numero_pedido LIKE ? GROUP BY p.id ORDER BY p.id";

        List<Projeto> lista = new ArrayList<>();
        try (Connection conn = connect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, "%" + (filtro == null ? "" : filtro.trim()) + "%");
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                lista.add(new Projeto(rs.getInt("id"), rs.getString("numero_pedido"),
                        rs.getString("cor_etiqueta"), rs.getInt("total"), rs.getInt("separados")));
            }
        } catch (SQLException e) {
            System.err.println("Erro ao listar projetos: " + e.getMessage());
        }
        return lista;
    }

    /** Retorna false se o número do pedido já existir. */
    public static boolean criarProjeto(String numeroPedido, String cor) {
        String sql = "INSERT INTO projetos (numero_pedido, cor_etiqueta) VALUES (?, ?)";
        try (Connection conn = connect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, numeroPedido);
            pstmt.setString(2, cor);
            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.err.println("Erro ao criar projeto: " + e.getMessage());
            return false;
        }
    }

    /** Exclui o projeto e todos os seus itens numa única transação. */
    public static boolean excluirProjeto(int projetoId) {
        try (Connection conn = connect()) {
            conn.setAutoCommit(false);
            try (PreparedStatement itens = conn.prepareStatement("DELETE FROM itens_pedido WHERE pedido_id = ?");
                 PreparedStatement proj = conn.prepareStatement("DELETE FROM projetos WHERE id = ?")) {
                itens.setInt(1, projetoId);
                itens.executeUpdate();
                proj.setInt(1, projetoId);
                proj.executeUpdate();
                conn.commit();
                return true;
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        } catch (SQLException e) {
            System.err.println("Erro ao excluir projeto: " + e.getMessage());
            return false;
        }
    }

    // ------------------------------------------------------------------
    // ITENS DO PEDIDO (CRUD)
    // ------------------------------------------------------------------

    public static List<ItemPedido> listarItens(int projetoId) {
        String sql = "SELECT id, codigo_barras, descricao, tipo, status FROM itens_pedido WHERE pedido_id = ? ORDER BY id";
        List<ItemPedido> lista = new ArrayList<>();
        try (Connection conn = connect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, projetoId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                lista.add(new ItemPedido(rs.getInt("id"), rs.getString("codigo_barras"),
                        rs.getString("descricao"), rs.getString("tipo"), rs.getString("status")));
            }
        } catch (SQLException e) {
            System.err.println("Erro ao listar itens: " + e.getMessage());
        }
        return lista;
    }

    /** Retorna false se o código de barras já existir. */
    public static boolean inserirItem(int projetoId, String codigo, String descricao, String tipo, String status) {
        String sql = "INSERT INTO itens_pedido (pedido_id, codigo_barras, descricao, tipo, status) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = connect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, projetoId);
            pstmt.setString(2, codigo);
            pstmt.setString(3, descricao);
            pstmt.setString(4, tipo);
            pstmt.setString(5, status);
            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.err.println("Erro ao inserir item: " + e.getMessage());
            return false;
        }
    }

    public static boolean atualizarItem(int id, String codigo, String descricao, String tipo, String status) {
        String sql = "UPDATE itens_pedido SET codigo_barras = ?, descricao = ?, tipo = ?, status = ? WHERE id = ?";
        try (Connection conn = connect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, codigo);
            pstmt.setString(2, descricao);
            pstmt.setString(3, tipo);
            pstmt.setString(4, status);
            pstmt.setInt(5, id);
            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.err.println("Erro ao atualizar item: " + e.getMessage());
            return false;
        }
    }

    public static boolean atualizarStatus(int id, String status) {
        String sql = "UPDATE itens_pedido SET status = ? WHERE id = ?";
        try (Connection conn = connect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, status);
            pstmt.setInt(2, id);
            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.err.println("Erro ao atualizar status: " + e.getMessage());
            return false;
        }
    }

    public static boolean excluirItem(int id) {
        String sql = "DELETE FROM itens_pedido WHERE id = ?";
        try (Connection conn = connect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.err.println("Erro ao excluir item: " + e.getMessage());
            return false;
        }
    }
}
