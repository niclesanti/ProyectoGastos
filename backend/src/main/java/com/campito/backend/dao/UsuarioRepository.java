package com.campito.backend.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.campito.backend.model.Usuario;
import java.util.Optional;
import com.campito.backend.model.ProveedorAutenticacion;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    Optional<Usuario> findByEmailAndProveedor(String email, ProveedorAutenticacion proveedor);
    
    Optional<Usuario> findByEmail(String email);
}
