package ro.ase.acs.mind_path.dto.mapper;

@FunctionalInterface
public interface DtoMapper<S, T> {
    T toDto(S source);
}
