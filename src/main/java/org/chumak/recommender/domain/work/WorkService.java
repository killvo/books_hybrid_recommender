package org.chumak.recommender.domain.work;

import lombok.RequiredArgsConstructor;
import org.chumak.recommender.database.domain.work.Work;
import org.chumak.recommender.database.domain.work.WorkRepository;
import org.chumak.recommender.domain.work.dto.WorkDto;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class WorkService {
    private final WorkRepository workRepository;

    public Work save(Work work) {
        return workRepository.save(work);
    }

    public Optional<Work> findById(String id) {
        return workRepository.findById(id);
    }

    public List<Work> findAll() {
        return workRepository.findAll();
    }

    public List<Work> findByIds(List<String> workIds) {
        return workRepository.findAllByIdIn(workIds);
    }

    @Transactional
    public List<WorkDto> findAllWorksDto() {
        return findAll().stream()
                .map(WorkMapper.MAPPER::toDto)
                .collect(Collectors.toList());
    }

    public List<WorkDto> findWorksDtoByIds(List<String> workIds) {
        return findByIds(workIds).stream()
                .map(WorkMapper.MAPPER::toDto)
                .collect(Collectors.toList());
    }
}
