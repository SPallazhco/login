package ec.sergy.controller;

import ec.sergy.entity.Note;
import ec.sergy.service.AuthenticatedUserService;
import ec.sergy.service.NoteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/api/notes")
public class NoteController {

    @Autowired
    private NoteService noteService;

    @Autowired
    private AuthenticatedUserService authenticatedUserService;

    // Crear una nueva nota asociada al usuario autenticado
    @PostMapping
    public Note createNote(@RequestParam String title, @RequestParam String description) {
        String username = authenticatedUserService.getAuthenticatedUsername();
        return noteService.createNoteByUsername(username, title, description);
    }

    // Obtener todas las notas del usuario autenticado
    @GetMapping
    public List<Note> getNotesByUser() {
        String username = authenticatedUserService.getAuthenticatedUsername();
        return noteService.getNotesByUsername(username);
    }

    // Actualizar el estado de una nota
    @PutMapping("/{noteId}/status")
    public Note updateNoteStatus(@PathVariable Long noteId, @RequestParam Note.NoteStatus status) {
        return noteService.updateNoteStatus(noteId, status);
    }
}
