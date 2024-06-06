package br.com.ecowave.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import br.com.ecowave.model.ItensReciclados;
import br.com.ecowave.repository.ItensRecicladosRepository;

@Service
public class ItensRecicladosService {

    private final ItensRecicladosRepository itensRecicladosRepository;

    public ItensRecicladosService(ItensRecicladosRepository itensRecicladosRepository) {
        this.itensRecicladosRepository = itensRecicladosRepository;
    }

    public ItensReciclados save(ItensReciclados itensReciclados) {
        return itensRecicladosRepository.save(itensReciclados);
    }

    public ItensReciclados findById(long id) {
        return itensRecicladosRepository.findById(id).orElse(null);
    }

    public int deleteById(long id) {
        try {
            itensRecicladosRepository.deleteById(id);
            return 1;
        } catch (Exception e) {
            return 0;
        }
    }

    public Page<ItensReciclados> findByUsuarioIdPaginado(long idUsuario, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return itensRecicladosRepository.findByUsuario_IdUsuario(idUsuario, pageable);
    }


    public Page<ItensReciclados> findByTipoItemContainingPaginado(String tipoItem, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return itensRecicladosRepository.findByTipoItemContaining(tipoItem, pageable);
    }

    public long findTotalItensByUsuarioId(long idUsuario) {
        return itensRecicladosRepository.countByUsuario_IdUsuario(idUsuario);
    }



    public long findTotalItens() {
        return itensRecicladosRepository.count();
    }

    public long findTotalQuantidadeItens() {
        return itensRecicladosRepository.findAll()
                .stream()
                .mapToLong(ItensReciclados::getQuantidadeItem)
                .sum();
    }

    public long findTotalQuantidadeItensByUsuarioId(long idUsuario) {
        return itensRecicladosRepository.countByUsuario_IdUsuario(idUsuario);
    }

    public Page<ItensReciclados> findAllPaginado(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return itensRecicladosRepository.findAll(pageable);
    }

}
