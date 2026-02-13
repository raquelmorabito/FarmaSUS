package br.gov.farmasus.patient.service;

import br.gov.farmasus.patient.domain.AlergiaPaciente;
import br.gov.farmasus.patient.domain.OrigemAlergia;
import br.gov.farmasus.patient.domain.PerfilPaciente;
import br.gov.farmasus.patient.dto.AlergiaPacienteRequest;
import br.gov.farmasus.patient.dto.AlergiaPacienteResponse;
import br.gov.farmasus.patient.dto.PerfilClinicoPacienteResponse;
import br.gov.farmasus.patient.dto.TipoSanguineoRequest;
import br.gov.farmasus.patient.repository.AlergiaPacienteRepository;
import br.gov.farmasus.patient.repository.PerfilPacienteRepository;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class PerfilClinicoPacienteService {
  private final AlergiaPacienteRepository alergiaRepository;
  private final PerfilPacienteRepository perfilRepository;

  public PerfilClinicoPacienteService(
      AlergiaPacienteRepository alergiaRepository,
      PerfilPacienteRepository perfilRepository) {
    this.alergiaRepository = alergiaRepository;
    this.perfilRepository = perfilRepository;
  }

  @Transactional(readOnly = true)
  public PerfilClinicoPacienteResponse obterPerfil(Long pacienteId) {
    String tipoSanguineo = perfilRepository.findByPacienteId(pacienteId)
        .map(PerfilPaciente::getTipoSanguineo)
        .orElse(null);
    List<AlergiaPacienteResponse> alergias = listarAlergias(pacienteId);
    return new PerfilClinicoPacienteResponse(pacienteId, tipoSanguineo, alergias);
  }

  @Transactional(readOnly = true)
  public List<AlergiaPacienteResponse> listarAlergias(Long pacienteId) {
    return alergiaRepository.findAllByPacienteIdOrderByDescricaoAsc(pacienteId)
        .stream()
        .map(this::toResponse)
        .toList();
  }

  @Transactional
  public AlergiaPacienteResponse criarAlergia(Long pacienteId, AlergiaPacienteRequest request, OrigemAlergia origem) {
    AlergiaPaciente alergia = new AlergiaPaciente();
    alergia.setPacienteId(pacienteId);
    alergia.setDescricao(request.descricao().trim());
    alergia.setOrigem(origem);
    return toResponse(alergiaRepository.save(alergia));
  }

  @Transactional
  public AlergiaPacienteResponse atualizarAlergia(Long pacienteId, Long alergiaId, AlergiaPacienteRequest request) {
    AlergiaPaciente alergia = alergiaRepository.findByIdAndPacienteId(alergiaId, pacienteId)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Alergia nao encontrada"));
    alergia.setDescricao(request.descricao().trim());
    return toResponse(alergiaRepository.save(alergia));
  }

  @Transactional
  public void removerAlergia(Long pacienteId, Long alergiaId) {
    AlergiaPaciente alergia = alergiaRepository.findByIdAndPacienteId(alergiaId, pacienteId)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Alergia nao encontrada"));
    alergiaRepository.delete(alergia);
  }

  @Transactional
  public PerfilClinicoPacienteResponse definirTipoSanguineo(Long pacienteId, TipoSanguineoRequest request) {
    PerfilPaciente perfil = perfilRepository.findByPacienteId(pacienteId)
        .orElseGet(() -> {
          PerfilPaciente novo = new PerfilPaciente();
          novo.setPacienteId(pacienteId);
          return novo;
        });
    perfil.setTipoSanguineo(request.tipoSanguineo().trim().toUpperCase());
    perfilRepository.save(perfil);
    return obterPerfil(pacienteId);
  }

  private AlergiaPacienteResponse toResponse(AlergiaPaciente alergia) {
    return new AlergiaPacienteResponse(
        alergia.getId(),
        alergia.getPacienteId(),
        alergia.getDescricao(),
        alergia.getOrigem().name()
    );
  }
}

