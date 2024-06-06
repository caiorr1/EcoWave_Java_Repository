package br.com.ecowave.service;

import br.com.ecowave.model.Amigos;
import br.com.ecowave.repository.AmigosRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AmigosService {

    @Autowired
    private AmigosRepository amigosRepository;


    public Page<Amigos> obterAmigosPaginado(Long idUsuario, int page, int size) {
        PageRequest pageRequest = PageRequest.of(page, size);
        return amigosRepository.findByIdUsuario(idUsuario, pageRequest);
    }


    public void adicionarAmigo(Long idUsuario, Long idAmigo) {
        Amigos amizade = new Amigos();
        amizade.setIdUsuario(idUsuario);
        amizade.setIdAmigo(idAmigo);
        amigosRepository.save(amizade);
    }

    public List<Amigos> obterAmigos(Long idUsuario) {
        return amigosRepository.findByIdUsuario(idUsuario);
    }

    public void removerAmigo(Long idUsuario, Long idAmigo) {
        amigosRepository.deleteByIds(idUsuario, idAmigo);
    }
}
