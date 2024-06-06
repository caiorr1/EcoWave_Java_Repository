package br.com.ecowave.service;

import br.com.ecowave.model.Usuario;
import br.com.ecowave.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Pageable;

import java.util.Date;

@Service
public class UsuarioService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public void registrarUsuario(Usuario usuario) {
        if (usuario.getDataRegistroUsuario() == null) {
            usuario.setDataRegistroUsuario(new Date());
        }
        String senhaCriptografada = passwordEncoder.encode(usuario.getSenhaUsuario());
        usuario.setSenhaUsuario(senhaCriptografada);
        usuarioRepository.save(usuario);
    }

    public boolean verificarCredenciais(String nomeUsuario, String senha) {
        Usuario usuario = usuarioRepository.findByNomeUsuario(nomeUsuario);
        if (usuario != null) {
            return passwordEncoder.matches(senha, usuario.getSenhaUsuario());
        }
        return false;
    }

    public Usuario findByIdUsuario(Long idUsuario) {
        return usuarioRepository.findById(idUsuario).orElse(null);
    }

    public Page<Usuario> obterTodosUsuariosPaginado(Pageable pageable) {
        return usuarioRepository.findAll(pageable);
    }

}
