package kz.bsbnb.repository;

import kz.bsbnb.common.model.Decision;
import kz.bsbnb.common.model.Question;
import kz.bsbnb.common.model.Voter;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * @author Ruslan.
 */
//@Transactional(readOnly = true)
public interface IDecisionRepository extends PagingAndSortingRepository<Decision, Long> {

    List<Decision> findByQuestionId(Question question);

    List<Decision> findByQuestionIdAndVoterId(Question question, Voter voter);

    @Modifying
    @Transactional
    @Query("delete from Decision d where d.id = ?1")
    void deleteByIds(Long id);
}
