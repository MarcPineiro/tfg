package edu.udg.tfg.FileManagement.services;


import edu.udg.tfg.FileManagement.entities.ElementEntity;
import edu.udg.tfg.FileManagement.repositories.ElementRepository;
import jakarta.ws.rs.NotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
public class ElementService {

    @Autowired
    private ElementRepository elementRepository;

    public Boolean isFolder(UUID elementId) {
        Optional<ElementEntity> optional = elementRepository.findById(elementId);
        return optional.map(ElementEntity::isFolder).orElseThrow(() -> new NotFoundException("Element not found"));
    }
}