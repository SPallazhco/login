package ec.sergy.service;
import ec.sergy.entity.Note;
import ec.sergy.entity.User;
import ec.sergy.repository.NoteRepository;
import ec.sergy.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class NoteService {

    private final NoteRepository noteRepository;
    private final UserRepository userRepository;

    public NoteService(NoteRepository noteRepository, UserRepository userRepository) {
        this.noteRepository = noteRepository;
        this.userRepository = userRepository;
    }

    public Note createNoteByUsername(String email, String title, String description) {
        // Buscar al usuario por su username
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        // Crear y guardar la nota
        Note note = new Note();
        note.setUserId(user.getId());
        note.setTitle(title);
        note.setDescription(description);
        note.setCreatedAt(LocalDateTime.now());
        note.setStatus(Note.NoteStatus.NEW);

        return noteRepository.save(note);
    }

    public List<Note> getNotesByUsername(String email) {
        // Buscar al usuario por su username
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        // Obtener las notas asociadas al usuario
        return noteRepository.findByUserId(user.getId());
    }

    public Note updateNoteStatus(Long noteId, Note.NoteStatus status) {
        Note note = noteRepository.findById(noteId)
                .orElseThrow(() -> new RuntimeException("Nota no encontrada"));

        note.setStatus(status);
        return noteRepository.save(note);
    }
}