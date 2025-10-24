package me.foglio.repository;

import me.foglio.model.File;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.UUID;

public interface FileRepository extends CrudRepository<File, UUID> {
    List<File> findByOwner_Id(Long ownerId);
}
