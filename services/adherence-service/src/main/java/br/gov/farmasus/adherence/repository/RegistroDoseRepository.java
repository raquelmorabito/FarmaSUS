package br.gov.farmasus.adherence.repository;

import br.gov.farmasus.adherence.domain.RegistroDose;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface RegistroDoseRepository extends JpaRepository<RegistroDose, Long> {
  @Query("select r from RegistroDose r where r.dose.pacienteId = :pacienteId order by r.registradoEm desc")
  List<RegistroDose> findAllByPacienteIdOrderByRegistradoEmDesc(@Param("pacienteId") Long pacienteId);
}
