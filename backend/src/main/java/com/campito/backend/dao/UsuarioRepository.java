package com.campito.backend.dao;

import org.springframework.data.jpa.repository.JpaRepository;

import com.campito.backend.model.Usuario;
import java.util.Optional;
import java.util.UUID;
import com.campito.backend.model.ProveedorAutenticacion;

public interface UsuarioRepository extends JpaRepository<Usuario, UUID> {

    Optional<Usuario> findByEmailAndProveedor(String email, ProveedorAutenticacion proveedor);
    
    Optional<Usuario> findByEmail(String email);
}
