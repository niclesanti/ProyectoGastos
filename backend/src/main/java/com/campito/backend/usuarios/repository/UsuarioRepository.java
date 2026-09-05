package com.campito.backend.usuarios.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.campito.backend.usuarios.domain.entity.Usuario;
import java.util.Optional;
import java.util.UUID;
import com.campito.backend.usuarios.domain.entity.ProveedorAutenticacion;

public interface UsuarioRepository extends JpaRepository<Usuario, UUID> {

    Optional<Usuario> findByEmailAndProveedor(String email, ProveedorAutenticacion proveedor);
    
    Optional<Usuario> findByEmail(String email);
}
