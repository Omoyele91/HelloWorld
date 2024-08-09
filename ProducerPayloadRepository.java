package com.stanbic.bua.repository;

import com.stanbic.bua.entity.PayloadLog;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ProducerPayloadRepository extends CrudRepository<PayloadLog, Long> {
    @Query(value = "SELECT * FROM RBX_P_COLLECTION_PAYLOAD", nativeQuery = true)
    List<PayloadLog> payLoads();
}
