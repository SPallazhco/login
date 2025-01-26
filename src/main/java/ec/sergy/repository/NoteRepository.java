package ec.sergy.repository;

import ec.sergy.entity.Note;
import ec.sergy.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NoteRepository extends JpaRepository<Note, Long> {

    // Buscar notas asociadas a un usuario directamente
    List<Note> findByUserId(Long userId);

}
