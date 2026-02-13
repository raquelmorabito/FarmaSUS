package db.migration;

import java.sql.PreparedStatement;
import java.util.Map;
import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class V2__seed_usuarios extends BaseJavaMigration {
  @Override
  public void migrate(Context context) throws Exception {
    Map<String, String> placeholders = context.getConfiguration().getPlaceholders();
    String pacienteLogin = required(placeholders, "seed_paciente_login");
    String pacienteSenha = required(placeholders, "seed_paciente_senha");
    String pacienteTipo = required(placeholders, "seed_paciente_tipo");
    String profLogin = required(placeholders, "seed_prof_login");
    String profSenha = required(placeholders, "seed_prof_senha");
    String profTipo = required(placeholders, "seed_prof_tipo");

    BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    upsertUsuario(
        context,
        pacienteLogin,
        encoder.encode(pacienteSenha),
        pacienteTipo
    );
    upsertUsuario(
        context,
        profLogin,
        encoder.encode(profSenha),
        profTipo
    );
  }

  private static void upsertUsuario(Context context, String login, String senhaHash, String tipoUsuario) throws Exception {
    String updateSql = "update usuarios set senha_hash = ?, tipo_usuario = ? where login = ?";
    try (PreparedStatement update = context.getConnection().prepareStatement(updateSql)) {
      update.setString(1, senhaHash);
      update.setString(2, tipoUsuario);
      update.setString(3, login);
      int updated = update.executeUpdate();
      if (updated > 0) {
        return;
      }
    }

    String insertSql = "insert into usuarios (login, senha_hash, tipo_usuario) values (?, ?, ?)";
    try (PreparedStatement insert = context.getConnection().prepareStatement(insertSql)) {
      insert.setString(1, login);
      insert.setString(2, senhaHash);
      insert.setString(3, tipoUsuario);
      insert.executeUpdate();
    }
  }

  private static String required(Map<String, String> placeholders, String key) {
    String value = placeholders.get(key);
    if (value == null || value.isBlank()) {
      throw new IllegalStateException("Placeholder Flyway obrigatorio ausente: " + key);
    }
    return value;
  }
}
