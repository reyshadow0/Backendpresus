package ec.edu.uteq.presustentaciones.controllers;

import ec.edu.uteq.presustentaciones.entities.Rubrica;
import ec.edu.uteq.presustentaciones.repositories.RubricaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@CrossOrigin(origins = "http://localhost:4200")
@RestController
@RequestMapping("/api/rubricas")
@RequiredArgsConstructor
public class RubricaController {
    private final RubricaRepository rubricaRepository;

    @GetMapping
    public List<Rubrica> listar() { return rubricaRepository.findAll(); }

    @PostMapping
    public Rubrica crear(@RequestBody Rubrica rubrica) { return rubricaRepository.save(rubrica); }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        rubricaRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
